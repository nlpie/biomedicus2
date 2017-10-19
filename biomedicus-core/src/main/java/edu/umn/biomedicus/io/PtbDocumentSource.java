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

package edu.umn.biomedicus.io;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.ProcessorSetting;
import edu.umn.biomedicus.common.StandardViews;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.types.syntax.PartsOfSpeech;
import edu.umn.biomedicus.common.types.text.ImmutableParseToken;
import edu.umn.biomedicus.common.types.text.ParseToken;
import edu.umn.biomedicus.common.types.text.Sentence;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentBuilder;
import edu.umn.biomedicus.framework.DocumentSource;
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.Span;
import edu.umn.biomedicus.framework.store.TextView;
import edu.umn.biomedicus.utilities.PtbReader;
import edu.umn.biomedicus.utilities.PtbReader.Node;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document source that reads penn treebank format files.
 *
 * @author Ben Knoll
 * @since 1.7.0
 */
public class PtbDocumentSource implements DocumentSource {

  private static final Logger LOGGER = LoggerFactory.getLogger(PtbDocumentSource.class);

  private final Charset charset;

  private final long total;

  private final Iterator<Path> iterator;

  private final String viewName;

  @Inject
  PtbDocumentSource(@ProcessorSetting("inputDirectory") Path directoryPath,
      @ProcessorSetting("extension") String extension,
      @ProcessorSetting("charsetName") String charsetName,
      @ProcessorSetting("viewName") String viewName) throws IOException {
    charset = Charset.forName(charsetName);
    total = Files.walk(directoryPath).filter(f -> f.toString().endsWith(extension)).count();
    iterator = Files.walk(directoryPath)
        .filter(f -> f.toString().endsWith(extension)).iterator();
    this.viewName = viewName;
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public Document next(DocumentBuilder factory) throws BiomedicusException {
    Path next = iterator.next();

    try {
      String documentId = next.getFileName().toString();
      Document document = factory.create(documentId);

      List<Label<Sentence>> sentenceLabels = new ArrayList<>();
      List<Label<ParseToken>> parseTokenLabels = new ArrayList<>();
      List<Label<PartOfSpeech>> partOfSpeechLabels = new ArrayList<>();

      PtbReader reader = PtbReader.createFromFile(next, charset);
      Optional<Node> option;
      StringBuilder documentBuilder = new StringBuilder();
      while ((option = reader.nextNode()).isPresent()) {
        int sentBegin = documentBuilder.length();
        Node node = option.get();
        Iterator<Node> leafIterator = node.leafIterator();
        while (leafIterator.hasNext()) {
          Node leaf = leafIterator.next();
          if ("-NONE-".equals(leaf.getLabel())) {
            continue;
          }

          Optional<String> optionalWord = leaf.getWord();
          if (!optionalWord.isPresent()) {
            continue;
          }
          String word = optionalWord.get();

          int begin = documentBuilder.length();
          int end = begin + word.length();

          Span tokenSpan = Span.create(begin, end);

          documentBuilder.append(word).append(' ');

          parseTokenLabels.add(Label.create(tokenSpan, ImmutableParseToken.builder()
              .hasSpaceAfter(true).text(word).build()));

          String label = leaf.getLabel();
          PartOfSpeech partOfSpeech = PartsOfSpeech.forTagWithFallback(label)
              .orElseGet(() -> PartOfSpeech.XX);
          partOfSpeechLabels.add(Label.create(tokenSpan, partOfSpeech));

        }

        int sentEnd = documentBuilder.length();

        sentenceLabels.add(Label.create(Span.create(sentBegin, sentEnd), new Sentence()));
      }

      TextView systemView = document.newTextView().withText(documentBuilder.toString())
          .withName(viewName)
          .build();

      systemView.getLabeler(Sentence.class).labelAll(sentenceLabels);
      systemView.getLabeler(ParseToken.class).labelAll(parseTokenLabels);
      systemView.getLabeler(PartOfSpeech.class).labelAll(partOfSpeechLabels);

      return document;
    } catch (IOException e) {
      LOGGER.error("Error reading PTB document: {}", next.toString());
      throw new BiomedicusException(e);
    }
  }

  @Override
  public long estimateTotal() {
    return total;
  }

  @Override
  public void close() throws Exception {

  }
}
