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
import mockit.*;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.jcas.JCas;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class JdbcResultSetIteratorTest {
    @Tested
    private JdbcResultSetIterator jdbcResultSetIterator;

    @Injectable
    private ResultSet resultSet;

    @Injectable
    private String analyzerVersion;

    @Mocked
    private ClinicalNoteAnnotation clinicalNoteAnnotation;

    @Test
    public void testHasNext() throws Exception {
        new StrictExpectations() {{
            resultSet.next(); result = true;
        }};

        Assert.assertEquals(true, jdbcResultSetIterator.hasNext());
    }

    @Test
    public void testNotHasNext() throws Exception {
        new StrictExpectations() {{
            resultSet.next(); result = false;
        }};

        Assert.assertEquals(false, jdbcResultSetIterator.hasNext());
    }

    @Test
    public void testPopulateNextSystemViewPopulatesMetadata(@Mocked final JCas systemView,
                                                            @Mocked final TypeSystem typeSystem,
                                                            @Mocked final Type type,
                                                            @Mocked final Feature feature,
                                                            @Mocked final ResultSetMetaData resultSetMetaData) throws Exception {
        Deencapsulation.setField(jdbcResultSetIterator, "metaDataFeatureShortNames", null);

        new NonStrictExpectations() {{
            typeSystem.getType(anyString); result = type;
            type.getFeatures(); result = Arrays.asList(feature, feature, feature);
            feature.getShortName(); returns("a", "b", "c");
            resultSet.getMetaData(); result = resultSetMetaData;
            resultSetMetaData.getColumnCount(); result = 3;
            resultSetMetaData.getColumnName(1); result = "a";
            resultSetMetaData.getColumnName(2); result = "b";
            resultSetMetaData.getColumnName(3); result = "d";
            new HashSet<>();
        }};

        jdbcResultSetIterator.populateNextSystemView(systemView);

        Set<String> metadataFeatureShortNames = Deencapsulation.getField(jdbcResultSetIterator, "metaDataFeatureShortNames");
        Assert.assertEquals(2, metadataFeatureShortNames.size());
        Assert.assertTrue(metadataFeatureShortNames.contains("a"));
        Assert.assertTrue(metadataFeatureShortNames.contains("b"));

        new Verifications() {{
            resultSetMetaData.getColumnName(anyInt); times = 3;
        }};
    }

    @Test
    public void testPopulateNextSystemViewSkipMetadata(@Mocked final JCas systemView) throws Exception {
        setFeatureNames();

        jdbcResultSetIterator.populateNextSystemView(systemView);

        new Verifications() {{
            resultSet.getMetaData();
            times = 0;
        }};
    }

    public void setFeatureNames() {
        Set<String> featureNames = new HashSet<>();
        featureNames.add("a");
        featureNames.add("b");
        Deencapsulation.setField(jdbcResultSetIterator, featureNames);
    }

    @Test
    public void testPopulateNextSystemViewNoteText(@Mocked final JCas systemView) throws Exception {
        setFeatureNames();

        new NonStrictExpectations() {{
            resultSet.getString("note_text"); result = "blah";
            systemView.setDocumentText("blah");
        }};

        jdbcResultSetIterator.populateNextSystemView(systemView);
    }

    @Test
    public void testPopulateNextSystemViewNoteTextTrimmed(@Mocked final JCas systemView) throws Exception {
        setFeatureNames();

        new NonStrictExpectations() {{
            resultSet.getString("note_text"); result = "blah ";
            systemView.setDocumentText("blah");
        }};

        jdbcResultSetIterator.populateNextSystemView(systemView);
    }

    @Test
    public void testPopulateNextSystemViewClinicalNoteAnnotation(@Mocked final JCas systemView,
                                                                 @Mocked final Type type,
                                                                 @Mocked final Feature feature) throws Exception {
        setFeatureNames();

        new NonStrictExpectations() {{
            resultSet.getString("a"); result = "a_val";
            resultSet.getString("b"); result = "b_val";
        }};

        jdbcResultSetIterator.populateNextSystemView(systemView);

        new VerificationsInOrder() {{
            type.getFeatureByBaseName("a");
            clinicalNoteAnnotation.setStringValue(feature, "a_val");
            type.getFeatureByBaseName("b");
            clinicalNoteAnnotation.setStringValue(feature, "b_val");
        }};

        new Verifications() {{
            clinicalNoteAnnotation.setRetrievalTime(anyString);
            clinicalNoteAnnotation.setAnalyzerVersion(anyString);
            clinicalNoteAnnotation.addToIndexes();
        }};
    }

    @Test
    public void testClose() throws Exception {
        jdbcResultSetIterator.close();
        new FullVerificationsInOrder() {{
            resultSet.close();
        }};
    }

    @Test(expectedExceptions = IOException.class)
    public void testCloseThrows() throws Exception {
        new Expectations() {{
            resultSet.close(); result = new SQLException();
        }};
        jdbcResultSetIterator.close();
    }
}