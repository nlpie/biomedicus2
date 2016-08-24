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

package edu.umn.biomedicus.sections;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.umn.biomedicus.Biomedicus;
import edu.umn.biomedicus.annotations.Setting;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 *
 */
@Singleton
class RuleBasedSectionDetectorModel {
    private final Pattern sectionHeaderPattern;

    @Inject
    RuleBasedSectionDetectorModel(@Setting("sections.headers.path") Path path) throws BiomedicusException {
        sectionHeaderPattern = Biomedicus.Patterns.loadPatternByJoiningLines(path);
    }

    Pattern getSectionHeaderPattern() {
        return sectionHeaderPattern;
    }
}
