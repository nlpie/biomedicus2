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
 * Provides file paths by appending the name of a file.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class BaseFileNameProvider implements FileNameProvider {
    /**
     * The file name to return.
     */
    private final String fileName;

    /**
     * Constructor which takes the file name to apply.
     *
     * @param fileName file name
     */
    BaseFileNameProvider(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getFileName() throws BiomedicusException {
        return fileName;
    }
}
