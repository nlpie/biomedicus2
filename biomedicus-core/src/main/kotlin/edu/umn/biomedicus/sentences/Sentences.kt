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

package edu.umn.biomedicus.sentences

import com.google.gson.Gson
import edu.umn.biomedicus.annotations.ComponentSetting
import edu.umn.biomedicus.annotations.Setting
import edu.umn.biomedicus.framework.LifecycleManaged
import edu.umn.biomedicus.python.PythonEnvironment
import edu.umn.biomedicus.tokenization.ParseToken
import edu.umn.nlpengine.*
import okhttp3.*
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ConnectException
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.concurrent.thread
import kotlin.math.min
import kotlin.math.roundToLong

class SentencesModule : SystemModule() {
    override fun setup() {
        addLabelClass<Sentence>()
        addLabelClass<TextSegment>()
    }
}

/**
 * A unit of language of multiple words making up a complete thought.
 */
@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class Sentence(
        override val startIndex: Int,
        override val endIndex: Int,
        val sentenceClass: Int
) : Label() {
    constructor(
            textRange: TextRange,
            sentenceClass: Int
    ) : this(textRange.startIndex, textRange.endIndex, sentenceClass)

    constructor(startIndex: Int, endIndex: Int) : this(startIndex, endIndex, 1)

    constructor(textRange: TextRange) : this(textRange, 1)

    /**
     * Retrieves a label index of all the [ParseToken] labels inside of this sentence.
     */
    fun tokens(): LabelIndex<ParseToken> {
        return document?.labelIndex<ParseToken>()?.inside(this)
                ?: throw IllegalStateException("This sentence has not been added to a document.")
    }

    companion object Classes {
        const val unknown = 0
        const val sentence = 1
    }
}

@LabelMetadata(classpath = "biomedicus.v2", distinct = true)
data class TextSegment(override val startIndex: Int, override val endIndex: Int) : Label() {
    constructor(textRange: TextRange) : this(textRange.startIndex, textRange.endIndex)
}


data class SentencesRequest(
    val text: String,
    val tokens: List<Any?>? = null
)

data class SentenceResponse(
    var sentences: List<ResponseSentence>? = null
)

data class ResponseSentence(
        var begin: Int? = null,
        var end: Int? = null,
        var category: String? = null,
        var tokens: List<List<Any>>? = null
)

data class IsReadyResponse(
        var ready: Boolean? = false
)

private val JSON = MediaType.parse("application/json; charset=utf-8")

/**
 * Maintains a connection to the BioMedICUS python server for sentences.
 */
@Singleton
class SentencesService @Inject constructor(
        @Setting("sentences.launch") launch: Boolean,
        @Setting("sentences.url") private val url: String,
        @Setting("paths.data") private val dataPath: Path,
        @Setting("paths.conf") private val confPath: Path,
        environmentProvider: Provider<PythonEnvironment>
) : LifecycleManaged {
    private val environment: PythonEnvironment by lazy { environmentProvider.get() }

    var serverProcess: Process? = null

    private val client: OkHttpClient = OkHttpClient()

    init {
        if (launch) {
            environment.installCheck()
            startup()
        }
    }

    fun startup() {
        val processBuilder = environment.createProcessBuilder(
                "-m", "biomedicus.services.sentences"
        )
        processBuilder.directory(dataPath.toFile())
        processBuilder.environment()["BIOMEDICUS_CONF_FILE"] = confPath.resolve("biomedicusConfiguration.yml").toString()

        val serverProcess = processBuilder.start()

        thread(start = true, name = "Listener-biomedicus-server-debug-logging") {
            BufferedReader(InputStreamReader(serverProcess.inputStream)).useLines {
                it.forEach { logger.debug(it) }
            }
        }

        thread(start = true, name = "Listener-biomedicus-server-error-logging") {
            BufferedReader(InputStreamReader(serverProcess.errorStream)).useLines {
                it.forEach { logger.error(it) }
            }
        }

        this.serverProcess = serverProcess

        logger.info("Waiting for sentences server to start.")
        val random = ThreadLocalRandom.current()
        var timeout = (500.0 * random.nextDouble()).roundToLong()
        repeat (100) {
            val request = Request.Builder().url("$url/is_ready").get().build()
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    logger.info("Sentences server started successfully.")
                    return
                }
            } catch (exception: ConnectException) {
                logger.debug("Failed to connect to sentence server, retrying in $timeout milliseconds")
            }

            // sleep 30 seconds
            Thread.sleep(timeout)
            timeout = (min(60_000.0, timeout.toDouble()) * random.nextDouble() * 1.5).roundToLong()
        }
        error("Failed to start server successfully.")
    }

    fun sendMessage(sentencesRequest: SentencesRequest): SentenceResponse {
        val gson = Gson()
        val json = gson.toJson(sentencesRequest)

        val body = RequestBody.create(JSON, json)

        val request = Request.Builder()
                .url(url)
                .post(body)
                .build()
        var timeout = 500.0
        val random = ThreadLocalRandom.current()
        repeat(100) {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    return gson.fromJson(body.string(), SentenceResponse::class.java)
                }
            }
            Thread.sleep((min(60_000.0, timeout) * random.nextDouble()).roundToLong())
            timeout *= 1.5
        }
        error("Sentence request not successful")
    }

    override fun doShutdown() {
        serverProcess?.also { it.destroy() }?.waitFor()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SentencesService::class.java)
    }
}

class SentenceDetector @Inject constructor(
        private val sentencesService: SentencesService
) : DocumentsProcessor {
    override fun process(document: Document) {
        val text = document.text

        val request = SentencesRequest(text)

        val response = sentencesService.sendMessage(request)
        val labeler = document.labeler<Sentence>()
        val sentences = response.sentences
        if (sentences != null) {
            for (sentence in sentences) {
                labeler.add(Sentence(sentence.begin!!, sentence.end!!))
            }
        } else {
            error("Null sentences.")
        }
    }
}