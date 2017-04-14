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
import edu.umn.biomedicus.application.DocumentProcessor;
import edu.umn.biomedicus.common.labels.Label;
import edu.umn.biomedicus.common.labels.Search;
import edu.umn.biomedicus.common.labels.Searcher;
import edu.umn.biomedicus.common.labels.SearcherFactory;
import edu.umn.biomedicus.common.types.text.Document;
import edu.umn.biomedicus.common.types.text.Span;
import edu.umn.biomedicus.exc.BiomedicusException;

import javax.inject.Inject;

/**
 *
 */
public class SearcherPrinter implements DocumentProcessor {

    private final Searcher searcher;
    private final Document document;

    @Inject
    public SearcherPrinter(SearcherFactory searcherFactory,
                           @ProcessorSetting("searchPattern") String searchPattern,
                           Document document) {

        searcher = searcherFactory.searcher(searchPattern);
        this.document = document;
    }

    @Override
    public void process() throws BiomedicusException {
        Search search = searcher.createSearcher(document);

        while (true) {
            boolean found = search.search();
            if (!found) {
                break;
            }
            System.out.println("Matching Text: " + search.getSpan().get().getCovered(document.getText()));

            for (String group : search.getGroups()) {
                System.out.println("\tGroup Name: " + group);

                if (search.getSpan(group).isPresent()) {
                    Span span = search.getSpan(group).get();
                    System.out.println("\t\tCovered Text: " + span.getCovered(document.getText()));
                }

                if (search.getLabel(group).isPresent()) {
                    Label<?> label = search.getLabel(group).get();
                    System.out.println("\t\tStored Label: " + label.getValue().toString());
                }
            }
        }
    }
}
