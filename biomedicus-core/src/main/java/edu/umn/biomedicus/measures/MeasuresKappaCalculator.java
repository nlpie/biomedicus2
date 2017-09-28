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

package edu.umn.biomedicus.measures;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * Calculates pairwise Cohen Kappa and Fleiss' Kappa for all raters.
 *
 * @author Ben Knoll
 * @since 1.8.0
 * @deprecated using gate for kappa for this
 */
@Deprecated
public class MeasuresKappaCalculator {

  private static final Pattern SPACE = Pattern.compile(" ");

  private static final Pattern TAB = Pattern.compile("\t");

  private final Path originalsFolder;

  private final List<Path> raterFolders;

  private final List<RaterResponses> raterResponses = new ArrayList<>();

  public MeasuresKappaCalculator(Path originalsFolder, List<Path> raterFolders) {
    this.originalsFolder = originalsFolder;
    this.raterFolders = raterFolders;
    for (Path ignored : raterFolders) {
      raterResponses.add(new RaterResponses());
    }
  }

  public void compute() throws IOException {


    List<List<String>> allWords = new ArrayList<>();
    List<String> numbers = new ArrayList<>();

    Files.walkFileTree(originalsFolder, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Path relativePath = originalsFolder.relativize(file);

        List<BufferedReader> raterReaders = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
          for (Path raterFolder : raterFolders) {
            Path raterResponsePath = raterFolder.resolve(relativePath);
            if (Files.notExists(raterResponsePath)) {
              return FileVisitResult.CONTINUE;
            }
            raterReaders.add(Files.newBufferedReader(raterResponsePath));
          }

          String line;

          while ((line = reader.readLine()) != null) {
            Set<String> wordsSet = new HashSet<>();
            SPACE.splitAsStream(line).forEach(wordsSet::add);
            numbers.add(reader.readLine());
            SPACE.splitAsStream(reader.readLine()).forEach(wordsSet::add);
            List<String> words = new ArrayList<>(wordsSet);
            allWords.add(words);

            for (int rater = 0; rater < raterReaders.size(); rater++) {
              ExampleResponse exampleResponse = new ExampleResponse();
              raterResponses.get(rater).exampleResponses.add(exampleResponse);
              BufferedReader raterReader = raterReaders.get(rater);
              String isMeasureString = raterReader.readLine();
              if (isMeasureString == null) {

                continue;
              }
              String[] tabs = TAB.split(isMeasureString);
              String response = tabs[0];
              switch (response) {
                case "y":
                  exampleResponse.isMeasure = true;
                  break;
                case "n":
                  exampleResponse.isMeasure = false;
                  break;
                case "u":
                case "u ":
                  exampleResponse.isMeasure = null;
                  break;
                default:
                  System.out.println("Unexpected response: " + response);
                  break;
              }

              Set<String> units;
              if (tabs.length > 1) {
                units = new HashSet<>(Arrays.asList(SPACE.split(tabs[1])));
              } else {
                units = Collections.emptySet();
              }
              Set<String> annotations;
              if (tabs.length > 2) {
                annotations = new HashSet<>(Arrays.asList(SPACE.split(tabs[2])));
              } else {
                annotations = Collections.emptySet();
              }
              for (String word : words) {
                exampleResponse.wordIsUnitResponses.add(units.contains(word));
                exampleResponse.wordIsAnnotationResponses.add(annotations.contains(word));
              }
            }
          }
        } finally {
          for (BufferedReader reader : raterReaders) {
            reader.close();
          }
        }

        return FileVisitResult.CONTINUE;
      }
    });

    int examples = raterResponses.get(0).exampleResponses.size();

    Set<Integer> struckExamples = new HashSet<>();
    for (RaterResponses raterResponse : raterResponses) {
      for (int i = 0; i < examples; i++) {
        if (raterResponse.exampleResponses.get(i).isMeasure == null) {
          struckExamples.add(i);
        }
      }
    }

    int[] examplesArr = new int[raterResponses.size() * examples];
    List<Integer> unitsTokens = new ArrayList<>();
    List<Integer> subjectsTokens = new ArrayList<>();

    System.out.println();
    for (int i = 0; i < raterResponses.size(); i++) {
      for (int j = i + 1; j < raterResponses.size(); j++) {
        Agreements exampleAgreements = new Agreements();
        Agreements unitAgreements = new Agreements();
        Agreements annotationAgreements = new Agreements();

        for (int k = 0; k < examples; k++) {
          String number = numbers.get(k);
          List<String> words = allWords.get(k);

          if (struckExamples.contains(k)) {
            continue;
          }

          ExampleResponse firstResponse = raterResponses.get(i).exampleResponses.get(k);
          ExampleResponse secondResponse = raterResponses.get(j).exampleResponses.get(k);

          if (firstResponse.isMeasure) {
            if (secondResponse.isMeasure) {
              exampleAgreements.positiveAgree++;
            } else {
              exampleAgreements.firstPositiveDisagree++;
            }
          } else {
            if (secondResponse.isMeasure) {
              exampleAgreements.secondPositiveDisagree++;
            } else {
              exampleAgreements.negativeAgree++;
            }
          }

          for (int l = 0; l < firstResponse.wordIsUnitResponses.size(); l++) {
            String word = words.get(l);
            if (firstResponse.wordIsUnitResponses.get(l)) {
              if (secondResponse.wordIsUnitResponses.get(l)) {
                unitAgreements.positiveAgree++;
              } else {
                unitAgreements.firstPositiveDisagree++;
              }
            } else {
              if (secondResponse.wordIsUnitResponses.get(l)) {
                unitAgreements.secondPositiveDisagree++;
              } else {
                unitAgreements.negativeAgree++;
              }
            }

            if (firstResponse.wordIsAnnotationResponses.get(l)) {
              if (secondResponse.wordIsAnnotationResponses.get(l)) {
                annotationAgreements.positiveAgree++;
              } else {
                annotationAgreements.firstPositiveDisagree++;
              }
            } else {
              if (secondResponse.wordIsAnnotationResponses.get(l)) {
                annotationAgreements.secondPositiveDisagree++;
              } else {
                annotationAgreements.negativeAgree++;
              }
            }
          }
        }

        System.out.println("Between raters " + raterFolders.get(i).getFileName().toString()
            + " and " + raterFolders.get(j).getFileName().toString());
        System.out.println("'Is measure' Cohen Kappa: " + exampleAgreements.getCohenKappa());
        System.out.println("'Units' Cohen Kappa: " + unitAgreements.getCohenKappa());
        System.out.println("'Annotations' Cohen Kappa: " + annotationAgreements.getCohenKappa());
      }
    }

    try (BufferedWriter bufferedWriter = Files
        .newBufferedWriter(Paths.get("raters.txt"), StandardOpenOption.CREATE_NEW)) {
      for (int i = 0; i < raterResponses.size(); i++) {
        for (int j = 0; j < examples; j++) {

        }
      }
    }
  }


  public static void main(String args[]) {
    if (args.length < 3) {
      System.out.println("As arguments, pass the folder of the original number outputs, the ");
    }
    assert args.length > 2;
    Path originals = Paths.get(args[0]);
    List<Path> raterFolders = new ArrayList<>();
    for (int i = 1; i < args.length; i++) {
      raterFolders.add(Paths.get(args[i]));
    }

    MeasuresKappaCalculator measuresKappaCalculator = new MeasuresKappaCalculator(originals,
        raterFolders);

    try {
      measuresKappaCalculator.compute();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public class RaterResponses {

    private List<ExampleResponse> exampleResponses = new ArrayList<>();
  }

  public class ExampleResponse {

    private Boolean isMeasure = null;
    private List<Boolean> wordIsUnitResponses = new ArrayList<>();
    private List<Boolean> wordIsAnnotationResponses = new ArrayList<>();
  }

  public class Agreements {

    int positiveAgree = 0;
    int negativeAgree = 0;
    int firstPositiveDisagree = 0;
    int secondPositiveDisagree = 0;


    double getCohenKappa() {
      int total = positiveAgree + negativeAgree + firstPositiveDisagree + secondPositiveDisagree;
      double proportionateAgree = (positiveAgree + negativeAgree) / (double) total;
      double expectedPositive = (positiveAgree + firstPositiveDisagree) / (double) total *
          (positiveAgree + secondPositiveDisagree) / (double) total;
      double expectedNegative = (negativeAgree + secondPositiveDisagree) / (double) total *
          (negativeAgree + firstPositiveDisagree) / (double) total;

      double agreeExpected = expectedPositive + expectedNegative;
      return (proportionateAgree - agreeExpected) / (1 - agreeExpected);
    }
  }

}
