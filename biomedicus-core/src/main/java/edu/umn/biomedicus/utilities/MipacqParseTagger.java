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

package edu.umn.biomedicus.utilities;

import edu.umn.biomedicus.common.TextIdentifiers;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.nlpengine.Document;
import edu.umn.nlpengine.LabeledText;
import edu.umn.biomedicus.sentences.Sentence;
import edu.umn.biomedicus.utilities.PtbReader.Node;
import edu.umn.nlpengine.Labeler;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Optional;

/**
 * This class attaches parse data from MiPACQ .parse files to their source documents.
 *
 * @author Ben Knoll
 * @since 1.7.0
 */
public class MipacqParseTagger implements DocumentProcessor {

  @Override
  public void process(Document document) throws BiomedicusException {
    LabeledText systemView = TextIdentifiers.getSystemLabeledText(document);

    String sourcePath = document.getMetadata().get("path");

    if (sourcePath == null) {
      throw new BiomedicusException("Path not stored on document");
    }

    String text = systemView.getText();
    Labeler<Sentence> sentenceLabeler = systemView.labeler(Sentence.class);

    Path path = Paths.get(sourcePath.replaceFirst(".source$", ".parse"));
    try {
      PtbReader reader = PtbReader.createFromFile(path);

      int current = 0;
      Optional<Node> optional;
      while ((optional = reader.nextNode()).isPresent()) {
        int start = -1;
        Node node = optional.get();
        Iterator<Node> leafIterator = node.leafIterator();
        while (leafIterator.hasNext()) {
          Node leaf = leafIterator.next();
          String word = leaf.leafGetWord();
          while (current < text.length() && Character.isWhitespace(text.charAt(current))) {
            current++;
          }
          int index = text.indexOf(word, current);
          if (index == current) {
            if (start == -1) {
              start = index;
            }
            current = index + word.length();
          }
        }
        if (start != -1 && start != current) {
          sentenceLabeler.add(new Sentence(start, current));
        }
      }
    } catch (IOException e) {
      throw new BiomedicusException(e);
    }
  }
}
