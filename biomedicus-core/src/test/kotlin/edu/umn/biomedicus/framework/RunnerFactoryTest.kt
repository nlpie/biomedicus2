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

import com.google.inject.Guice
import com.google.inject.Stage
import edu.umn.nlpengine.*
import java.lang.System
import kotlin.concurrent.thread
import kotlin.test.Test
import kotlin.test.assertSame
import kotlin.test.assertTrue


private var processed: Document? = null
private var done = false

class DPStub : DocumentsProcessor {
    override fun process(document: Document) {
        processed = document
    }

    override fun done() {
        done = true
    }

}

class RunnerFactoryTest {
    class TestArtifactsProcessor : ArtifactsProcessor {
        override fun process(artifact: Artifact) {

        }

        override fun done() {

        }
    }


    @Test
    fun `processors shared across threads`() {
        System.setProperty("biomedicus.paths.home", ".")
        val application = Bootstrapper.create(Guice.createInjector(Stage.DEVELOPMENT))
        val runnerFactory = application.getInstance(RunnerFactory::class.java)

        val runner1 = runnerFactory.getRunner("test",
                mapOf(Pair("pipelineComponent", TestArtifactsProcessor::class.java.name)),
                emptyMap())

        var runner2: Runner? = null
        thread(start = true) {
            runner2 = runnerFactory.getRunner("test",
                    mapOf(Pair("pipelineComponent", TestArtifactsProcessor::class.java.name)),
                    emptyMap())
        }.join()

        assertTrue(runner1 === runner2, "Runners should be shared across threads")
    }

    @Test
    fun `document processor runner`() {
        System.setProperty("biomedicus.paths.home", ".")
        val application = Bootstrapper.create(Guice.createInjector(Stage.DEVELOPMENT))
        val runnerFactory = application.getInstance(RunnerFactory::class.java)

        val runner = runnerFactory.getRunner("test",
                mapOf(Pair("pipelineComponent", DPStub::class.java.name),
                        Pair("documentName", "blah")),
                emptyMap())

        val artifact = StandardArtifact("blah")
        val document = artifact.addDocument("blah", "some text")
        runner.processArtifact(artifact)
        runner.done()

        assertSame(document, processed, "document passed to processor")
        assertTrue(done, "done not called after finished")
    }
}
