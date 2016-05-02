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

import edu.umn.biomedicus.common.semantics.SubstanceUsage;
import edu.umn.biomedicus.common.semantics.SubstanceUsageBuilder;
import edu.umn.biomedicus.common.semantics.SubstanceUsageType;
import edu.umn.biomedicus.common.simple.SimpleTextSpan;
import edu.umn.biomedicus.common.simple.Spans;
import edu.umn.biomedicus.common.text.*;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.type.*;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * UIMA implementation of the {@link AbstractDocument} model in the BIOMEDicus type system. Uses a
 * combination of {@link JCas} and {@link ClinicalNoteAnnotation} as a backing data store.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
class JCasDocument extends AbstractDocument {
    /**
     * The system view JCas of the document.
     */
    private final JCas view;

    /**
     * The clinical note annotation containing document metadata.
     */
    private final ClinicalNoteAnnotation documentAnnotation;

    /**
     * Default constructor. Instantiates a Document class backed up by a system view {@link JCas}, and the
     * {@link ClinicalNoteAnnotation} within that system view.
     *
     * @param view the {@link JCas} system view
     */
    JCasDocument(JCas view) throws BiomedicusException {
        this.view = view;
        AnnotationIndex<Annotation> index = view.getAnnotationIndex(ClinicalNoteAnnotation.type);
        FSIterator<Annotation> it = index.iterator();
        if (it.hasNext()) {
            @SuppressWarnings("unchecked")
            ClinicalNoteAnnotation annotation = (ClinicalNoteAnnotation) it.next();
            documentAnnotation = annotation;
        } else {
            documentAnnotation = new ClinicalNoteAnnotation(view);
        }
    }

    @Override
    public Iterable<Token> getTokens() {
        final AnnotationIndex<Annotation> tokens = view.getAnnotationIndex(TokenAnnotation.type);
        return () -> new FSIteratorAdapter<>(tokens, UimaAdapters::tokenAdapter);
    }

    @Override
    public Iterable<Sentence> getSentences() {
        final AnnotationIndex<Annotation> sentences = view.getAnnotationIndex(SentenceAnnotation.type);
        return () -> new FSIteratorAdapter<>(sentences, UimaAdapters::sentenceAdapter);
    }

    @Override
    public Sentence createSentence(int begin, int end) {
        SentenceAnnotation sentenceAnnotation = new SentenceAnnotation(view, begin, end);
        sentenceAnnotation.addToIndexes();
        return new SentenceAdapter(view, sentenceAnnotation);
    }

    @Override
    public Iterable<Term> getTerms() {
        final AnnotationIndex<Annotation> terms = view.getAnnotationIndex(TermAnnotation.type);
        return () -> new FSIteratorAdapter<>(terms, UimaAdapters::termAdapter);
    }

    @Override
    public void addTerm(Term term) {
        TermAdapter.copyOf(term, view);
    }

    @Override
    public Reader getReader() {
        InputStream sofaDataStream = view.getSofaDataStream();
        return new BufferedReader(new InputStreamReader(sofaDataStream));
    }

    @Override
    public Token createToken(int begin, int end) {
        TokenAnnotation tokenAnnotation = new TokenAnnotation(view, begin, end);
        tokenAnnotation.addToIndexes();
        return UimaAdapters.tokenAdapter(tokenAnnotation);
    }

    @Override
    public String getText() {
        return view.getDocumentText();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JCasDocument that = (JCasDocument) o;

        return view.equals(that.view);

    }

    @Override
    public int hashCode() {
        return view.hashCode();
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
        Iterable<Annotation> textSegmentAnnotation = view.getAnnotationIndex(TextSegmentAnnotation.type);
        if (textSegmentAnnotation.iterator().hasNext()) {
            return StreamSupport.stream(textSegmentAnnotation.spliterator(), false)
                    .map(a -> new SimpleTextSpan(Spans.spanning(a.getBegin(), a.getEnd()), view.getDocumentText()));
        } else {
            String documentText = view.getDocumentText();
            TextSpan textSpan = Spans.textSpan(documentText);
            return Stream.of(textSpan);
        }
    }

    @Override
    public SectionBuilder createSection(Span span) {
        SectionAnnotation sectionAnnotation = new SectionAnnotation(view, span.getBegin(), span.getEnd());
        return new SectionBuilderAdapter(view, sectionAnnotation);
    }


    @Override
    public Iterable<Section> getSections() {
        return () -> new FSIteratorAdapter<>(view.getAnnotationIndex(SectionAnnotation.type),
                (annotation) -> new SectionAdapter(view, (SectionAnnotation) annotation));
    }

    @Override
    public Iterable<Section> getSectionsAtLevel(int level) {
        Iterable<Section> sections = () -> new FSIteratorAdapter<>(view.getAnnotationIndex(SectionAnnotation.type),
                (annotation) -> new SectionAdapter(view, (SectionAnnotation) annotation));
        return () -> StreamSupport.stream(sections.spliterator(), false).filter(s -> s.getLevel() == level).iterator();
    }

    @Override
    public SubstanceUsageBuilder createSubstanceUsage(Sentence sentence, SubstanceUsageType substanceUsageType) {
        SubstanceUsageAnnotation substanceUsageAnnotation = new SubstanceUsageAnnotation(view,
                sentence.getBegin(), sentence.getEnd());
        return new UimaSubstanceUsageBuilder(view, substanceUsageAnnotation);
    }

    @Override
    public Iterable<SubstanceUsage> getSubstanceUsages() {
        return () -> new FSIteratorAdapter<>(view.getAnnotationIndex(SubstanceUsageAnnotation.type),
                (annotation) -> new SubstanceUsageAdapter(view, (SubstanceUsageAnnotation) annotation));
    }

    private MapEntry getMapEntry(String key) throws CASException {
        FSIterator<TOP> metaDataIterator = view.getJFSIndexRepository().getAllIndexedFS(MapEntry.type);
        while (metaDataIterator.hasNext()) {
            @SuppressWarnings("unchecked")
            MapEntry mapEntry = (MapEntry) metaDataIterator.next();
            if (Objects.equals(mapEntry.getKey(), key)) {
                return mapEntry;
            }
        }
        metaDataIterator = view.getView(CAS.NAME_DEFAULT_SOFA).getJFSIndexRepository().getAllIndexedFS(MapEntry.type);
        while (metaDataIterator.hasNext()) {
            @SuppressWarnings("unchecked")
            MapEntry mapEntry = (MapEntry) metaDataIterator.next();
            if (Objects.equals(mapEntry.getKey(), key)) {
                return mapEntry;
            }
        }

        return null;
    }

    @Nullable
    @Override
    public String getMetadata(String key) throws BiomedicusException {
        MapEntry mapEntry;
        try {
            mapEntry = getMapEntry(key);
        } catch (CASException e) {
            throw new BiomedicusException(e);
        }
        return mapEntry == null ? null : mapEntry.getValue();
    }

    @Override
    public void setMetadata(String key, String value) throws BiomedicusException {
        MapEntry mapEntry;
        try {
            mapEntry = getMapEntry(key);
        } catch (CASException e) {
            throw new BiomedicusException(e);
        }
        if (mapEntry != null) {
            mapEntry.removeFromIndexes();
        } else {
            mapEntry = new MapEntry(view);
        }
        mapEntry.setValue(value);
        mapEntry.addToIndexes();
    }

    @Override
    public void createNewInformationAnnotation(Span span, String kind) {
        NewInformationAnnotation newInformationAnnotation = new NewInformationAnnotation(view, span.getBegin(), span.getEnd());
        newInformationAnnotation.setKind(kind);
        newInformationAnnotation.addToIndexes();
    }

    @Override
    public boolean hasNewInformationAnnotation(Span span, String kind) {

        AnnotationIndex<NewInformationAnnotation> newInfos = view.getAnnotationIndex(NewInformationAnnotation.class);
        for (NewInformationAnnotation newInfo : newInfos) {
            if (newInfo.getBegin() == span.getBegin() && newInfo.getEnd() == span.getEnd() && Objects.equals(newInfo.getKind(), kind)) {
                return true;
            }
        }
        return false;
    }
}
