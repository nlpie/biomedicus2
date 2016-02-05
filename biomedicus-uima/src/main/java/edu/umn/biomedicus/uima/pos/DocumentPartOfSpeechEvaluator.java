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

package edu.umn.biomedicus.uima.pos;

import edu.umn.biomedicus.model.semantics.PartOfSpeech;
import edu.umn.biomedicus.model.text.Document;
import edu.umn.biomedicus.model.text.Token;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class DocumentPartOfSpeechEvaluator {
    private static final ConfusionMatrix EMPTY = new ConfusionMatrix();

    private final Document evaluee;
    private final Document gold;
    private final Map<PartOfSpeech, ConfusionMatrix> counts;
    private final Map<PartOfSpeech, Map<String, Integer>> misses;
    private final String documentId;
    private int correct;
    private int total;

    public DocumentPartOfSpeechEvaluator(Document evaluee,
                                         Document gold,
                                         Map<PartOfSpeech, ConfusionMatrix> counts,
                                         Map<PartOfSpeech, Map<String, Integer>> misses,
                                         int correct,
                                         int total) {
        this.evaluee = evaluee;
        this.gold = gold;
        this.counts = counts;
        this.misses = misses;
        this.correct = correct;
        this.total = total;
        documentId = evaluee.getIdentifier();
    }

    public DocumentPartOfSpeechEvaluator(String documentId) {
        this.evaluee = null;
        this.gold = null;
        Map<PartOfSpeech, ConfusionMatrix> counts = new EnumMap<>(PartOfSpeech.class);
        Map<PartOfSpeech, Map<String, Integer>> misses = new EnumMap<>(PartOfSpeech.class);
        for (PartOfSpeech partOfSpeech : PartOfSpeech.values()) {
            counts.put(partOfSpeech, new ConfusionMatrix());
            misses.put(partOfSpeech, new HashMap<>());
        }
        this.counts = counts;
        this.misses = misses;
        this.correct = 0;
        this.total = 0;
        this.documentId = documentId;
    }

    public static DocumentPartOfSpeechEvaluator create(Document evaluee, Document gold) {
        Map<PartOfSpeech, ConfusionMatrix> counts = new EnumMap<>(PartOfSpeech.class);
        Map<PartOfSpeech, Map<String, Integer>> misses = new EnumMap<>(PartOfSpeech.class);
        for (PartOfSpeech partOfSpeech : PartOfSpeech.values()) {
            counts.put(partOfSpeech, new ConfusionMatrix());
            misses.put(partOfSpeech, new HashMap<>());
        }
        return new DocumentPartOfSpeechEvaluator(evaluee, gold, counts, misses, 0, 0);
    }

    public void evaluate() {
        Iterator<Token> evalueeTokens = evaluee.getTokens().iterator();
        Iterator<Token> goldTokens = gold.getTokens().iterator();

        while (evalueeTokens.hasNext()) {
            if (!goldTokens.hasNext()) {
                throw new AssertionError("More evaluation tokens than gold tokens.");
            }

            Token evalueeToken = evalueeTokens.next();
            Token goldToken = goldTokens.next();

            if (evalueeToken.getBegin() != goldToken.getBegin() || evalueeToken.getEnd() != goldToken.getEnd()) {
                throw new AssertionError("Gold tokens should be equivalent to evaluee tokens.");
            }

            PartOfSpeech evalueePartOfSpeech = evalueeToken.getPartOfSpeech();
            PartOfSpeech goldPartOfSpeech = goldToken.getPartOfSpeech();

            if (goldPartOfSpeech == null || !PartOfSpeech.REAL_TAGS.contains(goldPartOfSpeech)) {
                continue;
            }
            if (goldPartOfSpeech.equals(evalueePartOfSpeech)) {
                counts.get(goldPartOfSpeech).incrementTruePositives();
                correct++;
            } else {
                counts.get(goldPartOfSpeech).incrementFalseNegatives();
                if (evalueePartOfSpeech != null) {
                    counts.get(evalueePartOfSpeech).incrementFalsePositives();
                }
                misses.get(goldPartOfSpeech).merge(goldToken.getText() + "/" + evalueePartOfSpeech, 1, Integer::sum);
            }
            total++;
        }

        if (goldTokens.hasNext()) {
            throw new AssertionError("More gold tokens than evaluation tokens.");
        }
    }

    public void write(Writer writer) throws IOException {
        writer.write(documentId + "," + correct + "," + total + ",");
        for (PartOfSpeech partOfSpeech : PartOfSpeech.REAL_TAGS) {
            ConfusionMatrix confusionMatrix = counts.getOrDefault(partOfSpeech, EMPTY);
            confusionMatrix.write(writer, total);
        }
        writer.write("\n");
    }

    public void writeMisses(Writer writer) throws IOException{
        for (PartOfSpeech partOfSpeech : PartOfSpeech.REAL_TAGS) {
            writer.write(partOfSpeech.toString() + ": ");
            List<Map.Entry<String, Integer>> collect = misses.get(partOfSpeech)
                    .entrySet()
                    .stream()
                    .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                    .limit(10)
                    .collect(Collectors.toList());
            for (Map.Entry<String, Integer> entry : collect) {
                writer.write(entry.getKey() + " " + entry.getValue() + ", ");
            }
            writer.write("\n");
        }
    }

    public static void writeHeader(Writer writer) throws IOException {
        writer.write("DocumentID,Correct,Total,");
        for (PartOfSpeech partOfSpeech : PartOfSpeech.REAL_TAGS) {
            String tag = partOfSpeech.name();
            writer.write(tag + "-Hit," + tag + "-Total,");
        }
        writer.write("\n");
    }

    public void add(DocumentPartOfSpeechEvaluator other) {
        correct += other.correct;
        total += other.total;

        for (PartOfSpeech partOfSpeech : PartOfSpeech.REAL_TAGS) {
            counts.get(partOfSpeech).add(other.counts.get(partOfSpeech));
            for (Map.Entry<String, Integer> stringIntegerEntry : other.misses.get(partOfSpeech).entrySet()) {
                misses.get(partOfSpeech).merge(stringIntegerEntry.getKey(), stringIntegerEntry.getValue(), Integer::sum);
            }

        }
    }
}
