package edu.umn.biomedicus.model.text;

/**
 * A section in a document.
 *
 * @since 1.4
 */
public interface Section extends TextSpan {
    /**
     * Returns the title of the section.
     *
     * @return section title.
     */
    String getSectionTitle();

    /**
     * Returns the content start of the section.
     *
     * @return content start.
     */
    int contentStart();

    /**
     * Returns the level of the section, ex. 0 for a section 1 for a subsection.
     *
     * @return section level.
     */
    int getLevel();

    /**
     * Returns whether this section has subsections.
     *
     * @return
     */
    boolean hasSubsections();

    String getKind();

    Iterable<Sentence> getSentences();
}
