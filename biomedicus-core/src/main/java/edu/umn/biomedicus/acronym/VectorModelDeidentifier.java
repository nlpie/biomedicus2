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

package edu.umn.biomedicus.acronym;

import edu.umn.biomedicus.exc.BiomedicusException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * Main class to perform offline de-identification of a vector model.
 *
 * Created by gpfinley on 10/17/16.
 */
public class VectorModelDeidentifier {

  /**
   * Remove all words from an acronym vector model not present in a file of words to keep (one word
   * per line).
   *
   * @param args path to expansions model; path to vector space; path to sense map; path to a file
   * of words to keep (one word per line); directory to export model components to
   */
  public static void main(String[] args) throws BiomedicusException, IOException {
    String expansionsModelPath = args[0];
    String vectorSpacePath = args[1];
    String senseMapPath = args[2];
    String keepWordsFile = args[3];
    String outDir = args[4];
    AcronymExpansionsModel aem = new AcronymExpansionsModel.Loader(Paths.get(expansionsModelPath))
        .loadModel();
    AcronymVectorModel avm = new AcronymVectorModel.Loader(null, false, Paths.get(vectorSpacePath),
        Paths.get(senseMapPath), false, aem).loadModel();

    Set<String> keepWords = new HashSet<>(Files.readAllLines(Paths.get(keepWordsFile)));
    avm.removeWordsExcept(keepWords);

    avm.writeToDirectory(Paths.get(outDir), null);
  }
}