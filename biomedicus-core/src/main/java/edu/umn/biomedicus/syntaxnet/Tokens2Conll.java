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

import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.text.NormForm;
import edu.umn.biomedicus.common.text.ParseToken;
import edu.umn.biomedicus.common.text.Token;

import java.util.List;
import java.util.StringJoiner;

public class Tokens2Conll {
    private final List<Label<ParseToken>> tokenLabels;
    private final List<Label<NormForm>> norms;

    public Tokens2Conll(List<Label<ParseToken>> tokenLabels, List<Label<NormForm>> norms) {
        this.tokenLabels = tokenLabels;
        this.norms = norms;
        if (tokenLabels.size() != norms.size()) {
            throw new IllegalArgumentException("Must have same number of tokens and norms");
        }
    }

    public String conllString() {
        StringBuilder conllBuilder = new StringBuilder();
        for (int i = 0; i < tokenLabels.size(); i++) {
            Token token = tokenLabels.get(i).value();
            NormForm normForm = norms.get(i).value();
            String text = token.text();
            StringJoiner tokenBuilder = new StringJoiner("\t", "", "\n");
            tokenBuilder.add(Integer.toString((i++ + 1))); // sentence position
            tokenBuilder.add(text); // text
            tokenBuilder.add(normForm.getNormalForm()); // LEMMA
            tokenBuilder.add("_"); // UPOSTAG
            tokenBuilder.add("_"); // XPOSTAG
            tokenBuilder.add("_"); // FEATS
            tokenBuilder.add("_"); // HEAD
            tokenBuilder.add("_"); // DEPREL
            tokenBuilder.add("_"); // DEPS
            tokenBuilder.add(token.hasSpaceAfter() ? "_" : "SpaceAfter=No"); // MISC
            conllBuilder.append(tokenBuilder.toString());
        }
        return conllBuilder.toString();
    }
}
