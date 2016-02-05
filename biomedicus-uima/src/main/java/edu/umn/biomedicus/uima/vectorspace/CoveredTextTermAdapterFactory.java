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

package edu.umn.biomedicus.uima.vectorspace;

/**
 * Responsible for creating {@link CoveredTextTermAdapter} instances.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
class CoveredTextTermAdapterFactory implements TermAdapterFactory {
    /**
     * The index of the annotation type name in the params.
     */
    public static final int INDEX_ANNOTATION_TYPE_NAME = 0;

    @Override
    public TermAdapter create(String[] params) {
        return new CoveredTextTermAdapter(params[INDEX_ANNOTATION_TYPE_NAME]);
    }
}
