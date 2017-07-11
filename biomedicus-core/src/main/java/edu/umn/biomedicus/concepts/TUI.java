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

package edu.umn.biomedicus.concepts;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

/**
 *
 */
public class TUI {

  public static final Pattern TUI_PATTERN = Pattern.compile("T([\\d]{3})");

  private final int identifier;

  public TUI(int identifier) {
    this.identifier = identifier;
  }

  public TUI(String wordForm) {
    Matcher matcher = TUI_PATTERN.matcher(wordForm);
    if (matcher.find()) {
      String identifier = matcher.group(1);
      this.identifier = Integer.parseInt(identifier);
    } else {
      throw new IllegalArgumentException("Word form does not match TUI pattern");
    }
  }

  public int identifier() {
    return identifier;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    TUI tui = (TUI) o;

    return identifier == tui.identifier;

  }

  @Override
  public int hashCode() {
    return identifier;
  }

  @Override
  public String toString() {
    return String.format("T%03d", identifier);
  }
}
