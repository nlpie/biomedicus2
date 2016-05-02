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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

/**
 *
 */
public class JdbcPagesIterator implements Iterator<JdbcResultSetIterator> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcPagesIterator.class);

    private final int pageSize;
    private final int totalResults;
    private final PreparedStatement statement;
    private final String analyzerVersion;
    private final int pages;

    private int currentPage;

    public JdbcPagesIterator(int pageSize, int totalResults, PreparedStatement statement, String analyzerVersion) {
        this.pageSize = pageSize;
        this.totalResults = totalResults;
        this.statement = statement;
        this.analyzerVersion = analyzerVersion;
        currentPage = 0;
        pages = totalResults / pageSize + (totalResults % pageSize > 0 ? 1 : 0);
    }

    @Override
    public boolean hasNext() {
        LOGGER.debug("Checking if there is another page of results");
        return currentPage < pages;
    }

    @Override
    public JdbcResultSetIterator next() {
        LOGGER.debug("Getting next page of results, current page: {}", currentPage);
        try {
            statement.clearParameters();
            int start = currentPage * pageSize;
            statement.setInt(1, Math.min(totalResults, start + pageSize));
            statement.setInt(2, start + 1);
            ResultSet resultSet = statement.executeQuery();
            currentPage++;
            return new JdbcResultSetIterator(resultSet, analyzerVersion);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
