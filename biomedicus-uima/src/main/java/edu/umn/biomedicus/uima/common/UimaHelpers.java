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

package edu.umn.biomedicus.uima.common;

import edu.umn.biomedicus.exc.BiomedicusException;
import java.io.IOException;
import java.nio.file.Path;
import org.apache.uima.UIMAFramework;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.collection.metadata.CpeDescription;
import org.apache.uima.util.InvalidXMLException;
import org.apache.uima.util.XMLInputSource;

/**
 *
 */
public final class UimaHelpers {

  private UimaHelpers() {
    throw new UnsupportedOperationException("Instantiation of utility class");
  }

  public static CollectionReaderDescription loadCollectionReaderDescription(Path path)
      throws BiomedicusException {
    CollectionReaderDescription collectionReaderDescription;
    try {
      XMLInputSource aInput = new XMLInputSource(path.toFile());
      collectionReaderDescription = UIMAFramework.getXMLParser()
          .parseCollectionReaderDescription(aInput);
    } catch (IOException | InvalidXMLException e) {
      throw new BiomedicusException(e);
    }
    return collectionReaderDescription;
  }

  public static CpeDescription loadCpeDescription(Path path) throws BiomedicusException {
    CpeDescription cpeDescription;
    try {
      XMLInputSource inputSource = new XMLInputSource(path.toFile());
      cpeDescription = UIMAFramework.getXMLParser().parseCpeDescription(inputSource);
    } catch (InvalidXMLException | IOException e) {
      throw new BiomedicusException(e);
    }
    return cpeDescription;
  }
}
