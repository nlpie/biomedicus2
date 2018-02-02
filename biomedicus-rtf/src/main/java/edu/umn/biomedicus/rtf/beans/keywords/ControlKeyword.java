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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

/**
 *
 */
@XmlRootElement
@XmlType
@XmlSeeAlso({BinaryKeywordAction.class, DestinationKeywordAction.class, HexKeywordAction.class,
    OutputKeywordAction.class, PropertyKeywordAction.class, PropertyResetKeywordAction.class,
    SkipDestinationIfUnknownKeywordAction.class, UnicodeKeywordAction.class})
public class ControlKeyword {

  private String keyword;

  private AbstractKeywordAction keywordAction;

  @XmlElement(required = true)
  public String getKeyword() {
    return keyword;
  }

  public void setKeyword(String keyword) {
    this.keyword = keyword;
  }

  @XmlElementRef(required = true)
  public AbstractKeywordAction getKeywordAction() {
    return keywordAction;
  }

  public void setKeywordAction(AbstractKeywordAction keywordAction) {
    this.keywordAction = keywordAction;
  }
}
