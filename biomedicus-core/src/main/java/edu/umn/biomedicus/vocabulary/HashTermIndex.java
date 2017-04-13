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

package edu.umn.biomedicus.vocabulary;

import edu.umn.biomedicus.common.terms.AbstractTermIndex;
import edu.umn.biomedicus.common.terms.IndexedTerm;
import edu.umn.biomedicus.common.terms.TermIndex;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 */
public class HashTermIndex extends AbstractTermIndex {
    private final Map<String, Integer> map = new HashMap<>();
    private final String[] terms;

    public HashTermIndex(String[] terms) {
        this.terms = terms;
        for (int i = 0; i < terms.length; i++) {
            map.put(terms[i], i);
        }
    }

    public HashTermIndex(Collection<String> collection) {
        int size = collection.size();
        terms = new String[size];
        Iterator<String> iterator = collection.iterator();
        for (int i = 0; i < size; i++) {
            terms[i] = iterator.next();
            map.put(terms[i], i);
        }
    }

    public HashTermIndex(TermIndex termIndex) {
        int size = termIndex.size();
        terms = new String[size];

        int i = 0;
        Iterator<IndexedTerm> iterator = termIndex.iterator();
        while (iterator.hasNext()) {
            IndexedTerm next = iterator.next();
            String string = termIndex.getTerm(next);
            terms[i] = string;
            map.put(string, i++);
        }
    }


    @Override
    public boolean contains(String string) {
        return map.containsKey(string);
    }

    @Override
    public int size() {
        return terms.length;
    }

    @Override
    protected String getTerm(int termIdentifier) {
        return terms[termIdentifier];
    }

    @Override
    protected int getIdentifier(CharSequence term) {
        Integer integer = map.get(term.toString());
        return integer != null ? integer : -1;
    }
}
