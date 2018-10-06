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
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech
import edu.umn.biomedicus.exc.BiomedicusException
import edu.umn.biomedicus.framework.SearchExpr
import edu.umn.biomedicus.framework.SearchExprFactory
import edu.umn.biomedicus.sentences.Sentence
import edu.umn.biomedicus.tagging.PosTag
import edu.umn.biomedicus.tokenization.ParseToken
import edu.umn.nlpengine.*
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Singleton

class TimeModule : SystemModule() {
    override fun setup() {
        addLabelClass<DayOfWeek>()
        addLabelClass<TimeOfDayWord>()
        addLabelClass<SeasonWord>()
        addLabelClass<Month>()
        addLabelClass<YearNumber>()
        addLabelClass<YearRange>()
        addLabelClass<TextTime>()
        addLabelClass<TextDate>()
        addLabelClass<TemporalPhrase>()
    }

}

/**
 * A day of week in text, e.g. "Monday", "tue.", etc
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class DayOfWeek(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class TimeOfDayWord(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class SeasonWord(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

/**
 * A month in text, e.g. "Jan", "February", "Sept."
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class Month(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}

@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class YearNumber(
        override val startIndex: Int,
        override val endIndex: Int,
        val value: Int
) : Label() {


    constructor(
            textRange: TextRange,
            value: Int
    ) : this(textRange.startIndex, textRange.endIndex, value)
}

@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class YearRange(override val startIndex: Int, override val endIndex: Int) : Label()

@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class TextTime(override val startIndex: Int, override val endIndex: Int) : Label()

@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class TextDate(override val startIndex: Int, override val endIndex: Int) : Label()

@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class TemporalPhrase(override val startIndex: Int, override val endIndex: Int) : Label()


/**
 * Data model for days of week
 */
@Singleton
class DaysOfWeek(val values: List<String>) {
    @Inject constructor(
            @Setting("time.days.asDataPath") path: Path
    ) : this(path.toFile().readLines())
}

/**
 * Detects days of week in text
 */
class DetectDaysOfWeek(val values: List<String>) : DocumentTask {
    @Inject constructor(daysOfWeek: DaysOfWeek) : this(daysOfWeek.values)

    override fun run(document: Document) {
        val tokens = document.labelIndex<ParseToken>()
        val posTags = document.labelIndex<PosTag>()

        val labeler = document.labeler<DayOfWeek>()

        posTags.filter { it.partOfSpeech == PartOfSpeech.NN || it.partOfSpeech == PartOfSpeech.NNP }
                .map {
                    tokens.firstAtLocation(it)
                            ?: throw BiomedicusException("Pos tag without token")
                }
                .filter { values.contains(it.text.toLowerCase()) }
                .forEach { labeler.add(DayOfWeek(it)) }
    }
}


@Singleton
class TimesOfDay(val values: List<String>) {
    @Inject constructor(@Setting("time.timesOfDay.asDataPath") path: String) : this(
            File(path).readLines(StandardCharsets.UTF_8)
    )
}

class DetectTimesOfDay(val values: List<String>) : DocumentTask {
    @Inject constructor(timesOfDay: TimesOfDay) : this(timesOfDay.values)

    override fun run(document: Document) {
        val labeler = document.labeler<TimeOfDayWord>()

        document.labelIndex<ParseToken>()
                .filter { values.contains(it.text.toLowerCase()) }
                .forEach { labeler.add(TimeOfDayWord(it)) }
    }
}


@Singleton
class Seasons(val values: List<String>) {
    @Inject constructor(
            @Setting("time.seasons.asDataPath") path: Path
    ) : this(
            path.toFile().readLines(StandardCharsets.UTF_8)
    )
}

class DetectSeasonWords(val values: List<String>) : DocumentTask {
    @Inject constructor(seasons: Seasons) : this(seasons.values)

    override fun run(document: Document) {
        val labeler = document.labeler<SeasonWord>()

        document.labelIndex<ParseToken>()
                .filter { values.contains(it.text.toLowerCase()) }
                .forEach { labeler.add(SeasonWord(it)) }
    }
}


/**
 * The data model for months.
 */
@Singleton
class Months(val months: List<String>) {
    @Inject constructor(
            @Setting("time.months.asDataPath") path: Path
    ) : this(path.toFile().readLines())
}

/**
 * Detects months in text.
 */
class DetectMonths(val months: List<String>) : DocumentTask {
    @Inject constructor(months: Months) : this(months.months)

    override fun run(document: Document) {
        val tokens = document.labelIndex<ParseToken>()
        val posTags = document.labelIndex<PosTag>()

        val labeler = document.labeler<Month>()

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


internal val yearPattern = Regex("(18|19|20)\\d{2}")

class DetectYearNumbers : DocumentTask {
    override fun run(document: Document) {
        val tokens = document.labelIndex<ParseToken>()

        val labeler = document.labeler<YearNumber>()

        tokens.filter { yearPattern.matches(it.text) }
                .forEach { labeler.add(YearNumber(it, it.text.toInt())) }
    }
}


@Singleton
class YearRangePattern(val expr: SearchExpr) {
    @Inject constructor(searchExprFactory: SearchExprFactory) : this(
            searchExprFactory.parse(
                    "[?NumberRange YearNumber (Number<getNumberType=eCARDINAL> | YearNumber)]"
            )
    )
}


class DetectYearRanges(val expr: SearchExpr) : DocumentTask {
    @Inject constructor(pattern: YearRangePattern) : this(pattern.expr)

    override fun run(document: Document) {
        val searcher = expr.createSearcher(document)

        val labeler = document.labeler<YearRange>()

        while (searcher.search()) {
            labeler.add(YearRange(searcher.begin, searcher.end))
        }
    }
}


@Singleton
data class TextTimePattern(val expr: SearchExpr) {
    @Inject constructor(searchExprFactory: SearchExprFactory) : this(searchExprFactory.parse(
            """
[?Number] ParseToken<getText=i"a.m."|i"am"|i"p.m."|i"pm"|i"a.m"|i"p.m"> |
[?ParseToken<getText=r"[0-2]?[0-9]">] ParseToken<getText=":"> ParseToken<getText=r"[0-5][0-9]"> ParseToken<getText=i"a.m."|i"am"|i"p.m."|i"pm"|i"a.m"|i"p.m">?
                """
    ))
}

class DetectTextTimes(val expr: SearchExpr) : DocumentTask {
    @Inject constructor(textTimePattern: TextTimePattern) : this(textTimePattern.expr)

    override fun run(document: Document) {
        val sentences = document.labelIndex<Sentence>()

        val labeler = document.labeler(TextTime::class.java)

        val searcher = expr.createSearcher(document)
        for (sentence in sentences) {
            while (searcher.search(sentence)) {
                labeler.add(TextTime(searcher.begin, searcher.end))
            }
        }
    }
}


@Singleton
data class DatePattern(val expr: SearchExpr) {
    @Inject constructor(searchExprFactory: SearchExprFactory) : this(searchExprFactory.parse("""
([?weekday:DayOfWeek] ParseToken<getText=",">? ->)? [?month:Month] (-> dayNo:Number<getNumberType=eCARDINAL>)? (ParseToken<getText=","|"of"> -> year:YearNumber)?
| [?monthNo:ParseToken<getText=r"([1-9]|1[0-2])">] ParseToken<getText="/"> ParseToken<getText=r"[1-9]|[1-2][1-9]|3[0-1]"> (ParseToken<getText="/"|"-"> -> YearNumber)?
| [?monthNo:ParseToken<getText=r"([1-9]|1[0-2])">] ParseToken<getText="-"> ParseToken<getText=r"[1-9]|[1-2][1-9]|3[0-1]"> ParseToken<getText="-"> -> YearNumber
| [?YearNumber] ParseToken<getText="-"> ParseToken<getText=r"[1-9]|[1-2][1-9]|3[0-1]"> ParseToken<getText="-"> monthNo:ParseToken<getText=r"([1-9]|1[0-2])">
"""))
}

class DetectDates(val expr: SearchExpr) : DocumentTask {
    @Inject constructor(pattern: DatePattern) : this(pattern.expr)

    override fun run(document: Document) {
        val searcher = expr.createSearcher(document)

        val sentences = document.labelIndex<Sentence>()

        val labeler = document.labeler<TextDate>()

        for (sentence in sentences) {
            while (searcher.search(sentence)) {
                labeler.add(TextDate(searcher.begin, searcher.end))
            }
        }
    }
}


@Singleton
class TemporalPhrasePattern(val expr: SearchExpr) {
    @Inject constructor(searchExprFactory: SearchExprFactory) : this(
            searchExprFactory.parse("""
([?PosTag<getPartOfSpeech=eIN>] ->)? ([?ParseToken<getText="a"|"the">] ->)? (
  ([?PosTag<getPartOfSpeech=eJJ>] PosTag<getPartOfSpeech=eJJ>{0,2} ->)? ([?Quantifier] ->)? [?TimeUnit] ParseToken<getText="ago">? |
  [?DayOfWeek] -> TimeOfDayWord |
  [?TextDate] (ParseToken<getText="through"|"to"> -> TextDate)? |
  [?ParseToken<getText="present"|"presently"|"current"|"currently"|"recently"|"recent">] |
  [?ParseToken<getText="age">] ParseToken<getText="of"> -> Number |
  ([?ParseToken<getText="last"|"previous"|"next">] ->)? [?SeasonWord] |
  ([?Number] | [?TextTime]) PosTag<getPartOfSpeech=eIN> PosTag<getPartOfSpeech=eDT> -> TimeOfDayWord |
  [?TextTime] |
  [?YearRange] |
  [?TimeOfDayWord]
)""")
    )
}

class DetectTemporalPhrases(val expr: SearchExpr) : DocumentTask {
    @Inject constructor(pattern: TemporalPhrasePattern) : this(pattern.expr)

    override fun run(document: Document) {
        val sentences = document.labelIndex<Sentence>()

        val labeler = document.labeler<TemporalPhrase>()

        val searcher = expr.createSearcher(document)
        for (sentence in sentences) {
            while (searcher.search(sentence)) {
                labeler.add(TemporalPhrase(searcher.begin, searcher.end))
            }
        }
    }
}
