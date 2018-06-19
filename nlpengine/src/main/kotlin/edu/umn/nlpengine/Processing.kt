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

package edu.umn.nlpengine


/**
 * The source of a collection of artifacts to be processed in a pipeline.
 */
interface ArtifactSource : AutoCloseable {
    fun estimateTotal(): Long

    /**
     * When there are more artifacts in the pipeline, pass an artifact to
     */
    fun tryAdvance(consumer: (Artifact) -> Unit): Boolean
}


/**
 * An operation to be run once on every artifact run through the processing pipeline. One instance
 * is created per [Artifact] and [run] is called once per instance.
 */
interface ArtifactTask {
    /**
     * Performs the processing on the [artifact] this operation is responsible for processing.
     */
    fun run(artifact: Artifact)
}


/**
 * An operation to be run once on one of the [Document] objects on each [Artifact] in the pipeline.
 * One instance is created per [Artifact] and [run] is called exactly once per instance.
 */
interface DocumentTask  {
    /**
     * Performs the processing on the [document] this operation is responsible for processing.
     */
    fun run(document: Document)
}


/**
 * Responsible for performing processing on a [Document] from every [Artifact] in
 * the pipeline. Is instantiated once, globally, and [process] is called for every [Artifact],
 * potentially from multiple threads. Implementations are responsible for thread-safety in the
 * [process] method.
 */
interface DocumentsProcessor {
    /**
     * Performs processing on one of the [Document] in the collection of [Artifact] objects being
     * processed.
     */
    fun process(document: Document)

    /**
     * Called once all the [Artifact] objects in the collection are finished processing.
     */
    fun done() {}
}


/**
 * Responsible for performing processing on every [Artifact] in the pipeline. Is instantiated once,
 * globally, and [process]
 */
interface ArtifactsProcessor {
    /**
     * Performs processing on one of the [Document] in the collection of [Artifact] objects being
     * processed.
     */
    fun process(artifact: Artifact)

    /**
     * Called once all the [Artifact] objects in the collection are finished processing.
     */
    fun done()
}


/**
 * Internal interface for a class responsible for running some process on a pipeline component.
 */
interface Runner {
    fun processArtifact(artifact: Artifact)

    fun done() { }
}
