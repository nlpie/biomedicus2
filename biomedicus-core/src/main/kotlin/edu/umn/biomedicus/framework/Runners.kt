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

import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.name.Named
import edu.umn.nlpengine.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/**
 * Creates and initializes runners for the different processor types.
 */
@Singleton
class RunnerFactory @Inject constructor(
        private val injector: Injector,
        @Named("globalSettings") private val globalSettings: Map<String, Any>,
        private val settingsTransformerProvider: Provider<SettingsTransformer>
) {
    private val contexts = ConcurrentHashMap<String, BiomedicusScopes.Context>()
    private val processorRunners = ConcurrentHashMap<String, Runner>()

    fun getRunner(
            processorIdentifier: String,
            processorSettings: Map<String, *>,
            processorScopedObjects: Map<Key<*>, Any>,
            pipelineComponent: Class<*> = processorSettings["pipelineComponent"]
                    ?.let { it as? String }
                    ?.let {
                        Class.forName(it)
                    } ?: error("pipelineComponent illegal value")
    ): Runner {
        val (settingsInjector, processorContext) =
                createContext(processorIdentifier, processorSettings, processorScopedObjects)

        @Suppress("UNCHECKED_CAST")
        return when {
            pipelineComponent.isSubclass<DocumentTask>() -> DocumentTaskRunner(
                    pipelineComponent as Class<out DocumentTask>,
                    processorContext,
                    settingsInjector,
                    processorSettings
            )
            pipelineComponent.isSubclass<ArtifactTask>() -> ArtifactTaskRunner(
                    pipelineComponent as Class<out ArtifactTask>,
                    processorContext,
                    settingsInjector
            )
            pipelineComponent.isSubclass<ArtifactsProcessor>() -> processorRunners
                    .computeIfAbsent(processorIdentifier) {
                        ArtifactsProcessorRunner(
                                pipelineComponent as Class<out ArtifactsProcessor>,
                                processorContext,
                                settingsInjector
                        )
                    }
            pipelineComponent.isSubclass<DocumentsProcessor>() -> processorRunners
                    .computeIfAbsent(processorIdentifier) {
                        DocumentsProcessorRunner(
                                pipelineComponent as Class<out DocumentsProcessor>,
                                processorContext,
                                settingsInjector,
                                processorSettings
                        )
                    }
            else -> throw IllegalArgumentException("Unknown processor class ${pipelineComponent.canonicalName}")
        }
    }

    fun sourceRunner(
            processorIdentifier: String,
            processorSettings: Map<String, *>,
            processorScopedObjects: Map<Key<*>, Any>,
            sourceClass: Class<out ArtifactSource> = processorSettings["sourceClass"]
                    ?.let { it as? String }
                    ?.let {
                        Class.forName(it).asSubclass(ArtifactSource::class.java)
                    } ?: throw IllegalStateException("sourceClass illegal value")
    ): ArtifactSourceRunner {
        val (settingsInjector, processorContext) =
                createContext(processorIdentifier, processorSettings, processorScopedObjects)


        return ArtifactSourceRunner(sourceClass, processorContext, settingsInjector)
    }

    private fun createContext(
            processorIdentifier: String,
            processorSettings: Map<String, *>,
            processorScopedObjects: Map<Key<*>, Any>
    ): Pair<Injector, BiomedicusScopes.Context> {
        val settingsTransformer = settingsTransformerProvider.get()
        settingsTransformer.setAnnotationFunction { ComponentSettingImpl(it) }

        settingsTransformer.addAll(globalSettings)
        settingsTransformer.addAll(processorSettings)

        val settingsSeededObjects = settingsTransformer.settings
        val keys = settingsSeededObjects.keys
        val settingsModule = ProcessorSettingsModule(keys)
        val settingsInjector = injector.createChildInjector(settingsModule)

        val processorScopeMap = HashMap<Key<*>, Any>()
        processorScopeMap.putAll(settingsSeededObjects)
        processorScopeMap.putAll(processorScopedObjects)

        val processorContext = contexts.computeIfAbsent(processorIdentifier) {
            BiomedicusScopes.createProcessorContext(processorScopeMap)
        }
        return Pair(settingsInjector, processorContext)
    }
}

/**
 * Runs [ArtifactTask] instances.
 */
class ArtifactTaskRunner(
        private val taskClass: Class<out ArtifactTask>,
        private val processorContext: BiomedicusScopes.Context,
        private val settingsInjector: Injector
) : Runner {
    companion object {
        val log: Logger = LoggerFactory.getLogger(ArtifactTaskRunner::class.java)
    }

    override fun processArtifact(artifact: Artifact): Unit = processorContext.call {
        val processor = settingsInjector.getInstance(taskClass)
        try {
            processor.run(artifact)
        } catch (e: Exception) {
            log.error("Processing failed on artifact: ${artifact.artifactID}")
            throw e
        }
    }
}

/**
 * Runs [DocumentTask] instances.
 */
class DocumentTaskRunner(
        private val taskClass: Class<out DocumentTask>,
        private val processorContext: BiomedicusScopes.Context,
        private val settingsInjector: Injector,
        processorSettings: Map<String, *>
) : Runner {
    companion object {
        val log: Logger = LoggerFactory.getLogger(DocumentTaskRunner::class.java)
    }

    private val documentName = processorSettings.getSetting<String>("documentName")

    init {
        processorContext.call {
            settingsInjector.getInstance(taskClass)
        }
    }

    override fun processArtifact(artifact: Artifact): Unit = artifact.documents[documentName]
            ?.let {
                processorContext.call {
                    try {
                        settingsInjector.getInstance(taskClass).run(it)
                    } catch (e: Exception) {
                        log.error("Processing failed on artifact: ${artifact.artifactID}")
                        throw e
                    }
                }
            } ?: throw IllegalArgumentException("No document with name: $documentName")

}

/**
 * Runs [ArtifactsProcessor] instances.
 */
class ArtifactsProcessorRunner(
        pipelineComponent: Class<out ArtifactsProcessor>,
        private val processorContext: BiomedicusScopes.Context,
        private val settingsInjector: Injector
) : Runner {
    companion object {
        val log: Logger = LoggerFactory.getLogger(ArtifactsProcessorRunner::class.java)
    }

    private val processor = processorContext.call {
        settingsInjector.getInstance(pipelineComponent)
    }

    override fun processArtifact(artifact: Artifact): Unit = processorContext.call {
        try {
            processor.process(artifact)
        } catch (e: Exception) {
            log.error("Processing failed on artifact: ${artifact.artifactID}")
            throw e
        }
    }

    override fun done(): Unit = processorContext.call {
        processor.done()
    }
}

/**
 * Runs [DocumentsProcessor] instances.
 */
class DocumentsProcessorRunner(
        pipelineComponent: Class<out DocumentsProcessor>,
        private val processorContext: BiomedicusScopes.Context,
        private val settingsInjector: Injector,
        processorSettings: Map<String, *>
) : Runner {
    companion object {
        val log: Logger = LoggerFactory.getLogger(DocumentsProcessorRunner::class.java)
    }

    private val documentName = processorSettings.getSetting<String>("documentName")

    private val processor = processorContext.call {
        settingsInjector.getInstance(pipelineComponent)
    }

    override fun processArtifact(artifact: Artifact) = artifact.documents[documentName]
            ?.let {
                processorContext.call {
                    try {
                        processor.process(it)
                    } catch (e: Exception) {
                        log.error("Processing failed on artifact: ${artifact.artifactID}")
                        throw e
                    }
                }
            } ?: throw IllegalArgumentException("No document with name: $documentName")

    override fun done(): Unit = processorContext.call {
        processor.done()
    }
}

/**
 * Runs [ArtifactSource] instances.
 */
class ArtifactSourceRunner(
        sourceClass: Class<out ArtifactSource>,
        private val processorContext: BiomedicusScopes.Context,
        private val settingsInjector: Injector
) : Closeable {
    private val source = processorContext.call {
        settingsInjector.getInstance(sourceClass)
    }

    fun estimateTotal(): Long = processorContext.call {
        source.estimateTotal()
    }

    fun tryAdvance(consumer: (Artifact) -> Unit): Boolean =
            processorContext.call {
                source.tryAdvance(consumer)
            }

    override fun close() = processorContext.call {
        source.close()
    }
}

internal inline fun <reified T> Map<String, *>.getSetting(key: String): T {
    return this[key]?.let { it as? T }
            ?: throw IllegalStateException("Setting not found with key: $key")
}

internal inline fun <reified T> Class<*>.isSubclass(): Boolean {
    return T::class.java.isAssignableFrom(this)
}
