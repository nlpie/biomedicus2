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

package edu.umn.biomedicus.utilities;

import edu.umn.biomedicus.annotations.ProcessorSetting;
import edu.umn.biomedicus.common.StandardViews;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.framework.Searcher;
import edu.umn.biomedicus.framework.SearchExpr;
import edu.umn.biomedicus.framework.SearchExprFactory;
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.TextView;
import edu.umn.nlpengine.Label;
import edu.umn.nlpengine.Span;
import javax.inject.Inject;

/**
 *
 */
public class SearcherPrinter implements DocumentProcessor {

  private final SearchExpr searchExpr;

  @Inject
  public SearcherPrinter(
      SearchExprFactory searchExprFactory,
      @ProcessorSetting("searchPattern") String searchPattern
  ) {
    searchExpr = searchExprFactory.parse(searchPattern);
  }

  @Override
  public void process(Document document) throws BiomedicusException {
    TextView systemView = StandardViews.getSystemView(document);

    Searcher searcher = searchExpr.createSearcher(systemView);

    while (true) {
      boolean found = searcher.search();
      if (!found) {
        break;
      }
      System.out
          .println("Matching Text: " + searcher.getSpan().get().coveredString(systemView.getText()));

      for (String group : searcher.getGroupNames()) {
        System.out.println("\tGroup Name: " + group);

        if (searcher.getSpan(group).isPresent()) {
          Span span = searcher.getSpan(group).get();
          System.out.println("\t\tCovered Text: " + span.coveredString(systemView.getText()));
        }

        if (searcher.getLabel(group).isPresent()) {
          Label label = searcher.getLabel(group).get();
          System.out.println("\t\tStored Label: " + label.toString());
        }
      }
    }
  }
}
