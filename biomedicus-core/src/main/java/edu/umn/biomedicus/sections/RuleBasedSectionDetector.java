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
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Section;
import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.util.regex.Matcher;
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
    private final Labeler<Section> section2Labeler;
    private int prevBegin;
    private int prevEnd;
    private int begin;
    private String text;

    /**
     * Injectable constructor.
     *
     * @param document the document to process.
     * @param ruleBasedSectionDetectorModel patterns.
     */
    @Inject
    RuleBasedSectionDetector(Document document, RuleBasedSectionDetectorModel ruleBasedSectionDetectorModel, Labeler<Section> section2Labeler) {
        this.headers = ruleBasedSectionDetectorModel.getSectionHeaderPattern();
        this.section2Labeler = section2Labeler;
        text = document.getText();
    }


    @Override
    public void process() throws BiomedicusException {
        Matcher matcher = headers.matcher(text);
        prevBegin = 0;
        prevEnd = 0;
        while (matcher.find()) {
            begin = matcher.start();
            checkSentence();

            prevBegin = begin;
            prevEnd = matcher.end();
        }

        begin = text.length();
        checkSentence();
    }

    private void checkSentence() throws BiomedicusException {
        if (!text.substring(prevBegin, begin).isEmpty()) {
            Section section = Section.builder()
                    .setContentStart(prevEnd)
                    .setSectionTitle(text.substring(prevBegin, prevEnd).trim())
                    .setHasSubsections(false)
                    .setLevel(0)
                    .build();
            section2Labeler.value(section).label(Span.create(prevBegin, begin));
        }
    }
}
