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

package edu.umn.biomedicus.io;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.ProcessorSetting;
import edu.umn.biomedicus.common.TextIdentifiers;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.nlpengine.Document;
import edu.umn.nlpengine.LabeledText;
import edu.umn.biomedicus.tagging.PosTag;
import edu.umn.biomedicus.tokenization.ParseToken;
import edu.umn.nlpengine.LabelIndex;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class PtbTagsWriter implements DocumentProcessor {

  private final Path outputDir;

  @Inject
  public PtbTagsWriter(
      @ProcessorSetting("writer.ptbTags.outputDir.path") Path outputDir
  ) {
    this.outputDir = outputDir;
  }

  @Override
  public void process(Document document) throws BiomedicusException {
    String documentId = document.getDocumentId();

    LabeledText systemView = TextIdentifiers.getSystemLabeledText(document);

    if (systemView == null) {
      throw new BiomedicusException("null system view");
    }

    String text = systemView.getText();
    LabelIndex<ParseToken> parseTokenLabelIndex = systemView.labelIndex(ParseToken.class);
    LabelIndex<PosTag> partOfSpeechLabelIndex = systemView.labelIndex(PosTag.class);

    StringBuilder rewriter = new StringBuilder(text);

    int added = 0;
    for (ParseToken parseTokenLabel : parseTokenLabelIndex) {
      int end = parseTokenLabel.getEndIndex() + added;
      String insertion = "/" + partOfSpeechLabelIndex.firstAtLocation(parseTokenLabel)
          .getPartOfSpeech().toString();
      rewriter.insert(end, insertion);
      if (rewriter.charAt(end + insertion.length()) != ' ') {
        rewriter.insert(end + insertion.length(), ' ');
        added += 1;
      }
      added += insertion.length();
    }

    try {
      Path fileName = outputDir.resolve(documentId);
      Files.write(fileName, rewriter.toString().getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new BiomedicusException(e);
    }
  }
}
