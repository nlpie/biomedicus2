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

package edu.umn.biomedicus.uima.adapter

import edu.umn.biomedicus.framework.ArtifactSourceRunner
import edu.umn.biomedicus.framework.RunnerFactory
import edu.umn.biomedicus.uima.labels.LabelAdapters
import edu.umn.nlpengine.Artifact
import org.apache.uima.cas.CAS
import org.apache.uima.collection.CollectionReader_ImplBase
import org.apache.uima.resource.ResourceInitializationException
import org.apache.uima.util.Progress
import org.apache.uima.util.ProgressImpl
import java.util.*

class ArtifactSourceUimaAdapter : CollectionReader_ImplBase() {

    private var guiceInjector: GuiceInjector? = null

    private var runner: ArtifactSourceRunner? = null

    private var labelAdapters: LabelAdapters? = null

    private var hasNext: Boolean = false

    private var next: Artifact? = null

    private var completed = 0

    private var total: Long = 0

    override fun initialize() {
        guiceInjector = uimaContext.getResourceObject("guiceInjector")
                ?.let { it as? GuiceInjector }
        val injector = guiceInjector
                ?.attach()
                ?: throw ResourceInitializationException()
        labelAdapters = injector.getInstance(LabelAdapters::class.java)

        val settingsMap = uimaContext.configParameterNames
                .associate {
                    Pair(it, uimaContext.getConfigParameterValue(it))
                }

        val runnerFactory = injector.getInstance(RunnerFactory::class.java)
        val runner = runnerFactory.getSourceRunner(settingsMap, emptyMap())

        total = runner.estimateTotal()

        this.runner = runner

        tryAdvance()
    }

    override fun getProgress(): Array<Progress> {
        return arrayOf(ProgressImpl(completed, total.toInt(), Progress.ENTITIES))
    }

    private fun tryAdvance() {
        hasNext = runner?.tryAdvance {
            next = it
        } ?: throw IllegalStateException("Runner was null.")
        if (!hasNext) {
            runner?.close()
            guiceInjector?.detach()
        }
    }

    override fun hasNext(): Boolean {
        return hasNext
    }

    override fun getNext(aCAS: CAS?) {
        if (!hasNext) {
            throw NoSuchElementException("No next cas.")
        }
        val next = next
        completed++
        tryAdvance()
        CASArtifact(labelAdapters, next, aCAS)
    }

    override fun close() {

    }
}