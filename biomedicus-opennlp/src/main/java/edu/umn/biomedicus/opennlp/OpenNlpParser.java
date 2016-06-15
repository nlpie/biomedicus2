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

package edu.umn.biomedicus.opennlp;

import com.google.inject.Inject;
import edu.umn.biomedicus.common.text.Sentence;
import edu.umn.biomedicus.common.text.Token;
import edu.umn.biomedicus.parsing.Parser;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.util.Span;

/**
 *
 */
public class OpenNlpParser implements Parser {
    private final opennlp.tools.parser.Parser parser;

    @Inject
    public OpenNlpParser(OpenNlpParserModel openNlpParserModel) {
        parser = openNlpParserModel.createParser();
    }

    @Override
    public void parseSentence(Sentence sentence) {
        String sentenceText = sentence.getText();
        int begin = sentence.getBegin();
        Parse p = new Parse(sentenceText, new Span(0, sentenceText.length()), AbstractBottomUpParser.INC_NODE, 0, 0);
        int i = 0;
        for (Token token : sentence.getTokens()) {
            int tokenBegin = token.getBegin();
            int tokenEnd = token.getEnd();
            int offsetBegin = tokenBegin - begin;
            int offsetEnd = tokenEnd - begin;
            Span span = new Span(offsetBegin, offsetEnd);
            Parse tokParse = new Parse(sentenceText, span, AbstractBottomUpParser.TOK_NODE, 0, i);
            Parse taggedParse = new Parse(sentenceText, span, token.getPartOfSpeech().toString(), 0, i++);
            taggedParse.insert(tokParse);
            p.insert(taggedParse);
        }
        Parse parse = parser.parse(p);
        StringBuffer stringBuffer = new StringBuffer();
        parse.show(stringBuffer);
        sentence.setParseTree(stringBuffer.toString());
    }
}
