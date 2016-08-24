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

package edu.umn.biomedicus.common.types.text;

public final class TermToken implements Token {
    private final String text;
    private final boolean hasSpaceAfter;

    public TermToken(String text, boolean hasSpaceAfter) {
        this.text = text;
        this.hasSpaceAfter = hasSpaceAfter;
    }

    @Override
    public String text() {
        return text;
    }

    @Override
    public boolean hasSpaceAfter() {
        return hasSpaceAfter;
    }

    @Override
    public String toString() {
        return "TermToken(\"" + text + "\")";
    }
}
