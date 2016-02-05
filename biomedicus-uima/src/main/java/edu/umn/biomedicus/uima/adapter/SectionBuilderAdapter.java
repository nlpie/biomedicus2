package edu.umn.biomedicus.uima.adapter;

import edu.umn.biomedicus.model.text.Section;
import edu.umn.biomedicus.model.text.SectionBuilder;
import edu.umn.biomedicus.type.SectionAnnotation;
import org.apache.uima.jcas.JCas;

/**
 *
 */
class SectionBuilderAdapter implements SectionBuilder {
    private final JCas jCas;

    private final SectionAnnotation sectionAnnotation;

    SectionBuilderAdapter(JCas jCas, SectionAnnotation sectionAnnotation) {
        this.jCas = jCas;
        this.sectionAnnotation = sectionAnnotation;
    }

    @Override
    public SectionBuilder withSectionTitle(String sectionTitle) {
        sectionAnnotation.setSectionTitle(sectionTitle);
        return this;
    }

    @Override
    public SectionBuilder withContentStart(int contentStart) {
        sectionAnnotation.setContentStart(contentStart);
        return this;
    }

    @Override
    public SectionBuilder withLevel(int level) {
        sectionAnnotation.setLevel(level);
        return this;
    }

    @Override
    public SectionBuilder withHasSubsections(boolean hasSubsections) {
        sectionAnnotation.setHasSubsections(hasSubsections);
        return this;
    }

    @Override
    public SectionBuilder withKind(String kind) {
        sectionAnnotation.setKind(kind);
        return this;
    }

    @Override
    public Section build() {
        sectionAnnotation.addToIndexes();
        return new SectionAdapter(jCas, sectionAnnotation);
    }
}
