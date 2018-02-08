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

package edu.umn.biomedicus.uima.xmi;

import edu.umn.biomedicus.exc.BiomedicusException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Semaphore;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.Resource_ImplBase;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.xml.sax.SAXException;

/**
 * Implementation class for writing the type system.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class TypeSystemWriter extends Resource_ImplBase {

  /**
   * Semaphore which prevents the type system from being written more than once.
   */
  private Semaphore writeOnce = new Semaphore(1);

  /**
   * {@inheritDoc} <p>Writes the type system to the path if it hasn't already been written. Uses the
   * semaphore with 1 permit {@code writeOnce}.</p>
   */
  public void writeToPath(Path path) throws BiomedicusException {
    if (writeOnce.tryAcquire()) {
      try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path)) {
        TypeSystemDescription description = getCasManager().getCasDefinition().getTypeSystemDescription();
        description.toXML(bufferedWriter);
      } catch (IOException | ResourceInitializationException | SAXException e) {
        throw new BiomedicusException(e);
      }
    }
  }
}
