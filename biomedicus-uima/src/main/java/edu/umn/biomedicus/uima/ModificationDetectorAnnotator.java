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

package edu.umn.biomedicus.uima;

import edu.umn.biomedicus.type.SentenceAnnotation;
import edu.umn.biomedicus.type.TermAnnotation;
import edu.umn.biomedicus.type.TokenAnnotation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.CasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.*;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Modification Detector Annotator that uses rules to idenfiy various types of contexts
 * e.g. negation, history, famility history, etc.
 * what gets detected is controlled by the descriptor file
 * TODO: this needs to be migrated to be uima-agnostic
 */
public class ModificationDetectorAnnotator extends CasAnnotator_ImplBase {

    private static final Logger LOGGER = LogManager.getLogger(ModificationDetectorAnnotator.class);

    // The character following a match candidate must be a space, symbol, or s for plural
    public static final String terminatingChars = " .,;:'[{]}|-_=+!?s";

    public static final String PARAM_MOD_TYPE = "ModificationType";

    public static final String PARAM_LEFT_CONTEXT_CUES = "leftContextCues";

    public static final String PARAM_RIGHT_CONTEXT_CUES = "rightContextCues";

    public static final String PARAM_LEFT_CONTEXT_SCOPE = "leftContextScope";

    public static final String PARAM_RIGHT_CONTEXT_SCOPE = "rightContextScope";

    public static final String PARAM_SCOPE_DELIM_POS = "scopeDelimitersPOS";

    public static final String PARAM_SCOPE_DELIM_TXT = "scopeDelimitersTxt";

    private String modType;

    private int leftContextScope;

    private int rightContextScope;

    private Type tokenType;

    private Type termType;

    private Feature normFeature;

    private HashSet<String> leftContextCuesHash = new HashSet<>();

    private HashSet<String> rightContextCuesHash = new HashSet<>();

    private HashSet<String> scopeDelimitersPOSHash = new HashSet<>();

    private HashSet<String> scopeDelimitersTxtHash = new HashSet<>();

    @Override
    public void initialize(UimaContext context) throws ResourceInitializationException {
        super.initialize(context);

        LOGGER.info("Initializing Modification Detector Annotator");

        modType = (String) context.getConfigParameterValue(PARAM_MOD_TYPE);
        String[] leftContextCues = (String[]) context.getConfigParameterValue(PARAM_LEFT_CONTEXT_CUES);
        String[] rightContextCues = (String[]) context.getConfigParameterValue(PARAM_RIGHT_CONTEXT_CUES);
        leftContextScope = Integer.parseInt((String) context.getConfigParameterValue(PARAM_LEFT_CONTEXT_SCOPE));
        rightContextScope = Integer.parseInt((String) context.getConfigParameterValue(PARAM_RIGHT_CONTEXT_SCOPE));
        String[] scopeDelimitersPOS = (String[]) context.getConfigParameterValue(PARAM_SCOPE_DELIM_POS);
        String[] scopeDelimitersTxt = (String[]) context.getConfigParameterValue(PARAM_SCOPE_DELIM_TXT);

        Collections.addAll(leftContextCuesHash, leftContextCues);

        Collections.addAll(rightContextCuesHash, rightContextCues);

        Collections.addAll(scopeDelimitersPOSHash, scopeDelimitersPOS);

        Collections.addAll(scopeDelimitersTxtHash, scopeDelimitersTxt);


    }


    @Override
    public void process(CAS aCAS) throws AnalysisEngineProcessException {
        CAS sysCAS = aCAS.getView(Views.SYSTEM_VIEW);
        JCas sysJCas;
        try {
            sysJCas = sysCAS.getJCas();
        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }

        // iterate over sentences
        // and create two maps indexed by the starting position
        // 1. map of terms
        // 2. map of tokens

        List<TokenAnnotation> addedTokens = new ArrayList<>();

        for (AnnotationFS sentence : AnnotationUtils.getAnnotations(sysCAS, SentenceAnnotation.class)) {

            TreeMap<Integer, List<TermAnnotation>> termMap = new TreeMap<>();
            TreeMap<Integer, TokenAnnotation> tokenMap = new TreeMap<>();

            // use sentence boundary as left/right scope if the left/rightContextScope is set to -1
            // giving a space of 1000 tokens should do it
            if (leftContextScope == -1) {
                leftContextScope = 1000;
            }
            if (rightContextScope == -1) {
                rightContextScope = 1000;
            }

            // fill up the TermAnnotation map
            ArrayList sentenceTermAnnotations = (ArrayList) AnnotationUtils.getCoveredAnnotations(sysCAS, sentence, termType);
            for (Object sentenceTermAnnotation : sentenceTermAnnotations) {
                TermAnnotation currTermAnnotation = (TermAnnotation) sentenceTermAnnotation;

                // we could have more than one TermAnnotation starting at the same position
                // so, let's stuff multiple terms with the same start offset
                // into an array and put that into the HashTree index by the offset position
                Integer begin = currTermAnnotation.getBegin();
                termMap.compute(begin, (Integer key, @Nullable List<TermAnnotation> value) -> {
                    if (value == null) {
                        value = new ArrayList<>();
                    }
                    value.add(currTermAnnotation);
                    return value;
                });
            }

            // fill up the TokenAnnotation map
            // as we step through TokenAnnotations
            // let's find multi-word tokens needed for modification signaling
            ArrayList sentenceTokenAnnotations = (ArrayList) AnnotationUtils.getCoveredAnnotations(sysCAS, sentence, tokenType);
            Iterator titer = sentenceTokenAnnotations.iterator();
            int likelyModCueFlag = 0; // flag indicating that there is a modification flag (applies to short sentences only)

            TokenAnnotation currTok = null;
            TokenAnnotation prevTok = null;
            TokenAnnotation pprevTok;

            while (titer.hasNext()) {
                pprevTok = prevTok;
                prevTok = currTok;
                currTok = (TokenAnnotation) titer.next();

                // put the current token into the map no matter what
                tokenMap.put(currTok.getBegin(), currTok);


                // ok, now lets do some just in time tokenization
                // that applies specifically to identification of modification cues
                // like "status post", "in the past", "family history", "family medical history"
                // - check for multi-word tokens
                // - if we find one, it will override the single word token at the same start position

                // s/p, s\p, s-p, status/post
                if (prevTok != null  && pprevTok != null && (pprevTok.getCoveredText().toLowerCase().equals("s") || pprevTok.getCoveredText().toLowerCase().equals("status")) && (prevTok.getCoveredText().toLowerCase().equals("/") || prevTok.getCoveredText().toLowerCase().equals("\\") || prevTok.getCoveredText().toLowerCase().equals("-")) && (currTok.getCoveredText().toLowerCase().equals("p") || currTok.getCoveredText().toLowerCase().equals("post"))) {
                    // create a new token
                    TokenAnnotation newtoken = new TokenAnnotation(sysJCas);
                    newtoken.setBegin(pprevTok.getBegin());
                    newtoken.setEnd(currTok.getEnd());
                    newtoken.setPartOfSpeech("RB");
                    newtoken.setStringValue(normFeature, "status post");
                    // insert the new token into tokenmap
                    tokenMap.put(newtoken.getBegin(), newtoken);
                    addedTokens.add(newtoken);
                }

                // status post
                if (prevTok != null && prevTok.getCoveredText().toLowerCase().equals("status") && currTok.getCoveredText().toLowerCase().equals("post")) {
                    // create a new token
                    TokenAnnotation newtoken = new TokenAnnotation(sysJCas);
                    newtoken.setBegin(prevTok.getBegin());
                    newtoken.setEnd(currTok.getEnd());
                    newtoken.setPartOfSpeech("RB");
                    newtoken.setStringValue(normFeature, "status post");
                    newtoken.addToIndexes();

                    // insert the new token into tokenmap
                    tokenMap.put(newtoken.getBegin(), newtoken);
                    addedTokens.add(newtoken);
                }

                // family history
                if (prevTok != null && prevTok.getCoveredText().toLowerCase().equals("family") && currTok.getCoveredText().toLowerCase().equals("history")) {
                    // create a new token
                    TokenAnnotation newtoken = new TokenAnnotation(sysJCas);
                    newtoken.setBegin(prevTok.getBegin());
                    newtoken.setEnd(currTok.getEnd());
                    newtoken.setPartOfSpeech("NN");
                    newtoken.setStringValue(normFeature, "family history");
                    newtoken.addToIndexes();

                    // insert the new token into tokenmap
                    tokenMap.put(newtoken.getBegin(), newtoken);
                    addedTokens.add(newtoken);
                }


                // differential diagnosis
                if (prevTok != null && pprevTok != null && (pprevTok.getCoveredText().toLowerCase().equals("m") || pprevTok.getCoveredText().toLowerCase().equals("am")) && (prevTok.getCoveredText().toLowerCase().equals("not")) && (currTok.getCoveredText().toLowerCase().equals("sure") || currTok.getCoveredText().toLowerCase().equals("certain") || currTok.getCoveredText().toLowerCase().equals("confident"))) {
                    // create a new token
                    TokenAnnotation newtoken = new TokenAnnotation(sysJCas);
                    newtoken.setBegin(pprevTok.getBegin());
                    newtoken.setEnd(currTok.getEnd());
                    newtoken.setPartOfSpeech("RB");
                    newtoken.setStringValue(normFeature, "status post");
                    // insert the new token into tokenmap
                    tokenMap.put(newtoken.getBegin(), newtoken);
                    addedTokens.add(newtoken);
                }


                // there is some modification indicator cue in this sentence
                if (leftContextCuesHash.contains(currTok.getCoveredText().toLowerCase()) || rightContextCuesHash.contains(currTok.getCoveredText().toLowerCase())) {
                    likelyModCueFlag = 1;
                }

            }


            // step through the TermAnnotation map and look for modification indicators/cues
            for (Map.Entry<Integer, List<TermAnnotation>> entry: termMap.entrySet()) {
                int tposition = entry.getKey();
                // check to see if we have a short sentence with a modification indicator
                if (likelyModCueFlag == 1 && sentenceTokenAnnotations.size() <= 10) {
                    // set negation flag for the current term and move on
                    for (TermAnnotation termAnnotation : entry.getValue()) {
                        if (modType.equals("negation")) {
                            termAnnotation.setIsNegated(true);
                        } else {
                            termAnnotation.setAspect(modType);
                        }
                    }
                    continue;
                }


                /**
                 *
                 * This is where we begin searching around
                 *
                 */


                // if we don't have a short sentence, then we need to snoop around
                // look left from current position in the stream of tokens
                // searching for a context cue
                int poscur = tposition;
                int leftTokCnt = 0;

                // flag to indicate that a modification cue was found
                int foundMod = 0;

                while (poscur-- >= sentence.getBegin()) { // wasting some time here by making tiny steps, but oh well
                    // get the tokens to the left
                    if (tokenMap.containsKey(new Integer(poscur))) {
                        // keep counting tokens so we don't go past the scope
                        leftTokCnt++;

                        TokenAnnotation cTok = tokenMap.get(new Integer(poscur));

                        // check if the current token is a scope boundary
                        if (scopeDelimitersPOSHash.contains(cTok.getPartOfSpeech()) || scopeDelimitersTxtHash.contains(cTok.getCoveredText().toLowerCase())) {
                            if (!cTok.getCoveredText().toLowerCase().equals("no") && !cTok.getCoveredText().toLowerCase().equals("any")) {
                                break;
                            }
                        }

                        // check if the token is within scope and the covered text is on the list of cues
                        if (leftTokCnt <= leftContextScope && leftContextCuesHash.contains(cTok.getCoveredText().toLowerCase())) {
                            // set negation flag for all terms starting at this position
                            for (TermAnnotation cTrm : entry.getValue()) {
                                if (modType.equals("negation")) {
                                    cTrm.setIsNegated(true);
                                } else {
                                    cTrm.setAspect(modType);
                                }
                            }

                            // if we found a modification context cue, no need to search any further
                            foundMod = 1;
                            break;
                        }

                    }
                }

                // we search left context first
                // if something is found and modification feature was set on the
                // current TermAnnotation, no need to examine the right context -
                // we can just move on
                if (foundMod == 1) {
                    continue;
                }

                // otherwise, need to keep looking
                //look right

                poscur = tposition;
                int rightTokCnt = 0;

                while (poscur++ <= sentence.getEnd()) { // wasting some time here by making tiny steps, but oh well

                    // get the tokens to the right
                    if (tokenMap.containsKey(new Integer(poscur))) {

                        // keep counting tokens so we don't go past the scope
                        rightTokCnt++;

                        TokenAnnotation cTok = tokenMap.get(new Integer(poscur));
                        //System.out.println(cTok.getCoveredText());

                        // check if the current token is a scope boundary
                        if (scopeDelimitersPOSHash.contains(cTok.getPartOfSpeech()) || scopeDelimitersTxtHash.contains(cTok.getCoveredText().toLowerCase())) {
                            if (!cTok.getCoveredText().toLowerCase().equals("no") && !cTok.getCoveredText().toLowerCase().equals("any")) {

                                break;
                            }
                        }


                        // check if the token is within scope and the covered text is on the list of cues
                        if (rightTokCnt <= rightContextScope && rightContextCuesHash.contains(cTok.getCoveredText().toLowerCase())) {
                            //System.out.println("\tRCUE: "+cTok.getCoveredText());

                            // set negation flag for all terms starting at this position
                            for (TermAnnotation termAnnotation : entry.getValue()) {
                                if (modType.equals("negation")) {
                                    termAnnotation.setIsNegated(true);
                                } else {
                                    termAnnotation.setAspect(modType);
                                }

                                //System.out.println("TERM: "+cTrm.getCoveredText());
                            }

                        }

                    }
                }
            } // end of loop iterating over TermAnnotations
        }

        for (TokenAnnotation addedToken : addedTokens) {
            addedToken.removeFromIndexes();
        }

    }

    @Override
    public void typeSystemInit(TypeSystem typeSystem) throws AnalysisEngineProcessException {

        // Initialize types
        super.typeSystemInit(typeSystem);

        // Initialize type
        tokenType = typeSystem.getType(TokenAnnotation.class.getCanonicalName());
        termType = typeSystem.getType(TermAnnotation.class.getCanonicalName());

        // Initialize features
        normFeature = tokenType.getFeatureByBaseName("normalForm");
        //labelFeature = constituentMappingType.getFeatureByBaseName("label");
        //identifierFeature = constituentMappingType.getFeatureByBaseName("identifier");
    }
}

