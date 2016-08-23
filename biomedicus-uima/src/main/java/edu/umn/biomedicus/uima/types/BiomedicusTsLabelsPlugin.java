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
import com.google.inject.Module;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.semantics.*;
import edu.umn.biomedicus.common.style.Bold;
import edu.umn.biomedicus.common.style.Underlined;
import edu.umn.biomedicus.common.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.syntax.PartsOfSpeech;
import edu.umn.biomedicus.common.terms.IndexedTerm;
import edu.umn.biomedicus.common.text.*;
import edu.umn.biomedicus.uima.labels.AbstractLabelAdapter;
import edu.umn.biomedicus.uima.labels.LabelAdapterFactory;
import edu.umn.biomedicus.uima.labels.LabelableModule;
import edu.umn.biomedicus.uima.labels.UimaPlugin;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.text.AnnotationFS;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
    }

    public static class NegatedLabelAdapter extends AbstractLabelAdapter<Negated> {
        @Inject
        public NegatedLabelAdapter(CAS cas) {
            super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_5.Negated"));
        }

        @Override
        protected Negated createLabelValue(FeatureStructure featureStructure) {
            return new Negated();
        }
    }

    public static class HistoricalLabelAdapter extends AbstractLabelAdapter<Historical> {
        @Inject
        public HistoricalLabelAdapter(CAS cas) {
            super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_5.Historical"));
        }

        @Override
        protected Historical createLabelValue(FeatureStructure featureStructure) {
            return new Historical();
        }
    }

    public static class ProbableLabelAdapter extends AbstractLabelAdapter<Probable> {
        @Inject
        public ProbableLabelAdapter(CAS cas) {
            super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_5.Probable"));
        }

        @Override
        protected Probable createLabelValue(FeatureStructure featureStructure) {
            return new Probable();
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
            annotationFS.setIntValue(indexFeature, label.value().term().indexedTerm());
        }

        @Override
        protected WordIndex createLabelValue(FeatureStructure featureStructure) {
            IndexedTerm indexedTerm = new IndexedTerm(featureStructure.getIntValue(indexFeature));
            return new WordIndex(indexedTerm);
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
            annotationFS.setIntValue(indexFeature, label.value().term().indexedTerm());
        }

        @Override
        protected NormIndex createLabelValue(FeatureStructure featureStructure) {
            IndexedTerm indexedTerm = new IndexedTerm(featureStructure.getIntValue(indexFeature));
            return new NormIndex(indexedTerm);
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
        return map;
    }
}
