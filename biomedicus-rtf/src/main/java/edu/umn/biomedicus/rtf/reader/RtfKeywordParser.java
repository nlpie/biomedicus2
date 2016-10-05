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

import edu.umn.biomedicus.rtf.exc.EndOfFileException;
import edu.umn.biomedicus.rtf.exc.InvalidKeywordException;
import edu.umn.biomedicus.rtf.exc.InvalidParameterException;
import edu.umn.biomedicus.rtf.exc.RtfReaderException;

import java.util.Map;

/**
 *
 */
public class RtfKeywordParser {

    public static final int KEYWORD_MAX = 30;

    public static final int PARAMETER_MAX = 20;

    private final Map<String, KeywordAction> keywordActionMap;

    public RtfKeywordParser(Map<String, KeywordAction> keywordActionMap) {
        this.keywordActionMap = keywordActionMap;
    }

    public KeywordAction parse(int index, RtfSource rtfSource) throws RtfReaderException {
        int ch = rtfSource.readCharacter();

        Integer parameter = null;

        if (ch == -1) {
            throw new EndOfFileException();
        }

        String controlWord;
        if (!Character.isAlphabetic(ch)) {
            controlWord = "" + (char) ch;
            ch = rtfSource.readCharacter();
        } else {
            StringBuilder controlWordBuilder = new StringBuilder(KEYWORD_MAX);
            do {
                controlWordBuilder.append((char) ch);
                ch = rtfSource.readCharacter();
            } while (controlWordBuilder.length() < KEYWORD_MAX && Character.isAlphabetic(ch));
            controlWord = controlWordBuilder.toString();
            if (controlWord.length() >= KEYWORD_MAX) {
                throw new InvalidKeywordException("Keyword too long");
            }

            boolean parameterIsNegative = false;

            if (ch == '-') {
                parameterIsNegative = true;
                ch = rtfSource.readCharacter();
            }

            if (Character.isDigit(ch)) {
                StringBuilder parameterBuilder = new StringBuilder(PARAMETER_MAX);
                do {
                    parameterBuilder.append((char) ch);
                    ch = rtfSource.readCharacter();
                } while (parameterBuilder.length() < PARAMETER_MAX && Character.isDigit(ch));
                String parameterString = parameterBuilder.toString();
                if (parameterString.length() >= PARAMETER_MAX) {
                    throw new InvalidParameterException("Parameter too long");
                }
                parameter = (parameterIsNegative ? -1 : 1) * Integer.parseInt(parameterString);
            }
        }

        if (ch != ' ') {
            rtfSource.unreadChar();
        }

        KeywordAction keywordAction = keywordActionMap.get(controlWord);

        if (keywordAction != null) {
            keywordAction = keywordAction.copy();
        } else {
            keywordAction = new UnknownKeywordAction();
        }

        keywordAction.setControlWord(controlWord);
        keywordAction.setParameter(parameter);
        keywordAction.setBegin(index);
        keywordAction.setEnd(rtfSource.getIndex());

        return keywordAction;
    }


}
