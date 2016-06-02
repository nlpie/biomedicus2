package edu.umn.biomedicus.uima.labels;

import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class DefaultLabelAdapter<T, U extends Annotation> implements LabelAdapter<T> {
    private final Class<U> jCasClass;

    private final BiConsumer<T, U> forwardAdapter;

    private final Function<U, T> reverseAdapter;

    private final Constructor<U> constructor;

    private DefaultLabelAdapter(Class<U> jCasClass,
                                BiConsumer<T, U> forwardAdapter,
                                Function<U, T> reverseAdapter) {
        this.jCasClass = jCasClass;
        this.forwardAdapter = forwardAdapter;
        this.reverseAdapter = reverseAdapter;
        try {
            constructor = jCasClass.getConstructor(JCas.class, Integer.TYPE, Integer.TYPE);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("This method should always be there for a JCas cover class");
        }
    }

    public static <T, U extends Annotation> LabelAdapter<T> create(Class<U> jCasClass,
                                                                   BiConsumer<T, U> forwardAdapter,
                                                                   Function<U, T> featuresAdapter) {
        return new DefaultLabelAdapter<>(jCasClass, forwardAdapter, featuresAdapter);
    }

    @Override
    public Class<? extends Annotation> getjCasClass() {
        return jCasClass;
    }

    @Override
    public void createLabel(JCas jCas, Label<T> label) throws BiomedicusException {
        try {
            U annotation = constructor.newInstance(jCas, label.toSpan().getBegin(), label.toSpan().getEnd());
            forwardAdapter.accept(label.value(), annotation);
            annotation.addToIndexes();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new BiomedicusException("Could not create annotation for label", e);
        }
    }

    @Override
    public Label<T> adaptAnnotation(Annotation annotation) {
        if (!jCasClass.isInstance(annotation)) {
            throw new IllegalArgumentException("Annotation is invalid type");
        }
        T label = reverseAdapter.apply(jCasClass.cast(annotation));
        return new Label<>(new Span(annotation.getBegin(), annotation.getEnd()), label);
    }
}
