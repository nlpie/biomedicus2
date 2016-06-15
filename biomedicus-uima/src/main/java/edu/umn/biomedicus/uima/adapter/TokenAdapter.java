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

package edu.umn.biomedicus.uima.adapter;

import edu.umn.biomedicus.common.semantics.PartOfSpeech;
import edu.umn.biomedicus.common.semantics.PartsOfSpeech;
import edu.umn.biomedicus.common.text.Token;
import edu.umn.biomedicus.uima.type1_5.ParseToken;
import org.apache.uima.jcas.JCas;

/**
 * Adapter from the annotation type {@link ParseToken} to the biomedicus model interface
 * {@link Token}.
 */
class TokenAdapter extends AnnotationAdapter<ParseToken> implements Token {
    TokenAdapter(JCas jCas, ParseToken tokenAnnotation) {
        super(jCas, tokenAnnotation);
    }

    @Override
    public PartOfSpeech getPartOfSpeech() {
        return PartsOfSpeech.MAP.get(getAnnotation().getPartOfSpeech());
    }

    @Override
    public void setPennPartOfSpeech(PartOfSpeech partOfSpeech) {
        getAnnotation().setPartOfSpeech(partOfSpeech.toString());
    }

    @Override
    public String getNormalForm() {
        return getAnnotation().getNormalForm();
    }

    @Override
    public void setNormalForm(String normalForm) {
        getAnnotation().setNormalForm(normalForm);
    }

    @Override
    public void setIsStopword(boolean isStopword) {
        getAnnotation().setIsStopword(isStopword);
    }

    @Override
    public boolean isStopword() {
        return getAnnotation().getIsStopword();
    }

    @Override
    public boolean isMisspelled() {
        return getAnnotation().getIsMisspelled();
    }

    @Override
    public void setIsMisspelled(boolean misspelled) {
        getAnnotation().setIsMisspelled(misspelled);
    }

    @Override
    public String correctSpelling() {
        return getAnnotation().getCorrectSpelling();
    }

    @Override
    public void setCorrectSpelling(String correctSpelling) {
        getAnnotation().setCorrectSpelling(correctSpelling);
    }

    @Override
    public void beginEditing() {
        getAnnotation().removeFromIndexes();
    }

    @Override
    public void endEditing() {
        getAnnotation().addToIndexes();
    }
}
