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

package edu.umn.biomedicus.rtf.beans.keywords;

import edu.umn.biomedicus.rtf.reader.KeywordAction;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Base class for a keyword action.
 *
 * @since 1.3.0
 */
@XmlRootElement
public abstract class AbstractKeywordAction implements KeywordAction {

  private String controlWord;

  private int begin = -1;

  private int end = -1;

  @Nullable
  private Integer parameter = null;

  @XmlTransient
  @Override
  public int getBegin() {
    return begin;
  }

  @Override
  public void setBegin(int begin) {
    this.begin = begin;
  }

  @XmlTransient
  @Override
  public int getEnd() {
    return end;
  }

  @Override
  public void setEnd(int end) {
    this.end = end;
  }

  @XmlTransient
  @Override
  public int getParameter() {
    return Objects.requireNonNull(parameter);
  }

  @Override
  public void setParameter(@Nullable Integer parameter) {
    this.parameter = parameter;
  }

  @Override
  public boolean hasParameter() {
    return parameter != null;
  }

  @XmlTransient
  @Override
  public String getControlWord() {
    return controlWord;
  }

  @Override
  public void setControlWord(String controlWord) {
    this.controlWord = controlWord;
  }

  @XmlTransient
  @Override
  public boolean isKnown() {
    return true;
  }
}
