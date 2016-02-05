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

import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.jcas.JCas;

import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Adapts a CAS object into a stream of terms.
 *
 * @since 1.3.0
 */
public interface TermAdapter {
    /**
     * Adapts the cas into a stream of terms.
     *
     * @param cas cas view.
     * @param exclusionTest a test which determines which true annotations should be included, and which false
     *                      annotations should be excluded.
     * @return stream of string terms.
     */
    Stream<String> terms(JCas cas, Predicate<AnnotationFS> exclusionTest);
}
