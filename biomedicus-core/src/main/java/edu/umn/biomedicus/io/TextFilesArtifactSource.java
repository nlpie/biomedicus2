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
import edu.umn.nlpengine.Artifact;
import edu.umn.nlpengine.ArtifactSource;
import edu.umn.nlpengine.StandardArtifact;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Spliterator;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document source that reads text files from a directory and dumps the content into a view
 *
 * @author Ben Knoll
 * @since 1.7.0
 */
public class TextFilesArtifactSource implements ArtifactSource {

  private static final Logger LOGGER = LoggerFactory.getLogger(TextFilesArtifactSource.class);

  public static final String SOURCE_PATH = "sourcePath";

  private final Charset charset;

  private final long total;

  private final Spliterator<Path> iterator;

  private final String documentName;

  private final Path inputDirectory;

  @Inject
  TextFilesArtifactSource(
      @ComponentSetting("inputDirectory.orig") String directoryPath,
      @ComponentSetting("extension") String extension,
      @ComponentSetting("charsetName") String charsetName,
      @ComponentSetting("documentName") String documentName
  ) throws IOException {
    charset = Charset.forName(charsetName);
    inputDirectory = Paths.get(directoryPath);
    total = Files.walk(inputDirectory).filter(f -> f.toString().endsWith(extension)).count();
    LOGGER.debug("Reading {} files from {}", total, inputDirectory);
    iterator = Files.walk(inputDirectory).filter(f -> f.toString().endsWith(extension))
        .spliterator();
    this.documentName = documentName;
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
    return iterator.tryAdvance((next) -> {
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("Reading document: " + next.toString());
      }
      try {
        String s = new String(Files.readAllBytes(next), charset);
        String documentId = inputDirectory.relativize(next).toString();
        Artifact artifact = new StandardArtifact(documentId);
        artifact.addDocument(documentName, s);

        artifact.getMetadata().put(SOURCE_PATH, next.toString());
        consumer.invoke(artifact);
      } catch (IOException e) {
        LOGGER.error("Failed on document: " + next.toString());
        throw new IllegalStateException(e);
      }
    });
  }
}
