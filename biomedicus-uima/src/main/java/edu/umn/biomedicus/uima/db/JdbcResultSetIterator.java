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

import edu.umn.biomedicus.type.ClinicalNoteAnnotation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.jcas.JCas;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * File for iterating over the document results returned from a sql query.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class JdbcResultSetIterator implements Closeable {
    /**
     * Class logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(JdbcResultSetIterator.class);

    /**
     * Date formatter for current date.
     */
    private final DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.LONG);

    /**
     * The sql result set that we are iterating over.
     */
    private final ResultSet resultSet;

    /**
     * The biomedicus version, so we can write it to the document metadata.
     */
    private final String analyzerVersion;

    /**
     * The features that exist on the {@link ClinicalNoteAnnotation} object and as columns in the query.
     */
    @Nullable
    private Set<String> metaDataFeatureShortNames;

    /**
     * Creates a new {@code JdbcResultSetIterator} from the result set and with the analyzer version.
     *
     * @param resultSet       result set to iterate over
     * @param analyzerVersion analyzer version to store in document annotations
     */
    public JdbcResultSetIterator(ResultSet resultSet, String analyzerVersion) {
        this.resultSet = resultSet;
        this.analyzerVersion = analyzerVersion;
    }

    /**
     * Return whether or not there is another result.
     *
     * @return true if there is another result, false otherwise.
     * @throws SQLException rethrown
     */
    public boolean hasNext() throws SQLException {
        LOGGER.trace("checking if result set has more results");
        return resultSet.next();
    }

    /**
     * Uses the current row in the result set to populate a {@code JCas systemView}.
     *
     * @param systemView the systemView to populate
     * @throws SQLException rethrown
     */
    public void populateNextSystemView(JCas systemView) throws SQLException {
        LOGGER.trace("populating a system view with cas");
        if (metaDataFeatureShortNames == null) {
            Type type = systemView.getTypeSystem().getType(ClinicalNoteAnnotation.class.getCanonicalName());
            Set<String> featureShortNames = type.getFeatures().stream()
                    .map(Feature::getShortName)
                    .collect(Collectors.toSet());

            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            metaDataFeatureShortNames = new HashSet<>();
            for (int column = 1; column <= columnCount; column++) {
                String columnName = metaData.getColumnName(column);
                if (featureShortNames.contains(columnName)) {
                    metaDataFeatureShortNames.add(columnName);
                }
            }
        }

        String documentText = resultSet.getString("note_text");
        if (documentText == null) {
            documentText = "";
        } else {
            documentText = documentText.trim();
        }
        systemView.setDocumentText(documentText);

        ClinicalNoteAnnotation documentAnnotation = new ClinicalNoteAnnotation(systemView);
        Type type = documentAnnotation.getType();
        for (String featureShortName : metaDataFeatureShortNames) {
            Feature feature = type.getFeatureByBaseName(featureShortName);
            documentAnnotation.setStringValue(feature, resultSet.getString(featureShortName));
        }
        documentAnnotation.setRetrievalTime(dateFormatter.format(new Date()));
        documentAnnotation.setAnalyzerVersion(analyzerVersion);
        documentAnnotation.addToIndexes();
    }

    /**
     * Closes the result set.
     *
     * @throws IOException if we fail to close the result set.
     */
    @Override
    public void close() throws IOException {
        LOGGER.trace("closing result set");
        try {
            resultSet.close();
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }
}
