/*
 * Copyright (c) 2015 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.uima.common;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Provides convenience accessor methods for an UIMA {@link CAS} object.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public final class CasHelper {
    /**
     * The CAS object.
     */
    private final CAS cas;

    /**
     * Constructor which initializes a {@code CasHelper} using a CAS.
     *
     * @param cas cas object to access.
     */
    public CasHelper(CAS cas) {
        this.cas = cas;
    }

    /**
     * Constructor which initializes a {@code CasHelper} using a JCas.
     *
     * @param jCas JCas object to access.
     */
    public CasHelper(JCas jCas) {
        this(jCas.getCas());
    }

    /**
     * Provides a stream of the {@link FeatureStructure} objects of a specific type by getting all indexed FSes of the
     * type from the index repository.
     *
     * @param type type to get feature structures of.
     * @return stream of feature structures.
     */
    public Stream<FeatureStructure> featureStructuresOfType(Type type) {
        Iterable<FeatureStructure> iterable = () -> cas.getIndexRepository().getAllIndexedFS(type);
        return StreamSupport.stream(iterable.spliterator(), false);
    }

    /**
     * Provides a stream of the {@link FeatureStructure} objects of a specific type  name by getting all indexed FSes
     * the type from the index repository.
     *
     * @param typeName the fully qualified UIMA type name of the FeatureStructure.
     * @return a stream of all the feature structures of the type.
     */
    public Stream<FeatureStructure> featureStructuresOfType(String typeName) {
        Type type = getType(typeName);
        return featureStructuresOfType(type);
    }

    /**
     * Gets the UIMA type with the specified type name.
     *
     * @param typeName type name to get.
     * @return the UIMA Type object.
     */
    public Type getType(String typeName) {
        return cas.getTypeSystem().getType(typeName);
    }
}
