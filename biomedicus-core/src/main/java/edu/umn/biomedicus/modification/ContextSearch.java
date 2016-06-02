package edu.umn.biomedicus.modification;

import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.semantics.PartOfSpeech;
import edu.umn.biomedicus.common.text.*;

import java.util.ArrayList;
import java.util.List;

class ContextSearch {
    private final Labels<Sentence2> sentences;
    private final int leftContextScope;
    private final List<String> leftContextCues;
    private final List<String> rightContextCues;
    private final int rightContextScope;
    private final String documentText;
    private final Labels<PartOfSpeech> partOfSpeechLabels;
    private final Labels<?> modifiableTerms;
    private final Labels<?> tokens;
    private final List<PartOfSpeech> scopeDelimitersPos;
    private final List<String> scopeDelimitersTxt;

    ContextSearch(ContextCues contextCues,
                  Document document,
                  Labels<Sentence2> sentences,
                  Labels<?> modifiableTerms,
                  Labels<?> tokens,
                  Labels<PartOfSpeech> partOfSpeechLabels) {
        leftContextScope = contextCues.getLeftContextScope();
        leftContextCues = contextCues.getLeftContextCues();
        rightContextCues = contextCues.getRightContextCues();
        rightContextScope = contextCues.getRightContextScope();
        scopeDelimitersPos = contextCues.getScopeDelimitersPos();
        scopeDelimitersTxt = contextCues.getScopeDelimitersTxt();
        documentText = document.getText();
        this.sentences = sentences;
        this.modifiableTerms = modifiableTerms;
        this.tokens = tokens;
        this.partOfSpeechLabels = partOfSpeechLabels;
    }

    List<Span> findMatches() {
        List<Span> matchingSpans = new ArrayList<>();
        for (Label<Sentence2> sentence : sentences) {
            Labels<?> sentenceTokens = tokens.insideSpan(sentence);
            for (Label<?> modifiableTerm : modifiableTerms.insideSpan(sentence)) {
                if (matches(sentenceTokens, modifiableTerm)) {
                    matchingSpans.add(sentence.toSpan());
                }
            }
        }

        return matchingSpans;
    }

    private boolean matches(Labels<?> sentenceTokens, Label<?> modifiableTerm) {
        Labels<?> leftContextTokens = sentenceTokens.leftwardsFrom(modifiableTerm);
        for (Label<?> leftContextToken : leftContextTokens.limit(leftContextScope)) {
            String tokenText = leftContextToken.getCovered(documentText).toString();
            if (scopeDelimitersTxt.contains(tokenText)) {
                break;
            }
            if (partOfSpeechLabels.insideSpan(leftContextToken).stream().anyMatch(scopeDelimitersPos::contains)) {
                break;
            }
            if (leftContextCues.contains(tokenText)) {
                return true;
            }
        }

        Labels<?> rightContextTokens = sentenceTokens.rightwardsFrom(modifiableTerm);
        for (Label<?> rightContextToken : rightContextTokens.limit(rightContextScope)) {
            String tokenText = rightContextToken.getCovered(documentText).toString();
            if (scopeDelimitersTxt.contains(tokenText)) {
                break;
            }
            if (partOfSpeechLabels.insideSpan(rightContextToken).stream().anyMatch(scopeDelimitersPos::contains)) {
                break;
            }
            if (rightContextCues.contains(tokenText)) {
                return true;
            }
        }
        return false;
    }
}
