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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.annotation.Nonnull;
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
  @Nonnull
  public SearchExpr parse(@Nonnull String expr) {
    return SearchExpr.parse(labelAliases, expr);
  }

  /**
   * Reads the file at the specified path and parses it into a Search Expression.
   *
   * @param path the path of the file to read
   * @param charset the charset to use
   * @return SearchExpr from the file
   * @throws IOException if there is an error reading the file.
   */
  @Nonnull
  public SearchExpr readAndParse(
      @Nonnull String path,
      @Nonnull Charset charset
  ) throws IOException {
    return SearchExpr.parse(labelAliases, new String(Files.readAllBytes(Paths.get(path)), charset));
  }

  /**
   * Reads the file at the specified path and parses it into a search expression using the default
   * charset.
   *
   * @param path the path of the file to read
   * @return SearchExpr from the file
   * @throws IOException if there is an error finding or reading the file.
   */
  @Nonnull
  public SearchExpr readAndParse(
      @Nonnull String path
  ) throws IOException {
    return readAndParse(path, Charset.defaultCharset());
  }
}
