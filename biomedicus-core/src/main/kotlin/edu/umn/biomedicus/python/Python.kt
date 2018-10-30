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
import edu.umn.nlpengine.Artifact
import edu.umn.nlpengine.ArtifactsProcessor
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class PythonEnvironment @Inject constructor(
        @Setting("python.home.asDataPath") pythonHome: Path,
        @Setting("python.executable") pyExec: String,
        @Setting("python.nlpnewt.dist") newt: String,
        @Setting("python.biomedicus") biomedicus: String,
        @Setting("python.keras_contrib") kc: String
) {
    private val venv: Path = pythonHome.resolve("venv")

    private val script: Path = pythonHome.resolve("pyRun.sh")

    private val bioDist: Path = pythonHome.resolve(biomedicus)

    private val kcDist: Path = pythonHome.resolve(kc)

    private val newtDist: Path = pythonHome.resolve(newt)

    init {
        if (Files.notExists(venv)) {
            val installVenv = ProcessBuilder(pyExec, "-m", "pip", "install", "virtualenv").start()
            val venvExit = installVenv.waitFor()
            if (venvExit != 0) {
                writeErrorStream(installVenv)
                error("Non-zero exit code while trying to create the virtual environment")
            }

            val createVenv = ProcessBuilder(pyExec, "-m", "virtualenv", venv.toString()).start()
            val exit = createVenv.waitFor()
            if (exit != 0) {
                writeErrorStream(createVenv)
                error("Non-zero exit code while trying to create the virtual environment")
            }
        }
    }

    fun createProcessBuilder(vararg args: String): ProcessBuilder {
        return ProcessBuilder(
            script.toString(), venv.resolve("bin").resolve("activate").toString(),
            *args
        )
    }

    companion object {
        val logger = LoggerFactory.getLogger(PythonEnvironment::class.java)
    }

    fun installCheck() {
        val biomedicusCheck = createProcessBuilder("-c", "\"import biomedicus\"")
                .start()

        if (biomedicusCheck.waitFor() != 0) {
            install()
        }
    }

    fun install() {
        logger.info("Installing keras_contrib for biomedicus python")
        val kcInstall = createProcessBuilder("-m", "pip", "install", kcDist.toString())
                .start()
        if (kcInstall.waitFor() != 0) {
            writeErrorStream(kcInstall)
            error("Non-zero exit code installing keras-contrib")
        }

        logger.info("Installing tensorflow for biomedicus python")
        val tfInstall = createProcessBuilder("-m", "pip", "install", "tensorflow")
                .start()
        if (tfInstall.waitFor() != 0) {
            writeErrorStream(tfInstall)
            error("Non-zero exit code installing tensorflow")
        }

        logger.info("Installing nlp-newt for biomedicus python")
        val newtInstall = createProcessBuilder("-m", "pip", "install", newtDist.toString())
                .start()
        if (newtInstall.waitFor() != 0) {
            writeErrorStream(newtInstall)
            error("Non-zero exit code installing nlp-newt")
        }

        logger.info("Installing biomedicus python")
        val bioInstall = createProcessBuilder("-m", "pip", "install", bioDist.toString())
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
}

@Singleton
class NewtDocumentsService @Inject constructor(
        @Setting("python.nlpnewt.doc_service.launch") launch: Boolean,
        @Setting("python.nlpnewt.doc_service.port") port: Int,
        @Setting("python.nlpnewt.doc_service.url") private val url: String
) {

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
        private val logger = LoggerFactory.getLogger(PythonValidation::class.java)
    }
}
