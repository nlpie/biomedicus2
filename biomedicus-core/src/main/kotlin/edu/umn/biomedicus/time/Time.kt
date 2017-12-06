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

package edu.umn.biomedicus.time

import edu.umn.biomedicus.annotations.Setting
import edu.umn.biomedicus.common.TextIdentifiers
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech
import edu.umn.biomedicus.exc.BiomedicusException
import edu.umn.biomedicus.framework.DocumentProcessor
import edu.umn.biomedicus.framework.SearchExpr
import edu.umn.biomedicus.framework.SearchExprFactory
import edu.umn.biomedicus.sentences.Sentence
import edu.umn.biomedicus.tagging.PosTag
import edu.umn.biomedicus.tokenization.ParseToken
import edu.umn.nlpengine.Document
import edu.umn.nlpengine.TextRange
import java.io.File
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A day of week in text, e.g. "Monday", "tue.", etc
 */
data class DayOfWeek(override val startIndex: Int, override val endIndex: Int) : TextRange {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * Data model for days of week
 */
@Singleton
class DaysOfWeek @Inject constructor(
        @Setting("time.daysPath") path: String
) {
    val values = File(path).readLines()
}

/**
 * Detects days of week in text
 */
class DayOfWeekDetector @Inject constructor(
        daysOfWeek: DaysOfWeek
) : DocumentProcessor {
    private val values = daysOfWeek.values

    override fun process(document: Document) {
        val text = TextIdentifiers.getSystemLabeledText(document)

        val tokens = text.labelIndex(ParseToken::class)
        val posTags = text.labelIndex(PosTag::class)

        val labeler = text.labeler(DayOfWeek::class)

        posTags.filter { it.partOfSpeech == PartOfSpeech.NN || it.partOfSpeech == PartOfSpeech.NNP }
                .map {
                    tokens.firstAtLocation(it)
                            ?: throw BiomedicusException("Pos tag without token")
                }
                .filter { values.contains(it.text.toLowerCase()) }
                .forEach { labeler.add(DayOfWeek(it)) }
    }
}

data class TimeOfDayWord(override val startIndex: Int, override val endIndex: Int) : TextRange {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

@Singleton
class TimesOfDay @Inject constructor(@Setting("time.timesOfDayPath") path: String) {
    val values = File(path).readLines(StandardCharsets.UTF_8)
}

class TimeOfDayWordDetector @Inject constructor(timesOfDay: TimesOfDay) : DocumentProcessor {
    private val values = timesOfDay.values

    override fun process(document: Document) {
        val text = TextIdentifiers.getSystemLabeledText(document)

        val labeler = text.labeler(TimeOfDayWord::class)

        text.labelIndex(ParseToken::class)
                .filter { values.contains(it.text.toLowerCase()) }
                .forEach { labeler.add(TimeOfDayWord(it)) }
    }
}

data class SeasonWord(override val startIndex: Int, override val endIndex: Int) : TextRange {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

@Singleton
class Seasons @Inject constructor(@Setting("time.seasonsPath") path: String) {
    val values = File(path).readLines(StandardCharsets.UTF_8)
}

class SeasonWordDetector @Inject constructor(seasons: Seasons) : DocumentProcessor {
    private val values = seasons.values

    override fun process(document: Document) {
        val text = TextIdentifiers.getSystemLabeledText(document)

        val labeler = text.labeler(SeasonWord::class)

        text.labelIndex(ParseToken::class)
                .filter { values.contains(it.text.toLowerCase()) }
                .forEach { labeler.add(SeasonWord(it)) }
    }
}


/**
 * A month in text, e.g. "Jan", "February", "Sept."
 */
data class Month(override val startIndex: Int, override val endIndex: Int) : TextRange {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * The data model for months.
 */
@Singleton
class Months @Inject constructor(
        @Setting("time.monthsPath") path: String
) {
    val months = File(path).readLines()
}

/**
 * Detects months in text.
 */
class MonthDetector @Inject constructor(
        months: Months
) : DocumentProcessor {
    private val months = months.months

    override fun process(document: Document) {
        val text = TextIdentifiers.getSystemLabeledText(document)

        val tokens = text.labelIndex(ParseToken::class)
        val posTags = text.labelIndex(PosTag::class)

        val labeler = text.labeler(Month::class)

        posTags
                .filter {
                    it.partOfSpeech == PartOfSpeech.NN || it.partOfSpeech == PartOfSpeech.NNP
                            || it.partOfSpeech == PartOfSpeech.MD
                }
                .map {
                    tokens.firstAtLocation(it)
                            ?: throw BiomedicusException("Pos tag without token")
                }
                .filter { months.contains(it.text.toLowerCase()) }
                .forEach { labeler.add(Month(it)) }
    }
}

data class YearNumber(
        override val startIndex: Int,
        override val endIndex: Int,
        val value: Int
) : TextRange {
    constructor(
            textRange: TextRange,
            value: Int
    ) : this(textRange.startIndex, textRange.endIndex, value)
}

internal val yearPattern = Regex("(18|19|20)\\d{2}")

class YearNumberDetector() : DocumentProcessor {
    override fun process(document: Document) {
        val systemView = TextIdentifiers.getSystemLabeledText(document)

        val tokens = systemView.labelIndex(ParseToken::class)

        val labeler = systemView.labeler(YearNumber::class)

        tokens.filter { yearPattern.matches(it.text) }
                .forEach { labeler.add(YearNumber(it, it.text.toInt())) }
    }
}

data class YearRange(override val startIndex: Int, override val endIndex: Int) : TextRange

@Singleton
class YearRangePattern @Inject constructor(searchExprFactory: SearchExprFactory) {
    val expr = searchExprFactory.parse("[NumberRange YearNumber (Number<getNumberType=eCARDINAL> | YearNumber)]")
}

class YearRangeDetector @Inject constructor(pattern: YearRangePattern) : DocumentProcessor {
    private val expr = pattern.expr

    override fun process(document: Document) {
        val systemView = TextIdentifiers.getSystemLabeledText(document)

        val searcher = expr.createSearcher(systemView)

        val labeler = systemView.labeler(YearRange::class)

        while (searcher.search()) {
            labeler.add(YearRange(searcher.begin, searcher.end))
        }
    }
}

data class TextTime(override val startIndex: Int, override val endIndex: Int) : TextRange

@Singleton
data class TextTimePattern(val expr: SearchExpr) {
    @Inject constructor(searchExprFactory: SearchExprFactory): this(searchExprFactory.parse(
            """
[?Number] ParseToken<getText=i"a.m."|i"am"|i"p.m."|i"pm"|i"a.m"|i"p.m"> |
[?ParseToken<getText=r"[0-2]?[0-9]">] ParseToken<getText=":"> ParseToken<getText=r"[0-5][0-9]"> ParseToken<getText=i"a.m."|i"am"|i"p.m."|i"pm"|i"a.m"|i"p.m">?
                """
    ))
}

class TextTimeDetector(val expr: SearchExpr) : DocumentProcessor {
    @Inject constructor(textTimePattern: TextTimePattern): this(textTimePattern.expr)

    override fun process(document: Document) {
        val text = TextIdentifiers.getSystemLabeledText(document)

        val sentences = text.labelIndex<Sentence>()

        val labeler = text.labeler(TextTime::class.java)

        val searcher = expr.createSearcher(text)
        for (sentence in sentences) {
            while (searcher.search(sentence)) {
                labeler.add(TextTime(searcher.begin, searcher.end))
            }
        }
    }
}

data class TextDate(override val startIndex: Int, override val endIndex: Int) : TextRange

@Singleton
data class DatePattern(val expr: SearchExpr) {
    @Inject constructor(
            searchExprFactory: SearchExprFactory
    ) : this(searchExprFactory.parse("""
([?weekday:DayOfWeek] ParseToken<getText=",">? ->)? [?month:Month] (-> dayNo:Number<getNumberType=eCARDINAL>)? (ParseToken<getText=","|"of"> -> year:YearNumber)?
| [?monthNo:ParseToken<getText=r"([1-9]|1[0-2])">] ParseToken<getText="/"> ParseToken<getText=r"[1-9]|[1-2][1-9]|3[0-1]"> (ParseToken<getText="/"|"-"> -> YearNumber)?
| [?monthNo:ParseToken<getText=r"([1-9]|1[0-2])">] ParseToken<getText="-"> ParseToken<getText=r"[1-9]|[1-2][1-9]|3[0-1]"> ParseToken<getText="-"> -> YearNumber
| [?YearNumber] ParseToken<getText="-"> ParseToken<getText=r"[1-9]|[1-2][1-9]|3[0-1]"> ParseToken<getText="-"> monthNo:ParseToken<getText=r"([1-9]|1[0-2])">
"""))
}

class DateDetector @Inject constructor(pattern: DatePattern) : DocumentProcessor {
    private val expr = pattern.expr

    override fun process(document: Document) {
        val systemView = TextIdentifiers.getSystemLabeledText(document)

        val searcher = expr.createSearcher(systemView)

        val sentences = systemView.labelIndex<Sentence>()

        val labeler = systemView.labeler(TextDate::class)

        for (sentence in sentences) {
            while (searcher.search(sentence)) {
                labeler.add(TextDate(searcher.begin, searcher.end))
            }
        }
    }
}

data class TemporalPhrase(override val startIndex: Int, override val endIndex: Int) : TextRange {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

@Singleton
class TemporalPhrasePattern @Inject constructor(
        searchExprFactory: SearchExprFactory
) {
    val expr = searchExprFactory.parse("""
([?PosTag<getPartOfSpeech=eIN>] ->)? ([?ParseToken<getText="a"|"the">] ->)? (
  ([?PosTag<getPartOfSpeech=eJJ>] PosTag<getPartOfSpeech=eJJ>{0,2} ->)? ([?Quantifier] ->)? [?TimeUnit] ParseToken<getText="ago">? |
  [?DayOfWeek] -> TimeOfDayWord |
  [?TextDate] (ParseToken<getText="through"|"to"> -> TextDate)? |
  [?ParseToken<getText="present"|"presently"|"current"|"currently"|"recently"|"recent">] |
  [?ParseToken<getText="age">] ParseToken<getText="of"> -> Number |
  ([?ParseToken<getText="last"|"previous"|"next">] ->)? [?SeasonWord] |
  ([?Number] | [?TextTime]) [?PosTag<getPartOfSpeech=eIN>] PosTag<getPartOfSpeech=eDT> -> TimeOfDayWord |
  [?TextTime] |
  [?YearRange] |
  [?TimeOfDayWord]
)""")
}

class TemporalPhraseDetector @Inject constructor(
        pattern: TemporalPhrasePattern
) : DocumentProcessor {
    private val expr = pattern.expr

    override fun process(document: Document) {
        val text = TextIdentifiers.getSystemLabeledText(document)

        val sentences = text.labelIndex(Sentence::class.java)

        val labeler = text.labeler(TemporalPhrase::class)

        val searcher = expr.createSearcher(text)
        for (sentence in sentences) {
            while (searcher.search(sentence)) {
                labeler.add(TemporalPhrase(searcher.begin, searcher.end))
            }
        }
    }
}
