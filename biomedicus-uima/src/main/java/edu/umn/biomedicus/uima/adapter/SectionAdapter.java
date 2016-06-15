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
import edu.umn.biomedicus.common.text.Sentence;
import edu.umn.biomedicus.type.SectionAnnotation;
import edu.umn.biomedicus.type.SentenceAnnotation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Implementation of {@link Section} of UIMA.
 *
 * @author Ben Knoll
 * @since 1.4
 */
class SectionAdapter extends AnnotationAdapter<SectionAnnotation> implements Section {
    /**
     * Protected constructor for AnnotationAdapter. Initializes the two fields, {@code jCas} and {@code annotation}.
     *
     * @param jCas       the {@link JCas} document the annotation is stored in.
     * @param annotation the {@link Annotation} itself.
     */
    SectionAdapter(JCas jCas, SectionAnnotation annotation) {
        super(jCas, annotation);
    }

    @Override
    public String getSectionTitle() {
        return getAnnotation().getSectionTitle();
    }

    @Override
    public int contentStart() {
        return getAnnotation().getContentStart();
    }

    @Override
    public int getLevel() {
        return getAnnotation().getLevel();
    }

    @Override
    public boolean hasSubsections() {
        return getAnnotation().getHasSubsections();
    }

    @Override
    public String getKind() {
        return getAnnotation().getKind();
    }

    @Override
    public Iterable<Sentence> getSentences() {
        return () -> getCoveredStream(SentenceAnnotation.type, UimaAdapters::sentenceAdapter).iterator();
    }
}
