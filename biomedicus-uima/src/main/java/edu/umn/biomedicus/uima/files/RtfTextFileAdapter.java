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

package edu.umn.biomedicus.uima.files;

import static java.nio.charset.StandardCharsets.US_ASCII;

import com.google.inject.Injector;
import edu.umn.biomedicus.common.types.encoding.IllegalXmlCharacter;
import edu.umn.biomedicus.common.types.encoding.ImmutableIllegalXmlCharacter;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.Labeler;
import edu.umn.biomedicus.framework.store.TextView;
import edu.umn.biomedicus.uima.adapter.GuiceInjector;
import edu.umn.biomedicus.uima.adapter.UimaAdapters;
import edu.umn.biomedicus.uima.common.Views;
import edu.umn.biomedicus.uima.labels.LabelAdapter;
import edu.umn.biomedicus.uima.labels.LabelAdapters;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.uima.UimaContext;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.resource.ResourceAccessException;
import org.apache.uima.resource.metadata.ProcessingResourceMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class RtfTextFileAdapter implements InputFileAdapter {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory
      .getLogger(RtfTextFileAdapter.class);

  /**
   * Date formatter for adding date to metadata.
   */
  private final DateFormat dateFormatter = DateFormat
      .getDateInstance(DateFormat.LONG);
  @Nullable
  protected Document document;
  /**
   * View to load data into.
   */
  @Nullable
  private String viewName;
  private LabelAdapters labelAdapters;

  private static boolean isValid(int ch) {
    return (ch >= 0x20 && ch <= 0x7F) || ch == 0x09 || ch == 0x0A
        || ch == 0x0D;
  }

  @Override
  public void initialize(UimaContext uimaContext,
      ProcessingResourceMetaData processingResourceMetaData) {
    LOGGER.info("Initializing xml validating file adapter.");
    try {
      GuiceInjector guiceInjector = (GuiceInjector) uimaContext.getResourceObject("guiceInjector");
      labelAdapters = guiceInjector.attach().getInstance(LabelAdapters.class);
      guiceInjector.detach();
    } catch (ResourceAccessException | BiomedicusException e) {
      throw new IllegalStateException("");
    }
  }

  @Override
  public void adaptFile(CAS cas, Path path)
      throws CollectionException, IOException {
    if (cas == null) {
      LOGGER.error("Null CAS");
      throw new IllegalArgumentException("CAS was null");
    }

    String fileName = path.getFileName().toString();
    int period = fileName.lastIndexOf('.');
    if (period == -1) {
      period = fileName.length();
    }
    String documentId = fileName.substring(0, period);

    document = UimaAdapters.createDocument(cas, labelAdapters, documentId);

    List<Label<IllegalXmlCharacter>> illegalXmlCharacters
        = new ArrayList<>();
    StringBuilder stringBuilder = new StringBuilder();
    try (Reader stringReader = Files.newBufferedReader(path, US_ASCII)) {
      int ch;
      while ((ch = stringReader.read()) != -1) {
        if (isValid(ch)) {
          stringBuilder.append((char) ch);
        } else {
          int len = stringBuilder.length();
          LOGGER.warn(
              "Illegal rtf character with code point: {} at {} in {}",
              ch, len, path.toString());
          IllegalXmlCharacter xmlCharacter
              = ImmutableIllegalXmlCharacter.builder()
              .value(ch)
              .build();
          Label<IllegalXmlCharacter> label
              = new Label<>(len, len, xmlCharacter);
          illegalXmlCharacters.add(label);
        }
      }
    }

    String documentText = stringBuilder.toString();
    TextView odTextView = document.newTextView()
        .withName(Views.ORIGINAL_DOCUMENT_VIEW)
        .withText(documentText)
        .build();
    Labeler<IllegalXmlCharacter> illCharLabeler = odTextView
        .getLabeler(IllegalXmlCharacter.class);
    for (Label<IllegalXmlCharacter> illChar : illegalXmlCharacters) {
      illCharLabeler.label(illChar);
    }
  }

  @Override
  public void setTargetView(String viewName) {
    this.viewName = viewName;
  }
}
