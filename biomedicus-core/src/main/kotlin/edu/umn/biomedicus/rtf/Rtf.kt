/*
 * Copyright (c) 2018 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.rtf

import edu.umn.nlpengine.Label
import edu.umn.nlpengine.LabelMetadata
import edu.umn.nlpengine.SystemModule

/**
 * Types used during RTF processing.
 */
class RtfModule : SystemModule() {
    override fun setup() {
        addLabelClass<IllegalXmlCharacter>()
        addLabelClass<ViewIndex>()
        addLabelClass<UnknownControlWord>()
        addLabelClass<RowEnd>()
        addLabelClass<CellEnd>()
        addLabelClass<NestRowEnd>()
        addLabelClass<NestCellEnd>()
        addLabelClass<LineBreak>()
        addLabelClass<TableRowFormattingDefaults>()
        addLabelClass<CellDefinitionEnd>()
        addLabelClass<NewParagraph>()
        addLabelClass<ParagraphInTable>()
    }
}

/**
 * A label of a character that cannot be stored in XML.
 */
@LabelMetadata("biomedicus.v2.rtf")
data class IllegalXmlCharacter(
        override val startIndex: Int,
        override val endIndex: Int,
        val value: Int
) : Label()

/**
 * A index from one view to where it came from in another view.
 */
@LabelMetadata("biomedicus.v2.rtf")
data class ViewIndex(
        override val startIndex: Int,
        override val endIndex: Int,
        val destinationName: String,
        val destinationIndex: Int
) : Label()

/**
 * An rtf control word.
 */
interface ControlWord {
    val name: String
    val param: Int
    val index: Int
    val known: Boolean
}

/**
 * An unknown control word.
 */
@LabelMetadata("biomedicus.v2.rtf")
data class UnknownControlWord(
        override val startIndex: Int,
        override val endIndex: Int,
        override val name: String,
        override val param: Int,
        override val index: Int,
        override val known: Boolean
) : Label(), ControlWord

/**
 * An rtf row end control word.
 */
@LabelMetadata("biomedicus.v2.rtf")
data class RowEnd(
        override val startIndex: Int,
        override val endIndex: Int,
        override val name: String,
        override val param: Int,
        override val index: Int,
        override val known: Boolean
) : Label(), ControlWord

/**
 * An rtf cell end control word.
 */
@LabelMetadata("biomedicus.v2.rtf")
data class CellEnd(
        override val startIndex: Int,
        override val endIndex: Int,
        override val name: String,
        override val param: Int,
        override val index: Int,
        override val known: Boolean
) : Label(), ControlWord

/**
 * An rtf nested row end control word.
 */
@LabelMetadata("biomedicus.v2.rtf")
data class NestRowEnd(
        override val startIndex: Int,
        override val endIndex: Int,
        override val name: String,
        override val param: Int,
        override val index: Int,
        override val known: Boolean
) : Label(), ControlWord

/**
 * An rtf nested cell end control word.
 */
@LabelMetadata("biomedicus.v2.rtf")
data class NestCellEnd(
        override val startIndex: Int,
        override val endIndex: Int,
        override val name: String,
        override val param: Int,
        override val index: Int,
        override val known: Boolean
) : Label(), ControlWord

/**
 * An rtf line break control word.
 */
@LabelMetadata("biomedicus.v2.rtf")
data class LineBreak(
        override val startIndex: Int,
        override val endIndex: Int,
        override val name: String,
        override val param: Int,
        override val index: Int,
        override val known: Boolean
) : Label(), ControlWord


/**
 * An rtf control word for "trowd" which sets the formatting for table rows to the defaults.
 */
@LabelMetadata("biomedicus.v2.rtf")
data class TableRowFormattingDefaults(
        override val startIndex: Int,
        override val endIndex: Int,
        override val name: String,
        override val param: Int,
        override val index: Int,
        override val known: Boolean
) : Label(), ControlWord

/**
 * A control word annotation for cellx.
 */
@LabelMetadata("biomedicus.v2.rtf")
data class CellDefinitionEnd(
        override val startIndex: Int,
        override val endIndex: Int,
        override val name: String,
        override val param: Int,
        override val index: Int,
        override val known: Boolean
) : Label(), ControlWord

/**
 * A control word annotation for the new paragraph control word in rtf.
 */
@LabelMetadata("biomedicus.v2.rtf")
data class NewParagraph(
        override val startIndex: Int,
        override val endIndex: Int,
        override val name: String,
        override val param: Int,
        override val index: Int,
        override val known: Boolean
) : Label(), ControlWord

/**
 * An rtf annotation for a paragraph that is inside of a table.
 */
@LabelMetadata("biomedicus.v2.rtf")
data class ParagraphInTable(override val startIndex: Int, override val endIndex: Int) : Label()
