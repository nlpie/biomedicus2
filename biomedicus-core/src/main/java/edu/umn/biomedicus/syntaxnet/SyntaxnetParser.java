/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.syntaxnet;

import com.google.inject.Inject;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.text.DependencyParse;
import edu.umn.biomedicus.common.text.NormForm;
import edu.umn.biomedicus.common.text.ParseToken;
import edu.umn.biomedicus.common.text.Sentence;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.util.List;

public final class SyntaxnetParser implements DocumentProcessor {
    private final SyntaxnetParserService syntaxnetParserService;
    private final Labels<Sentence> sentenceLabels;
    private final Labels<ParseToken> tokenLabels;
    private final Labels<NormForm> normFormLabels;
    private final Labeler<DependencyParse> dependencyParseLabeler;

    @Inject
    SyntaxnetParser(SyntaxnetParserService syntaxnetParserService,
                    Labels<Sentence> sentenceLabels,
                    Labels<ParseToken> tokenLabels,
                    Labels<NormForm> normFormLabels,
                    Labeler<DependencyParse> dependencyParseLabeler) {
        this.syntaxnetParserService = syntaxnetParserService;
        this.sentenceLabels = sentenceLabels;
        this.tokenLabels = tokenLabels;
        this.normFormLabels = normFormLabels;
        this.dependencyParseLabeler = dependencyParseLabeler;
    }

    @Override
    public void process() throws BiomedicusException {
        ParserEval parser = syntaxnetParserService.getParser();

        for (Label<Sentence> sentenceLabel : sentenceLabels) {
            List<Label<ParseToken>> sentenceTokenLabels = tokenLabels.insideSpan(sentenceLabel).all();
            List<Label<NormForm>> sentenceNormLabels = normFormLabels.insideSpan(sentenceLabel).all();
            String conllString = new Tokens2Conll(sentenceTokenLabels, sentenceNormLabels).conllString();
            String conllOutput = parser.send(conllString);
            dependencyParseLabeler.value(new DependencyParse(conllOutput)).label(sentenceLabel);
        }
    }
}
