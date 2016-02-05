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

package edu.umn.biomedicus.uima.files;

import edu.umn.biomedicus.exc.BiomedicusException;

/**
 * Appends an appropriate file name to the output directory.
 *
 * @since 1.3.0
 */
public interface FileNameProvider {
    /**
     * Returns an appropriate file name by appending it to the provided path.
     *
     * @return file name
     */
    String getFileName() throws BiomedicusException;
}
