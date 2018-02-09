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

package edu.umn.biomedicus.rtf.beans.keywords;

import edu.umn.biomedicus.rtf.exc.RtfReaderException;
import edu.umn.biomedicus.rtf.reader.KeywordAction;
import edu.umn.biomedicus.rtf.reader.State;
import edu.umn.nlpengine.Span;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 */
@XmlRootElement
@XmlType
public class UnicodeKeywordAction extends AbstractKeywordAction {

  @Override
  public void executeKeyword(State state) throws RtfReaderException {
    if (!hasParameter()) {
      throw new RtfReaderException("Unicode keyword without a parameter.");
    }
    state.directWriteCharacter((char) getParameter(), Span.create(getBegin(), getEnd()));
    int propertyValue = state.getPropertyValue("DocumentFormatting", "UnicodeByteCount");
    state.setIgnoreNextChars(propertyValue);
  }

  @Override
  public KeywordAction copy() {
    return new UnicodeKeywordAction();
  }
}
