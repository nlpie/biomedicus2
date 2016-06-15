/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.uima.stopword;

import edu.umn.biomedicus.stopwords.Stopwords;
import edu.umn.biomedicus.stopwords.StopwordsList;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 */
public class StopwordsListResource implements StopwordsResource, SharedResourceObject {
    private StopwordsList stopwordsList;
    @Override
    public void load(DataResource dataResource) throws ResourceInitializationException {
        InputStream inputStream;
        try {
            inputStream = dataResource.getInputStream();
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
        try {
            stopwordsList = StopwordsList.loadFromInputStream(inputStream);
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        }
    }

    @Override
    public Stopwords getStopwords() {
        return stopwordsList;
    }
}
