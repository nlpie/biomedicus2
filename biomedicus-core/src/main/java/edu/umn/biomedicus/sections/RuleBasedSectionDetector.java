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

package edu.umn.biomedicus.sections;

import com.google.inject.Inject;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.application.TextView;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.LabelIndex;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.types.style.Bold;
import edu.umn.biomedicus.common.types.style.Underlined;
import edu.umn.biomedicus.common.types.text.*;
import edu.umn.biomedicus.exc.BiomedicusException;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Section detector based off rules for clinical notes.
 *
 * @author Ben Knoll
 * @author Yan Wang (rules)
 * @since 1.4
 */
public class RuleBasedSectionDetector implements DocumentProcessor {
    private final Pattern headers;
    private final LabelIndex<Sentence> sentenceLabelIndex;
    private final Labeler<Section> sectionLabeler;
    private final Labeler<SectionTitle> sectionTitleLabeler;
    private final Labeler<SectionContent> sectionContentLabeler;
    private final String text;
    private final LabelIndex<Bold> boldLabelIndex;
    private final LabelIndex<Underlined> underlinedLabelIndex;

    /**
     * Injectable constructor.
     *
     * @param document                      the document to process.
     * @param ruleBasedSectionDetectorModel patterns.
     */
    @Inject
    RuleBasedSectionDetector(TextView document,
                             RuleBasedSectionDetectorModel ruleBasedSectionDetectorModel) {
        this.headers = ruleBasedSectionDetectorModel.getSectionHeaderPattern();
        sentenceLabelIndex = document.getLabelIndex(Sentence.class);
        boldLabelIndex = document.getLabelIndex(Bold.class);
        underlinedLabelIndex = document.getLabelIndex(Underlined.class);
        sectionLabeler = document.getLabeler(Section.class);
        sectionTitleLabeler = document.getLabeler(SectionTitle.class);
        sectionContentLabeler = document.getLabeler(SectionContent.class);
        text = document.getText();
    }


    @Override
    public void process() throws BiomedicusException {
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

    private void makeSection(@Nullable Label<Sentence> header,
                             @Nullable Label<Sentence> firstSentence,
                             @Nullable Label<Sentence> lastSentence)
            throws BiomedicusException {
        if (header == null || firstSentence == null || lastSentence == null) {
            return;
        }

        sectionLabeler.value(ImmutableSection.builder().build())
                .label(Span.create(header.getBegin(), lastSentence.getEnd()));
        sectionTitleLabeler.value(new SectionTitle()).label(header);
        sectionContentLabeler.value(new SectionContent())
                .label(Span.create(firstSentence.getBegin(),
                        lastSentence.getEnd()));
    }
}
