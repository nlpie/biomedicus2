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

package edu.umn.biomedicus.exc;

/**
 * <p>This is the superclass for all exceptions in BioMedICUS.</p>
 *
 * <p>{@code BiomedicusException} extends {@link Exception}.</p>
 *
 * @since 1.3.0
 */
public class BiomedicusException extends Exception {

  private static final long serialVersionUID = 7521732353239537026L;

  /**
   * Creates a new exception with a null message.
   */
  public BiomedicusException() {
    super();
  }

  /**
   * Creates a new exception with the specified cause and a null message.
   *
   * @param aCause the original exception that caused this exception to be thrown, if any
   */
  public BiomedicusException(Throwable aCause) {
    super(aCause);
  }

  /**
   * Creates a new {@code BiomedicusException} using a format string.
   *
   * @param message format string
   * @param args arguments
   */
  public BiomedicusException(String message, String... args) {
    super(String.format(message, args));
  }

  public BiomedicusException(String message, Throwable cause) {
    super(message, cause);
  }
}
