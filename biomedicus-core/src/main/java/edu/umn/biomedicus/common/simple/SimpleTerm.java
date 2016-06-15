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

package edu.umn.biomedicus.common.simple;

import edu.umn.biomedicus.common.semantics.Concept;
import edu.umn.biomedicus.common.text.Term;
import edu.umn.biomedicus.common.text.TextSpan;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A simple, immutable {@link Term} implementation.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class SimpleTerm implements Term {
    private final int begin;
    private final int end;
    /**
     * The concept that the term contains
     */
    private final Concept concept;

    /**
     * Alternative concepts.
     */
    private final List<Concept> alternativeConcepts;

    /**
     * Default constructor.
     *
     * @param begin               the begin offset
     * @param end                 the end offset
     * @param concept             the primary concept of the term
     * @param alternativeConcepts the alternative concepts of the term
     */
    public SimpleTerm(int begin, int end, Concept concept, List<Concept> alternativeConcepts) {
        this.begin = begin;
        this.end = end;
        this.concept = concept;
        this.alternativeConcepts = alternativeConcepts;
    }

    @Override
    public Concept getPrimaryConcept() {
        return concept;
    }

    @Override
    public List<Concept> getAlternativeConcepts() {
        return alternativeConcepts;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == null) {
            return false;
        }
        if (!getClass().equals(o.getClass())) {
            return false;
        }
        if (this == o) {
            return true;
        }

        SimpleTerm that = (SimpleTerm) o;

        return begin == that.begin && end == that.end && concept.equals(that.concept) && alternativeConcepts.equals(that.alternativeConcepts);
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(begin);
        result = 31 * result + Integer.hashCode(end);
        result = 31 * result + concept.hashCode();
        result = 31 * result + alternativeConcepts.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SimpleTerm{"
                + "begin=" + getBegin()
                + ", end=" + getEnd()
                + ", concept=" + concept
                + ", alternativeConcepts=" + alternativeConcepts
                + '}';
    }

    @Override
    public int getBegin() {
        return begin;
    }

    @Override
    public int getEnd() {
        return end;
    }
}
