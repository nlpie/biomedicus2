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

import edu.umn.biomedicus.rtfuima.type.*;
import edu.umn.biomedicus.type.CellAnnotation;
import edu.umn.biomedicus.type.NestedCellAnnotation;
import edu.umn.biomedicus.type.NestedRowAnnotation;
import edu.umn.biomedicus.type.RowAnnotation;
import edu.umn.biomedicus.uima.common.Views;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Annotates table elements from rtf text using the row/cell/nestedcell/nestedrow end annotations.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class TableAnnotator extends JCasAnnotator_ImplBase {
    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TableAnnotator.class);

    @Override
    public void process(JCas aJCas) throws AnalysisEngineProcessException {
        LOGGER.info("Annotating rtf tables.");
        JCas systemView;
        try {
            systemView = aJCas.getView(Views.SYSTEM_VIEW);
        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }
        AnnotationIndex<Annotation> paragraphInTableIndex = systemView.getAnnotationIndex(ParagraphInTable.type);

        List<Integer> inTable = new ArrayList<>();
        for (Annotation annotation : paragraphInTableIndex) {
            for (int i = annotation.getBegin(); i <= annotation.getEnd(); i++) {
                inTable.add(i);
            }
        }
        int[] indexes = inTable.stream().mapToInt(i -> i).sorted().distinct().toArray();

        if (indexes.length == 0) {
            return;
        }

        // divide "in table" paragraphs into rows.
        AnnotationIndex<Annotation> rowEndAnnotationIndex = systemView.getAnnotationIndex(RowEnd.type);
        for (Annotation rowEnd : rowEndAnnotationIndex) {
            int rowEndIndex = rowEnd.getBegin();
            int insert = Arrays.binarySearch(indexes, rowEndIndex);
            if (insert < 0) {
                insert = insert * -1 - 1;
            }
            int end = indexes[insert];
            int ptr = insert;
            while (ptr > 0 && indexes[ptr] - indexes[ptr - 1] == 1){
               --ptr;
            }
            int begin = indexes[ptr];
            Arrays.fill(indexes, ptr, insert, -2);
            new RowAnnotation(systemView, begin, end).addToIndexes();
        }

        TableAnnotationDivider tableAnnotationDivider = TableAnnotationDivider.in(systemView);
        tableAnnotationDivider.using(CellEnd.type)
                .divide(RowAnnotation.type)
                .into(CellAnnotation.type)
                .execute();

        tableAnnotationDivider.using(NestRowEnd.type)
                .divide(CellAnnotation.type)
                .into(NestedRowAnnotation.type)
                .execute();

        tableAnnotationDivider.using(NestCellEnd.type)
                .divide(NestedRowAnnotation.type)
                .into(NestedCellAnnotation.type)
                .execute();
    }


}
