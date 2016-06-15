/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.uima.copying;

import org.apache.uima.cas.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

/**
 * Class for copying {@code FeatureStructure}s of different types. After the target feature structure has been
 * initialized this class is responsible for copying the value of individual features or the contents of an array.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
class FsCopiers {
    /**
     * A Map from the string UIMA type name of a FeatureStructure to a function which copies one fs to another.
     */
    private final Map<String, BiConsumer<FeatureStructure, FeatureStructure>> fsCopiers;

    /**
     * The class responsible for copying feature values
     */
    private final FeatureCopiers featureCopiers;

    /**
     * Callback for when a new {@code FeatureStructure} is encountered.
     */
    private final UnaryOperator<FeatureStructure> featureStructureEncounteredCallback;

    /**
     * Constructor which takes the values of the class's fields.
     *
     * @param fsCopiers methods for copying one feature structure to another by their type name.
     * @param featureCopiers class for copying the values of Features
     * @param featureStructureEncounteredCallback callback for encountered feature structures
     */
    FsCopiers(Map<String, BiConsumer<FeatureStructure, FeatureStructure>> fsCopiers,
              FeatureCopiers featureCopiers,
              UnaryOperator<FeatureStructure> featureStructureEncounteredCallback) {
        this.fsCopiers = fsCopiers;
        this.featureCopiers = featureCopiers;
        this.featureStructureEncounteredCallback = featureStructureEncounteredCallback;
    }

    /**
     * Convenience constructor which only needs the callback for encountered feature structures.
     *
     * @param featureStructureEncounteredCallback callback for encountered feature structures
     */
    FsCopiers(UnaryOperator<FeatureStructure> featureStructureEncounteredCallback, FeatureCopiers featureCopiers) {
        this.featureCopiers = featureCopiers;
        this.featureStructureEncounteredCallback = featureStructureEncounteredCallback;

        fsCopiers = new HashMap<>();
        fsCopiers.put(CAS.TYPE_NAME_BOOLEAN_ARRAY, copyArray(BooleanArrayFS.class));
        fsCopiers.put(CAS.TYPE_NAME_BYTE_ARRAY, copyArray(ByteArrayFS.class));
        fsCopiers.put(CAS.TYPE_NAME_DOUBLE_ARRAY, copyArray(DoubleArrayFS.class));
        fsCopiers.put(CAS.TYPE_NAME_FLOAT_ARRAY, copyArray(FloatArrayFS.class));
        fsCopiers.put(CAS.TYPE_NAME_FS_ARRAY, this::copyFsArray);
        fsCopiers.put(CAS.TYPE_NAME_LONG_ARRAY, copyArray(LongArrayFS.class));
        fsCopiers.put(CAS.TYPE_NAME_INTEGER_ARRAY, copyArray(IntArrayFS.class));
        fsCopiers.put(CAS.TYPE_NAME_SHORT_ARRAY, copyArray(ShortArrayFS.class));
        fsCopiers.put(CAS.TYPE_NAME_STRING_ARRAY, copyArray(StringArrayFS.class));
    }

    /**
     * Convenience constructor which only needs the callback for encountered feature structures.
     *
     * @param featureStructureEncounteredCallback callback for encountered feature structures
     */
    FsCopiers(UnaryOperator<FeatureStructure> featureStructureEncounteredCallback) {
        this.featureCopiers = new FeatureCopiers(featureStructureEncounteredCallback);
        this.featureStructureEncounteredCallback = featureStructureEncounteredCallback;

        fsCopiers = new HashMap<>();
        fsCopiers.put(CAS.TYPE_NAME_BOOLEAN_ARRAY, copyArray(BooleanArrayFS.class));
        fsCopiers.put(CAS.TYPE_NAME_BYTE_ARRAY, copyArray(ByteArrayFS.class));
        fsCopiers.put(CAS.TYPE_NAME_DOUBLE_ARRAY, copyArray(DoubleArrayFS.class));
        fsCopiers.put(CAS.TYPE_NAME_FLOAT_ARRAY, copyArray(FloatArrayFS.class));
        fsCopiers.put(CAS.TYPE_NAME_FS_ARRAY, this::copyFsArray);
        fsCopiers.put(CAS.TYPE_NAME_LONG_ARRAY, copyArray(LongArrayFS.class));
        fsCopiers.put(CAS.TYPE_NAME_INTEGER_ARRAY, copyArray(IntArrayFS.class));
        fsCopiers.put(CAS.TYPE_NAME_SHORT_ARRAY, copyArray(ShortArrayFS.class));
        fsCopiers.put(CAS.TYPE_NAME_STRING_ARRAY, copyArray(StringArrayFS.class));
    }

    /**
     * Creates a function which copies the contents of one array feature structure to another.
     *
     * @param aClass the array class
     * @return function which copies arrays for the type of class.
     */
    private static BiConsumer<FeatureStructure, FeatureStructure> copyArray(Class<? extends CommonArrayFS> aClass) {
        Method toArrayMethod;
        Method fromArrayMethod;
        try {
            toArrayMethod = aClass.getMethod("toArray");
            fromArrayMethod = aClass.getMethod("copyFromArray", toArrayMethod.getReturnType(), Integer.TYPE,
                    Integer.TYPE, Integer.TYPE);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return (from, to) -> {
            try {
                fromArrayMethod.invoke(to, toArrayMethod.invoke(from), 0, 0, ((CommonArrayFS) from).size());
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * Copies one array of fs references to another.
     *
     * @param from the array to copy from
     * @param to the array to copy to
     */
    private void copyFsArray(FeatureStructure from, FeatureStructure to) {
        ArrayFS sourceFses = (ArrayFS) from;
        ArrayFS targetFses = (ArrayFS) to;
        for (int index = 0; index < sourceFses.size(); index++) {
            FeatureStructure arrayMember = sourceFses.get(index);
            FeatureStructure toFs = featureStructureEncounteredCallback.apply(arrayMember);
            targetFses.set(index, toFs);
        }
    }

    /**
     * The default method for copying the features of one {@code FeatureStructure} to another of the same type.
     *
     * @param from the {@code FeatureStructure} to copy from.
     * @param to the {@code FeatureStructure} to copy to.
     */
    private void defaultCopy(FeatureStructure from, FeatureStructure to) {
        List<Feature> features = from.getType().getFeatures();
        for (Feature feature : features) {
            featureCopiers.copyFeature(feature, from, to);
        }
    }

    /**
     * Copies a generic {@code FeatureStructure} to another of the same type.
     *
     * @param from {@code FeatureStructure} to copy from.
     * @param to {@code FeatureStructure} to copy to.
     */
    public void copy(FeatureStructure from, FeatureStructure to) {
        fsCopiers.getOrDefault(from.getType().getName(), this::defaultCopy).accept(from, to);
        to.getCAS().addFsToIndexes(to);
    }
}
