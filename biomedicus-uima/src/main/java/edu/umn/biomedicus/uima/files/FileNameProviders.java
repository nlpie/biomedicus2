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

import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.TextView;
import java.util.UUID;
import javax.annotation.Nullable;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for file name providers.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public final class FileNameProviders {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(FileNameProviders.class);

  /**
   * Private constructor to prevent instantiation.
   */
  private FileNameProviders() {
    throw new UnsupportedOperationException();
  }

  /**
   * Constructor which takes a potentially null identifier and forms a document id, using a UUID if
   * the identifier is null.
   *
   * @param identifier document identifier
   * @param extension extension to add, should include the separator e.g. ".".
   * @return return new DocumentIdFileNameProvider with the created file name.
   */
  private static String withPotentiallyNullIdentifier(@Nullable String identifier,
      String extension) {
    String documentId =
        identifier == null ? "unidentified-" + UUID.randomUUID().toString() : identifier;
    return documentId + extension;
  }

  /**
   * Creates a file name provider given the initial view of a jCas passed to a ae_writer
   *
   * @param cas any cas view
   * @param extension extension to use, should include the separator e.g. ".".
   * @return a newly initialized file name provider.
   */
  public static String fromCAS(CAS cas, String extension) {
    CAS metadata = cas.getView("metadata");
    Type documentIdType = cas.getTypeSystem()
        .getType("edu.umn.biomedicus.uima.type1_5.DocumentId");
    Feature docIdFeat = documentIdType.getFeatureByBaseName("documentId");
    return metadata.getIndexRepository()
        .getAllIndexedFS(documentIdType)
        .get()
        .getStringValue(docIdFeat) + extension;
  }

  /**
   * Creates a file name provider from a biomedicus {@link TextView} type.
   *
   * @param document the document to pull the document identifier from.
   * @param extension the extension to give the file name
   * @return the file name provider which gives the file name.
   */
  public static String fromDocument(Document document, String extension) {
    String documentIdentifier = document.getDocumentId();
    return FileNameProviders.withPotentiallyNullIdentifier(documentIdentifier, extension);
  }
}
