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

import edu.umn.biomedicus.common.types.text.Token;
import edu.umn.biomedicus.exc.BiomedicusException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Evaluate acronym expansion on the CASI data set without other biomedicus processing.
 * Used for quick-and-dirty checking of a model.
 *
 * Created by gpfinley on 10/4/16.
 */
public class AcronymVectorOfflineEvaluation {

  AcronymVectorOfflineEvaluation(Path expansionsModelPath, Path vectorSpacePath, Path senseMapPath,
      Path dataPath) throws BiomedicusException, IOException {

    AcronymExpansionsModel aem = new AcronymExpansionsModel.Loader(expansionsModelPath).loadModel();

    AcronymVectorModel avm = new AcronymVectorModel.Loader(null, false, vectorSpacePath,
        senseMapPath, true, aem).loadModel();

    int correct = 0;
    int total = 0;
    List<Result> results = new ArrayList<>();
    BufferedReader reader = new BufferedReader(new FileReader(dataPath.toFile()));
    String line;
    while ((line = reader.readLine()) != null) {
      String[] fields = line.split("\\|");
      String acronym = fields[0];
      String expansion = fields[1];
      if (acronym.equals("ITP") || expansion.equals("UNSURED SENSE")) {
        continue;
      }
      if (expansion.equals("GENERAL ENGLISH")) {
        expansion = acronym.toLowerCase();
      }
      int begin = Integer.parseInt(fields[3]);
      int end = Integer.parseInt(fields[4]);
      String contents = fields[6];

      String modifiedContents =
          contents.substring(0, begin) + " TOKENOFINTEREST " + contents.substring(end);

      List<Token> tokenList = new ArrayList<>();
      int tokenOfInterest = 0;
      int i = 0;
      for (String tokText : modifiedContents.split("\\W+")) {
        Token tok;
        if (tokText.equals("TOKENOFINTEREST")) {
          tokText = acronym;
          tok = new SimpleToken(tokText);
          tokenOfInterest = i;
        } else {
          tok = new SimpleToken(tokText);
        }
        tokenList.add(tok);
        i++;
      }

      String hyp = avm.findBestSense(tokenList, tokenOfInterest);
      results.add(new Result(acronym, expansion, hyp));
      if (hyp.equals(expansion)) {
        correct++;
      }
      System.out.format(
          "\r%d   %d   %.1f%%           %s     %s                                                         ",
          total, correct, 100. * correct / total, hyp, expansion);
      total++;
    }
    System.out.println();

    Map<String, List<Result>> resultsByAcronym = getResultsByAcronym(results);
    printConfusionByAcronym(resultsByAcronym);
    System.out.println(accuracyByAcronym(resultsByAcronym));

    System.out.println(((double) correct) / total);
  }

  public static void main(String[] args) throws BiomedicusException, IOException {
    Path expansionsModelPath = Paths.get(args[0]);
    Path vectorSpacePath = Paths.get(args[1]);
    Path senseMapPath = Paths.get(args[2]);
    Path dataPath = Paths.get(args[3]);
    new AcronymVectorOfflineEvaluation(expansionsModelPath, vectorSpacePath, senseMapPath,
        dataPath);
  }

  static double getAccuracy(Iterable<Result> results) {
    int correct = 0;
    int total = 0;
    for (Result result : results) {
      if (result.isCorrect()) {
        correct++;
      }
      total++;
    }
    return (double) correct / total;
  }

  static Map<String, Double> accuracyByAcronym(Map<String, List<Result>> resultsByAcronym) {
    Map<String, Double> acrAcc = new HashMap<>();
    for (Map.Entry<String, List<Result>> e : resultsByAcronym.entrySet()) {
      acrAcc.put(e.getKey(), getAccuracy(e.getValue()));
    }
    return acrAcc;
  }

  static void printConfusionByAcronym(Map<String, List<Result>> resultsByAcronym) {
    for (Map.Entry<String, List<Result>> e : resultsByAcronym.entrySet()) {
      String acronym = e.getKey();
      System.out.println(acronym);
      List<Result> results = e.getValue();
      Set<String> senses = new HashSet<>();
      results.forEach(result -> {
        senses.add(result.gold);
        senses.add(result.hyp);
      });
      int[][] mat = new int[senses.size()][senses.size()];
      List<String> listSenses = new ArrayList<>(senses);
      for (Result result : results) {
        mat[listSenses.indexOf(result.gold)][listSenses.indexOf(result.hyp)]++;
      }
      for (int i = 0; i < 40; i++) {
        System.out.print(" ");
      }
      listSenses.forEach(sense -> System.out.print("\t" + sense));
      System.out.println();
      for (int i = 0; i < mat.length; i++) {
        System.out.print(listSenses.get(i));
        for (int spaceCount = 0; spaceCount < 40 - listSenses.get(i).length(); spaceCount++) {
          System.out.print(" ");
        }
        for (int j = 0; j < mat.length; j++) {
          System.out.print("\t" + mat[i][j]);
        }
        System.out.println();
      }
    }
  }

  static Map<String, List<Result>> getResultsByAcronym(Iterable<Result> results) {
    Map<String, List<Result>> resultsByAcronym = new LinkedHashMap<>();
    for (Result result : results) {
      List<Result> theseResults = resultsByAcronym.get(result.acronym);
      if (theseResults == null) {
        theseResults = new ArrayList<>();
        resultsByAcronym.put(result.acronym, theseResults);
      }
      theseResults.add(result);
    }
    return resultsByAcronym;
  }

  private static class Result {

    final String acronym;
    final String gold;
    final String hyp;

    Result(String acronym, String gold, String hyp) {
      this.acronym = acronym;
      this.gold = gold;
      this.hyp = hyp;
    }

    boolean isCorrect() {
      return gold.equals(hyp);
    }
  }

  private class SimpleToken implements Token {

    final String text;

    SimpleToken(String text) {
      this.text = text;
    }

    @Override
    public String text() {
      return text;
    }

    @Override
    public boolean hasSpaceAfter() {
      return true;
    }
  }


}
