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

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Builds a {@link RegionTagger} object.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class RegionTaggerBuilder {

    /**
     * The document to tag.
     */
    @Nullable
    private SymbolIndexedDocument symbolIndexedDocument = null;

    /**
     * The destination name.
     */
    @Nullable
    private String destinationName = null;

    /**
     * Begin index in the destination.
     */
    @Nullable
    private Integer beginDestinationIndex = null;

    /**
     * End index in the destination.
     */
    @Nullable
    private Integer endDestinationIndex = null;

    /**
     * Character to insert as a begin tag.
     */
    @Nullable
    private String beginTag = null;

    /**
     * Character to insert as an end tag.
     */
    @Nullable
    private String endTag = null;

    /**
     * Creates a new RegionTaggerBuilder with all fields null.
     *
     * @return newly initialized region tagger builder.
     */
    public static RegionTaggerBuilder create() {
        return new RegionTaggerBuilder();
    }

    /**
     * Sets the document to perform the tagging in.
     *
     * @param symbolIndexedDocument document to perform tagging in.
     * @return this builder.
     */
    public RegionTaggerBuilder withSymbolIndexedDocument(SymbolIndexedDocument symbolIndexedDocument) {
        this.symbolIndexedDocument = symbolIndexedDocument;
        return this;
    }

    /**
     * Sets the destination name that the tag will be in.
     *
     * @param destinationName name of destination.
     * @return this builder.
     */
    public RegionTaggerBuilder withDestinationName(String destinationName) {
        this.destinationName = destinationName;
        return this;
    }

    /**
     * Sets the begin index of the tagged region.
     *
     * @param beginDestinationIndex begin index of the tagged region.
     * @return this builder.
     */
    public RegionTaggerBuilder withBegin(int beginDestinationIndex) {
        this.beginDestinationIndex = beginDestinationIndex;
        return this;
    }

    /**
     * Sets the end index of the tagged region.
     *
     * @param endDestinationIndex end index of the tagged region.
     * @return this builder.
     */
    public RegionTaggerBuilder withEnd(int endDestinationIndex) {
        this.endDestinationIndex = endDestinationIndex;
        return this;
    }

    /**
     * Sets the character to insert as a begin tag.
     *
     * @param beginTag the begin tag character.
     * @return this builder.
     */
    public RegionTaggerBuilder withBeginTag(String beginTag) {
        this.beginTag = beginTag;
        return this;
    }

    /**
     * Sets the character to insert as an end tag.
     *
     * @param endTag the end tag character.
     * @return this builder.
     */
    public RegionTaggerBuilder withEndTag(String endTag) {
        this.endTag = endTag;
        return this;
    }

    /**
     * Creates the {@link RegionTagger} with the previously set properties.
     *
     * @return newly initialized {@link RegionTagger}.
     */
    public RegionTagger createRegionTagger() {
        return new RegionTagger(Objects.requireNonNull(symbolIndexedDocument), Objects.requireNonNull(destinationName),
                Objects.requireNonNull(beginDestinationIndex), Objects.requireNonNull(endDestinationIndex),
                Objects.requireNonNull(beginTag), Objects.requireNonNull(endTag));
    }
}