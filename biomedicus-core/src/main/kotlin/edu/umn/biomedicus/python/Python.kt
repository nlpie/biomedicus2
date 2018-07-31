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

package edu.umn.biomedicus.python

import edu.umn.biomedicus.annotations.Setting
import edu.umn.biomedicus.framework.LifecycleManaged
import edu.umn.nlpengine.Artifact
import edu.umn.nlpengine.ArtifactsProcessor
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.PosixFilePermission
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.concurrent.thread
import kotlin.math.log


@Singleton
class PythonEnvironment @Inject constructor(
        @Setting("biomedicus.paths.home") val home: Path,
        @Setting("python.home") pyHome: String,
        @Setting("python.executable") pyExec: String
) {
    val pythonHome: Path = home.resolve(pyHome)

    private val venv: Path

    private val script: Path

    init {
        Files.createDirectories(pythonHome)

        venv = pythonHome.resolve("venv")

        script = pythonHome.resolve("pyRun.sh")

        if (Files.notExists(venv)) {
            val createVenv = ProcessBuilder(pyExec, "-m", "virtualenv", venv.toString())
            val process = createVenv.start()
            val exit = process.waitFor()
            if (exit != 0) {
                BufferedReader(InputStreamReader(process.errorStream)).useLines {
                    it.forEach { logger.error(it) }
                }
                error("Non-zero exit code while trying to create the virtual environment")
            }

            javaClass.getResourceAsStream("/pyRun.sh").use { input ->
                Files.newOutputStream(script).use { output ->
                    input.copyTo(output)
                }
            }
            Files.setPosixFilePermissions(script, setOf(PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_EXECUTE))
        }
    }

    fun createProcessBuilder(vararg args: String): ProcessBuilder {
        return ProcessBuilder(script.toString(), venv.resolve("bin").resolve("activate").toString(), *args)
    }

    companion object {
        val logger = LoggerFactory.getLogger(PythonEnvironment::class.java)
    }
}

/**
 * Pipeline component which validates the ability to use a python environment.
 */
class PythonValidation @Inject constructor(
        pythonEnvironment: PythonEnvironment
) : ArtifactsProcessor {
    init {
        val process = pythonEnvironment.createProcessBuilder("-V").start()
        val line = BufferedReader(InputStreamReader(process.inputStream)).use {
            it.readLine()
        }

        process.waitFor()

        if (!line.startsWith("Python")) {
            error("Failed to activate python createProcessBuilder: $line")
        } else {
            logger.info("Successfully activated python, version: $line")
        }

    }

    override fun process(artifact: Artifact) {

    }

    override fun done() {

    }

    companion object {
        val logger = LoggerFactory.getLogger(PythonValidation::class.java)
    }
}

/**
 * Maintains a connection to the BioMedICUS python server.
 */
@Singleton
class PythonServer @Inject constructor(
        @Setting("python.home") pyHome: String,
        @Setting("python.server.launch") launch: Boolean,
        @Setting("python.server.host") private val host: String,
        @Setting("python.server.port") private val port: Int,
        @Setting("python.sentences.vocabPath") vocabPath: Path,
        @Setting("python.sentences.wordsModel") wordsModel: String,
        @Setting("python.sentences.configPath") configPath: Path,
        @Setting("python.sentences.weightsPath") weightsPath: Path,
        environmentProvider: Provider<PythonEnvironment>
) : LifecycleManaged {
    private val environment: PythonEnvironment by lazy { environmentProvider.get() }

    var serverProcess: Process? = null

    init {
        if (launch) {
            if (!installCheck()) {
                install()
            }

            startup()
        }
    }

    fun installCheck(): Boolean {
        val biomedicusCheck = environment.createProcessBuilder("-c", "\"import biomedicus\"")
                .start()

        return biomedicusCheck.waitFor() == 0
    }

    fun install() {
        val kcWhl = environment.pythonHome.resolve("keras_contrib-2.0.8-py2.py3-none-any.whl")
        javaClass.getResourceAsStream("/keras_contrib-2.0.8-py2.py3-none-any.whl").use { input ->
            Files.newOutputStream(kcWhl, StandardOpenOption.CREATE_NEW).use { output ->
                input.copyTo(output)
            }
        }
        val kcInstall = environment.createProcessBuilder("-m", "pip", "install", kcWhl.toString())
                .start()

        logger.info("Installing keras_contrib for biomedicus python")

        if (kcInstall.waitFor() != 0) {
            writeErrorStream(kcInstall)
            error("Non-zero exit code installing keras-contrib")
        }

        logger.info("Installing tensorflow for biomedicus python")

        val tfInstall = environment.createProcessBuilder("-m", "pip", "install", "tensorflow")
                .start()

        if (tfInstall.waitFor() != 0) {
            writeErrorStream(tfInstall)
            error("Non-zero exit code installing tensorflow")
        }

        val bioWhl = environment.pythonHome.resolve("biomedicus_python-1.0-py2.py3-none-any.whl")
        javaClass.getResourceAsStream("/biomedicus_python-1.0-py2.py3-none-any.whl").use { input ->
            Files.newOutputStream(bioWhl, StandardOpenOption.CREATE_NEW).use { output ->
                input.copyTo(output)
            }
        }

        logger.info("Installing biomedicus python")

        val bioInstall = environment.createProcessBuilder("-m", "pip", "install", bioWhl.toString())
                .start()

        if (bioInstall.waitFor() != 0) {
            writeErrorStream(bioInstall)
            error("Non-zero exit code installing biomedicus")
        }
    }

    private fun writeErrorStream(process: Process) {
        BufferedReader(InputStreamReader(process.errorStream)).useLines {
            it.forEach { logger.error(it) }
        }
    }

    fun startup() {
        val serverProcess = environment.createProcessBuilder("-m", "biomedicus.server",
                host, port.toString()).start()

        thread(start = true, name = "Listener-biomedicus-server-debug-logging") {
            BufferedReader(InputStreamReader(serverProcess.inputStream)).useLines {
                it.forEach { logger.debug(it) }
            }
        }.start()

        thread(start = true, name = "Listener-biomedicus-server-error-logging") {
            BufferedReader(InputStreamReader(serverProcess.errorStream)).useLines {
                it.forEach { logger.error(it) }
            }
        }

        this.serverProcess = serverProcess
    }

    override fun doShutdown() {
        serverProcess?.also { it.destroy() }?.waitFor()
    }

    companion object {
        val logger = LoggerFactory.getLogger(PythonServer::class.java)
    }
}