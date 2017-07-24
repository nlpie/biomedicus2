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

package edu.umn.biomedicus.rtf.reader;

import edu.umn.biomedicus.rtf.exc.RtfReaderException;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Created by benknoll on 6/18/15.
 */
public class UnknownKeywordAction implements KeywordAction {

  private String controlWord;

  private int begin = -1;

  private int end = -1;

  @Nullable
  private Integer parameter = null;

  @Override
  public void executeKeyword(State state) throws RtfReaderException {
  }

  @Override
  public KeywordAction copy() {
    return new UnknownKeywordAction();
  }

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

  @Override
  public int getBegin() {
    return begin;
  }

  @Override
  public void setBegin(int begin) {
    this.begin = begin;
  }

  @Override
  public int getEnd() {
    return end;
  }

  @Override
  public void setEnd(int end) {
    this.end = end;
  }

  @Override
  public String getControlWord() {
    return controlWord;
  }

  @Override
  public void setControlWord(String controlWord) {
    this.controlWord = controlWord;
  }

  @Override
  public boolean isKnown() {
    return false;
  }


}
