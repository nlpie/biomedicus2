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

package edu.umn.biomedicus.rtf.reader;

import edu.umn.biomedicus.rtf.exc.RtfReaderException;

import javax.annotation.Nullable;

/**
 * Interface for a keyword action, which performs some kind of manipulation of the state when a keyword is encountered
 * in the RTF document.
 *
 * @since 1.3.0
 */
public interface KeywordAction {
    /**
     * Executes the keyword.
     *
     * @param state             current state.
     * @throws RtfReaderException if there is some kind of error executing the action
     */
    void executeKeyword(State state) throws RtfReaderException;

    KeywordAction copy();

    void setBegin(int begin);

    int getBegin();

    void setEnd(int end);

    int getEnd();

    void setParameter(@Nullable Integer parameter);

    int getParameter();

    boolean hasParameter();

    void setControlWord(String controlWord);

    String getControlWord();

    boolean isKnown();
}
