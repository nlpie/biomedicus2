/*
 * Copyright (c) 2017 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.utilities;

import edu.umn.biomedicus.framework.store.Span;

public class ParseNode {
    private final Span parent;
    private final String label;

    public ParseNode(Span parent, String label) {
        this.parent = parent;
        this.label = label;
    }

    public Span getParent() {
        return parent;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParseNode parseNode = (ParseNode) o;

        if (!parent.equals(parseNode.parent)) return false;
        return label.equals(parseNode.label);
    }

    @Override
    public int hashCode() {
        int result = parent.hashCode();
        result = 31 * result + label.hashCode();
        return result;
    }
}
