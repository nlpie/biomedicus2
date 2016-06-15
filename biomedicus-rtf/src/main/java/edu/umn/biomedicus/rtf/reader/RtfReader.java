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

import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.rtf.exc.RtfReaderException;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Class responsible for parsing an RTF document.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class RtfReader {

    /**
     * Parses the rtf control words.
     */
    private final RtfKeywordParser rtfKeywordParser;

    /**
     * Source of RTF data.
     */
    private final RtfSource rtfSource;

    /**
     * Stack of states.
     */
    private final Deque<State> stateStack;

    /**
     * The current state.
     */
    private State currentState;

    /**
     * Creates a new rtf reader.
     *
     * @param rtfKeywordParser the parser for rtf keywords.
     * @param rtfSource the source of rtf data.
     * @param initialState the initial state.
     */
    public RtfReader(RtfKeywordParser rtfKeywordParser, RtfSource rtfSource, State initialState) {
        this.rtfKeywordParser = rtfKeywordParser;
        this.rtfSource = rtfSource;
        this.currentState = initialState;
        this.stateStack = new ArrayDeque<>();
    }

    /**
     * Runs the parsing.
     *
     * @throws IOException
     * @throws RtfReaderException
     */
    public void parseFile() throws IOException, RtfReaderException {
        while (true) {
            int index = rtfSource.getIndex();
            int ch = rtfSource.readCharacter();
            if (ch == -1) {
                break;
            }
            switch (ch) {
                case '{':
                    stateStack.addFirst(currentState);
                    currentState = currentState.copy();
                    break;
                case '}':
                    currentState = stateStack.removeFirst();
                    break;
                case '\\':
                    KeywordAction keywordAction = rtfKeywordParser.parse(index, rtfSource);
                    currentState.handleKeyword(keywordAction);
                    break;
                case 0:
                    break;
                default:
                    currentState.writeCharacter(ch, Span.create(index, rtfSource.getIndex()));
                    break;
            }
        }
        currentState.finishState();
    }
}
