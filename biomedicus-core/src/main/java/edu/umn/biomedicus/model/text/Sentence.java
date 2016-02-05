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

package edu.umn.biomedicus.model.text;

import edu.umn.biomedicus.model.semantics.SubstanceUsageType;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A biomedicus basic unit for a sentence in text.
 *
 * @since 1.0.0
 */
public interface Sentence extends TextSpan {
    /**
     * Gets the tokens within this sentence.
     *
     * @return a list of the tokens in the sentence.
     */
    default List<Token> getTokens() {
        return tokens().collect(Collectors.toList());
    }

    /**
     * Gets the tokens within this sentence as a stream.
     *
     * @return a stream of the tokens in the sentence.
     */
    Stream<Token> tokens();

    /**
     * Gets the terms within this sentence.
     *
     * @return list of terms
     */
    default List<Term> getTerms() {
        return terms().collect(Collectors.toList());
    }

    /**
     * Gets the terms within this sentence as a stream.
     *
     * @return list of the terms
     */
    Stream<Term> terms();

    /**
     * Gets the dependencies string.
     *
     * @return dependencies.
     */
    String getDependencies();

    /**
     * Sets the dependencies string.
     *
     * @param dependencies dependencies.
     */
    void setDependencies(String dependencies);

    /**
     * Gets the parse tree.
     *
     * @return
     */
    String getParseTree();

    void setParseTree(String parseTree);

    boolean isSocialHistoryCandidate();

    void setIsSocialHistoryCandidate(boolean isSocialHistoryCandidate);

    Collection<SubstanceUsageType> getSubstanceUsageTypes();

    void addSubstanceUsageType(SubstanceUsageType substanceUsageType);
}
