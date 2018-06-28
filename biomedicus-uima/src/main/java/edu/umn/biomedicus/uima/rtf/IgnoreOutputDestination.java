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

package edu.umn.biomedicus.uima.rtf;

import edu.umn.biomedicus.rtf.reader.KeywordAction;
import edu.umn.biomedicus.rtf.reader.OutputDestination;
import edu.umn.biomedicus.rtf.reader.State;

/**
 *
 */
public class IgnoreOutputDestination implements OutputDestination {

  private final String destinationName;

  private int count = 0;

  public IgnoreOutputDestination(String destinationName) {
    this.destinationName = destinationName;
  }

  @Override
  public int writeChar(char ch, State state) {
    return count++;
  }

  @Override
  public void finishDestination() {

  }

  @Override
  public void controlWordEncountered(KeywordAction keywordAction) {

  }

  @Override
  public String getName() {
    return destinationName;
  }
}
