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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * A keyword action that switches the current state to a different destination.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
@XmlRootElement
@XmlType
public class DestinationKeywordAction extends AbstractKeywordAction {

  /**
   * The name of the destination.
   */
  private String destinationName;

  /**
   * Getter for the destination name.
   *
   * @return the destination name.
   */
  @XmlElement(required = true)
  public String getDestinationName() {
    return destinationName;
  }

  /**
   * Sets the destination name.
   *
   * @param destinationName the destination name.
   */
  public void setDestinationName(String destinationName) {
    this.destinationName = destinationName;
  }

  @Override
  public void executeKeyword(State state) throws RtfReaderException {
    state.changeDestination(destinationName);
  }

  @Override
  public KeywordAction copy() {
    DestinationKeywordAction destinationKeywordAction = new DestinationKeywordAction();
    destinationKeywordAction.setDestinationName(destinationName);
    return destinationKeywordAction;
  }
}
