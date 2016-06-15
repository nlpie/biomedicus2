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

package edu.umn.biomedicus.common.utilities;

import java.io.IOException;
import java.util.Scanner;

/**
 * Utility class for dealing with classpath resources.
 *
 * @since 1.2.0
 */
public final class Resources {
    /**
     * Private constructor to prevent instantiation of a utility class.
     */
    private Resources() {
        throw new UnsupportedOperationException();
    }

    /**
     * Loads a resource to a String using the UTF_8 encoding.
     *
     * @param resourceName the qualified classpath name of the resource.
     * @return String with the contents of the resource.
     * @throws IOException
     */
    public static String toString(String resourceName) throws IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try (Scanner scanner = new Scanner(classLoader.getResourceAsStream(resourceName), "UTF_8").useDelimiter("\\A")) {
            return scanner.hasNext() ? scanner.next() : "";
        }
    }
}
