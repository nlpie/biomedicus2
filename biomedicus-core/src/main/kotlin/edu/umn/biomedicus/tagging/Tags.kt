package edu.umn.biomedicus.tagging

import edu.umn.biomedicus.common.types.syntax.PartOfSpeech
import edu.umn.nlpengine.Label

data class PosTag(
        override val startIndex: Int,
        override val endIndex: Int,
        val partOfSpeech: PartOfSpeech
) : Label {
    constructor(
            label: Label,
            partOfSpeech: PartOfSpeech
    ): this(label.startIndex, label.endIndex, partOfSpeech)
}
