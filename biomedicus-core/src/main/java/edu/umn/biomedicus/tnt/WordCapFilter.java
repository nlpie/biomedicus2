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

package edu.umn.biomedicus.tnt;

import edu.umn.biomedicus.common.tuples.WordCap;

import java.util.function.Predicate;

/**
 *
 */
public class WordCapFilter implements Predicate<WordCap> {
    private boolean filterCapitalized = false;

    private boolean filterNotCapitalized = false;

    public WordCapFilter() {
    }

    public WordCapFilter(boolean filterCapitalized, boolean filterNotCapitalized) {
        this.filterCapitalized = filterCapitalized;
        this.filterNotCapitalized = filterNotCapitalized;
    }

    @Override
    public boolean test(WordCap wordCap) {
        return !(filterCapitalized && wordCap.isCapitalized() || filterNotCapitalized && !wordCap.isCapitalized());
    }

    public boolean isFilterCapitalized() {
        return filterCapitalized;
    }

    public void setFilterCapitalized(boolean filterCapitalized) {
        this.filterCapitalized = filterCapitalized;
    }

    public boolean isFilterNotCapitalized() {
        return filterNotCapitalized;
    }

    public void setFilterNotCapitalized(boolean filterNotCapitalized) {
        this.filterNotCapitalized = filterNotCapitalized;
    }
}
