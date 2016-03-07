package edu.umn.biomedicus.uima.adapter;

import edu.umn.biomedicus.common.text.Section;
import edu.umn.biomedicus.common.text.Sentence;
import edu.umn.biomedicus.type.SectionAnnotation;
import edu.umn.biomedicus.type.SentenceAnnotation;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * Implementation of {@link Section} of UIMA.
 *
 * @author Ben Knoll
 * @since 1.4
 */
class SectionAdapter extends AnnotationAdapter<SectionAnnotation> implements Section {
    /**
     * Protected constructor for AnnotationAdapter. Initializes the two fields, {@code jCas} and {@code annotation}.
     *
     * @param jCas       the {@link JCas} document the annotation is stored in.
     * @param annotation the {@link Annotation} itself.
     */
    SectionAdapter(JCas jCas, SectionAnnotation annotation) {
        super(jCas, annotation);
    }

    @Override
    public String getSectionTitle() {
        return getAnnotation().getSectionTitle();
    }

    @Override
    public int contentStart() {
        return getAnnotation().getContentStart();
    }

    @Override
    public int getLevel() {
        return getAnnotation().getLevel();
    }

    @Override
    public boolean hasSubsections() {
        return getAnnotation().getHasSubsections();
    }

    @Override
    public String getKind() {
        return getAnnotation().getKind();
    }

    @Override
    public Iterable<Sentence> getSentences() {
        return () -> getCoveredStream(SentenceAnnotation.type, UimaAdapters::sentenceAdapter).iterator();
    }
}
