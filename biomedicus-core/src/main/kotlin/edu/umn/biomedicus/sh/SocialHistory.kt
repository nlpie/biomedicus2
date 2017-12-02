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

package edu.umn.biomedicus.sh

import edu.umn.biomedicus.sections.SubstanceUsageElementType
import edu.umn.biomedicus.sections.SubstanceUsageKind
import edu.umn.nlpengine.Label
import edu.umn.nlpengine.Span

data class SocialHistoryCandidate(
        override val startIndex: Int,
        override val endIndex: Int,
        val substanceUsageKind: SubstanceUsageKind
): Label {
    constructor(
            label: Label,
            substanceUsageKind: SubstanceUsageKind
    ): this(label.startIndex, label.endIndex, substanceUsageKind)
}

data class SubstanceUsageElement(
        override val startIndex: Int,
        override val endIndex: Int,
        val substanceUsageKind: SubstanceUsageKind,
        val substanceUsageElementType: SubstanceUsageElementType
): Label {
    constructor(
            label: Label,
            substanceUsageKind: SubstanceUsageKind,
            substanceUsageElementType: SubstanceUsageElementType
    ): this(label.startIndex, label.endIndex, substanceUsageKind, substanceUsageElementType)
}