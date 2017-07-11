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

package edu.umn.biomedicus.utilities;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.ProcessorSetting;
import edu.umn.biomedicus.common.StandardViews;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentBuilder;
import edu.umn.biomedicus.framework.DocumentSource;
import edu.umn.biomedicus.framework.store.Document;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.function.Predicate;

/**
 *
 */
public class MipacqSourceReader implements DocumentSource {

  private final long total;

  private final Iterator<Path> iterator;

  @Inject
  public MipacqSourceReader(@ProcessorSetting("inputDirectory") Path inputDirectory)
      throws IOException {
    Predicate<Path> endsWithSource = f -> f.toString().endsWith(".source");
    total = Files.walk(inputDirectory).filter(endsWithSource).count();
    iterator = Files.walk(inputDirectory).filter(endsWithSource).iterator();
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public Document next(DocumentBuilder factory) throws BiomedicusException {
    try {
      Path next = iterator.next();
      StringBuilder sb = new StringBuilder();
      Files.lines(next, StandardCharsets.UTF_8).forEach(line -> {
        if (!line.startsWith("[")) {
          sb.append(line).append("\n");
        }
      });
      Document document = factory.create(next.getFileName().toString());
      document.putMetadata("path", next.toString());
      document.newTextView().withName(StandardViews.SYSTEM).withText(sb.toString()).build();
      return document;
    } catch (IOException e) {
      throw new BiomedicusException(e);
    }
  }

  @Override
  public long estimateTotal() {
    return total;
  }
}
