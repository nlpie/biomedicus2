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

package edu.umn.biomedicus.uima.rtfrewriting;

/**
 * Class for rewriting an rtf document.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class RtfRewriterCursor {
    /**
     * The document to rewrite.
     */
    private final SymbolIndexedDocument symbolIndexedDocument;

    /**
     * The index of the current symbol pointed to by the cursor..
     */
    private int symbolIndex = -1;

    /**
     * Creates a new rtf rewriter cursor.
     *
     * @param symbolIndexedDocument the document to be rewritten.
     */
    public RtfRewriterCursor(SymbolIndexedDocument symbolIndexedDocument) {
        this.symbolIndexedDocument = symbolIndexedDocument;
    }

    /**
     * Moves to the symbol at the designated character index in the destination.
     *
     * @param index the character index to move to.
     * @param destinationName the destination the character appears in.
     */
    public void moveTo(int index, String destinationName) {
        symbolIndex = symbolIndexedDocument.symbolIndex(index, destinationName);
    }

    /**
     * Inserts a tag before the current symbol.
     *
     * @param tag the tag to insert.
     */
    public void insertBefore(String tag) {
        if (symbolIndex == -1) {
            throw new IllegalStateException();
        }
        symbolIndexedDocument.insertBeforeSymbol(symbolIndex, tag);
    }

    /**
     * Inserts a tag after the current symbol.
     *
     * @param tag the tag to insert.
     */
    public void insertAfter(String tag) {
        if (symbolIndex == -1) {
            throw new IllegalStateException();
        }
        symbolIndexedDocument.insertTextAfter(symbolIndex, tag);
    }

    /**
     * Moves the cursor forward one symbol.
     */
    public void forward() {
        symbolIndex++;
    }

    /**
     * Moves the cursor back one symbol.
     */
    public void back() {
        symbolIndex--;
    }

    /**
     * Determines whether the next symbol forward is inside or outside the specified destination.
     *
     * @param destinationName the rtf destination to check.
     * @return {@code true} if it is not in the specified destination, {@code false} otherwise.
     */
    public boolean nextIsOutsideDestination(String destinationName) {
        return symbolIndexedDocument.symbolIsOutsideDestination(symbolIndex + 1, destinationName);
    }

    /**
     * Moves the cursor forward until we reach the specified destination.
     *
     * @param destinationName destination to move cursor to.
     */
    public void advanceToDestination(String destinationName) {
        while (symbolIndexedDocument.symbolIsOutsideDestination(symbolIndex, destinationName)) {
            symbolIndex++;
        }
    }

    /**
     * Sets the cursor to a specific symbol index.
     *
     * @param symbolIndex the symbol index.
     */
    public void setSymbolIndex(int symbolIndex) {
        this.symbolIndex = symbolIndex;
    }

    /**
     * Returns the current symbol index the cursor is at.
     *
     * @return integer symbol index.
     */
    public int getSymbolIndex() {
        return symbolIndex;
    }

    /**
     * Determines if the next index on the cursor has an offset greater than zero.
     *
     * @return true if the next offset is greater than 0, false otherwise.
     */
    public boolean nextOffsetNonZero() {
        return symbolIndexedDocument.symbolOffsetIsNonZero(symbolIndex + 1);
    }

    public String getContext() {
        return symbolIndexedDocument.getContext(symbolIndex);
    }
}
