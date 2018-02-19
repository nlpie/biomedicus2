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

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.ProcessorSetting;
import edu.umn.nlpengine.Artifact;
import edu.umn.nlpengine.ArtifactSource;
import edu.umn.nlpengine.StandardArtifact;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Spliterator;
import java.util.function.Predicate;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class MipacqSourceReader implements ArtifactSource {

  private final long total;

  private final Spliterator<Path> iterator;

  private final String documentName;

  @Inject
  public MipacqSourceReader(
      @ProcessorSetting("inputDirectory") Path inputDirectory,
      @ProcessorSetting("documentName") String documentName
  ) throws IOException {
    this.documentName = documentName;
    Predicate<Path> endsWithSource = f -> f.toString().endsWith(".source");
    total = Files.walk(inputDirectory).filter(endsWithSource).count();
    iterator = Files.walk(inputDirectory).filter(endsWithSource).spliterator();
  }

  @Override
  public long estimateTotal() {
    return total;
  }

  @Override
  public void close() {

  }

  @Override
  public boolean tryAdvance(@NotNull Function1<? super Artifact, Unit> consumer) {
    return iterator.tryAdvance((next) -> {
      StringBuilder sb = new StringBuilder();
      try {
        Files.lines(next, StandardCharsets.UTF_8).forEach(line -> {
          if (!line.startsWith("[")) {
            sb.append(line).append("\n");
          }
        });
      } catch (IOException e) {
        e.printStackTrace();
      }
      Artifact artifact = new StandardArtifact(next.getFileName().toString());
      artifact.getMetadata().put("path", next.toString());
      artifact.addDocument(documentName, sb.toString());
      consumer.invoke(artifact);
    });
  }
}
