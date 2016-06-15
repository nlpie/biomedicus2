/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.common.text;

/**
 * A section in a document.
 *
 * @since 1.4
 */
public interface Section extends TextSpan {
    /**
     * Returns the title of the section.
     *
     * @return section title.
     */
    String getSectionTitle();

    /**
     * Returns the content start of the section.
     *
     * @return content start.
     */
    int contentStart();

    /**
     * Returns the level of the section, ex. 0 for a section 1 for a subsection.
     *
     * @return section level.
     */
    int getLevel();

    /**
     * Returns whether this section has subsections.
     *
     * @return
     */
    boolean hasSubsections();

    String getKind();

    Iterable<Sentence> getSentences();
}
