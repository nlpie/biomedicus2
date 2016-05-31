/*
 * Copyright (c) 2015 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.uima.db;

import edu.umn.biomedicus.uima.common.Views;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.FileUtils;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.sql.*;

/**
 *
 */
public class SimpleJdbcReader extends CollectionReader_ImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleJdbcReader.class);

    /**
     * The JDBC driver class to use.
     */
    public static final String PARAM_JDBC_DRIVER = "jdbcDriver";

    /**
     * The full JDBC url for connecting to the database.
     */
    public static final String PARAM_JDBC_URL = "jdbcUrl";

    /**
     * The username used by JDBC to connect to the database
     */
    public static final String PARAM_JDBC_USER = "jdbcUser";

    /**
     * The password used by JDBC to connect to the database
     */
    public static final String PARAM_JDBC_PASSWORD = "jdbcPassword";

    /**
     * The file containing the SQL statement to use
     */
    public static final String PARAM_QUERY_FILE = "queryFile";

    /**
     * The total number of results this should fetch
     */
    public static final String PARAM_TOTAL_RESULTS = "totalResults";

    /**
     * The current row in the results.
     */
    private int currentRow = 0;

    /**
     * The total number of results.
     */
    @Nullable
    private Integer totalResults;

    /**
     * The connection to the sql database.
     */
    @Nullable
    private Connection conn;

    /**
     * The sql statement.
     */
    @Nullable
    private PreparedStatement statement;

    /**
     * The result set iterator.
     */
    @Nullable
    private JdbcResultSetIterator jdbcResultSetIterator;

    @Override
    public void initialize() throws ResourceInitializationException {
        LOGGER.debug("initializing jdbc collection reader");
        super.initialize();

        try {
            Class.forName((String) getConfigParameterValue(PARAM_JDBC_DRIVER));
        } catch (ClassNotFoundException e) {
            throw new ResourceInitializationException(e);
        }

        String analyzerVersion = getMetaData().getVersion();

        String databaseURL = (String) getConfigParameterValue(PARAM_JDBC_URL);
        String databaseUser = (String) getConfigParameterValue(PARAM_JDBC_USER);
        String databasePassword = (String) getConfigParameterValue(PARAM_JDBC_PASSWORD);
        String databaseQueryFile = (String) getConfigParameterValue(PARAM_QUERY_FILE);

        totalResults = (Integer) getConfigParameterValue(PARAM_TOTAL_RESULTS);

        String databaseQuery;
        try {
            databaseQuery = FileUtils.file2String(new File(databaseQueryFile), "UTF-8");
        } catch (IOException e) {
            throw new ResourceInitializationException(e);
        }

        LOGGER.info(databaseQuery);

        currentRow = 0;

        ResultSet resultSet;
        try {
            conn = DriverManager.getConnection(databaseURL, databaseUser, databasePassword);
            statement = conn.prepareStatement(databaseQuery);
            resultSet = statement.executeQuery();
        } catch (SQLException e) {
            throw new ResourceInitializationException(e);
        }

        jdbcResultSetIterator = new JdbcResultSetIterator(resultSet, analyzerVersion);
    }

    @Override
    public void getNext(CAS aCAS) throws IOException, CollectionException {
        assert jdbcResultSetIterator != null;
        LOGGER.trace("getting next document from result set");
        JCas systemView;
        try {
            CAS aCASView = aCAS.createView(Views.SYSTEM_VIEW);
            systemView = aCASView.getJCas();
        } catch (CASException e) {
            throw new CollectionException(e);
        }

        try {
            jdbcResultSetIterator.populateNextSystemView(systemView);
            currentRow++;
            if (currentRow % 1000 == 0) {
                LOGGER.debug("Documents read: {}", currentRow);
            }
        } catch (SQLException e) {
            throw new CollectionException(e);
        }
    }

    @Override
    public boolean hasNext() throws IOException, CollectionException {
        assert totalResults != null;
        assert jdbcResultSetIterator != null;
        if (totalResults == currentRow) {
            return false;
        }

        try {
            return jdbcResultSetIterator.hasNext();
        } catch (SQLException e) {
            throw new CollectionException(e);
        }
    }

    @Override
    public Progress[] getProgress() {
        assert totalResults != null;
        LOGGER.trace("Progress: {}", currentRow);
        return new Progress[]{new ProgressImpl(currentRow, totalResults, Progress.ENTITIES, true)};
    }

    /**
     * Tries to close the result set
     *
     * @throws IOException if we fail to close the result set
     */
    @Override
    public void close() throws IOException {
        LOGGER.info("Finished reading, closing");
        try {
            if (jdbcResultSetIterator != null) {
                jdbcResultSetIterator.close();
            }
            assert statement != null;
            statement.close();
            assert conn != null;
            conn.close();
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }
}
