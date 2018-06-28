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

package edu.umn.biomedicus.parsing

import edu.umn.biomedicus.tokenization.ParseToken
import edu.umn.nlpengine.*
import java.util.*

class ParsingModule : SystemModule() {
    override fun setup() {
        addEnumClass<UDRelation>()

        addLabelClass<DependencyParse>()
        addLabelClass<Dependency>()
        addLabelClass<ConstituencyParse>()
    }
}

@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class ConstituencyParse(
        override val startIndex: Int,
        override val endIndex: Int,
        val parseTree: String
) : Label() {
    constructor(textRange: TextRange, parseTree: String) : this(textRange.startIndex, textRange.endIndex, parseTree)
}

/**
 * The root of the dependency tree in a sentence.
 *
 * @property startIndex the start index of the word which is the root
 * @property endIndex the index after the word which is the root
 * @property root the span of the root
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class DependencyParse(
        override val startIndex: Int,
        override val endIndex: Int,
        val root: Span
) : Label() {
    constructor(
            textRange: TextRange,
            root: TextRange
    ) : this(textRange.startIndex, textRange.endIndex, root.toSpan())
}

/**
 * A dependency relation in text.
 *
 * @property startIndex the index the dependant starts at
 * @property endIndex the index after the dependant
 * @property relation the relation between dependant and head
 * @property head the span of the head
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class Dependency(
        override val startIndex: Int,
        override val endIndex: Int,
        val dep: ParseToken,
        val relation: UDRelation?,
        val head: Dependency?
) : Label() {
    constructor(
            dep: ParseToken,
            relation: UDRelation?,
            head: Dependency?
    ) : this(dep.startIndex, dep.endIndex, dep, relation, head)

    fun selfAndParentIterator(): Iterator<Dependency> {
        return ancestorIterator(this)
    }

    fun parentIterator(): Iterator<Dependency> {
        return ancestorIterator(head)
    }

    private fun ancestorIterator(blah: Dependency?): Iterator<Dependency> {
        return object : Iterator<Dependency> {
            var ptr = blah

            override fun hasNext(): Boolean {
                return ptr != null
            }

            override fun next(): Dependency {
                val temp: Dependency = ptr ?: throw NoSuchElementException("No parent.")
                ptr = temp.head
                return temp
            }
        }
    }
}

/**
 * Finds the head of a phrase [dependencies] by finding the dependency that links outside of the
 * phrase.
 */
fun findHead(dependencies: Collection<Dependency>): Dependency {
    return dependencies.find { test ->
        dependencies.none {
            test.head?.locationEquals(it) ?: false
        }
    } ?: throw AssertionError("Could not find root of phrase")
}

/**
 * Returns the universal dependency relation enum represented by [string]
 */
fun getUniversalDependencyRelation(string: String) : UDRelation {
    return UDRelation.values().firstOrNull { string == it.lowercase }
    ?: throw AssertionError("Could not find dependency relation: $string")
}

/**
 * A merge of the universal dependency v1 and v2 relations.
 */
enum class UDRelation {
    /**
     * clausal modifier of noun (adjectival clause)
     */
    ACL,
    /**
     * adverbial clause modifier
     */
    ADVCL,
    /**
     * adverbial modifier
     */
    ADVMOD,
    /**
     * adjectival modifier
     */
    AMOD,
    /**
     * appositional modifier
     */
    APPOS,
    /**
     * auxiliary
     */
    AUX,
    /**
     * passive auxiliary
     */
    AUXPASS,
    /**
     * case marking
     */
    CASE,
    /**
     * coordinating conjunction
     */
    CC,
    /**
     * clausal complement
     */
    CCOMP,
    /**
     * classifier
     */
    CLF,
    /**
     * compound
     */
    COMPOUND,
    /**
     * conjunct
     */
    CONJ,
    /**
     * copula
     */
    COP,
    /**
     * clausal subject
     */
    CSUBJ,
    /**
     * clausal passive subject
     */
    CSUBJPASS,
    /**
     * unspecified dependency
     */
    DEP,
    /**
     * determiner
     */
    DET,
    /**
     * discourse element
     */
    DISCOURSE,
    /**
     * dislocated elements
     */
    DISLOCATED,
    /**
     * Direct object
     */
    DOBJ,
    /**
     * expletive
     */
    EXPL,
    /**
     * fixed multiword expression
     */
    FIXED,
    /**
     * flat multiword expression
     */
    FLAT,
    /**
     * goes with
     */
    GOESWITH,
    /**
     * indirect object
     */
    IOBJ,
    /**
     * list
     */
    LIST,
    /**
     * marker
     */
    MARK,
    /**
     * multi-word expression
     */
    MWE,
    /**
     * name
     */
    NAME,
    /**
     * negation modifier
     */
    NEG,
    /**
     * nominal modifier
     */
    NMOD,
    /**
     * nominal subject
     */
    NSUBJ,
    /**
     * passive nominal subject
     */
    NSUBJPASS,
    /**
     * numeric modifier
     */
    NUMMOD,
    /**
     * object
     */
    OBJ,
    /**
     * oblique nominal
     */
    OBL,
    /**
     * orphan
     */
    ORPHAN,
    /**
     * parataxis
     */
    PARATAXIS,
    /**
     * punctuation
     */
    PUNCT,
    /**
     * : overridden disfluency
     */
    REPARANDUM,
    /**
     * remnant in ellipsis
     */
    REMNANT,
    /**
     * ROOT
     */
    ROOT,
    /**
     * vocative
     */
    VOCATIVE,
    /**
     * open clausal complement
     */
    XCOMP;

    val lowercase = name.toLowerCase()

    override fun toString() = lowercase
}
