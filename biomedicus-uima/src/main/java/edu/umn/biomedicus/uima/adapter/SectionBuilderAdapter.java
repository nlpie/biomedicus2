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

package edu.umn.biomedicus.uima.adapter;

import edu.umn.biomedicus.common.text.Section;
import edu.umn.biomedicus.common.text.SectionBuilder;
import edu.umn.biomedicus.type.SectionAnnotation;
import org.apache.uima.jcas.JCas;

/**
 *
 */
class SectionBuilderAdapter implements SectionBuilder {
    private final JCas jCas;

    private final SectionAnnotation sectionAnnotation;

    SectionBuilderAdapter(JCas jCas, SectionAnnotation sectionAnnotation) {
        this.jCas = jCas;
        this.sectionAnnotation = sectionAnnotation;
    }

    @Override
    public SectionBuilder withSectionTitle(String sectionTitle) {
        sectionAnnotation.setSectionTitle(sectionTitle);
        return this;
    }

    @Override
    public SectionBuilder withContentStart(int contentStart) {
        sectionAnnotation.setContentStart(contentStart);
        return this;
    }

    @Override
    public SectionBuilder withLevel(int level) {
        sectionAnnotation.setLevel(level);
        return this;
    }

    @Override
    public SectionBuilder withHasSubsections(boolean hasSubsections) {
        sectionAnnotation.setHasSubsections(hasSubsections);
        return this;
    }

    @Override
    public SectionBuilder withKind(String kind) {
        sectionAnnotation.setKind(kind);
        return this;
    }

    @Override
    public Section build() {
        sectionAnnotation.addToIndexes();
        return new SectionAdapter(jCas, sectionAnnotation);
    }
}
