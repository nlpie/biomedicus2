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

package edu.umn.biomedicus.processing;

import java.io.Serializable;

/**
 * Runs on document text before it is run through some analysis component. Replaces potentially problematic sequences of
 * characters with dummy text that will not cause problems for the analysis component.
 *
 * @since 1.1.0
 */
@FunctionalInterface
public interface Preprocessor extends Serializable {
    /**
     * Processes the text given.
     *
     * @param text text to process
     * @return processed text
     */
    String processText(CharSequence text);
}
