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

package edu.umn.biomedicus.uima.types;

import com.google.inject.Inject;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.types.semantics.*;
import edu.umn.biomedicus.common.types.style.Bold;
import edu.umn.biomedicus.common.types.style.Underlined;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.types.syntax.PartsOfSpeech;
import edu.umn.biomedicus.common.terms.IndexedTerm;
import edu.umn.biomedicus.common.types.text.*;
import edu.umn.biomedicus.uima.labels.AbstractLabelAdapter;
import edu.umn.biomedicus.uima.labels.LabelAdapterFactory;
import edu.umn.biomedicus.uima.labels.UimaPlugin;
import opennlp.tools.parser.Cons;
import org.apache.uima.cas.*;
import org.apache.uima.cas.text.AnnotationFS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class BiomedicusTsLabelsPlugin implements UimaPlugin {


    public static class SectionLabelAdapter extends AbstractLabelAdapter<Section> {

        private final Feature kindFeature;

        @Inject
        SectionLabelAdapter(CAS cas) {
            super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.Section"));
            kindFeature = type.getFeatureByBaseName("kind");
        }

        @Override
        protected Section createLabelValue(FeatureStructure featureStructure) {
            return new Section(featureStructure.getStringValue(kindFeature));
        }

        @Override
        protected void fillAnnotation(Label<Section> label, AnnotationFS annotationFS) {
            annotationFS.setStringValue(kindFeature, label.value().getKind());
        }
    }

    public static class SectionTitleLabelAdapter extends AbstractLabelAdapter<SectionTitle> {

        @Inject
        SectionTitleLabelAdapter(CAS cas) {
            super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.SectionTitle"));
        }

        @Override
        protected SectionTitle createLabelValue(FeatureStructure featureStructure) {
            return new SectionTitle();
        }
    }

    public static class SectionContentLabelAdapter extends AbstractLabelAdapter<SectionContent> {

        @Inject
        SectionContentLabelAdapter(CAS cas) {
            super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.SectionContent"));
        }

        @Override
        protected SectionContent createLabelValue(FeatureStructure featureStructure) {
            return new SectionContent();
        }
    }

    public static class SentenceLabelAdapter extends AbstractLabelAdapter<Sentence> {
        @Inject
        public SentenceLabelAdapter(CAS cas) {
            super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.Sentence"));
        }

        @Override
        protected Sentence createLabelValue(FeatureStructure featureStructure) {
            return new Sentence();
        }

        @Override
        public boolean isDistinct() {
            return true;
        }
    }

    public static class TextSegmentLabelAdapter extends AbstractLabelAdapter<TextSegment> {
        @Inject
        public TextSegmentLabelAdapter(CAS cas) {
            super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.type.TextSegmentAnnotation"));
        }

        @Override
        protected TextSegment createLabelValue(FeatureStructure featureStructure) {
            return new TextSegment();
        }

        @Override
        public boolean isDistinct() {
            return true;
        }
    }

    public static class DependencyParseLabelAdapter extends AbstractLabelAdapter<DependencyParse> {
        private final Feature parseTreeFeature;

        @Inject
        public DependencyParseLabelAdapter(CAS cas) {
            super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.DependencyParse"));
            parseTreeFeature = getType().getFeatureByBaseName("parseTree");
        }


        @Override
        protected void fillAnnotation(Label<DependencyParse> label, AnnotationFS annotationFS) {
            annotationFS.setStringValue(parseTreeFeature, label.value().parseTree());
        }

        @Override
        protected DependencyParse createLabelValue(FeatureStructure featureStructure) {
            return new DependencyParse(featureStructure.getStringValue(parseTreeFeature));
        }

        @Override
        public boolean isDistinct() {
            return true;
        }
    }

    static abstract class DictionaryTermModifierLabelAdapter<T extends DictionaryTermModifier> extends AbstractLabelAdapter<T> {
        private final Feature cues;
        private final Type cueType;
        private final Function<List<Span>, T> constructor;

        DictionaryTermModifierLabelAdapter(CAS cas, Type type, Function<List<Span>, T> constructor) {
            super(cas, type);
            this.constructor = constructor;
            cues = type.getFeatureByBaseName("cues");
            cueType = cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.ModificationCue");
        }

        @Override
        protected void fillAnnotation(Label<T> label, AnnotationFS annotationFS) {
            T value = label.value();
            List<Span> cueTerms = value.getCueTerms();
            ArrayFS fsArray = cas.createArrayFS(cueTerms.size());
            for (int i = 0; i < cueTerms.size(); i++) {
                Span cueTerm = cueTerms.get(i);
                AnnotationFS cueAnnotation = cas.createAnnotation(cueType, cueTerm.getBegin(), cueTerm.getEnd());
                cas.addFsToIndexes(cueAnnotation);
                fsArray.set(i, cueAnnotation);
            }
            cas.addFsToIndexes(fsArray);
            annotationFS.setFeatureValue(cues, fsArray);
        }

        @Override
        protected T createLabelValue(FeatureStructure featureStructure) {
            FeatureStructure cuesValue = featureStructure.getFeatureValue(cues);
            if (!(cuesValue instanceof ArrayFS)) {
                throw new IllegalStateException("Cues is not ArrayFS");
            }
            ArrayFS cuesArray = (ArrayFS) cuesValue;

            int size = cuesArray.size();
            List<Span> cueTerms = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                FeatureStructure cueFs = cuesArray.get(i);
                if (!(cueFs instanceof AnnotationFS)) {
                    throw new IllegalStateException();
                }
                AnnotationFS cueAnnotation = (AnnotationFS) cueFs;
                Span span = new Span(cueAnnotation.getBegin(), cueAnnotation.getEnd());
                cueTerms.add(span);
            }

            return constructor.apply(cueTerms);
        }
    }

    private static class NegatedLabelAdapter extends DictionaryTermModifierLabelAdapter<Negated> {
        @Inject
        public NegatedLabelAdapter(CAS cas) {
            super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.Negated"), Negated::new);
        }
    }

    private static class HistoricalLabelAdapter extends DictionaryTermModifierLabelAdapter<Historical> {
        @Inject
        public HistoricalLabelAdapter(CAS cas) {
            super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.Historical"), Historical::new);
        }
    }

    private static class ProbableLabelAdapter extends DictionaryTermModifierLabelAdapter<Probable> {
        @Inject
        public ProbableLabelAdapter(CAS cas) {
            super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.Probable"), Probable::new);
        }
    }

    public static class TermTokenLabelAdapter extends AbstractTokenLabelAdapter<TermToken> {
        @Inject
        public TermTokenLabelAdapter(CAS cas) {
            super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.TermToken"));
        }

        @Override
        protected TermToken createToken(String text, boolean hasSpaceAfter) {
            return new TermToken(text, hasSpaceAfter);
        }
    }

    public static class AcronymLabelAdapter extends AbstractTokenLabelAdapter<Acronym> {
        @Inject
        public AcronymLabelAdapter(CAS cas) {
            super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.Acronym"));
        }

        @Override
        protected Acronym createToken(String text, boolean hasSpaceAfter) {
            return new Acronym(text, hasSpaceAfter);
        }
    }

    public static class ParseTokenLabelAdapter extends AbstractTokenLabelAdapter<ParseToken> {
        @Inject
        public ParseTokenLabelAdapter(CAS cas) {
            super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.ParseToken"));
        }

        @Override
        protected ParseToken createToken(String text, boolean hasSpaceAfter) {
            return new ParseToken(text, hasSpaceAfter);
        }

        @Override
        public boolean isDistinct() {
            return true;
        }
    }

    public static class PartOfSpeechLabelAdapter extends AbstractLabelAdapter<PartOfSpeech> {
        private final Feature partOfSpeechFeature;

        @Inject
        public PartOfSpeechLabelAdapter(CAS cas) {
            super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.PartOfSpeechTag"));
            partOfSpeechFeature = type.getFeatureByBaseName("partOfSpeech");

        }

        @Override
        protected void fillAnnotation(Label<PartOfSpeech> label, AnnotationFS annotationFS) {
            annotationFS.setStringValue(partOfSpeechFeature, label.value().toString());
        }

        @Override
        protected PartOfSpeech createLabelValue(FeatureStructure featureStructure) {
            return PartsOfSpeech.forTag(featureStructure.getStringValue(partOfSpeechFeature));
        }

        @Override
        public boolean isDistinct() {
            return true;
        }
    }

    public static class WordIndexLabelAdapter extends AbstractLabelAdapter<WordIndex> {
        private final Feature indexFeature;

        @Inject
        public WordIndexLabelAdapter(CAS cas) {
            super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_5.WordIndex"));
            indexFeature = type.getFeatureByBaseName("index");
        }

        @Override
        protected void fillAnnotation(Label<WordIndex> label, AnnotationFS annotationFS) {
            annotationFS.setIntValue(indexFeature, label.value().term().termIdentifier());
        }

        @Override
        protected WordIndex createLabelValue(FeatureStructure featureStructure) {
            IndexedTerm indexedTerm = new IndexedTerm(featureStructure.getIntValue(indexFeature));
            return new WordIndex(indexedTerm);
        }

        @Override
        public boolean isDistinct() {
            return true;
        }
    }

    public static class NormFormLabelAdapter extends AbstractLabelAdapter<NormForm> {
        private final Feature normFormFeature;

        @Inject
        public NormFormLabelAdapter(CAS cas) {
            super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.NormForm"));
            normFormFeature = type.getFeatureByBaseName("normForm");

        }

        @Override
        protected void fillAnnotation(Label<NormForm> label, AnnotationFS annotationFS) {
            annotationFS.setStringValue(normFormFeature, label.value().normalForm());
        }

        @Override
        protected NormForm createLabelValue(FeatureStructure featureStructure) {
            return new NormForm(featureStructure.getStringValue(normFormFeature));
        }

        @Override
        public boolean isDistinct() {
            return true;
        }
    }

    public static class NormIndexLabelAdapter extends AbstractLabelAdapter<NormIndex> {
        private final Feature indexFeature;

        @Inject
        public NormIndexLabelAdapter(CAS cas) {
            super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_5.NormIndex"));
            indexFeature = type.getFeatureByBaseName("index");
        }

        @Override
        protected void fillAnnotation(Label<NormIndex> label, AnnotationFS annotationFS) {
            annotationFS.setIntValue(indexFeature, label.value().term().termIdentifier());
        }

        @Override
        protected NormIndex createLabelValue(FeatureStructure featureStructure) {
            IndexedTerm indexedTerm = new IndexedTerm(featureStructure.getIntValue(indexFeature));
            return new NormIndex(indexedTerm);
        }

        @Override
        public boolean isDistinct() {
            return true;
        }
    }

    public static class MisspellingLabelAdapter extends AbstractLabelAdapter<Misspelling> {
        @Inject
        public MisspellingLabelAdapter(CAS cas) {
            super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.Misspelling"));
        }

        @Override
        protected Misspelling createLabelValue(FeatureStructure featureStructure) {
            return new Misspelling();
        }
    }

    public static class SpellCorrectionLabelAdapter extends AbstractTokenLabelAdapter<SpellCorrection> {
        @Inject
        public SpellCorrectionLabelAdapter(CAS cas) {
            super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.SpellCorrection"));
        }

        @Override
        protected SpellCorrection createToken(String text, boolean hasSpaceAfter) {
            return new SpellCorrection(text, hasSpaceAfter);
        }
    }

    public static class BoldLabelAdapter extends AbstractLabelAdapter<Bold> {

        @Inject
        BoldLabelAdapter(CAS cas) {
            super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.rtfuima.type.Bold"));
        }

        @Override
        protected Bold createLabelValue(FeatureStructure featureStructure) {
            return new Bold();
        }
    }

    public static class UnderlinedLabelAdapter extends AbstractLabelAdapter<Underlined> {
        @Inject
        UnderlinedLabelAdapter(CAS cas) {
            super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.rtfuima.type.Underline"));
        }

        @Override
        protected Underlined createLabelValue(FeatureStructure featureStructure) {
            return new Underlined();
        }
    }

    public static class SocialHistoryCandidateLabelAdapter extends AbstractLabelAdapter<SocialHistoryCandidate> {
        private final Feature substanceUsageKind;

        SocialHistoryCandidateLabelAdapter(CAS cas) {
            super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.SocialHistoryCandidate"));
            substanceUsageKind = type.getFeatureByBaseName("substanceUsageKind");
        }

        @Override
        protected void fillAnnotation(Label<SocialHistoryCandidate> label, AnnotationFS annotationFS) {
            SocialHistoryCandidate socialHistoryCandidate = label.value();
            SubstanceUsageKind substanceUsageKind = socialHistoryCandidate.getSubstanceUsageKind();
            annotationFS.setStringValue(this.substanceUsageKind, substanceUsageKind.name());
        }

        @Override
        protected SocialHistoryCandidate createLabelValue(FeatureStructure featureStructure) {
            String substanceUsageAsString = featureStructure.getStringValue(substanceUsageKind);
            return new SocialHistoryCandidate(SubstanceUsageKind.valueOf(substanceUsageAsString));
        }
    }

    public static class SubstanceUsageElementLabelAdapter extends AbstractLabelAdapter<SubstanceUsageElement> {

        private final Feature kindFeature;
        private final Feature elementTypeFeature;

        SubstanceUsageElementLabelAdapter(CAS cas) {
            super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.SubstanceUsageElement"));
            kindFeature = type.getFeatureByBaseName("substanceUsageKind");
            elementTypeFeature = type.getFeatureByBaseName("substanceUsageElementType");
        }


        @Override
        protected void fillAnnotation(Label<SubstanceUsageElement> label, AnnotationFS annotationFS) {
            SubstanceUsageElement value = label.value();
            annotationFS.setStringValue(kindFeature, value.getSubstanceUsageKind().name());
            annotationFS.setStringValue(elementTypeFeature, value.getSubstanceUsageElementType().name());
        }

        @Override
        protected SubstanceUsageElement createLabelValue(FeatureStructure featureStructure) {
            return new SubstanceUsageElement(
                    SubstanceUsageElementType.valueOf(featureStructure.getStringValue(elementTypeFeature)),
                    SubstanceUsageKind.valueOf(featureStructure.getStringValue(kindFeature))
            );
        }
    }

    public static class ConstituencyParseLabelAdapter extends AbstractLabelAdapter<ConstituencyParse> {

        private final Feature parseFeature;

        ConstituencyParseLabelAdapter(CAS cas) {
            super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.ConstituencyParse"));
            parseFeature = type.getFeatureByBaseName("parse");
        }

        @Override
        protected void fillAnnotation(Label<ConstituencyParse> label, AnnotationFS annotationFS) {
            ConstituencyParse constituencyParse = label.value();
            annotationFS.setStringValue(parseFeature, constituencyParse.getParse());
        }

        @Override
        protected ConstituencyParse createLabelValue(FeatureStructure featureStructure) {
            return new ConstituencyParse(featureStructure.getStringValue(parseFeature));
        }
    }


    @Override
    public Map<Class<?>, LabelAdapterFactory> getLabelAdapterFactories() {
        Map<Class<?>, LabelAdapterFactory> map = new HashMap<>();
        map.put(Section.class, SectionLabelAdapter::new);
        map.put(SectionTitle.class, SectionTitleLabelAdapter::new);
        map.put(SectionContent.class, SectionContentLabelAdapter::new);
        map.put(TextSegment.class, TextSegmentLabelAdapter::new);
        map.put(Sentence.class, SentenceLabelAdapter::new);
        map.put(DependencyParse.class, DependencyParseLabelAdapter::new);
        map.put(DictionaryTerm.class, DictionaryTermLabelAdapter::new);
        map.put(Negated.class, NegatedLabelAdapter::new);
        map.put(Historical.class, HistoricalLabelAdapter::new);
        map.put(Probable.class, ProbableLabelAdapter::new);
        map.put(TermToken.class, TermTokenLabelAdapter::new);
        map.put(Acronym.class, AcronymLabelAdapter::new);
        map.put(ParseToken.class, ParseTokenLabelAdapter::new);
        map.put(PartOfSpeech.class, PartOfSpeechLabelAdapter::new);
        map.put(WordIndex.class, WordIndexLabelAdapter::new);
        map.put(NormForm.class, NormFormLabelAdapter::new);
        map.put(NormIndex.class, NormIndexLabelAdapter::new);
        map.put(Misspelling.class, MisspellingLabelAdapter::new);
        map.put(SpellCorrection.class, SpellCorrectionLabelAdapter::new);
        map.put(Bold.class, BoldLabelAdapter::new);
        map.put(Underlined.class, UnderlinedLabelAdapter::new);
        map.put(SocialHistoryCandidate.class, SocialHistoryCandidateLabelAdapter::new);
        map.put(SubstanceUsageElement.class, SubstanceUsageElementLabelAdapter::new);
        map.put(ConstituencyParse.class, ConstituencyParseLabelAdapter::new);
        return map;
    }
}
