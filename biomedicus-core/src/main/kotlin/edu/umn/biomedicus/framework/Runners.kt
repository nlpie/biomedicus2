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
    fun getRunner(
            processorSettings: Map<String, *>,
            processorScopedObjects: Map<Key<*>, Any>
    ): Runner {
        val (settingsInjector, processorContext) =
                createContext(processorSettings, processorScopedObjects)

        val processorClass = processorSettings["processorClass"]
                ?.let { it as? String }
                ?.let {
                    Class.forName(it)
                } ?: throw IllegalStateException("processorClass illegal value")


        @Suppress("UNCHECKED_CAST")
        when {
            processorClass.isSubclass<DocumentProcessor>() -> return DocumentProcessorRunner(
                    processorClass as Class<out DocumentProcessor>,
                    processorContext,
                    settingsInjector,
                    processorSettings
            )
            processorClass.isSubclass<ArtifactProcessor>() -> return ArtifactProcessorRunner(
                    processorClass as Class<out ArtifactProcessor>,
                    processorContext,
                    settingsInjector
            )
            processorClass.isSubclass<Aggregator>() -> return AggregatorRunner(
                    processorClass as Class<out Aggregator>,
                    processorContext,
                    settingsInjector
            )
            else -> throw IllegalArgumentException("Unknown processor class ${processorClass.canonicalName}")
        }
    }

    fun getSourceRunner(
            processorSettings: Map<String, *>,
            processorScopedObjects: Map<Key<*>, Any>
    ): ArtifactSourceRunner {
        val (settingsInjector, processorContext) =
                createContext(processorSettings, processorScopedObjects)

        val sourceClass = processorSettings["sourceClass"]
                ?.let { it as? String }
                ?.let {
                    Class.forName(it).asSubclass(ArtifactSource::class.java)
                } ?: throw IllegalStateException("sourceClass illegal value")

        return ArtifactSourceRunner(sourceClass, processorContext, settingsInjector)
    }

    private fun createContext(
            processorSettings: Map<String, *>,
            processorScopedObjects: Map<Key<*>, Any>
    ): Pair<Injector, BiomedicusScopes.Context> {
        val settingsTransformer = settingsTransformerProvider.get()
        settingsTransformer.setAnnotationFunction { ProcessorSettingImpl(it) }

        settingsTransformer.addAll(globalSettings)
        settingsTransformer.addAll(processorSettings)

        val settingsSeededObjects = settingsTransformer.settings
        val keys = settingsSeededObjects.keys
        val settingsModule = ProcessorSettingsModule(keys)
        val settingsInjector = injector.createChildInjector(settingsModule)

        val processorScopeMap = HashMap<Key<*>, Any>()
        processorScopeMap.putAll(settingsSeededObjects)
        processorScopeMap.putAll(processorScopedObjects)

        val processorContext = BiomedicusScopes.createProcessorContext(processorScopeMap)

        processorContext.call {
            processorSettings["eagerLoad"]
                    ?.let { it as? Array<*> }
                    ?.filterIsInstance<String>()
                    ?.forEach {
                        val provider = settingsInjector.getProvider(Class.forName(it))
                        if (provider is EagerLoadable) {
                            provider.eagerLoad()
                        } else {
                            val o = provider.get()
                            if (o is EagerLoadable) {
                                o.eagerLoad()
                            }
                        }
                    }
        }
        return Pair(settingsInjector, processorContext)
    }
}

/**
 * Runs [ArtifactProcessor] instances.
 */
class ArtifactProcessorRunner(
        private val processorClass: Class<out ArtifactProcessor>,
        private val processorContext: BiomedicusScopes.Context,
        private val settingsInjector: Injector
) : Runner {
    companion object {
        val log: Logger = LoggerFactory.getLogger(ArtifactProcessorRunner::class.java)
    }

    override fun processArtifact(artifact: Artifact): Unit = processorContext.call {
        val processor = settingsInjector.getInstance(processorClass)
        try {
            processor.process(artifact)
        } catch (e: Exception) {
            log.error("Processing failed on artifact: ${artifact.artifactID}")
            throw e
        }
    }
}

/**
 * Runs [DocumentProcessor] instances.
 */
class DocumentProcessorRunner(
        private val processorClass: Class<out DocumentProcessor>,
        private val processorContext: BiomedicusScopes.Context,
        private val settingsInjector: Injector,
        processorSettings: Map<String, *>
) : Runner {
    companion object {
        val log: Logger = LoggerFactory.getLogger(DocumentProcessorRunner::class.java)
    }

    private val documentName = processorSettings.getSetting<String>("documentName")

    override fun processArtifact(artifact: Artifact): Unit = artifact.documents[documentName]
            ?.let {
                processorContext.call {
                    try {
                        settingsInjector.getInstance(processorClass).process(it)
                    } catch (e: Exception) {
                        log.error("Processing failed on artifact: ${artifact.artifactID}")
                        throw e
                    }
                }
            } ?: throw IllegalArgumentException("No document with name: $documentName")

}

/**
 * Runs [Aggregator] instances.
 */
class AggregatorRunner(
        processorClass: Class<out Aggregator>,
        private val processorContext: BiomedicusScopes.Context,
        private val settingsInjector: Injector
) : Runner {
    companion object {
        val log: Logger = LoggerFactory.getLogger(AggregatorRunner::class.java)
    }

    private val aggregator = processorContext.call {
        settingsInjector.getInstance(processorClass)
    }

    override fun processArtifact(artifact: Artifact): Unit = processorContext.call {
        try {
            aggregator.process(artifact)
        } catch (e: Exception) {
            log.error("Processing failed on artifact: ${artifact.artifactID}")
            throw e
        }
    }

    override fun done(): Unit = processorContext.call {
        aggregator.done()
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