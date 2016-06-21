/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.uima.db;

import edu.umn.biomedicus.uima.type1_5.DocumentId;
import mockit.*;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    private DocumentId documentId;

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

    public void setFeatureNames() {
        Set<String> featureNames = new HashSet<>();
        featureNames.add("a");
        featureNames.add("b");
        Deencapsulation.setField(jdbcResultSetIterator, featureNames);
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