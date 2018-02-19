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

package edu.umn.biomedicus.sections;

import com.google.inject.Inject;
import edu.umn.biomedicus.formatting.Bold;
import edu.umn.biomedicus.formatting.Underlined;
import edu.umn.biomedicus.sentences.Sentence;
import edu.umn.nlpengine.Document;
import edu.umn.nlpengine.DocumentProcessor;
import edu.umn.nlpengine.LabelIndex;
import edu.umn.nlpengine.Labeler;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

/**
 * Section detector based off rules for clinical notes.
 *
 * @author Ben Knoll
 * @author Yan Wang (rules)
 * @since 1.4
 */
public class RuleBasedSectionDetector implements DocumentProcessor {

  private final Pattern headers;

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
  public void process(@NotNull Document document) {
    LabelIndex<Sentence> sentenceLabelIndex = document.labelIndex(Sentence.class);
    LabelIndex<Bold> boldLabelIndex = document.labelIndex(Bold.class);
    LabelIndex<Underlined> underlinedLabelIndex = document.labelIndex(Underlined.class);

    Labeler<SectionHeader> headerLabeler = document.labeler(SectionHeader.class);

    String text = document.getText();

    for (Sentence sentenceLabel : sentenceLabelIndex) {
      CharSequence sentenceText = sentenceLabel.coveredText(text);
      if (headers.matcher(sentenceText).find() ||
          !boldLabelIndex.atLocation(sentenceLabel).isEmpty() ||
          !underlinedLabelIndex.atLocation(sentenceLabel).isEmpty()) {
        headerLabeler.add(new SectionHeader(sentenceLabel));
      }
    }
  }
}
