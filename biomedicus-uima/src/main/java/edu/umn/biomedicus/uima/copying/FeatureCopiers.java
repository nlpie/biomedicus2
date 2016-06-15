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

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.TypeSystem;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Helper class for copying the value of a {@link Feature} from one {@link FeatureStructure} to another.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
class FeatureCopiers {
    /**
     * A map of UIMA fully-qualified type names their appropriate {@link FeatureCopier}.
     */
    private static final Map<String, FeatureCopier> FEATURE_COPIERS;

    /**
     * The {@link FeatureCopier} for the {@code uima.cas.String} type.
     */
    private static final FeatureCopier STRING_COPIER = primitiveFeatureCopier(FeatureStructure::getStringValue,
            FeatureStructure::setStringValue);

    /**
     * A callback for when a new {@link FeatureStructure} is encountered, returns the newly created
     * {@code FeatureStructure}
     */
    private final UnaryOperator<FeatureStructure> fsEncounteredCallback;

    /**
     * Static initializer for {@link #FEATURE_COPIERS}
     */
    static {
        Map<String, FeatureCopier> featureCopiersBuilder = new HashMap<>();

        featureCopiersBuilder.put(CAS.TYPE_NAME_BOOLEAN,
                primitiveFeatureCopier(FeatureStructure::getBooleanValue, FeatureStructure::setBooleanValue));

        featureCopiersBuilder.put(CAS.TYPE_NAME_BYTE,
                primitiveFeatureCopier(FeatureStructure::getByteValue, FeatureStructure::setByteValue));

        featureCopiersBuilder.put(CAS.TYPE_NAME_DOUBLE,
                primitiveFeatureCopier(FeatureStructure::getDoubleValue, FeatureStructure::setDoubleValue));

        featureCopiersBuilder.put(CAS.TYPE_NAME_FLOAT,
                primitiveFeatureCopier(FeatureStructure::getFloatValue, FeatureStructure::setFloatValue));

        featureCopiersBuilder.put(CAS.TYPE_NAME_INTEGER,
                primitiveFeatureCopier(FeatureStructure::getIntValue, FeatureStructure::setIntValue));

        featureCopiersBuilder.put(CAS.TYPE_NAME_LONG,
                primitiveFeatureCopier(FeatureStructure::getLongValue, FeatureStructure::setLongValue));

        featureCopiersBuilder.put(CAS.TYPE_NAME_SHORT,
                primitiveFeatureCopier(FeatureStructure::getShortValue, FeatureStructure::setShortValue));

        featureCopiersBuilder.put(CAS.TYPE_NAME_STRING, STRING_COPIER);

        FEATURE_COPIERS = Collections.unmodifiableMap(featureCopiersBuilder);
    }

    /**
     * Default constructor. Creates an instance setting the enqueue callback field to the parameter.
     *
     * @param fsEncounteredCallback a method which is called when a new {@code FeatureStructure} is encountered and
     *                              returns a newly initialized {@code FeatureStructure}
     */
    FeatureCopiers(UnaryOperator<FeatureStructure> fsEncounteredCallback) {
        this.fsEncounteredCallback = Objects.requireNonNull(fsEncounteredCallback);
    }

    /**
     * Creates a {@link FeatureCopier} for a primitive feature with the passed in getter and setter.
     *
     * @param featureGetter {@code FeatureStructure} getter method
     * @param featureSetter {@code FeatureStructure} setter method
     * @param <T>           feature primitive java type
     * @return {@code FeatureCopier} which copies features of the type from one {@code FeatureStructure} to another.
     */
    private static <T> FeatureCopier primitiveFeatureCopier(FeatureGetter<T> featureGetter,
                                                            FeatureSetter<T> featureSetter) {
        return (fromFeature, fromFs, toFs) -> {
            T value = featureGetter.get(fromFs, fromFeature);
            Feature toFeature = toFs.getType().getFeatureByBaseName(fromFeature.getShortName());
            featureSetter.set(toFs, toFeature, value);
        };
    }

    /**
     * {@code FeatureCopier} used for features which are references to {@code FeatureStructure}s.
     *
     * @param fromFeature the {@link Feature}
     * @param fromFs      the {@link FeatureStructure} to copy from
     * @param toFs        the {@link FeatureStructure} to copy to
     */
    private void defaultFeatureMapper(Feature fromFeature, FeatureStructure fromFs, FeatureStructure toFs) {
        TypeSystem typeSystem = fromFs.getCAS().getTypeSystem();
        if (typeSystem.subsumes(typeSystem.getType(CAS.TYPE_NAME_STRING), fromFeature.getRange())) {
            STRING_COPIER.copy(fromFeature, fromFs, toFs);
        } else {
            FeatureStructure fromFeatureValue = fromFs.getFeatureValue(fromFeature);
            if (fromFeatureValue != null) {
                FeatureStructure toFeatureValue = fsEncounteredCallback.apply(fromFeatureValue);
                Feature toFeature = toFs.getType().getFeatureByBaseName(fromFeature.getShortName());
                toFs.setFeatureValue(toFeature, toFeatureValue);
            }
        }
    }

    /**
     * Copies the specified {@code Feature} from one {@code FeatureStructure} to another.
     *
     * @param fromFeature {@link Feature} to copy
     * @param fromFs      {@link FeatureStructure} to copy from
     * @param toFs        {@link FeatureStructure} to copy to
     */
    public void copyFeature(Feature fromFeature, FeatureStructure fromFs, FeatureStructure toFs) {
        String featureTypeName = fromFeature.getRange().getName();
        FEATURE_COPIERS.getOrDefault(featureTypeName, this::defaultFeatureMapper).copy(fromFeature, fromFs, toFs);
    }
}
