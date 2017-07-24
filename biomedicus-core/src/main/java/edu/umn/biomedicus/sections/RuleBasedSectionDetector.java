/*
 * Copyright (c) 2017 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.sections;

import com.google.inject.Inject;
import edu.umn.biomedicus.common.StandardViews;
import edu.umn.biomedicus.common.types.style.Bold;
import edu.umn.biomedicus.common.types.style.Underlined;
import edu.umn.biomedicus.common.types.text.ImmutableSection;
import edu.umn.biomedicus.common.types.text.Section;
import edu.umn.biomedicus.common.types.text.SectionContent;
import edu.umn.biomedicus.common.types.text.SectionTitle;
import edu.umn.biomedicus.common.types.text.Sentence;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.framework.store.Labeler;
import edu.umn.biomedicus.framework.store.Span;
import edu.umn.biomedicus.framework.store.TextView;
import java.util.Iterator;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

/**
 * Section detector based off rules for clinical notes.
 *
 * @author Ben Knoll
 * @author Yan Wang (rules)
 * @since 1.4
 */
public class RuleBasedSectionDetector implements DocumentProcessor {

  private final Pattern headers;

  @Nullable
  private Labeler<Section> sectionLabeler;

  @Nullable
  private Labeler<SectionTitle> sectionTitleLabeler;

  @Nullable
  private Labeler<SectionContent> sectionContentLabeler;

  /**
   * Injectable constructor.
   *
   * @param ruleBasedSectionDetectorModel patterns.
   */
  @Inject
  RuleBasedSectionDetector(RuleBasedSectionDetectorModel ruleBasedSectionDetectorModel) {
    this.headers = ruleBasedSectionDetectorModel.getSectionHeaderPattern();
  }

  @Override
  public void process(Document document) throws BiomedicusException {
    TextView systemView = StandardViews.getSystemView(document);

    LabelIndex<Sentence> sentenceLabelIndex = systemView.getLabelIndex(Sentence.class);
    LabelIndex<Bold> boldLabelIndex = systemView.getLabelIndex(Bold.class);
    LabelIndex<Underlined> underlinedLabelIndex = systemView.getLabelIndex(Underlined.class);

    sectionLabeler = systemView.getLabeler(Section.class);
    sectionTitleLabeler = systemView.getLabeler(SectionTitle.class);
    sectionContentLabeler = systemView.getLabeler(SectionContent.class);

    String text = systemView.getText();

    Iterator<Label<Sentence>> sentenceLabelIterator = sentenceLabelIndex
        .iterator();
    Label<Sentence> header = null;
    Label<Sentence> firstSentence = null;
    Label<Sentence> lastSentence = null;

    while (sentenceLabelIterator.hasNext()) {
      Label<Sentence> sentenceLabel = sentenceLabelIterator.next();
      CharSequence sentenceText = sentenceLabel.getCovered(text);
      if (headers.matcher(sentenceText).matches() || boldLabelIndex
          .withTextLocation(sentenceLabel).isPresent()
          || underlinedLabelIndex.withTextLocation(sentenceLabel)
          .isPresent()) {
        makeSection(header, firstSentence, lastSentence);
        header = sentenceLabel;
        firstSentence = null;
        continue;
      }
      if (firstSentence == null) {
        firstSentence = sentenceLabel;
      }
      lastSentence = sentenceLabel;
    }

    makeSection(header, firstSentence, lastSentence);
  }

  private void makeSection(
      @Nullable Label<Sentence> header,
      @Nullable Label<Sentence> firstSentence,
      @Nullable Label<Sentence> lastSentence
  ) throws BiomedicusException {
    if (header == null || firstSentence == null || lastSentence == null) {
      return;
    }

    assert sectionLabeler != null : "impossible non-null section labeler";
    assert sectionTitleLabeler != null : "impossible non-null section title labeler";
    assert sectionContentLabeler != null : "impossible non-null section content labeler";

    sectionLabeler.value(ImmutableSection.builder().build())
        .label(Span.create(header.getBegin(), lastSentence.getEnd()));

    sectionTitleLabeler.value(new SectionTitle()).label(header);

    sectionContentLabeler.value(new SectionContent())
        .label(Span.create(firstSentence.getBegin(), lastSentence.getEnd()));
  }
}
