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

package edu.umn.biomedicus.framework

import edu.umn.nlpengine.Document
import edu.umn.nlpengine.Label
import edu.umn.nlpengine.Span
import edu.umn.nlpengine.TextRange
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Factory for parsing TagEx expressions into [TagEx] objects.
 */
@Singleton
class TagExFactory @Inject constructor(private val searchExprFactory: SearchExprFactory) {
    /**
     * Parses the expression into a [TagEx] object.
     */
    fun parse(expr: String): TagEx {
        return TagEx(searchExprFactory.parse(expr))
    }
}

/**
 * The a [TagEx] search match.
 */
interface TagExMatch : TextRange {
    /**
     * The named labels found during the match
     */
    val namedLabels: NamedLabels

    /**
     * The named spans found during the match
     */
    val namedSpans: NamedSpans
}

/**
 * Named labels of a TagEx search expression found during search.
 */
interface NamedLabels {
    /**
     * Returns the value of the label with the specified [name]
     */
    fun getLabel(name: String): Label?

    /**
     * Returns whether the label with the specified [name] was found
     */
    fun containsLabel(name: String): Boolean
}

/**
 * Get operator function which uses the return type [T] and [name] to retrieve a label value.
 */
inline operator fun <reified T> NamedLabels.get(name: String): T? {
    val label = getLabel(name)
    return label as? T
}

/**
 * Named spans of a TagEx search expression found during search.
 */
interface NamedSpans {
    /**
     * Returns the named span with the given name.
     */
    operator fun get(name: String): Span?
}

/**
 * A compiled search expression.
 */
class TagEx(private val expr: SearchExpr) {
    /**
     * Finds the first match of the expression in the [document] or null if there is no match.
     */
    fun find(document: Document): TagExMatch? {
        return find(document, document)
    }

    /**
     * Finds the first match of the expression in the [textRange] in the [document] or null if there
     * is no match.
     */
    fun find(document: Document, textRange: TextRange): TagExMatch? {
        val searcher = expr.createSearcher(document)
        searcher.search(textRange)
        return searcher.toSearchResult()?.let { SearcherTagExMatch(it) }
    }

    /**
     * Finds all the matches of the expression in the [document].
     */
    fun findAll(document: Document): Sequence<TagExMatch> {
        return findAll(document, document)
    }

    /**
     * Finds all the matches of the expression in the [textRange] in the [document].
     */
    fun findAll(
            document: Document,
            textRange: TextRange
    ): Sequence<TagExMatch> = object : Sequence<TagExMatch> {
        override fun iterator(): Iterator<TagExMatch> = object : Iterator<TagExMatch> {
            val searcher = expr.createSearcher(document)
            var hasNext = searcher.search(textRange)

            override fun hasNext(): Boolean {
                return hasNext
            }

            override fun next(): TagExMatch {
                if (!hasNext) {
                    throw NoSuchElementException("No next TagEx result.")
                }
                val result = searcher.toSearchResult()
                        ?: throw AssertionError(
                                "hasNext should be only be true if this is non-null"
                        )
                hasNext = searcher.search(textRange)
                return SearcherTagExMatch(result)
            }
        }
    }

    /**
     * Finds whether the entire [document] matches this expression.
     */
    fun match(document: Document): TagExMatch? {
        return match(document, document)
    }

    /**
     * Finds whether a [textRange] inside a [document] matches this expression.
     */
    fun match(document: Document, textRange: TextRange): TagExMatch? {
        val searcher = expr.createSearcher(document)
        searcher.match(textRange)
        return searcher.toSearchResult()?.let { SearcherTagExMatch(it) }
    }

    /**
     * Returns true if the entire [document] matches this expression, false otherwise.
     */
    fun matches(document: Document): Boolean {
        return matches(document, document)
    }

    /**
     * Returns true if the [textRange] inside a [document] matches this expression, false otherwise.
     */
    fun matches(document: Document, textRange: TextRange): Boolean {
        return expr.createSearcher(document).match(textRange)
    }
}

internal class SearcherTagExMatch(private val searchResult: SearchResult) : TagExMatch {
    override val namedLabels: NamedLabels = object : NamedLabels {
        override fun getLabel(name: String): Label? {
            return searchResult.getLabel(name)
        }

        override fun containsLabel(name: String): Boolean {
            return searchResult.getLabel(name) != null
        }
    }

    override val namedSpans: NamedSpans = object : NamedSpans {
        override fun get(name: String): Span? {
            return searchResult.getSpan(name)
        }

    }

    override val startIndex: Int
        get() = searchResult.begin

    override val endIndex: Int
        get() = searchResult.end
}