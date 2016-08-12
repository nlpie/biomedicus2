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

package edu.umn.biomedicus.writers;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.ProcessorSetting;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.ParseToken;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class PtbTagsWriter implements DocumentProcessor {

    private final Path outputDir;
    private final Document document;
    private final Labels<ParseToken> parseTokenLabels;
    private final Labels<PartOfSpeech> partOfSpeechLabels;

    @Inject
    public PtbTagsWriter(@ProcessorSetting("writer.ptbTags.outputDir.path") Path outputDir,
                         Document document,
                         Labels<ParseToken> parseTokenLabels,
                         Labels<PartOfSpeech> partOfSpeechLabels) {
        this.outputDir = outputDir;
        this.document = document;
        this.parseTokenLabels = parseTokenLabels;
        this.partOfSpeechLabels = partOfSpeechLabels;
    }

    @Override
    public void process() throws BiomedicusException {
        String text = document.getText();
        StringBuilder rewriter = new StringBuilder(text);
        int added = 0;
        for (Label<ParseToken> parseTokenLabel: parseTokenLabels) {
            int end = parseTokenLabel.getEnd() + added;
            String insertion = "/" + partOfSpeechLabels.withSpan(parseTokenLabel)
                    .orElseThrow(() -> new BiomedicusException("No part of speech for parse token."))
                    .value()
                    .toString();
            rewriter.insert(end, insertion);
            if (rewriter.charAt(end + insertion.length()) != ' ') {
                rewriter.insert(end + insertion.length(), ' ');
                added += 1;
            }
            added += insertion.length();
        }

        try {
            Path fileName = outputDir.resolve(document.getDocumentId());
            Files.write(fileName, rewriter.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new BiomedicusException(e);
        }
    }
}
