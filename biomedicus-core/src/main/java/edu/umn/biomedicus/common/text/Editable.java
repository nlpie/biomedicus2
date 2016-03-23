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

package edu.umn.biomedicus.common.text;

/**
 * Represents an object that requires a transaction lifecycle, for example, UIMA objects being removed from the index
 * and added after editing, or database objects being committed after editing is finished.
 *
 * @since 1.1.0
 */
public interface Editable {
    /**
     * Begins editing the object.
     */
    void beginEditing();

    /**
     * Commits edits made to the object.
     */
    void endEditing();
}
