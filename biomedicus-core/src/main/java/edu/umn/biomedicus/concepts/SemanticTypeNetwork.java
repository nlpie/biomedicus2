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

package edu.umn.biomedicus.concepts;

import com.google.inject.ProvidedBy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 */
@ProvidedBy(SemanticTypeNetworkLoader.class)
public class SemanticTypeNetwork {

  private static final Pattern PIPE = Pattern.compile("\\|");

  private final Map<TUI, TN> tuiToTN;

  private final Map<TUI, String> semanticTypeGroupMapping;

  public SemanticTypeNetwork(Map<TUI, TN> tuiToTN, Map<TUI, String> semanticTypeGroupMapping) {
    this.tuiToTN = tuiToTN;
    this.semanticTypeGroupMapping = semanticTypeGroupMapping;
  }

  public static SemanticTypeNetwork loadFromFiles(Path srdefPath, Path semgroupsPath)
      throws IOException {
    Map<TUI, TN> tuiToTN = Files.lines(srdefPath)
        .map(PIPE::split)
        .map(SrDefLine::new)
        .collect(Collectors.toMap(SrDefLine::ui, SrDefLine::tn));

    Map<TUI, String> semanticTypeGroupMapping = Files.lines(semgroupsPath)
        .map(PIPE::split)
        .map(SemGroupLine::new)
        .collect(Collectors.toMap(SemGroupLine::tui, SemGroupLine::group));

    return new SemanticTypeNetwork(tuiToTN, semanticTypeGroupMapping);
  }

  public boolean isa(TUI first, TUI second) {
    return tuiToTN.get(first).isA(tuiToTN.get(second));
  }

  public String getSemanticTypeGroup(TUI tui) {
    return semanticTypeGroupMapping.get(tui);
  }

  private static class SrDefLine {

    private final TUI ui;

    private final TN tn;

    public SrDefLine(String[] line) {
      ui = new TUI(line[1]);
      tn = new TN(line[3]);
    }

    public TUI ui() {
      return ui;
    }

    public TN tn() {
      return tn;
    }
  }

  private static class SemGroupLine {

    private final String group;

    private final TUI tui;

    public SemGroupLine(String[] line) {
      group = line[0];
      tui = new TUI(line[2]);
    }

    public String group() {
      return group;
    }

    public TUI tui() {
      return tui;
    }
  }
}
