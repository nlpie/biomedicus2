/*
 * Copyright (c) 2018 Regents of the University of Minnesota.
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
import edu.umn.biomedicus.acronyms.Acronym;
import edu.umn.biomedicus.common.dictionary.StringIdentifier;
import edu.umn.biomedicus.common.types.syntax.PartsOfSpeech;
import edu.umn.biomedicus.concepts.DictionaryTerm;
import edu.umn.biomedicus.family.Relative;
import edu.umn.biomedicus.formatting.Bold;
import edu.umn.biomedicus.formatting.Underlined;
import edu.umn.biomedicus.io.IllegalXmlCharacter;
import edu.umn.biomedicus.measures.CandidateUnitOfMeasure;
import edu.umn.biomedicus.measures.FuzzyValue;
import edu.umn.biomedicus.measures.IndefiniteQuantifierCue;
import edu.umn.biomedicus.measures.Number;
import edu.umn.biomedicus.measures.NumberRange;
import edu.umn.biomedicus.measures.Quantifier;
import edu.umn.biomedicus.measures.StandaloneQuantifier;
import edu.umn.biomedicus.measures.TimeFrequencyUnit;
import edu.umn.biomedicus.measures.TimeUnit;
import edu.umn.biomedicus.modification.DictionaryTermModifier;
import edu.umn.biomedicus.modification.Historical;
import edu.umn.biomedicus.modification.Negated;
import edu.umn.biomedicus.modification.Probable;
import edu.umn.biomedicus.normalization.NormForm;
import edu.umn.biomedicus.numbers.NumberType;
import edu.umn.biomedicus.parsing.ConstituencyParse;
import edu.umn.biomedicus.parsing.DependencyParse;
import edu.umn.biomedicus.sections.Section;
import edu.umn.biomedicus.sections.SectionContent;
import edu.umn.biomedicus.sections.SectionTitle;
import edu.umn.biomedicus.sentences.Sentence;
import edu.umn.biomedicus.sentences.TextSegment;
import edu.umn.biomedicus.sh.AlcoholCandidate;
import edu.umn.biomedicus.sh.DrugCandidate;
import edu.umn.biomedicus.sh.GenericMethodPhrase;
import edu.umn.biomedicus.sh.NicotineAmount;
import edu.umn.biomedicus.sh.NicotineCandidate;
import edu.umn.biomedicus.sh.NicotineCue;
import edu.umn.biomedicus.sh.NicotineFrequency;
import edu.umn.biomedicus.sh.NicotineMethod;
import edu.umn.biomedicus.sh.NicotineStatus;
import edu.umn.biomedicus.sh.NicotineTemporal;
import edu.umn.biomedicus.sh.NicotineType;
import edu.umn.biomedicus.sh.NicotineUnit;
import edu.umn.biomedicus.sh.SocialHistorySectionHeader;
import edu.umn.biomedicus.sh.UsageFrequency;
import edu.umn.biomedicus.sh.UsageFrequencyPhrase;
import edu.umn.biomedicus.sh.UsageStatus;
import edu.umn.biomedicus.structure.Cell;
import edu.umn.biomedicus.structure.NestedCell;
import edu.umn.biomedicus.structure.NestedRow;
import edu.umn.biomedicus.structure.Row;
import edu.umn.biomedicus.tagging.PosTag;
import edu.umn.biomedicus.time.DayOfWeek;
import edu.umn.biomedicus.time.Month;
import edu.umn.biomedicus.time.SeasonWord;
import edu.umn.biomedicus.time.TemporalPhrase;
import edu.umn.biomedicus.time.TextDate;
import edu.umn.biomedicus.time.TextTime;
import edu.umn.biomedicus.time.TimeOfDayWord;
import edu.umn.biomedicus.time.YearNumber;
import edu.umn.biomedicus.time.YearRange;
import edu.umn.biomedicus.tokenization.ParseToken;
import edu.umn.biomedicus.tokenization.TermToken;
import edu.umn.biomedicus.tokenization.WordIndex;
import edu.umn.biomedicus.uima.labels.AbstractLabelAdapter;
import edu.umn.biomedicus.uima.labels.LabelAdapterFactory;
import edu.umn.biomedicus.uima.labels.UimaPlugin;
import edu.umn.nlpengine.Span;
import edu.umn.nlpengine.TextRange;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.uima.cas.ArrayFS;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.FeatureStructure;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;

/**
 * Plugin containing all the label adapters that change BioMedICUS types to UIMA types.
 *
 * @author Ben Knoll
 * @since 1.5.0
 */
public final class BiomedicusTsLabelsPlugin implements UimaPlugin {


  @Override
  public List<Class<? extends TextRange>> getDistinctAutoAdapted() {
    return Arrays.asList(
        SocialHistorySectionHeader.class,
        NicotineCandidate.class,
        NicotineCue.class,
        AlcoholCandidate.class,
        DrugCandidate.class,
        IndefiniteQuantifierCue.class,
        FuzzyValue.class,
        Quantifier.class,
        TimeUnit.class,
        TimeFrequencyUnit.class,
        StandaloneQuantifier.class,
        DayOfWeek.class,
        Month.class,
        YearNumber.class,
        YearRange.class,
        TextDate.class,
        TemporalPhrase.class,
        SeasonWord.class,
        TimeOfDayWord.class,
        TextTime.class,
        UsageFrequency.class,
        UsageFrequencyPhrase.class,
        UsageStatus.class,
        GenericMethodPhrase.class,
        NicotineUnit.class,
        NicotineAmount.class,
        NicotineFrequency.class,
        NicotineTemporal.class,
        NicotineType.class,
        NicotineStatus.class,
        NicotineMethod.class
    );
  }

  @Override
  public List<Class<? extends TextRange>> getAutoAdapted() {
    return Collections.emptyList();
  }

  @Override
  public Map<Class<? extends TextRange>, LabelAdapterFactory> getLabelAdapterFactories() {
    Map<Class<? extends TextRange>, LabelAdapterFactory> map = new HashMap<>();
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
    map.put(PosTag.class, PosTagAdapter::new);
    map.put(WordIndex.class, WordIndexLabelAdapter::new);
    map.put(NormForm.class, NormFormLabelAdapter::new);
    map.put(Bold.class, BoldLabelAdapter::new);
    map.put(Underlined.class, UnderlinedLabelAdapter::new);
    map.put(ConstituencyParse.class, ConstituencyParseLabelAdapter::new);
    map.put(Row.class, RowLabelAdapter::new);
    map.put(Cell.class, CellLabelAdapter::new);
    map.put(NestedRow.class, NestedRowLabelAdapter::new);
    map.put(NestedCell.class, NestedCellLabelAdapter::new);
    map.put(Number.class, NumberLabelAdapter::new);
    map.put(CandidateUnitOfMeasure.class, CanididateUnitOfMeasureAdapter::new);
    map.put(IllegalXmlCharacter.class, IllegalXmlCharacterAdapter::new);
    map.put(NumberRange.class, NumberRangeAdapter::new);
    map.put(Relative.class, RelativeAdapter::new);
    return map;
  }

  public static class SectionLabelAdapter extends AbstractLabelAdapter<Section> {

    private final Feature kindFeature;

    @Inject
    SectionLabelAdapter(CAS cas) {
      super(cas, cas.getTypeSystem()
          .getType("edu.umn.biomedicus.uima.type1_6.Section"));
      kindFeature = type.getFeatureByBaseName("kind");
    }

    @Override
    protected void fillAnnotation(Section label, AnnotationFS annotationFS) {
      annotationFS.setStringValue(kindFeature, label.getKind());
    }

    @Override
    public Section annotationToLabel(AnnotationFS annotationFS) {
      return new Section(annotationFS.getBegin(), annotationFS.getEnd(),
          annotationFS.getStringValue(kindFeature));
    }
  }

  public static class SectionTitleLabelAdapter extends AbstractLabelAdapter<SectionTitle> {

    @Inject
    SectionTitleLabelAdapter(CAS cas) {
      super(cas, cas.getTypeSystem()
          .getType("edu.umn.biomedicus.uima.type1_6.SectionTitle"));
    }

    @Override
    public SectionTitle annotationToLabel(AnnotationFS annotationFS) {
      return new SectionTitle(annotationFS.getBegin(), annotationFS.getEnd());
    }
  }

  public static class SectionContentLabelAdapter extends AbstractLabelAdapter<SectionContent> {

    @Inject
    SectionContentLabelAdapter(CAS cas) {
      super(cas, cas.getTypeSystem()
          .getType("edu.umn.biomedicus.uima.type1_6.SectionContent"));
    }

    @Override
    public SectionContent annotationToLabel(AnnotationFS annotationFS) {
      return new SectionContent(annotationFS.getBegin(), annotationFS.getEnd());
    }
  }

  public static class SentenceLabelAdapter extends AbstractLabelAdapter<Sentence> {

    @Inject
    public SentenceLabelAdapter(CAS cas) {
      super(cas, cas.getTypeSystem()
          .getType("edu.umn.biomedicus.uima.type1_6.Sentence"));
    }

    @Override
    public boolean isDistinct() {
      return true;
    }

    @Override
    public Sentence annotationToLabel(AnnotationFS annotationFS) {
      return new Sentence(annotationFS.getBegin(), annotationFS.getEnd());
    }
  }

  public static class TextSegmentLabelAdapter extends AbstractLabelAdapter<TextSegment> {

    @Inject
    public TextSegmentLabelAdapter(CAS cas) {
      super(cas, cas.getTypeSystem()
          .getType("edu.umn.biomedicus.type.TextSegmentAnnotation"));
    }

    @Override
    public boolean isDistinct() {
      return true;
    }

    @Override
    public TextSegment annotationToLabel(AnnotationFS annotationFS) {
      return new TextSegment(annotationFS.getBegin(), annotationFS.getEnd());
    }
  }

  public static class DependencyParseLabelAdapter extends AbstractLabelAdapter<DependencyParse> {

    private final Feature parseTreeFeature;

    @Inject
    public DependencyParseLabelAdapter(CAS cas) {
      super(cas, cas.getTypeSystem().getType(
          "edu.umn.biomedicus.uima.type1_6.DependencyParse"));
      parseTreeFeature = getType().getFeatureByBaseName("parseTree");
    }


    @Override
    protected void fillAnnotation(DependencyParse label, AnnotationFS annotationFS) {
      annotationFS.setStringValue(parseTreeFeature, label.getParseTree());
    }

    @Override
    public boolean isDistinct() {
      return true;
    }

    @Override
    public DependencyParse annotationToLabel(AnnotationFS annotationFS) {
      return new DependencyParse(annotationFS.getBegin(), annotationFS.getEnd(),
          annotationFS.getStringValue(parseTreeFeature));
    }
  }

  static abstract class DictionaryTermModifierLabelAdapter<T extends DictionaryTermModifier>
      extends AbstractLabelAdapter<T> {

    private final Feature cues;
    private final Type cueType;

    DictionaryTermModifierLabelAdapter(CAS cas, Type type) {
      super(cas, type);
      cues = type.getFeatureByBaseName("cues");
      cueType = cas.getTypeSystem()
          .getType("edu.umn.biomedicus.uima.type1_6.ModificationCue");
    }

    @Override
    protected void fillAnnotation(T label, AnnotationFS annotationFS) {
      List<Span> cueTerms = label.getCueTerms();
      ArrayFS fsArray = cas.createArrayFS(cueTerms.size());
      for (int i = 0; i < cueTerms.size(); i++) {
        Span cueTerm = cueTerms.get(i);

        AnnotationFS cueAnnotation = cas.createAnnotation(cueType, cueTerm.getStartIndex(),
            cueTerm.getEndIndex());

        cas.addFsToIndexes(cueAnnotation);
        fsArray.set(i, cueAnnotation);
      }
      cas.addFsToIndexes(fsArray);
      annotationFS.setFeatureValue(cues, fsArray);
    }

    protected abstract T create(int start, int end, List<Span> cueTerms);

    @Override
    public T annotationToLabel(AnnotationFS annotationFS) {
      FeatureStructure cuesValue = annotationFS.getFeatureValue(cues);
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
        Span span = new Span(cueAnnotation.getBegin(),
            cueAnnotation.getEnd());
        cueTerms.add(span);
      }

      return create(annotationFS.getBegin(), annotationFS.getEnd(), cueTerms);
    }
  }

  private static class NegatedLabelAdapter extends DictionaryTermModifierLabelAdapter<Negated> {

    @Inject
    public NegatedLabelAdapter(CAS cas) {
      super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.Negated"));
    }

    @Override
    protected Negated create(int start, int end, List<Span> cueTerms) {
      return new Negated(start, end, cueTerms);
    }
  }

  private static class HistoricalLabelAdapter
      extends DictionaryTermModifierLabelAdapter<Historical> {

    @Inject
    public HistoricalLabelAdapter(CAS cas) {
      super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.Historical"));
    }

    @Override
    protected Historical create(int start, int end, List<Span> cueTerms) {
      return new Historical(start, end, cueTerms);
    }
  }

  private static class ProbableLabelAdapter extends DictionaryTermModifierLabelAdapter<Probable> {

    @Inject
    public ProbableLabelAdapter(CAS cas) {
      super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.Probable"));
    }

    @Override
    protected Probable create(int start, int end, List<Span> cueTerms) {
      return new Probable(start, end, cueTerms);
    }
  }

  public static class TermTokenLabelAdapter extends AbstractTokenLabelAdapter<TermToken> {

    @Inject
    public TermTokenLabelAdapter(CAS cas) {
      super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.TermToken"));
    }

    @Override
    public boolean isDistinct() {
      return true;
    }

    @Override
    protected TermToken createToken(int begin, int end, String text, boolean hasSpaceAfter) {
      return new TermToken(begin, end, text, hasSpaceAfter);
    }
  }

  public static class AcronymLabelAdapter extends AbstractTokenLabelAdapter<Acronym> {

    @Inject
    public AcronymLabelAdapter(CAS cas) {
      super(cas, cas.getTypeSystem()
          .getType("edu.umn.biomedicus.uima.type1_6.Acronym"));
    }

    @Override
    protected Acronym createToken(int begin, int end, String text, boolean hasSpaceAfter) {
      return new Acronym(begin, end, text, hasSpaceAfter);
    }
  }

  public static class ParseTokenLabelAdapter extends AbstractTokenLabelAdapter<ParseToken> {

    @Inject
    public ParseTokenLabelAdapter(CAS cas) {
      super(cas, cas.getTypeSystem()
          .getType("edu.umn.biomedicus.uima.type1_6.ParseToken"));
    }

    @Override
    public boolean isDistinct() {
      return true;
    }

    @Override
    protected ParseToken createToken(int begin, int end, String text, boolean hasSpaceAfter) {
      return new ParseToken(begin, end, text, hasSpaceAfter);
    }
  }

  public static class PosTagAdapter extends AbstractLabelAdapter<PosTag> {

    private final Feature partOfSpeechFeature;

    @Inject
    public PosTagAdapter(CAS cas) {
      super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_6.PartOfSpeechTag"));
      partOfSpeechFeature = type.getFeatureByBaseName("partOfSpeech");
    }

    @Override
    public PosTag annotationToLabel(AnnotationFS annotationFS) {
      return new PosTag(annotationFS.getBegin(), annotationFS.getEnd(),
          PartsOfSpeech.forTag(annotationFS.getStringValue(partOfSpeechFeature)));
    }

    @Override
    protected void fillAnnotation(PosTag label, AnnotationFS annotationFS) {
      annotationFS.setStringValue(partOfSpeechFeature, label.getPartOfSpeech().toString());
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
      super(cas, cas.getTypeSystem()
          .getType("edu.umn.biomedicus.uima.type1_5.WordIndex"));
      indexFeature = type.getFeatureByBaseName("index");
    }

    @Override
    protected void fillAnnotation(WordIndex label, AnnotationFS annotationFS) {
      annotationFS.setIntValue(indexFeature, label.getStringIdentifier().value());
    }

    @Override
    public boolean isDistinct() {
      return true;
    }

    @Override
    public WordIndex annotationToLabel(AnnotationFS annotationFS) {
      StringIdentifier stringIdentifier = new StringIdentifier(
          annotationFS.getIntValue(indexFeature)
      );
      return new WordIndex(annotationFS.getBegin(), annotationFS.getEnd(), stringIdentifier);
    }
  }

  public static class NormFormLabelAdapter extends AbstractLabelAdapter<NormForm> {

    private final Feature normFormFeature;
    private final Feature indexFeature;

    @Inject
    public NormFormLabelAdapter(CAS cas) {
      super(cas, cas.getTypeSystem()
          .getType("edu.umn.biomedicus.uima.type1_7.NormForm"));
      normFormFeature = type.getFeatureByBaseName("normForm");
      indexFeature = type.getFeatureByBaseName("index");
    }

    @Override
    protected void fillAnnotation(NormForm label, AnnotationFS annotationFS) {
      annotationFS.setStringValue(normFormFeature, label.getNormalForm());
      annotationFS.setIntValue(indexFeature, label.getNormIdentifier().value());
    }

    @Override
    public boolean isDistinct() {
      return true;
    }

    @Override
    public NormForm annotationToLabel(AnnotationFS annotationFS) {
      return new NormForm(annotationFS.getBegin(), annotationFS.getEnd(),
          annotationFS.getStringValue(normFormFeature),
          new StringIdentifier(annotationFS.getIntValue(indexFeature)));
    }
  }

  public static class BoldLabelAdapter extends AbstractLabelAdapter<Bold> {

    @Inject
    BoldLabelAdapter(CAS cas) {
      super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.rtfuima.type.Bold"));
    }

    @Override
    public Bold annotationToLabel(AnnotationFS annotationFS) {
      return new Bold(annotationFS.getBegin(), annotationFS.getEnd());
    }
  }

  public static class UnderlinedLabelAdapter extends AbstractLabelAdapter<Underlined> {

    @Inject
    UnderlinedLabelAdapter(CAS cas) {
      super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.rtfuima.type.Underline"));
    }

    @Override
    public Underlined annotationToLabel(AnnotationFS annotationFS) {
      return new Underlined(annotationFS.getBegin(), annotationFS.getEnd());
    }
  }

  public static class ConstituencyParseLabelAdapter
      extends AbstractLabelAdapter<ConstituencyParse> {

    private final Feature parseFeature;

    ConstituencyParseLabelAdapter(CAS cas) {
      super(cas, cas.getTypeSystem().getType(
          "edu.umn.biomedicus.uima.type1_6.ConstituencyParse"));
      parseFeature = type.getFeatureByBaseName("parse");
    }

    @Override
    protected void fillAnnotation(ConstituencyParse label, AnnotationFS annotationFS) {
      annotationFS.setStringValue(parseFeature, label.getParseTree());
    }

    @Override
    public ConstituencyParse annotationToLabel(AnnotationFS annotationFS) {
      return new ConstituencyParse(annotationFS.getBegin(), annotationFS.getEnd(),
          annotationFS.getStringValue(parseFeature));
    }
  }

  public static class RowLabelAdapter extends AbstractLabelAdapter<Row> {

    RowLabelAdapter(CAS cas) {
      super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.type.RowAnnotation"));
    }

    @Override
    public Row annotationToLabel(AnnotationFS annotationFS) {
      return new Row(annotationFS.getBegin(), annotationFS.getEnd());
    }
  }

  public static class CellLabelAdapter extends AbstractLabelAdapter<Cell> {

    CellLabelAdapter(CAS cas) {
      super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.type.CellAnnotation"));
    }

    @Override
    public Cell annotationToLabel(AnnotationFS annotationFS) {
      return new Cell(annotationFS.getBegin(), annotationFS.getEnd());
    }
  }

  public static class NestedRowLabelAdapter extends AbstractLabelAdapter<NestedRow> {

    NestedRowLabelAdapter(CAS cas) {
      super(cas, cas.getTypeSystem()
          .getType("edu.umn.biomedicus.type.NestedRowAnnotation"));
    }

    @Override
    public NestedRow annotationToLabel(AnnotationFS annotationFS) {
      return new NestedRow(annotationFS.getBegin(), annotationFS.getEnd());
    }
  }

  public static class NestedCellLabelAdapter extends AbstractLabelAdapter<NestedCell> {

    NestedCellLabelAdapter(CAS cas) {
      super(cas,
          cas.getTypeSystem().getType("edu.umn.biomedicus.type.NestedCellAnnotation"));
    }

    @Override
    public NestedCell annotationToLabel(AnnotationFS annotationFS) {
      return new NestedCell(annotationFS.getBegin(), annotationFS.getEnd());
    }
  }

  public static class NumberLabelAdapter extends AbstractLabelAdapter<Number> {

    private final Feature numFeature;
    private final Feature denomFeature;
    private final Feature typeFeature;

    NumberLabelAdapter(CAS cas) {
      super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_8.Number"));
      numFeature = cas.getTypeSystem()
          .getFeatureByFullName("edu.umn.biomedicus.uima.type1_8.Number:numerator");
      denomFeature = cas.getTypeSystem()
          .getFeatureByFullName("edu.umn.biomedicus.uima.type1_8.Number:denominator");
      typeFeature = cas.getTypeSystem()
          .getFeatureByFullName("edu.umn.biomedicus.uima.type1_8.Number:type");

    }

    @Override
    protected void fillAnnotation(Number label, AnnotationFS annotationFS) {
      annotationFS.setStringValue(numFeature, label.getNumerator());
      annotationFS.setStringValue(denomFeature, label.getDenominator());
      annotationFS.setStringValue(typeFeature, label.getNumberType().name());
    }

    @Override
    public Number annotationToLabel(AnnotationFS annotationFS) {
      String value = annotationFS.getStringValue(numFeature);
      String denom = annotationFS.getStringValue(denomFeature);
      NumberType numberType = NumberType.valueOf(annotationFS.getStringValue(typeFeature));
      return new Number(annotationFS.getBegin(), annotationFS.getEnd(), value, denom, numberType);
    }
  }

  public static class CanididateUnitOfMeasureAdapter
      extends AbstractLabelAdapter<CandidateUnitOfMeasure> {

    CanididateUnitOfMeasureAdapter(CAS cas) {
      super(cas, cas.getTypeSystem()
          .getType("edu.umn.biomedicus.uima.type1_8.CandidateUnitOfMeasure"));
    }

    @Override
    public CandidateUnitOfMeasure annotationToLabel(AnnotationFS annotationFS) {
      return new CandidateUnitOfMeasure(annotationFS.getBegin(), annotationFS.getEnd());
    }
  }

  public static class IllegalXmlCharacterAdapter extends AbstractLabelAdapter<IllegalXmlCharacter> {

    private final Feature valueFeature;

    IllegalXmlCharacterAdapter(CAS cas) {
      super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.type.IllegalXmlCharacter"));
      valueFeature = type.getFeatureByBaseName("value");
    }

    @Override
    protected void fillAnnotation(IllegalXmlCharacter label, AnnotationFS annotationFS) {
      annotationFS.setIntValue(valueFeature, label.getValue());
    }

    @Override
    public IllegalXmlCharacter annotationToLabel(AnnotationFS annotationFS) {
      return new IllegalXmlCharacter(annotationFS.getBegin(), annotationFS.getEnd(),
          annotationFS.getIntValue(valueFeature));
    }
  }

  public static class NumberRangeAdapter extends AbstractLabelAdapter<NumberRange> {

    private final Feature lowerValueFeature;
    private final Feature upperValueFeature;

    NumberRangeAdapter(CAS cas) {
      super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type1_9.NumberRange"));
      lowerValueFeature = type.getFeatureByBaseName("lowerValue");
      upperValueFeature = type.getFeatureByBaseName("upperValue");
    }

    @Override
    protected void fillAnnotation(NumberRange label, AnnotationFS annotationFS) {
      annotationFS.setStringValue(lowerValueFeature, label.getLower().toString());
      annotationFS.setStringValue(upperValueFeature, label.getUpper().toString());
    }

    @Override
    public NumberRange annotationToLabel(AnnotationFS annotationFS) {
      return new NumberRange(annotationFS.getBegin(), annotationFS.getEnd(),
          new BigDecimal(annotationFS.getStringValue(lowerValueFeature)),
          new BigDecimal(annotationFS.getStringValue(upperValueFeature)));
    }
  }

  public static class RelativeAdapter extends AbstractLabelAdapter<Relative> {

    private final Feature valueFeature;

    public RelativeAdapter(CAS cas) {
      super(cas, cas.getTypeSystem().getType("edu.umn.biomedicus.uima.type2_0.Relative"));
      valueFeature = cas.getTypeSystem()
          .getFeatureByFullName("edu.umn.biomedicus.uima.type2_0.Relative:value");
    }

    @Override
    public Relative annotationToLabel(AnnotationFS annotationFS) {
      return new Relative(annotationFS.getBegin(), annotationFS.getEnd(),
          annotationFS.getStringValue(valueFeature));
    }

    @Override
    protected void fillAnnotation(Relative label, AnnotationFS annotationFS) {
      annotationFS.setStringValue(valueFeature, label.getValue());
    }
  }
}
