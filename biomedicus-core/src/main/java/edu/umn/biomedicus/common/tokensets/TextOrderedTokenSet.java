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

package edu.umn.biomedicus.common.tokensets;

import edu.umn.biomedicus.common.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.common.text.SpanLike;
import edu.umn.biomedicus.common.text.Token;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * <p>A text-ordered token set, which is a {@link OrderedTokenSet} in which the tokens are in
 * the same order as they are in text.</p>
 * <p>Contains functionality for creating syntactic permutations around prepositions within the ordered token set.</p>
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public abstract class TextOrderedTokenSet implements OrderedTokenSet {
    /**
     * Parts of speech that should be included in a syntactic permutation's right size.
     */
    private static final Collection<PartOfSpeech> RIGHT_NOUNS_AND_MODIFIERS_POS;

    static {
        Set<PartOfSpeech> builder = new HashSet<>();
        Collections.addAll(builder,
                PartOfSpeech.NN,
                PartOfSpeech.JJ,
                PartOfSpeech.VBG,
                PartOfSpeech.VBN);
        RIGHT_NOUNS_AND_MODIFIERS_POS = Collections.unmodifiableSet(builder);
    }

    /**
     * Parts of speech that should not end a syntactic permutation on the right side of a preposition but should not be
     * included in the permutation
     */
    private static final Collection<PartOfSpeech> RIGHT_SKIP_POS;

    static {
        Set<PartOfSpeech> builder = new HashSet<>();
        Collections.addAll(builder,
                PartOfSpeech.IN,
                PartOfSpeech.DT,
                PartOfSpeech.PRP$);
        RIGHT_SKIP_POS = Collections.unmodifiableSet(builder);
    }

    /**
     * A set of English prepositions.
     */
    private static final Set<String> PREPOSITIONS;

    static {
        Set<String> builder = new HashSet<>();
        Collections.addAll(builder, "in", "of", "at", "on");
        PREPOSITIONS = Collections.unmodifiableSet(builder);
    }

    /**
     * Gets a {@link java.util.stream.Stream} of all the prepositions in this ordered token set.
     *
     * @return java stream of the prepositions in this token set
     */
    public Stream<Token> getPrepositionsStream() {
        return getTokens()
                .stream()
                .filter(p -> PartOfSpeech.IN.equals(p.getPartOfSpeech()))
                .filter(p -> PREPOSITIONS.contains(p.getNormalForm()));
    }

    /**
     * Gets the prepositions in this token set as a {@link java.util.List} of tokens
     *
     * @return a list of tokens which are prepositions
     */
    public List<Token> getPrepositions() {
        return getPrepositionsStream().collect(Collectors.toList());
    }

    /**
     * Returns a {@link java.util.List} of this set's continuous {@link Token} objects
     * occurring before a specific token.
     *
     * @param token the token in this ordered set to get the continuous nouns before
     * @return list
     */
    public List<Token> continuousNounsBefore(Token token) {
        List<Token> tokens = getTokens();
        int index = tokens.indexOf(token);
        if (index == -1) {
            throw new IllegalArgumentException("Token was not found in list of tokens");
        }
        int left = index;
        while (left > 0) {
            Token leftToken = tokens.get(left - 1);
            if (PartOfSpeech.NN.equals(leftToken.getPartOfSpeech())) {
                left--;
            } else {
                break;
            }
        }
        return tokens.subList(left, index);
    }

    /**
     * Returns all Nouns and their modifying tokens (adjectives, gerunds/present participle) after a specific token. It
     * will skip over determiners and possessive pronouns. This will usually be used on a prepositional phrase to allow
     * for the construction of a syntactic permutation.
     *
     * @param token token to find the following phrase, will usually be a preposition.
     * @return a list of tokens that constitute a noun preceded by its modifiers.
     */
    public List<Token> nounsAfter(Token token) {
        List<Token> tokens = getTokens();
        int right = tokens.indexOf(token);
        List<Token> tokenBuilder = new ArrayList<>(tokens.size() - right - 1);
        while (right < tokens.size() - 1) {
            Token rightToken = tokens.get(right + 1);
            PartOfSpeech partOfSpeech = rightToken.getPartOfSpeech();
            if (RIGHT_NOUNS_AND_MODIFIERS_POS.contains(partOfSpeech)) {
                tokenBuilder.add(rightToken);
            } else if (!RIGHT_SKIP_POS.contains(partOfSpeech)) {
                break;
            }
            right++;
        }
        return tokenBuilder;
    }

    @Override
    public SpanLike getSpan() {
        List<Token> tokens = getTokens();
        int begin = tokens.get(0).getBegin();
        int end = tokens.get(tokens.size() - 1).getEnd();
        return Span.create(begin, end);
    }

    /**
     * Get a {@link java.util.stream.Stream} of the syntactic permutations of this token set
     *
     * @return java stream of syntactic permutations
     */
    public Stream<OrderedTokenSet> prepositionalSyntacticPermutationsStream() {
        return getPrepositionsStream().map(this::prepositionalSyntacticPermutationAround);
    }

    /**
     * Returns a {@link java.util.Collection} of the {@link OrderedTokenSet} syntactic
     * permutations around prepositions for this ordered token set.
     *
     * @return collection of syntactic permutations for the prepositions in this token set
     */
    public Collection<OrderedTokenSet> prepositionalSyntacticPermutations() {
        return prepositionalSyntacticPermutationsStream().collect(Collectors.toList());
    }

    public TextOrderedTokenSet filter(Predicate<Token> tokenPredicate) {
        return new SimpleTextOrderedTokenSet(getTokens().stream().filter(tokenPredicate).collect(Collectors.toList()));
    }

    /**
     * Returns the syntactic permutation of prepositional phrases around a specific preposition token.
     *
     * @param token a preposition in this
     * @return the ordered token set of the permutation or null if it doesn't have a permutation
     */
    @Nullable
    public OrderedTokenSet prepositionalSyntacticPermutationAround(Token token) {
        List<Token> continuousNounsBefore = continuousNounsBefore(token);
        List<Token> nounsAfter = nounsAfter(token);
        if (nounsAfter.isEmpty() || continuousNounsBefore.isEmpty()) {
            return null;
        }
        List<Token> tokens = new ArrayList<>(nounsAfter.size() + continuousNounsBefore.size());
        tokens.addAll(nounsAfter);
        tokens.addAll(continuousNounsBefore);
        return new SimpleOrderedTokenSet(tokens);
    }

    /**
     * Returns all of this ordered token set's ordered subsets of tokens smaller than the given size as a {@link java.util.stream.Stream}
     * of {@link TextOrderedTokenSet}
     *
     * @param size the maximum size for subsets
     * @return stream of ordered token sets
     */
    public Stream<TextOrderedTokenSet> orderedSubsetsSmallerThan(int size) {
        TextOrderedTokenSubsetsSpliterator spliterator = TextOrderedTokenSubsetsSpliterator.create(this, size);
        return StreamSupport.stream(spliterator, false);
    }

    public Token firstToken() {
        return getTokens().get(0);
    }

    public Token lastToken() {
        List<Token> tokens = getTokens();
        return tokens.get(tokens.size() - 1);
    }

    public int size() {
        return getTokens().size();
    }
}
