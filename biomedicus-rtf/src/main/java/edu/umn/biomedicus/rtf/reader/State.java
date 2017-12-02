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
import edu.umn.nlpengine.Label;
import edu.umn.nlpengine.Span;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores the stateful properties of rtf processing.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class State {

  private static final Logger LOGGER = LoggerFactory.getLogger(State.class);

  /**
   * The map of the different output destinations.
   */
  private final Map<String, OutputDestination> outputDestinationMap;

  /**
   * A factory which creates new output destinations if we have not seen them before.
   */
  private final OutputDestinationFactory outputDestinationFactory;

  /**
   * Various properties changed by property value keywords.
   */
  private final Map<String, Map<String, Integer>> properties;

  /**
   * Listens for the indices of characters written to the output destinations.
   */
  private final IndexListener indexListener;

  /**
   * The current output destination.
   */
  @Nullable
  private OutputDestination outputDestination;

  /**
   * Enumerated value for the input type
   */
  private InputType inputType;

  /**
   * The number of binary characters to read.
   */
  private int binaryCharactersToRead;

  /**
   * Skips the next destination encountered if it does not know how to handle it.
   */
  private boolean skipDestinationIfUnknown;

  /**
   * The string builder for hex input.
   */
  private StringBuilder hexStringBuilder;

  /**
   * If the state is currently skipping writing to a destination.
   */
  private boolean skippingDestination;

  /**
   * How many characters should be ignored.
   */
  private int ignoreNextChars;

  /**
   * The current charset being used for hex-code input.
   */
  private Charset charset;

  private int hexStart;

  /**
   * Initializes a new state given the map of output destinations, a factory for new output
   * destinations, and the state properties.
   *
   * @param outputDestinationMap a map from a string identifier to output destinations.
   * @param outputDestinationFactory a factory which contains a new output destination.
   * @param indexListener listens for the indices of characters written to output destinations.
   */
  public State(Map<String, OutputDestination> outputDestinationMap,
      OutputDestinationFactory outputDestinationFactory,
      Map<String, Map<String, Integer>> properties,
      IndexListener indexListener) {
    this.outputDestinationMap = outputDestinationMap;
    this.outputDestinationFactory = outputDestinationFactory;
    this.properties = properties;
    this.indexListener = indexListener;
    inputType = InputType.NORMAL;
    binaryCharactersToRead = 0;
    skipDestinationIfUnknown = false;
    skippingDestination = false;
    charset = Charset.forName("Windows-1252");
  }

  /**
   * Creates a state with the default output destinations.
   *
   * @param outputDestinationFactory factory for new output destinations.
   * @return newly created State object.
   */
  public static State createState(OutputDestinationFactory outputDestinationFactory,
      Map<String, Map<String, Integer>> properties,
      IndexListener indexListener) throws RtfReaderException {
    Map<String, OutputDestination> outputDestinationMap = new HashMap<>();
    outputDestinationMap.put("Rtf", outputDestinationFactory.create("Rtf"));
    return new State(outputDestinationMap, outputDestinationFactory, properties, indexListener);
  }

  /**
   * Copies a child state object, which inherits the current values from this state object.
   *
   * @return new state object with the same values as this object.
   */
  public State copy() {
    Map<String, Map<String, Integer>> propertiesCopy = properties.entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey,
            entry -> entry.getValue()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))));

    State stateCopy = new State(outputDestinationMap, outputDestinationFactory, propertiesCopy,
        indexListener);
    stateCopy.outputDestination = outputDestination;
    stateCopy.skippingDestination = skippingDestination;
    stateCopy.charset = charset;
    return stateCopy;
  }

  /**
   * Sets the input type to hex.
   */
  public void setToHexInputType(int index) {
    inputType = InputType.HEX;
    hexStringBuilder = new StringBuilder(2);
    this.hexStart = index;
  }

  /**
   * Sets the input type to binary.
   *
   * @param binaryCharactersToRead number of bytes to read.
   */
  public void setToBinaryInputType(int binaryCharactersToRead) {
    inputType = InputType.BINARY;
    this.binaryCharactersToRead = binaryCharactersToRead;
  }

  /**
   * Marks to skip the next destination if it is unknown.
   */
  public void markSkipDestinationIfUnknown() {
    skipDestinationIfUnknown = true;
  }

  /**
   * Writes the character to the output destination.
   *
   * @param code character code
   * @param originalDocumentTextLocation the span of the character in the original document.
   * @throws RtfReaderException if there is some kind of error in writing to the output
   * destination.
   */
  public void writeCharacter(int code, Label originalDocumentTextLocation)
      throws RtfReaderException {
    if (skippingDestination) {
      return;
    }

    switch (inputType) {
      case BINARY:
        if (--binaryCharactersToRead == 0) {
          inputType = InputType.NORMAL;
        }
        LOGGER.debug("Not writing a binary byte");
        break;
      case NORMAL:
        directWriteCharacter((char) code, originalDocumentTextLocation);
        break;
      case HEX:
        hexStringBuilder.append((char) code);
        if (hexStringBuilder.length() == 2) {
          String hexString = hexStringBuilder.toString();
          Byte charByte = (byte) (Integer.parseInt(hexString, 16) & 0xff);
          code = charset.decode(ByteBuffer.wrap(new byte[]{charByte})).get(0);
          directWriteCharacter((char) code,
              Span.create(hexStart, originalDocumentTextLocation.getEndIndex()));
          inputType = InputType.NORMAL;
        }
        break;
      default:
        throw new IllegalStateException("Unknown input type.");
    }
  }

  /**
   * Internal method for writing characters to the output destination.
   *
   * @param ch character to write.
   * @param originalDocumentTextLocation the span of the character in the original document.
   * @throws RtfReaderException if there is some kind of failure writing to output destination.
   */
  public void directWriteCharacter(char ch, Label originalDocumentTextLocation)
      throws RtfReaderException {
    if (ignoreNextChars > 0) {
      ignoreNextChars--;
      return;
    }
    if (outputDestination != null) {
      int destinationIndex = outputDestination.writeChar(ch, this);
      indexListener.wroteToDestination(outputDestination.getName(), destinationIndex,
          originalDocumentTextLocation);
    }
  }

  /**
   * Tells the state to change the output destination.
   *
   * @param destinationName the name of the destination.
   */
  public void changeDestination(String destinationName) throws RtfReaderException {
    if ("SkipDestination".equals(destinationName)) {
      outputDestination = null;
      skippingDestination = true;
    }
    if (!skippingDestination) {
      if (!outputDestinationMap.containsKey(destinationName)) {
        outputDestinationMap.put(destinationName, outputDestinationFactory.create(destinationName));
      }
      outputDestination = outputDestinationMap.get(destinationName);
      if (outputDestination == null) {
        skippingDestination = true;
        skipDestinationIfUnknown = false;
      }
    }
  }

  /**
   * Handles a keyword action encountered by the parser.
   *
   * @param keywordAction the encountered keyword action.
   */
  public void handleKeyword(KeywordAction keywordAction) throws RtfReaderException {
    if (!skippingDestination) {
      if (skipDestinationIfUnknown && !keywordAction.isKnown()) {
        outputDestination = null;
        skippingDestination = true;
        skipDestinationIfUnknown = false;
        return;
      }
      skipDestinationIfUnknown = false;
      if (outputDestination != null) {
        outputDestination.controlWordEncountered(keywordAction);
      }
      keywordAction.executeKeyword(this);
    }
  }

  /**
   * Sets a property value in the state.
   *
   * @param group the property group name.
   * @param property the property name.
   * @param value the value to set the property to.
   */
  public void setPropertyValue(String group, String property, int value) {
    properties.get(group).put(property, value);
  }

  /**
   * Resets a property group to zeroes.
   *
   * @param group the property group.
   */
  public void resetPropertyGroup(String group) {
    properties.get(group).replaceAll((k, v) -> 0);
  }

  /**
   * Returns the value of a property.
   *
   * @param group the group name of the property.
   * @param property the property name.
   * @return the value of the property.
   */
  public int getPropertyValue(String group, String property) {
    Map<String, Integer> propertyGroup = properties.get(group);
    if (propertyGroup == null) {
      throw new IllegalArgumentException("Group not found");
    }
    Integer propertyValue = propertyGroup.get(property);
    if (propertyValue == null) {
      throw new IllegalArgumentException("Property not found");
    }
    return propertyValue;
  }

  /**
   * Finishes the current state, and all destinations in the state.
   */
  public void finishState() {
    outputDestinationMap.values()
        .stream()
        .forEach(OutputDestination::finishDestination);
  }

  /**
   * Sets the state to ignore a number of following characters.
   *
   * @param ignoreNextChars the number of characters to ignore.
   */
  public void setIgnoreNextChars(int ignoreNextChars) {
    this.ignoreNextChars = ignoreNextChars;
  }
}
