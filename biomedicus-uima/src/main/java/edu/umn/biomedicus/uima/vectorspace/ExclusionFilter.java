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

import edu.umn.biomedicus.common.text.Span;
import org.apache.uima.jcas.JCas;

import java.util.stream.Stream;

/**
 * Interface for excluding spans in text from term generation in the term vector space model.
 *
 * @since 1.3.0
 */
public interface ExclusionFilter {
    /**
     * Gets the excluded spans from a cas.
     *
     * @param cas the cas to find excluded regions in.
     * @return the excluded spans.
     */
    Stream<Span> excludedSpans(JCas cas);
}
