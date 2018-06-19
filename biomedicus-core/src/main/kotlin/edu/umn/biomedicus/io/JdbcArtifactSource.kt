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

package edu.umn.biomedicus.io

import edu.umn.biomedicus.annotations.ComponentSetting
import edu.umn.nlpengine.Artifact
import edu.umn.nlpengine.ArtifactSource
import edu.umn.nlpengine.StandardArtifact
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement
import java.util.*
import javax.inject.Inject

/**
 * A document source that pulls from a database using JDBC.
 */
class JdbcArtifactSource @Inject internal constructor(
        @ComponentSetting("documentName") private val documentName: String,
        @ComponentSetting("configFile") private val configFile: Path
) : ArtifactSource {

    private val connection: Connection

    private val statement: Statement

    private val resultSet: ResultSet

    private val idColumn: String

    private val textColumn: String

    private val metadataMappings: Map<String, String>

    private val size: Int

    init {
        @Suppress("UNCHECKED_CAST")
        val config: Map<String, Any> = FileInputStream(configFile.toFile()).use {
            Yaml().load(it) as Map<String, Any>
        }

        val metadataMap = config["metadataMap"]
        if (metadataMap is Map<*, *>) {
            @Suppress("UNCHECKED_CAST")
            this.metadataMappings = metadataMap as Map<String, String>
        } else {
            this.metadataMappings = emptyMap()
        }

        idColumn = config["idColumn"] as String
        textColumn = config["textColumn"] as String

        Class.forName(config["driver"] as String)

        val props = Properties()
        @Suppress("UNCHECKED_CAST")
        props.putAll(config["properties"] as Map<out Any, Any>)

        connection = DriverManager.getConnection(config["url"] as String, props)
        statement = connection.createStatement()
        val queryText = File(config["queryFile"] as String).readText()
        resultSet = statement.executeQuery(queryText)

        size = config["size"] as Int
    }

    override fun tryAdvance(consumer: (Artifact) -> Unit): Boolean {
        if (resultSet.next()) {
            val id = resultSet.getString(idColumn)

            val artifact = StandardArtifact(id)

            metadataMappings.forEach { column, target ->
                artifact.metadata[target] = resultSet.getString(column)
            }

            val text = resultSet.getString(textColumn)

            artifact.addDocument(documentName, text)

            consumer.invoke(artifact)

            return true
        }

        return false
    }

    override fun estimateTotal(): Long {
        return size.toLong()
    }

    override fun close() {
        try {
            resultSet.close()
        } catch (e: Exception) {

        }
        try {
            statement.close()
        } catch (e: Exception) {

        }
        try {
            connection.close()
        } catch (e: Exception) {

        }
    }
}
