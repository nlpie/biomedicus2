package edu.umn.biomedicus.uima.adapter;

import edu.umn.biomedicus.model.text.TextSpan;
import org.apache.uima.jcas.tcas.Annotation;

/**
 * An adapter for text span from UIMA annotations.
 *
 * @author Ben Knoll
 * @since 1.4
 */
class AnnotationTextSpan<T extends Annotation> implements TextSpan {
    /**
     * The annotation itself.
     */
    protected final T annotation;

    AnnotationTextSpan(T annotation) {
        this.annotation = annotation;
    }

    /**
     * Returns the annotation for use by subclasses.
     *
     * @return Annotation object itself.
     */
    protected T getAnnotation() {
        return annotation;
    }

    @Override
    public String getText() {
        return annotation.getCoveredText();
    }

    @Override
    public int getBegin() {
        return annotation.getBegin();
    }

    @Override
    public int getEnd() {
        return annotation.getEnd();
    }
}
