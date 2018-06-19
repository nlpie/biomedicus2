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
import edu.umn.biomedicus.annotations.ComponentSetting;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.types.syntax.PartsOfSpeech;
import edu.umn.biomedicus.sentences.Sentence;
import edu.umn.biomedicus.tagging.PosTag;
import edu.umn.biomedicus.tokenization.ParseToken;
import edu.umn.biomedicus.utilities.PtbReader;
import edu.umn.biomedicus.utilities.PtbReader.Node;
import edu.umn.nlpengine.Artifact;
import edu.umn.nlpengine.ArtifactSource;
import edu.umn.nlpengine.Document;
import edu.umn.nlpengine.Span;
import edu.umn.nlpengine.StandardArtifact;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document source that reads penn treebank format files.
 *
 * @author Ben Knoll
 * @since 1.7.0
 */
public class PTBArtifactSource implements ArtifactSource {

  private static final Logger LOGGER = LoggerFactory.getLogger(PTBArtifactSource.class);

  private final Charset charset;

  private final long total;

  private final Spliterator<Path> iterator;

  private final String viewName;

  @Inject
  PTBArtifactSource(
      @ComponentSetting("inputDirectory") Path directoryPath,
      @ComponentSetting("extension") String extension,
      @ComponentSetting("charsetName") String charsetName,
      @ComponentSetting("documentName") String documentName
  ) throws IOException {
    charset = Charset.forName(charsetName);
    total = Files.walk(directoryPath).filter(f -> f.toString().endsWith(extension)).count();
    iterator = Files.walk(directoryPath)
        .filter(f -> f.toString().endsWith(extension)).spliterator();
    this.viewName = documentName;
  }

  @Override
  public long estimateTotal() {
    return total;
  }

  @Override
  public void close() {

  }

  @Override
  public boolean tryAdvance(@Nonnull Function1<? super Artifact, Unit> consumer) {
    return iterator.tryAdvance((path) -> {
      String artifactID = path.getFileName().toString();
      Artifact artifact = new StandardArtifact(artifactID);

      List<Sentence> sentenceLabels = new ArrayList<>();
      List<ParseToken> parseTokenLabels = new ArrayList<>();
      List<PosTag> partOfSpeechLabels = new ArrayList<>();

      StringBuilder documentBuilder = new StringBuilder();
      try {
        PtbReader reader = PtbReader.createFromFile(path, charset);
        Optional<Node> option;
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

            Span tokenSpan = new Span(begin, end);

            documentBuilder.append(word).append(' ');

            parseTokenLabels.add(new ParseToken(tokenSpan, word, true));

            String label = leaf.getLabel();
            PartOfSpeech partOfSpeech = PartsOfSpeech.forTagWithFallback(label)
                .orElse(PartOfSpeech.XX);
            partOfSpeechLabels.add(new PosTag(tokenSpan, partOfSpeech));

          }

          int sentEnd = documentBuilder.length();

          sentenceLabels.add(new Sentence(sentBegin, sentEnd));
        }
      } catch (IOException e) {
        throw new IllegalStateException("Problem reading file: " + path.toString());
      }

      Document document = artifact.addDocument(viewName, documentBuilder.toString());

      document.labeler(Sentence.class).addAll(sentenceLabels);
      document.labeler(ParseToken.class).addAll(parseTokenLabels);
      document.labeler(PosTag.class).addAll(partOfSpeechLabels);

      consumer.invoke(artifact);
    });
  }
}
