/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.uima.types;

import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.text.Token;
import edu.umn.biomedicus.uima.labels.AbstractLabelAdapter;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;

public abstract class AbstractTokenLabelAdapter<T extends Token> extends AbstractLabelAdapter<T> {
    private final Feature textFeature;
    private final Feature hasSpaceAfterFeature;

    protected AbstractTokenLabelAdapter(CAS cas, Type type) {
        super(cas, type);
        textFeature = type.getFeatureByBaseName("text");
        hasSpaceAfterFeature = type.getFeatureByBaseName("hasSpaceAfter");
    }

    protected abstract T createToken(String text, boolean hasSpaceAfter);

    @Override
    protected void fillAnnotation(Label<T> label, AnnotationFS annotationFS) {
        T token = label.value();
        annotationFS.setStringValue(textFeature, token.text());
        annotationFS.setBooleanValue(hasSpaceAfterFeature, token.hasSpaceAfter());
    }

    @Override
    protected T createLabelValue(FeatureStructure featureStructure) {
        String text = featureStructure.getStringValue(textFeature);
        boolean hasSpaceAfter = featureStructure.getBooleanValue(hasSpaceAfterFeature);
        return createToken(text, hasSpaceAfter);
    }
}
