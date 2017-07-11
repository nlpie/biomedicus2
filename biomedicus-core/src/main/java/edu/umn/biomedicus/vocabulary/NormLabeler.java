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

package edu.umn.biomedicus.vocabulary;

import com.google.inject.Inject;
import edu.umn.biomedicus.common.StandardViews;
import edu.umn.biomedicus.common.terms.IndexedTerm;
import edu.umn.biomedicus.common.terms.TermIndex;
import edu.umn.biomedicus.common.types.text.ImmutableNormIndex;
import edu.umn.biomedicus.common.types.text.NormForm;
import edu.umn.biomedicus.common.types.text.NormIndex;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.framework.store.Labeler;
import edu.umn.biomedicus.framework.store.TextView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes the normal form and labels the index of the normal form.
 *
 * @author Ben Knoll
 * @since 1.6.0
 */
public final class NormLabeler implements DocumentProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(NormLabeler.class);

  private final TermIndex wordIndex;

  @Inject
  public NormLabeler(Vocabulary vocabulary) {
    this.wordIndex = vocabulary.getNormsIndex();
  }

  @Override
  public void process(Document document) throws BiomedicusException {
    TextView systemView = StandardViews.getSystemView(document);
    LabelIndex<NormForm> normFormLabelIndex = systemView.getLabelIndex(NormForm.class);
    Labeler<NormIndex> normIndexLabeler = systemView.getLabeler(NormIndex.class);

    LOGGER.info("Labeling norm term index identifiers in a document.");
    for (Label<NormForm> normFormLabel : normFormLabelIndex) {
      String normalForm = normFormLabel.value().normalForm();
      IndexedTerm term = wordIndex.getIndexedTerm(normalForm);
      NormIndex normIndex = ImmutableNormIndex.builder()
          .term(term)
          .build();
      normIndexLabeler.value(normIndex).label(normFormLabel);
    }
  }
}
