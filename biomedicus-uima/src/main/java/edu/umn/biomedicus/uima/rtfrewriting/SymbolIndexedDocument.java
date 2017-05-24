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

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A rtf document for modification, contains indices to all the symbols, which are entire control words or characters
 * in the rtf document.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class SymbolIndexedDocument {

    /**
     * A list of the symbols in the original document. A symbol is a character or control word in the rtf document.
     */
    private final List<SymbolLocation> symbolLocations;

    /**
     * A map from destination names to maps from character indexes in that destination to symbol indexes.
     */
    private final Map<String, Map<Integer, Integer>> destinationMap;

    /**
     * The original rtf document.
     */
    private StringBuilder document;

    /**
     * Internal private constructor. Initializes the indexes and document content.
     *
     * @param symbolLocations the ordered indexes of symbol locations in the document.
     * @param destinationMap  the map from destinations to character indexes to symbol indexes.
     * @param document        the document itself.
     */
    private SymbolIndexedDocument(List<SymbolLocation> symbolLocations,
                                  Map<String, Map<Integer, Integer>> destinationMap,
                                  String document) {
        this.symbolLocations = symbolLocations;
        this.destinationMap = destinationMap;
        this.document = new StringBuilder(document);
    }

    /**
     * Indexes all the symbols from an original document.
     *
     * @param originalDocumentView jCas original document view.
     * @return The newly created symbol indexed document.
     */
    public static SymbolIndexedDocument fromView(CAS originalDocumentView) {
        Type viewIndexType = originalDocumentView.getTypeSystem()
                .getType("edu.umn.biomedicus.rtfuima.type.ViewIndex");

        Feature destinationNameFeature = viewIndexType
                .getFeatureByBaseName("destinationName");
        Feature destinationIndexFeature = viewIndexType
                .getFeatureByBaseName("destinationIndex");

        AnnotationIndex<AnnotationFS> viewIndexAI = originalDocumentView
                .getAnnotationIndex(viewIndexType);

        List<SymbolLocation> symbolLocations = new ArrayList<>();

        Map<String, Map<Integer, Integer>> destinationMap = new HashMap<>();

        int index = 0;
        int lastEnd = 0;
        for (AnnotationFS annotation : viewIndexAI) {
            int begin = annotation.getBegin();
            int end = annotation.getEnd();

            String destinationName
                    = annotation.getStringValue(destinationNameFeature);

            SymbolLocation symbolLocation = new SymbolLocation(
                    destinationName,
                    begin - lastEnd,
                    end - begin,
                    index++
            );

            symbolLocations.add(symbolLocation);

            int destinationIndex
                    = annotation.getIntValue(destinationIndexFeature);

            destinationMap.compute(destinationName,
                    (String key, @Nullable Map<Integer, Integer> value) -> {
                        if (value == null) {
                            value = new HashMap<>();
                        }
                        value.put(destinationIndex, symbolLocations.size() - 1);

                        return value;
                    });
            lastEnd = end;
        }
        return new SymbolIndexedDocument(symbolLocations, destinationMap,
                originalDocumentView.getDocumentText());
    }

    /**
     * Returns the index of the symbol from the character index and the destination that a character occurs in.
     *
     * @param characterIndex  the index of the character in the destination.
     * @param destinationName the destination name.
     * @return integer index of the symbol.
     */
    public int symbolIndex(int characterIndex, String destinationName) {
        Map<Integer, Integer> indexesMap = destinationMap.get(destinationName);
        if (indexesMap == null) {
            throw new IllegalArgumentException("Destination does not exist");
        }
        Integer integer = indexesMap.get(characterIndex);
        if (integer == null) {
            throw new IllegalArgumentException(
                    "Character index does not map to a symbol");
        }
        return integer;
    }

    /**
     * Returns the edited document.
     *
     * @return string edited document.
     */
    public String getDocument() {
        return document.toString();
    }

    /**
     * Inserts text before the symbol at the index.
     *
     * @param symbolIndex index of symbol to insert before.
     * @param text        the text to insert.
     */
    public void insertBeforeSymbol(int symbolIndex, String text) {
        SymbolLocation symbolLocation = symbolLocations.get(symbolIndex);
        int insertionIndex = getOriginalDocumentIndex(symbolLocation);

        document.insert(insertionIndex, text);

        symbolLocation.addToOffset(text.length());
    }

    /**
     * Inserts text after the symbol at the index.
     *
     * @param symbolIndex index of the symbol to insert text after.
     * @param text        the text to insert.
     */
    public void insertTextAfter(int symbolIndex, String text) {
        SymbolLocation symbolLocation = symbolLocations.get(symbolIndex);
        int insertionIndex = getOriginalDocumentIndex(symbolLocation)
                + symbolLocation.getLength();

        document.insert(insertionIndex, text);

        if (symbolIndex + 1 < symbolLocations.size()) {
            SymbolLocation nextSymbol = symbolLocations.get(symbolIndex + 1);
            nextSymbol.addToOffset(text.length());
        }
    }

    /**
     * Gets the index in the original rtf document for a symbol location object.
     *
     * @param symbolLocation the symbol location object.
     * @return integer index in the original rtf document.
     */
    public int getOriginalDocumentIndex(SymbolLocation symbolLocation) {
        int index = symbolLocation.getOffset();

        for (int i = 0; i < symbolLocation.getIndex(); i++) {
            SymbolLocation pointer = symbolLocations.get(i);
            index += pointer.getOffset();
            index += pointer.getLength();
        }

        return index;
    }

    /**
     * Determines if a symbol is outside of the specified destination.
     *
     * @param symbolIndex     the index of the symbol to check.
     * @param destinationName the destination to check.
     * @return {@code true} if it is outside the destination, {@code false} otherwise.
     */
    public boolean symbolIsOutsideDestination(int symbolIndex,
                                              String destinationName) {
        return !symbolLocations.get(symbolIndex).getDestination()
                .equals(destinationName);
    }

    /**
     * Determines if the offset of a symbol is non-zero.
     *
     * @param symbolIndex the index of the symbol to check.
     * @return {@code true} if the offset is not 0, {@code false} otherwise.
     */
    public boolean symbolOffsetIsNonZero(int symbolIndex) {
        return symbolLocations.get(symbolIndex).getOffset() != 0;
    }

    public String getContext(int symbolIndex) {
        SymbolLocation symbolLocation = symbolLocations.get(symbolIndex);
        int insertionIndex = getOriginalDocumentIndex(symbolLocation);

        return document.substring(insertionIndex - 20, insertionIndex + 20);
    }
}
