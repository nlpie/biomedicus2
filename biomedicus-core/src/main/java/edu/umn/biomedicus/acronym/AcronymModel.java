package edu.umn.biomedicus.acronym;

import com.google.inject.ProvidedBy;
import edu.umn.biomedicus.model.text.Token;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * Describes any generic acronym detection and normalization model
 * The essential capabilities of the model are
 * 1) to determine if something is an acronym, and
 * 2) to expand a given acronym Token given its context Tokens
 * Models are generally serializable as well so they can be trained ahead of time
 *
 * @author Greg Finley
 */
@ProvidedBy(AcronymModelLoader.class)
public interface AcronymModel {

    boolean hasAcronym(Token token);

    String findBestSense(List<Token> allTokens, Token token);

    /**
     * For deidentification: remove a single word from the model entirely
     *
     * @param word the word to remove
     */
    void removeWord(String word);

    /**
     * Remove all words except a determined set from the model
     *
     * @param words a set of the words to keep
     */
    void removeWordsExcept(Set<String> words);
}
