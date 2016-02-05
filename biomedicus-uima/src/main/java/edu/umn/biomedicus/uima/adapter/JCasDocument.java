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

package edu.umn.biomedicus.uima.adapter;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.model.semantics.SubstanceUsage;
import edu.umn.biomedicus.model.semantics.SubstanceUsageBuilder;
import edu.umn.biomedicus.model.semantics.SubstanceUsageType;
import edu.umn.biomedicus.model.simple.SimpleTextSpan;
import edu.umn.biomedicus.model.simple.Spans;
import edu.umn.biomedicus.model.text.*;
import edu.umn.biomedicus.type.*;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * UIMA implementation of the {@link AbstractDocument} model in the BIOMEDicus type system. Uses a
 * combination of {@link JCas} and {@link ClinicalNoteAnnotation} as a backing data store.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
@DocumentScoped
class JCasDocument extends AbstractDocument {
    /**
     * The system view JCas of the document.
     */
    private final JCas systemView;

    /**
     * The clinical note annotation containing document metadata.
     */
    private final ClinicalNoteAnnotation documentAnnotation;

    /**
     * Default constructor. Instantiates a Document class backed up by a system view {@link JCas}, and the
     * {@link ClinicalNoteAnnotation} within that system view.
     *
     * @param systemView the {@link JCas} system view
     */
    @Inject
    JCasDocument(JCas systemView) {
        this.systemView = systemView;
        AnnotationIndex<Annotation> index = systemView.getAnnotationIndex(ClinicalNoteAnnotation.type);
        FSIterator<Annotation> it = index.iterator();
        if (it.hasNext()) {
            Annotation annotation = it.next();
            if (annotation instanceof ClinicalNoteAnnotation) {
                documentAnnotation = (ClinicalNoteAnnotation) annotation;
            } else {
                throw new RuntimeException("DocumentAnnotation index returned non document annotation");
            }
        } else {
            documentAnnotation = new ClinicalNoteAnnotation(systemView, 0, systemView.getSofaDataString().length());
            documentAnnotation.addToIndexes();
        }
    }

    @Override
    public Iterable<Token> getTokens() {
        final AnnotationIndex<Annotation> tokens = systemView.getAnnotationIndex(TokenAnnotation.type);
        return () -> new FSIteratorAdapter<>(tokens, UimaAdapters::tokenAdapter);
    }

    @Override
    public Iterable<Sentence> getSentences() {
        final AnnotationIndex<Annotation> sentences = systemView.getAnnotationIndex(SentenceAnnotation.type);
        return () -> new FSIteratorAdapter<>(sentences, UimaAdapters::sentenceAdapter);
    }

    @Override
    public Sentence createSentence(int begin, int end) {
        SentenceAnnotation sentenceAnnotation = new SentenceAnnotation(systemView, begin, end);
        sentenceAnnotation.addToIndexes();
        return new SentenceAdapter(systemView, sentenceAnnotation);
    }

    @Override
    public Iterable<Term> getTerms() {
        final AnnotationIndex<Annotation> terms = systemView.getAnnotationIndex(TermAnnotation.type);
        return () -> new FSIteratorAdapter<>(terms, UimaAdapters::termAdapter);
    }

    @Override
    public void addTerm(Term term) {
        TermAdapter.copyOf(term, systemView);
    }

    @Override
    public Reader getReader() {
        InputStream sofaDataStream = systemView.getSofaDataStream();
        return new BufferedReader(new InputStreamReader(sofaDataStream));
    }

    @Override
    public Token createToken(int begin, int end) {
        TokenAnnotation tokenAnnotation = new TokenAnnotation(systemView, begin, end);
        tokenAnnotation.addToIndexes();
        return UimaAdapters.tokenAdapter(tokenAnnotation);
    }

    @Override
    public String getText() {
        return systemView.getDocumentText();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JCasDocument that = (JCasDocument) o;

        if (!systemView.equals(that.systemView)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return systemView.hashCode();
    }

    @Override
    public void setCategory(String category) {
        documentAnnotation.setCategory(category);
    }

    @Override
    public String getCategory() {
        return documentAnnotation.getCategory();
    }

    @Override
    public void beginEditing() {
        documentAnnotation.removeFromIndexes();
    }

    @Override
    public void endEditing() {
        documentAnnotation.addToIndexes();
    }

    @Override
    public String getIdentifier() {
        return documentAnnotation.getDocumentId();
    }

    @Override
    public Stream<TextSpan> textSegments() {
        Iterable<Annotation> textSegmentAnnotation = systemView.getAnnotationIndex(TextSegmentAnnotation.type);
        if (textSegmentAnnotation.iterator().hasNext()) {
            return StreamSupport.stream(textSegmentAnnotation.spliterator(), false)
                    .map(a -> new SimpleTextSpan(Spans.spanning(a.getBegin(), a.getEnd()), systemView.getDocumentText()));
        } else {
            String documentText = systemView.getDocumentText();
            TextSpan textSpan = Spans.textSpan(documentText);
            return Stream.of(textSpan);
        }
    }

    @Override
    public SectionBuilder createSection(Span span) {
        SectionAnnotation sectionAnnotation = new SectionAnnotation(systemView, span.getBegin(), span.getEnd());
        return new SectionBuilderAdapter(systemView, sectionAnnotation);
    }


    @Override
    public Iterable<Section> getSections() {
        return () -> new FSIteratorAdapter<>(systemView.getAnnotationIndex(SectionAnnotation.type),
                (annotation) -> new SectionAdapter(systemView, (SectionAnnotation) annotation));
    }

    @Override
    public Iterable<Section> getSectionsAtLevel(int level) {
        Iterable<Section> sections = () -> new FSIteratorAdapter<>(systemView.getAnnotationIndex(SectionAnnotation.type),
                (annotation) -> new SectionAdapter(systemView, (SectionAnnotation) annotation));
        return () -> StreamSupport.stream(sections.spliterator(), false).filter(s -> s.getLevel() == level).iterator();
    }

    @Override
    public SubstanceUsageBuilder createSubstanceUsage(Sentence sentence, SubstanceUsageType substanceUsageType) {
        SubstanceUsageAnnotation substanceUsageAnnotation = new SubstanceUsageAnnotation(systemView,
                sentence.getBegin(), sentence.getEnd());
        return new UimaSubstanceUsageBuilder(systemView, substanceUsageAnnotation);
    }

    @Override
    public Iterable<SubstanceUsage> getSubstanceUsages() {
        return () -> new FSIteratorAdapter<>(systemView.getAnnotationIndex(SubstanceUsageAnnotation.type),
                (annotation) -> new SubstanceUsageAdapter(systemView, (SubstanceUsageAnnotation) annotation));
    }
}
