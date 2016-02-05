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

package edu.umn.biomedicus.uima.vectorspace;

import edu.umn.biomedicus.uima.CasHelper;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Adapts all of the values of a feature in CAS documents in UIMA to {@link Stream}s of String terms.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
class FeatureTermAdapter implements TermAdapter {
    /**
     * The fully qualified UIMA type name of the Feature Structure we are collecting Features from.
     */
    private final String fsTypeName;

    /**
     * The base name of the Feature to collect the value of.
     */
    private final String featureBaseName;

    /**
     * The constructor which initializes the type name and feature base name directly.
     *
     * @param fsTypeName fully qualified UIMA type name.
     * @param featureBaseName feature base name.
     */
    FeatureTermAdapter(String fsTypeName, String featureBaseName) {
        this.fsTypeName = fsTypeName;
        this.featureBaseName = featureBaseName;
    }

    @Override
    public Stream<String> terms(JCas cas, Predicate<AnnotationFS> exclusionTest) {
        CasHelper casHelper = new CasHelper(cas);
        Type type = casHelper.getType(fsTypeName);
        Feature feature = type.getFeatureByBaseName(featureBaseName);
        return casHelper.featureStructuresOfType(type)
                .filter(fs -> !(fs instanceof AnnotationFS) || exclusionTest.test((AnnotationFS) fs))
                .map(fs -> fs.getStringValue(feature));
    }
}
