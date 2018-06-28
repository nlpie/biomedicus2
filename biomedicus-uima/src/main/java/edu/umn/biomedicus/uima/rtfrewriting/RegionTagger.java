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

package edu.umn.biomedicus.uima.rtfrewriting;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inserts tags before and after a region in an rtf document.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class RegionTagger {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(RegionTagger.class);

  /**
   * The destination name of the document.
   */
  private final String destinationName;

  /**
   * The begin index of the region.
   */
  private final int beginIndex;

  /**
   * The end index of the region.
   */
  private final int endIndex;

  /**
   * Tag to insert at begin of region.
   */
  private final String beginTag;

  /**
   * Tag to insert at the end of the region.
   */
  private final String endTag;

  /**
   * Rewriter cursor to use.
   */
  private final RtfRewriterCursor rtfRewriterCursor;

  /**
   * Default constructor.
   *
   * @param symbolIndexedDocument The document to tag a region in.
   * @param destinationName The destination name of the document.
   * @param beginDestinationIndex The begin index of the region.
   * @param endDestinationIndex The end index of the region.
   * @param beginTag Tag to insert at begin of region.
   * @param endTag Tag to insert at the end of the region.
   */
  public RegionTagger(SymbolIndexedDocument symbolIndexedDocument,
      String destinationName,
      int beginDestinationIndex,
      int endDestinationIndex,
      String beginTag,
      String endTag) {
    this.destinationName = destinationName;
    this.beginIndex = symbolIndexedDocument.symbolIndex(beginDestinationIndex, destinationName);
    this.endIndex = symbolIndexedDocument.symbolIndex(endDestinationIndex - 1, destinationName);
    this.beginTag = beginTag;
    this.endTag = endTag;
    rtfRewriterCursor = new RtfRewriterCursor(symbolIndexedDocument);
  }

  /**
   * Performs the tagging of the region in the document. May insert more than one tag pair.
   */
  public void tagRegion() {
    LOGGER.debug("Tagging a region between symbols {} and {} in {}", beginIndex, endIndex,
        destinationName);

    rtfRewriterCursor.setSymbolIndex(beginIndex);
    rtfRewriterCursor.insertBefore(beginTag);

    int remaining;
    while ((remaining = endIndex - rtfRewriterCursor.getSymbolIndex()) != 0) {
      if (remaining < 0) {
        String msg = String
            .format("Passed the end symbol in document by %d symbols, context: \"%s\"",
                remaining * -1, rtfRewriterCursor.getContext());
        LOGGER.error(msg);
        throw new IllegalStateException(msg);
      }

      if (rtfRewriterCursor.nextIsOutsideDestination(destinationName)) {
        rtfRewriterCursor.insertAfter(endTag);
        rtfRewriterCursor.forward();
        rtfRewriterCursor.advanceToDestination(destinationName);
        rtfRewriterCursor.insertBefore(beginTag);
      } else if (rtfRewriterCursor.nextOffsetNonZero()) {
        rtfRewriterCursor.insertAfter(endTag);
        rtfRewriterCursor.forward();
        rtfRewriterCursor.insertBefore(beginTag);
      } else {
        rtfRewriterCursor.forward();
      }
    }
    rtfRewriterCursor.insertAfter(endTag);
  }
}
