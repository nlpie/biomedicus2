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

package edu.umn.biomedicus.uima.common;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * AnnotationUtils is a set of utility functions that simplify working with annotation types, the annotation index, and
 * collection of annotations.
 * @deprecated this utility class should be refactored into a more OOP design.
 */
@Deprecated
public class AnnotationUtils {

    public static String getDocumentId(CAS aCAS, Type metaDataType) {
        String documentId = null;
        CAS mdCAS = aCAS.getView("MetaData");
        AnnotationIndex<AnnotationFS> ai = mdCAS.getAnnotationIndex(metaDataType);
        Iterator<AnnotationFS> annIt = ai.iterator();
        while (annIt.hasNext()) {
            AnnotationFS md = annIt.next();
            Feature docIdFeature = metaDataType.getFeatureByBaseName("documentId");
            documentId = md.getStringValue(docIdFeature);
        }
        return documentId;
    }

    @SuppressWarnings("unchecked")
    public static <T extends AnnotationFS> AnnotationIndex<T> getAnnotations(CAS aCAS, Class<T> annotationClass) {
        TypeSystem typeSystem = aCAS.getTypeSystem();
        Type annotationType = typeSystem.getType(annotationClass.getCanonicalName());
        return (AnnotationIndex<T>) getAnnotations(aCAS, annotationType);
    }

    public static AnnotationIndex<? extends AnnotationFS> getAnnotations(CAS aCAS, Type annotationType) {
        return aCAS.getAnnotationIndex(annotationType);
    }

    public static void removeCoveredAnnotations(CAS aCAS, AnnotationFS containerAnnotation, Type annotationType) {
        int containerBegin = containerAnnotation.getBegin();
        int containerEnd = containerAnnotation.getEnd();

        List<AnnotationFS> removalList = new ArrayList<>();
        AnnotationIndex<AnnotationFS> subTypeIndex = aCAS.getAnnotationIndex(annotationType);
        for (AnnotationFS subAnnotation : subTypeIndex) {
            int subBegin = subAnnotation.getBegin();
            int subEnd = subAnnotation.getEnd();
            if (subBegin >= containerBegin && subEnd <= containerEnd) {
                // add to list first because cannot change index while iterating over it
                removalList.add(subAnnotation);
            }
        }

        // remove here because not iterating over index
        for (AnnotationFS oldAnnotation : removalList) {
            aCAS.removeFsFromIndexes(oldAnnotation);
        }

    }

    /**
     * The getCoveredAnnotations method retrieves from the annotation index all the AnnotationFS instances that occur
     * within the span of the specified containerAnnotation.
     *
     * @param aCAS                The CAS object containing both covering and contained annotations.
     * @param containerAnnotation The <tt>AnnotationFS</tt> instance that covers, or contains, the annotationType that
     *                            is returned
     * @param annotationType      The <tt>Type</tt> of the AnnotationFS that appears within the span of the covering
     *                            annotation.
     * @return A <tt>List&lt;AnnotationFS&gt; of annotations of the specified annotation Type that appear within the
     * span of the covering annotation.</tt>
     */
    public static List<AnnotationFS> getCoveredAnnotations(CAS aCAS, AnnotationFS containerAnnotation, Type annotationType) {

        List<AnnotationFS> subannotationList = new ArrayList<>();
        int containerBegin = containerAnnotation.getBegin();
        int containerEnd = containerAnnotation.getEnd();

        AnnotationIndex<AnnotationFS> subTypeIndex = aCAS.getAnnotationIndex(annotationType);
        for (AnnotationFS subAnnotation : subTypeIndex) {
            int subBegin = subAnnotation.getBegin();
            int subEnd = subAnnotation.getEnd();
            if (subBegin >= containerBegin && subEnd <= containerEnd) {
                subannotationList.add(subAnnotation);
            }
        }
        return subannotationList;

    }

    public static List<AnnotationFS> getCoveredAnnotations(AnnotationFS containerAnnotation, Class<? extends AnnotationFS> annotationClass) {
        CAS aCAS = containerAnnotation.getCAS();
        Type annotationType = aCAS.getTypeSystem().getType(annotationClass.getCanonicalName());
        return getCoveredAnnotations(aCAS, containerAnnotation, annotationType);
    }

    public static String[] annotationListToStringArray(List<AnnotationFS> annotationFSList) {
        return annotationListToStringArray(annotationFSList, null);
    }

    /**
     * The annotationListToStringArray method accepts a <tt>List</tt> of AnnotationFS instances and returns a
     * <tt>String[]</tt> of the document text spans that are covered by each annotation. The text spans are in order
     * matching the order of the annotation list provided as the parameter.
     *
     * @param annotationFSList A <tt>List</tt> containing AnnotationFS instances.
     * @return A <tt>String[]</tt> of each of the coveredText spans of the annotations
     */
    public static String[] annotationListToStringArray(List<AnnotationFS> annotationFSList, Feature stopwordFeature) {
        ArrayList<String> tokenStrings = new ArrayList<>();
        //String[] annotationTextArray = new String[annotationFSList.size()];
        for (int i = 0; i < annotationFSList.size(); i++) {
            String tokenText = annotationFSList.get(i).getCoveredText();
            AnnotationFS ann = annotationFSList.get(i);
            if (stopwordFeature != null) {
                boolean isStopword = ann.getBooleanValue(stopwordFeature);
                if (isStopword) continue;
            }

            tokenStrings.add(ann.getCoveredText());
        }

        String[] results = new String[tokenStrings.size()];
        return tokenStrings.toArray(results);
    }

    /**
     * The getCoveringAnnotations method identifies all of the annotations of a specific type that have <tt>begin</tt>
     * and <tt>end</tt> values that contain the specified childAnnotation. Ends are inclusive (equal childAnnotation and
     * coveringAnnotation boundaries are included).
     *
     * @param aCAS            The CAS object
     * @param containerType   The <tt>Type</tt> of the annotation that the containing annotation must be.
     * @param childAnnotation The <tt>AnnotationFS</tt> that must appear within the covering annotation.
     * @return A <tt>List&lt;AnnotationFS&gt;</tt> of all annotations of <tt>containerType</tt> that conatain the
     * specified childAnnotation.
     */
    public static List<AnnotationFS> getCoveringAnnotations(CAS aCAS, Type containerType, AnnotationFS childAnnotation) {

        ArrayList<AnnotationFS> annotationCollector = new ArrayList<>();
        AnnotationFS container = null;
        int childBegin = childAnnotation.getBegin();
        int childEnd = childAnnotation.getEnd();

        for (AnnotationFS containerCandidateAnnotation : aCAS.getAnnotationIndex(containerType)) {
            int containerBegin = containerCandidateAnnotation.getBegin();
            int containerEnd = containerCandidateAnnotation.getEnd();
            if (containerBegin <= childBegin && containerEnd >= childEnd) {
                // There are no overlapping annotations, so can return when first one is found.
                annotationCollector.add(containerCandidateAnnotation);
            }
        }

        return annotationCollector;

    }

    public static AnnotationFS createAnnotation(CAS view, String annotationTypeName, int begin, int end) {
        Type annotationType = view.getTypeSystem().getType(annotationTypeName);
        AnnotationFS annot = view.createAnnotation(annotationType, begin, end);
        view.addFsToIndexes(annot);
        return annot;
    }

    public static AnnotationFS createAnnotation(CAS view, Class<? extends AnnotationFS> annotationClass, int begin, int end) {
        String annotationTypeName = annotationClass.getCanonicalName();
        return createAnnotation(view, annotationTypeName, begin, end);
    }

    public static void setAnnotationFeatureStringValue(AnnotationFS annot, String featureName, String value) {
        Feature feature = annot.getType().getFeatureByBaseName(featureName);
        annot.setStringValue(feature, value);
    }

}
