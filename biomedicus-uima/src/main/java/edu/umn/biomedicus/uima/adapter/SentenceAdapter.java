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

import edu.umn.biomedicus.common.semantics.SubstanceUsageType;
import edu.umn.biomedicus.common.text.Sentence;
import edu.umn.biomedicus.common.text.Term;
import edu.umn.biomedicus.common.text.Token;
import edu.umn.biomedicus.type.SentenceAnnotation;
import edu.umn.biomedicus.type.TermAnnotation;
import edu.umn.biomedicus.type.TokenAnnotation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.StringArray;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Adapts the Biomedicus UIMA Annotation type {@link edu.umn.biomedicus.type.SentenceAnnotation} to the Biomedicus
 * {@link Sentence} interface.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
class SentenceAdapter extends AnnotationAdapter<SentenceAnnotation> implements Sentence {
    SentenceAdapter(JCas jCas, SentenceAnnotation sentenceAnnotation) {
        super(jCas, sentenceAnnotation);
    }

    @Override
    public Stream<Token> tokens() {
        return getCoveredStream(TokenAnnotation.type, UimaAdapters::tokenAdapter);
    }

    @Override
    public Stream<Term> terms() {
        return getCoveredStream(TermAnnotation.type, UimaAdapters::termAdapter);
    }

    @Override
    public String getDependencies() {
        return annotation.getDependencies();
    }

    @Override
    public void setDependencies(String dependencies) {
        annotation.setDependencies(dependencies);
    }

    @Override
    public String getParseTree() {
        return annotation.getParseTree();
    }

    @Override
    public void setParseTree(String parseTree) {
        annotation.setParseTree(parseTree);
    }

    @Override
    public boolean isSocialHistoryCandidate() {
        return annotation.getIsSocialHistoryCandidate();
    }

    @Override
    public void setIsSocialHistoryCandidate(boolean isSocialHistoryCandidate) {
        annotation.setIsSocialHistoryCandidate(isSocialHistoryCandidate);
    }

    @Override
    public Collection<SubstanceUsageType> getSubstanceUsageTypes() {
        return Stream.of(annotation.getSubstanceUsageTypes().toArray())
                .map(SubstanceUsageType::valueOf)
                .collect(Collectors.toList());
    }

    @Override
    public void addSubstanceUsageType(SubstanceUsageType substanceUsageType) {
        StringArray substanceUsageTypes = annotation.getSubstanceUsageTypes();
        substanceUsageTypes.removeFromIndexes();
        int size = substanceUsageTypes.size();
        StringArray newArray = new StringArray(getJCas(), size + 1);
        newArray.copyFromArray(substanceUsageTypes.toArray(), 0, 0, size);
        newArray.set(size, substanceUsageType.name());
        newArray.addToIndexes();
        annotation.setSubstanceUsageTypes(newArray);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SentenceAdapter that = (SentenceAdapter) o;

        return getAnnotation().equals(that.getAnnotation());

    }

    @Override
    public int hashCode() {
        return getAnnotation().hashCode();
    }
}
