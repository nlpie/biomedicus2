/*
 * Copyright (c) 2015 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.uima.rtf;

import edu.umn.biomedicus.rtf.reader.KeywordAction;
import edu.umn.biomedicus.rtf.reader.OutputDestination;
import edu.umn.biomedicus.rtf.reader.State;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Responsible for writing rtf data to a CAS view.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
class CasOutputDestination implements OutputDestination {
    /**
     * The view to write to.
     */
    private final JCas destinationView;

    /**
     * The builder for the document.
     */
    private final StringBuilder sofaBuilder;

    /**
     * Completed annotations.
     */
    private final Collection<AnnotationFS> completedAnnotations;

    /**
     * The annotation property watchers, which are responsible for watching property changes and creating annotations.
     */
    private final Collection<AnnotationPropertyWatcher> annotationPropertyWatchers;

    /**
     * The annotation type to create for control words.
     */
    private final Map<String, Type> annotationTypeForControlWord;

    /**
     * The name of the output destination.
     */
    private final String name;

    /**
     * Default constructor, initializes all fields.
     *
     * @param destinationView              The view to write to.
     * @param casMappings                  The property cas mappings
     * @param annotationTypeForControlWord The annotation type to create for control words.
     */
    CasOutputDestination(JCas destinationView,
                         List<PropertyCasMapping> casMappings,
                         Map<String, Type> annotationTypeForControlWord,
                         String name) {
        this.destinationView = destinationView;
        this.sofaBuilder = new StringBuilder();
        this.completedAnnotations = new ArrayList<>();
        this.annotationPropertyWatchers = casMappings.stream()
                .map(AnnotationPropertyWatcher::new)
                .collect(Collectors.toList());
        this.annotationTypeForControlWord = annotationTypeForControlWord;
        this.name = name;
    }

    @Override
    public int writeChar(char ch, State state) {
        for (AnnotationPropertyWatcher propertyWatcher : annotationPropertyWatchers) {
            AnnotationFS newAnnotation = propertyWatcher.handleChanges(sofaBuilder.length(), state, destinationView);
            if (newAnnotation != null) {
                completedAnnotations.add(newAnnotation);
            }
        }

        if (state.getPropertyValue("CharacterFormatting", "Hidden") == 0) {
            sofaBuilder.append(ch);
            return sofaBuilder.length() - 1;
        } else {
            return -1;
        }
    }

    @Override
    public void finishDestination() {
        destinationView.setDocumentText(sofaBuilder.toString());
        completedAnnotations.forEach(destinationView::addFsToIndexes);
    }

    @Override
    public void controlWordEncountered(KeywordAction keywordAction) {
        AnnotationFS annotation;
        int currentTextIndex = sofaBuilder.length();
        String controlWord = keywordAction.getControlWord();

        Type type;
        if (annotationTypeForControlWord.containsKey(controlWord)) {
            type = annotationTypeForControlWord.get(controlWord);
        } else {
            return;
        }
        annotation = destinationView.getCas().createAnnotation(type, currentTextIndex, currentTextIndex);
        Feature paramFeature = type.getFeatureByBaseName("param");
        if (keywordAction.hasParameter()) {
            annotation.setIntValue(paramFeature, keywordAction.getParameter());
        }
        Feature indexFeature = type.getFeatureByBaseName("index");
        annotation.setIntValue(indexFeature, keywordAction.getBegin());
        Feature knownFeature = type.getFeatureByBaseName("known");
        annotation.setBooleanValue(knownFeature, true);

        destinationView.addFsToIndexes(annotation);
    }

    @Override
    public String getName() {
        return name;
    }
}
