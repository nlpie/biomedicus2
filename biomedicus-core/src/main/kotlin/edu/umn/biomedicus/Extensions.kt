/*
 * Copyright (c) 2018 Regents of the University of Minnesota.
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

package edu.umn.biomedicus

import edu.umn.biomedicus.sentences.Sentence
import edu.umn.biomedicus.tagging.PosTag
import edu.umn.biomedicus.tokenization.ParseToken
import edu.umn.nlpengine.Document
import edu.umn.nlpengine.LabelIndex

/**
 * Returns the label index of all the sentences in the document.
 */
fun Document.sentences(): LabelIndex<Sentence> = labelIndex()

/**
 * Returns the label index of all the tokens in the document.
 */
fun Document.tokens(): LabelIndex<ParseToken> = labelIndex()

/**
 * Returns the label index of all the pos tags in the document.
 */
fun Document.posTags(): LabelIndex<PosTag> = labelIndex()
