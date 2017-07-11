/*
 * Copyright (c) 2017 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.uima.rtf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.uima.analysis_component.CasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
import org.apache.uima.jcas.tcas.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Annotates table elements from rtf text using the row/cell/nestedcell/nestedrow end annotations.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class TableAnnotator extends CasAnnotator_ImplBase {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory
      .getLogger(TableAnnotator.class);

  @Override
  public void process(CAS aCAS) throws AnalysisEngineProcessException {
    LOGGER.info("Annotating rtf tables.");
    TypeSystem typeSystem = aCAS.getTypeSystem();
    Type paragraphInTableType = typeSystem
        .getType("edu.umn.biomedicus.rtfuima.type.ParagraphInTable");

    CAS systemView = aCAS.getView("SystemView");

    AnnotationIndex<AnnotationFS> paragraphInTableIndex = systemView
        .getAnnotationIndex(paragraphInTableType);

    List<Integer> inTable = new ArrayList<>();
    for (AnnotationFS annotation : paragraphInTableIndex) {
      for (int i = annotation.getBegin(); i <= annotation.getEnd(); i++) {
        inTable.add(i);
      }
    }
    int[] indexes = inTable.stream().mapToInt(i -> i).sorted().distinct()
        .toArray();

    if (indexes.length == 0) {
      return;
    }

    // divide "in table" paragraphs into rows.
    Type rowEndType = typeSystem
        .getType("edu.umn.biomedicus.rtfuima.type.RowEnd");
    Type rowType = typeSystem
        .getType("edu.umn.biomedicus.type.RowAnnotation");

    AnnotationIndex<Annotation> rowEndAnnotationIndex = systemView
        .getAnnotationIndex(rowEndType);
    for (Annotation rowEnd : rowEndAnnotationIndex) {
      int rowEndIndex = rowEnd.getBegin();
      int insert = Arrays.binarySearch(indexes, rowEndIndex);
      if (insert < 0) {
        insert = insert * -1 - 1;
      }
      int end = indexes[insert];
      int ptr = insert;
      while (ptr > 0 && indexes[ptr] - indexes[ptr - 1] == 1) {
        --ptr;
      }
      int begin = indexes[ptr];
      Arrays.fill(indexes, ptr, insert, -2);

      systemView.addFsToIndexes(systemView.createAnnotation(rowType,
          begin, end));
    }

    Type cellEndType = typeSystem
        .getType("edu.umn.biomedicus.rtfuima.type.CellEnd");
    Type cellType = typeSystem
        .getType("edu.umn.biomedicus.type.CellAnnotation");

    Type nestRowEndType = typeSystem
        .getType("edu.umn.biomedicus.rtfuima.type.NestRowEnd");
    Type nestedRowType = typeSystem
        .getType("edu.umn.biomedicus.type.NestedRowAnnotation");

    Type nestedCellEndType = typeSystem
        .getType("edu.umn.biomedicus.rtfuima.type.NestCellEnd");
    Type nestedCellType = typeSystem
        .getType("edu.umn.biomedicus.type.NestedCellAnnotation");

    TableAnnotationDivider tableAnnotationDivider = TableAnnotationDivider
        .in(systemView);
    tableAnnotationDivider.using(cellEndType)
        .divide(rowType)
        .into(cellType)
        .execute();

    tableAnnotationDivider.using(nestRowEndType)
        .divide(cellType)
        .into(nestedRowType)
        .execute();

    tableAnnotationDivider.using(nestedCellEndType)
        .divide(nestedRowType)
        .into(nestedCellType)
        .execute();
  }
}
