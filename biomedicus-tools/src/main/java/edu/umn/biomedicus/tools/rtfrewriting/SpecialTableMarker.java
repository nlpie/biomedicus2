package edu.umn.biomedicus.tools.rtfrewriting;

/**
 * Inserts a marker at the end of a special table (color table, stylesheet, etc) in rtf, along with the number of
 * entries that table contains.
 *
 * @author Ben Knoll
 * @since 1.4.0
 */
public class SpecialTableMarker {
    /**
     * The document to insert a marker into the table for.
     */
    private final String document;

    /**
     * The character to insert as a marker.
     */
    private final String insertionString;

    /**
     * The control word that indicates the start of the table.
     */
    private final String controlWord;

    /**
     * Creates a {@code SpecialTableMarker}, which will insert a character and a number of the entries that a table
     * contains.
     *  @param document The document to insert a marker into the table for.
     * @param insertionString The character to insert as a marker.
     * @param controlWord The control word that indicates the start of the table.
     */
    public SpecialTableMarker(String document, String insertionString, String controlWord) {
        this.document = document;
        this.insertionString = insertionString;
        this.controlWord = controlWord;
    }

    /**
     * Performs the insertion.
     *
     * @return document with the character and count of existing entries inserted at the end of the table.
     */
    public String insertInTable() throws SpecialTableNotFoundException {
        int stack = 0;

        int characterIndex = document.indexOf(controlWord);

        if (characterIndex == -1) {
            throw new SpecialTableNotFoundException();
        }


        int tableIndex = 0;
        char character = document.charAt(characterIndex);
        while (character != '}' || stack > 0) {
            if (character == '{') {
                stack++;
            }
            if (character == '}') {
                stack--;
            }
            if (character == ';') {
                tableIndex++;
            }
            characterIndex++;
            character = document.charAt(characterIndex);
        }
        return document.substring(0, characterIndex) + insertionString + " " + tableIndex + document.substring(characterIndex);
    }
}
