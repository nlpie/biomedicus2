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
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentBuilder;
import edu.umn.biomedicus.framework.DocumentSource;
import edu.umn.biomedicus.framework.store.Document;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Document source that reads text files from a directory and dumps the content into a view
 *
 * @author Ben Knoll
 * @since 1.7.0
 */
public class TextFilesDocumentSource implements DocumentSource {

  private final Charset charset;

  private final long total;

  private final Iterator<Path> iterator;

  private final String viewName;

  private final String extension;

  @Inject
  TextFilesDocumentSource(
      @ProcessorSetting("inputDirectory") Path directoryPath,
      @ProcessorSetting("extension") String extension,
      @ProcessorSetting("charsetName") String charsetName,
      @ProcessorSetting("viewName") String viewName
  ) throws IOException {
    charset = Charset.forName(charsetName);
    this.extension = extension;
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
    try {
      Path next = iterator.next();
      String s = new String(Files.readAllBytes(next), charset);
      String documentId = next.getFileName().toString()
          .replaceFirst("\\.?" + Pattern.quote(extension) + "$", "");
      Document document = factory.create(documentId);
      document.newTextView().withName(viewName).withText(s).build();
      return document;
    } catch (IOException e) {
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
