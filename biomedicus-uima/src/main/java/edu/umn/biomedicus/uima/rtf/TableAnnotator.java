/*
 * Copyright (c) 2018 Regents of the University of Minnesota.
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

import edu.umn.biomedicus.common.TextIdentifiers;
import java.util.ArrayList;
import java.util.Collections;
import org.apache.uima.analysis_component.CasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.cas.text.AnnotationIndex;
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
  private static final Logger LOGGER = LoggerFactory.getLogger(TableAnnotator.class);

  @Override
  public void process(CAS aCAS) throws AnalysisEngineProcessException {
    LOGGER.debug("Annotating rtf tables.");
    CAS systemView = aCAS.getView(TextIdentifiers.SYSTEM);

    TypeSystem typeSystem = aCAS.getTypeSystem();
    Type intblType = typeSystem
        .getType("edu.umn.biomedicus.rtfuima.type.ParagraphInTable");
    Type rowEndType = typeSystem
        .getType("edu.umn.biomedicus.rtfuima.type.RowEnd");
    Type rowType = typeSystem
        .getType("edu.umn.biomedicus.type.RowAnnotation");

    ArrayList<Integer> intblBegins = new ArrayList<>();
    AnnotationIndex<AnnotationFS> intblIndex = systemView.getAnnotationIndex(intblType);
    for (AnnotationFS annotationFS : intblIndex) {
      intblBegins.add(annotationFS.getBegin());
    }

    ArrayList<Integer> rowEnds = new ArrayList<>();
    AnnotationIndex<AnnotationFS> rowEndIndex = systemView.getAnnotationIndex(rowEndType);
    for (AnnotationFS rowEndAnnotation : rowEndIndex) {
      rowEnds.add(rowEndAnnotation.getBegin());
    }

    int last = 0;
    for (Integer intblBegin : intblBegins) {
      if (intblBegin < last) {
        continue;
      }
      int insert = Collections.binarySearch(rowEnds, intblBegin);
      if (insert < 0) {
        insert = insert * -1 - 1;
        if (insert == rowEnds.size()) {
          LOGGER.warn("Rtf intbl paragraph after the last row end.");
          continue;
        }

        int end = rowEnds.get(insert);
        systemView.addFsToIndexes(systemView.createAnnotation(rowType, intblBegin, end));
      }
    }

    Type cellEndType = typeSystem.getType("edu.umn.biomedicus.rtfuima.type.CellEnd");
    Type cellType = typeSystem.getType("edu.umn.biomedicus.type.CellAnnotation");

    Type nestRowEndType = typeSystem
        .getType("edu.umn.biomedicus.rtfuima.type.NestRowEnd");
    Type nestedRowType = typeSystem
        .getType("edu.umn.biomedicus.type.NestedRowAnnotation");
    Type nestedCellEndType = typeSystem
        .getType("edu.umn.biomedicus.rtfuima.type.NestCellEnd");
    Type nestedCellType = typeSystem
        .getType("edu.umn.biomedicus.type.NestedCellAnnotation");

    TableAnnotationDivider tableAnnotationDivider = TableAnnotationDivider.in(systemView);
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
