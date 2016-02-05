package edu.umn.biomedicus.model.text;

/**
 * Interface for a builder for sections.
 *
 * @since 1.4
 */
public interface SectionBuilder {
    SectionBuilder withSectionTitle(String sectionTitle);

    SectionBuilder withContentStart(int contentStart);

    SectionBuilder withLevel(int level);

    SectionBuilder withHasSubsections(boolean hasSubsections);

    SectionBuilder withKind(String kind);

    Section build();
}
