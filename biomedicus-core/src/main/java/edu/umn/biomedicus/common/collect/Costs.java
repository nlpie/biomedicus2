/*
 * Copyright (c) 2017 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.common.collect;

/**
 *
 */
public class Costs {

  public static final Costs LEVENSHTEIN = new Costs(0, 1, 1, 1);

  private final int match;

  private final int replace;

  private final int delete;

  private final int insert;

  public Costs(int match, int replace, int delete, int insert) {
    this.match = match;
    this.replace = replace;
    this.delete = delete;
    this.insert = insert;
  }

  public int getMatch() {
    return match;
  }

  public int getReplace() {
    return replace;
  }

  public int getDelete() {
    return delete;
  }

  public int getInsert() {
    return insert;
  }
}
