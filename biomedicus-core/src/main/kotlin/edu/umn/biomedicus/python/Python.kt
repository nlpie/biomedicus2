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
import java.nio.file.attribute.PosixFilePermission
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton


@Singleton
class PythonEnvironment @Inject constructor(
        @Setting("biomedicus.paths.home") val home: Path,
        @Setting("python.home") pyHome: String,
        @Setting("python.executable") pyExec: String
) {
    private val pythonHome: Path = home.resolve(pyHome)

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
