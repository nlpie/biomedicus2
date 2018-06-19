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

import edu.umn.biomedicus.framework.RunnerFactory
import edu.umn.biomedicus.uima.labels.LabelAdapters
import edu.umn.nlpengine.Runner
import org.apache.uima.UimaContext
import org.apache.uima.analysis_component.CasAnnotator_ImplBase
import org.apache.uima.cas.CAS
import org.apache.uima.impl.UimaContext_ImplBase
import org.apache.uima.resource.ResourceInitializationException

class PipelineComponentUimaAdapter : CasAnnotator_ImplBase() {

    private var guiceInjector: GuiceInjector? = null

    private var runner: Runner? = null

    private var labelAdapters: LabelAdapters? = null

    override fun initialize(uimaContext: UimaContext) {
        super.initialize(uimaContext)

        val context = uimaContext as UimaContext_ImplBase

        val uniqueName = context.uniqueName

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
        runner = runnerFactory.getRunner(uniqueName, settingsMap, emptyMap())
    }

    override fun process(aCAS: CAS) {
        runner?.processArtifact(CASArtifact(labelAdapters, aCAS))
                ?: throw IllegalStateException("Runner was null")
    }

    override fun collectionProcessComplete() {
        super.collectionProcessComplete()
        runner?.done() ?: throw IllegalStateException("Runner was null")
    }

    override fun destroy() {
        super.destroy()
        guiceInjector?.detach().also { guiceInjector = null }
    }
}
