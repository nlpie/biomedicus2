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

package edu.umn.biomedicus.rtf.reader;

import edu.umn.biomedicus.rtf.exc.RtfReaderException;
import java.io.IOException;
import java.io.Reader;

/**
 *
 */
public class ReaderRtfSource implements RtfSource {

  private final Reader reader;

  private int index;

  public ReaderRtfSource(Reader reader) {
    this.reader = reader;
    index = 0;
  }

  @Override
  public int getIndex() {
    return index;
  }

  /**
   * @return
   * @throws RtfReaderException
   */
  @Override
  public int readCharacter() throws RtfReaderException {
    try {
      reader.mark(1);
    } catch (IOException e) {
      throw new RtfReaderException(e);
    }
    int code;
    try {
      code = reader.read();
    } catch (IOException e) {
      throw new RtfReaderException(e);
    }
    index++;
    return code;
  }

  /**
   * @throws RtfReaderException
   */
  @Override
  public void unreadChar() throws RtfReaderException {
    try {
      reader.reset();
    } catch (IOException e) {
      throw new RtfReaderException(e);
    }
    index--;
  }
}
