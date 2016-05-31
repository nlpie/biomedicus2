package edu.umn.biomedicus.acronym;

import com.google.inject.Inject;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.Labeler;
import edu.umn.biomedicus.common.labels.Labels;
import edu.umn.biomedicus.common.simple.SimpleToken;
import edu.umn.biomedicus.common.text.Acronym;
import edu.umn.biomedicus.common.text.AcronymExpansion;
import edu.umn.biomedicus.common.text.Document;
import edu.umn.biomedicus.common.text.Token;
import edu.umn.biomedicus.common.utilities.Patterns;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Will normalize/expand/disambiguate any acronyms or abbreviations that have been tagged by AcronymDetector.
 * Needs an AcronymModel to do this
 *
 * @author Greg Finley
 * @since 1.5.0
 */
@DocumentScoped
class AcronymExpander implements DocumentProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AcronymModel.class);

    private final AcronymModel model;

    private final Document document;

    private final Labels<Acronym> acronyms;

    private final Labeler<AcronymExpansion> acronymExpansionLabeler;

    @Inject
    public AcronymExpander(@Setting("acronym.model") AcronymModel model,
                           Document document,
                           Labels<Acronym> acronyms,
                           Labeler<AcronymExpansion> acronymExpansionLabeler) {
        this.model = model;
        this.document = document;
        this.acronyms = acronyms;
        this.acronymExpansionLabeler = acronymExpansionLabeler;
    }

    @Override
    public void process() throws BiomedicusException {
        LOGGER.info("Expanding acronyms and abbreviations");

        List<Token> allTokens = new ArrayList<>();
        for (Token token : document.getTokens()) {
            allTokens.add(token);
        }

        Token prevToken = null;
        Optional<Label<Acronym>> prevOptionalAcronym = Optional.empty();
        Token prevPrevToken = null;
        Optional<Label<Acronym>> prevPrevOptionalAcronym = Optional.empty();


        for (Token token : document.getTokens()) {

            Optional<Label<Acronym>> optionalAcronym = acronyms.withSpan(token).getOptionally();
            if (optionalAcronym.isPresent() && Patterns.ALPHABETIC_WORD.matcher(token.getText()).matches()) {
                boolean solved = false;

                // First see if these two or three tokens form an obvious abbreviation
                if (prevToken != null && prevOptionalAcronym.isPresent()) {
                    if (prevPrevToken != null && prevPrevOptionalAcronym.isPresent()) {
                        Token threeWordToken = new SimpleToken(document.getText(), prevPrevToken.getBegin(), token.getEnd());
                        solved = trySolve(allTokens, threeWordToken);
                    }
                    if (!solved) {
                        Token twoWordToken = new SimpleToken(document.getText(), prevToken.getBegin(), token.getEnd());
                        solved = trySolve(allTokens, twoWordToken);
                    }
                }

                if (!solved) {
                    String bestSense = model.findBestSense(allTokens, token);
                    if (!token.getText().equalsIgnoreCase(bestSense)) {
                        acronymExpansionLabeler.value(new AcronymExpansion(bestSense))
                                .label(token);
                    }
                }
            }
            prevPrevToken = prevToken;
            prevPrevOptionalAcronym = prevOptionalAcronym;
            prevToken = token;
            prevOptionalAcronym = optionalAcronym;
        }
    }

    private boolean trySolve(List<Token> allTokens, Token token) throws BiomedicusException {
        if (model.hasAcronym(token)) {
            String sense = model.findBestSense(allTokens, token);
            if (!token.getText().equalsIgnoreCase(sense)) {
                acronymExpansionLabeler.value(new AcronymExpansion(sense)).label(token);
            }
            return true;
        }
        return false;
    }
}
