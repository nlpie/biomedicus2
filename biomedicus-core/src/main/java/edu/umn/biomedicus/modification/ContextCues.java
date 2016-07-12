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

package edu.umn.biomedicus.modification;

import edu.umn.biomedicus.common.syntax.PartOfSpeech;
import edu.umn.biomedicus.serialization.YamlSerialization;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public final class ContextCues {
    private List<String> leftContextCues;
    private int leftContextScope;
    private List<String> rightContextCues;
    private int rightContextScope;
    private List<PartOfSpeech> scopeDelimitersPos;
    private List<String> scopeDelimitersTxt;

    public List<String> getLeftContextCues() {
        return leftContextCues;
    }

    public void setLeftContextCues(List<String> leftContextCues) {
        this.leftContextCues = leftContextCues;
    }

    public List<String> getRightContextCues() {
        return rightContextCues;
    }

    public void setRightContextCues(List<String> rightContextCues) {
        this.rightContextCues = rightContextCues;
    }

    public int getLeftContextScope() {
        return leftContextScope;
    }

    public void setLeftContextScope(int leftContextScope) {
        this.leftContextScope = leftContextScope;
    }

    public int getRightContextScope() {
        return rightContextScope;
    }

    public void setRightContextScope(int rightContextScope) {
        this.rightContextScope = rightContextScope;
    }

    public List<PartOfSpeech> getScopeDelimitersPos() {
        return scopeDelimitersPos;
    }

    public void setScopeDelimitersPos(List<PartOfSpeech> scopeDelimitersPos) {
        this.scopeDelimitersPos = scopeDelimitersPos;
    }

    public List<String> getScopeDelimitersTxt() {
        return scopeDelimitersTxt;
    }

    public void setScopeDelimitersTxt(List<String> scopeDelimitersTxt) {
        this.scopeDelimitersTxt = scopeDelimitersTxt;
    }

    public static ContextCues deserialize(Path path) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path)) {
            @SuppressWarnings("unchecked")
            ContextCues contextCues = (ContextCues) YamlSerialization.createYaml().load(inputStream);
            return contextCues;
        }
    }

    public void serialize(Path path) throws IOException {
        YamlSerialization.createYaml().dump(this, Files.newBufferedWriter(path, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING));
    }
}
