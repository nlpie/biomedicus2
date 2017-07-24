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

package edu.umn.biomedicus.stopwords;

import com.google.inject.Inject;
import edu.umn.biomedicus.common.StandardViews;
import edu.umn.biomedicus.common.types.semantics.StopWord;
import edu.umn.biomedicus.common.types.text.ParseToken;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.framework.DocumentProcessor;
import edu.umn.biomedicus.framework.store.Document;
import edu.umn.biomedicus.framework.store.Label;
import edu.umn.biomedicus.framework.store.LabelIndex;
import edu.umn.biomedicus.framework.store.TextView;
import edu.umn.biomedicus.framework.store.ValueLabeler;

public class StopwordsProcessor implements DocumentProcessor {

  private final Stopwords stopwords;

  @Inject
  public StopwordsProcessor(Stopwords stopwords) {
    this.stopwords = stopwords;
  }

  @Override
  public void process(Document document) throws BiomedicusException {
    TextView systemView = StandardViews.getSystemView(document);

    LabelIndex<ParseToken> parseTokenLabelIndex = systemView.getLabelIndex(ParseToken.class);
    ValueLabeler stopWordsLabeler = systemView.getLabeler(StopWord.class).value(new StopWord());

    for (Label<ParseToken> parseTokenLabel : parseTokenLabelIndex) {
      if (stopwords.isStopWord(parseTokenLabel.value())) {
        stopWordsLabeler.label(parseTokenLabel);
      }
    }
  }
}
