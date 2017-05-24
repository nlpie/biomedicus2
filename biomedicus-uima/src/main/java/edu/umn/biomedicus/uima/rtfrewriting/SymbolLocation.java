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
 * A character or control word in the original document. Stores information about the destination it maps to and the
 * distance from the previous symbol that maps to a destination.
 *
 * @author Ben Knoll
 * @since 1.4.0
 */
class SymbolLocation {

    /**
     * The destination the control word maps to.
     */
    private final String destination;

    /**
     * The index of the symbol in the sequence of symbols.
     */
    private final int index;

    /**
     * The distance in number of characters from the previous symbol in the original document that maps to an output
     * destination.
     */
    private int offset;

    /**
     * The length of the symbol.
     */
    private final int length;

    /**
     * Creates a new original document symbol.
     *
     * @param destination the destination the control word maps to.
     * @param offset the distance in number of characters from the previous symbol that maps to a destination.
     * @param length the length of the symbol.
     */
    SymbolLocation(String destination, int offset, int length, int index) {
        if (length < 0) {
            throw new IllegalArgumentException();
        }
        this.destination = destination;
        this.offset = offset;
        this.length = length;
        this.index = index;
    }

    /**
     * Gets the rtf output destination that this symbol appears in.
     *
     * @return name of the rtf output destination.
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Gets the offset of the symbol from the previous symbol in the output destination.
     *
     * @return integer offset of the symbol from the previous symbol.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Gets the length of the symbol.
     *
     * @return integer length of symbol
     */
    public int getLength() {
        return length;
    }

    /**
     * Increases the length of the offset by 1.
     */
    public void incrementOffset() {
        offset++;
    }

    /**
     * Increases the length of the offset by some amount.
     *
     * @param increment the amount to increase the offset by.
     */
    public void addToOffset(int increment) {
        offset += increment;
    }

    /**
     * The index in the list of symbol locations.
     *
     * @return integer index.
     */
    public int getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SymbolLocation that = (SymbolLocation) o;

        if (index != that.index) {
            return false;
        }
        if (offset != that.offset) {
            return false;
        }
        if (length != that.length) {
            return false;
        }
        return destination.equals(that.destination);

    }

    @Override
    public int hashCode() {
        int result = destination.hashCode();
        result = 31 * result + index;
        result = 31 * result + offset;
        result = 31 * result + length;
        return result;
    }
}
