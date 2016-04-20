/*
 * Copyright (c) 2015 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.rtf.reader;

/**
 * An RTF output destination.
 *
 * @since 1.3.0
 */
public interface OutputDestination {
    /**
     * Writes a character to the output destination.
     *
     * @param ch    the character to write.
     * @param state the current state.
     * @return the index in the output destination that the character was written to, or -1 if the character was not
     * written.
     */
    int writeChar(char ch, State state);

    /**
     * Finishes the destination, performing any wrap-up computations.
     */
    void finishDestination();

    /**
     * Called when the rtf reader encounters a control word.
     *
     * @param keywordAction
     */
    void controlWordEncountered(KeywordAction keywordAction);

    /**
     * Returns the name of the output destination.
     *
     * @return the name of the output destination.
     */
    String getName();
}
