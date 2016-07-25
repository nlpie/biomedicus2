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

import com.google.inject.Inject;
import com.google.inject.ProvidedBy;
import com.google.inject.Provider;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.text.Section;
import edu.umn.biomedicus.uima.labels.AbstractLabelAdapter;
import org.apache.uima.cas.*;
import org.apache.uima.cas.text.AnnotationFS;

final class SectionLabelAdapter extends AbstractLabelAdapter<Section> {
    private final Feature titleFeature;
    private final Feature levelFeature;
    private final Feature contentStartFeature;
    private final Feature hasSubsectionsFeature;
    private final Feature kindFeature;

    @Inject
    SectionLabelAdapter(CAS cas) {
        super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.type.SectionAnnotation"));

        titleFeature = type.getFeatureByBaseName("sectionTitle");
        levelFeature = type.getFeatureByBaseName("level");
        contentStartFeature = type.getFeatureByBaseName("contentStart");
        hasSubsectionsFeature = type.getFeatureByBaseName("hasSubsections");
        kindFeature = type.getFeatureByBaseName("kind");
    }

    @Override
    protected void fillAnnotation(Label<Section> label, AnnotationFS annotationFS) {
        Section section = label.value();
        annotationFS.setStringValue(titleFeature, section.getSectionTitle());
        annotationFS.setIntValue(levelFeature, section.getLevel());
        annotationFS.setIntValue(contentStartFeature, section.contentStart());
        annotationFS.setBooleanValue(hasSubsectionsFeature, section.hasSubsections());
        annotationFS.setStringValue(kindFeature, section.getKind());
    }

    @Override
    protected Section createLabelValue(FeatureStructure featureStructure) {
        return Section.builder()
                .setContentStart(featureStructure.getIntValue(contentStartFeature))
                .setHasSubsections(featureStructure.getBooleanValue(hasSubsectionsFeature))
                .setKind(featureStructure.getStringValue(kindFeature))
                .setLevel(featureStructure.getIntValue(levelFeature))
                .setSectionTitle(featureStructure.getStringValue(titleFeature))
                .build();
    }
}
