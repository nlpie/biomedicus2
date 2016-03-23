package edu.umn.biomedicus.uima.adapter;

import edu.umn.biomedicus.common.semantics.SubstanceUsageBuilder;
import edu.umn.biomedicus.common.text.Span;
import edu.umn.biomedicus.type.*;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.ArrayList;
import java.util.List;

/**
 * Uima builder for substance usage annotations.
 *
 * @author Ben Knoll
 * @since 1.4
 */
class UimaSubstanceUsageBuilder implements SubstanceUsageBuilder {
    private final JCas jCas;

    private final SubstanceUsageAnnotation substanceUsageAnnotation;

    private final List<Annotation> amounts;

    private final List<Annotation> frequencies;

    private final List<Annotation> types;

    private final List<Annotation> statuses;

    private final List<Annotation> temporals;

    private final List<Annotation> methods;

    public UimaSubstanceUsageBuilder(JCas jCas, SubstanceUsageAnnotation substanceUsageAnnotation) {
        this.jCas = jCas;
        this.substanceUsageAnnotation = substanceUsageAnnotation;
        amounts = new ArrayList<>();
        frequencies = new ArrayList<>();
        types = new ArrayList<>();
        statuses = new ArrayList<>();
        temporals = new ArrayList<>();
        methods = new ArrayList<>();
    }

    private static Annotation init(Annotation annotation, Span span) {
        annotation.setBegin(span.getBegin());
        annotation.setEnd(span.getEnd());
        annotation.addToIndexes();
        return annotation;
    }

    @Override
    public void addAmount(Span span) {
        amounts.add(init(new SubstanceUsageAmount(jCas), span));
    }

    @Override
    public void addFrequency(Span span) {
        frequencies.add(init(new SubstanceUsageFrequency(jCas), span));
    }

    @Override
    public void addType(Span span) {
        types.add(init(new SubstanceUsageType(jCas), span));
    }

    @Override
    public void addStatus(Span span) {
        statuses.add(init(new SubstanceUsageStatus(jCas), span));
    }

    @Override
    public void addTemporal(Span span) {
        temporals.add(init(new SubstanceUsageTemporal(jCas), span));
    }

    @Override
    public void addMethod(Span span) {
        methods.add(init(new SubstanceUsageMethod(jCas), span));
    }

    private FSArray toFsArray(List<Annotation> annotations) {
        FSArray array = new FSArray(jCas, annotations.size());

        for (int i = 0; i < annotations.size(); i++) {
            array.set(i, annotations.get(i));
        }

        array.addToIndexes();

        return array;
    }

    @Override
    public void build() {
        substanceUsageAnnotation.setAmounts(toFsArray(amounts));
        substanceUsageAnnotation.setFrequencies(toFsArray(frequencies));
        substanceUsageAnnotation.setFrequencies(toFsArray(types));
        substanceUsageAnnotation.setStatuses(toFsArray(statuses));
        substanceUsageAnnotation.setTemporal(toFsArray(temporals));
        substanceUsageAnnotation.setMethods(toFsArray(methods));

        substanceUsageAnnotation.addToIndexes();
    }
}
