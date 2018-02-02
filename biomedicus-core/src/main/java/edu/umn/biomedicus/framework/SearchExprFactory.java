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

package edu.umn.biomedicus.framework;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory class providing for instantiation of {@link SearchExpr} objects using only their
 * expression.
 *
 * @since 1.6.0
 */
@Singleton
public class SearchExprFactory {

  private final LabelAliases labelAliases;

  @Inject
  public SearchExprFactory(LabelAliases labelAliases) {
    this.labelAliases = labelAliases;
  }

  /**
   * Parses the search expression into a graph so it can be queried against documents.
   *
   * @param expr the string expression
   * @return the search expression graph object that can be used to search documents
   */
  public SearchExpr parse(String expr) {
    return SearchExpr.parse(labelAliases, expr);
  }
}
