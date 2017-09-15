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
import java.io.InputStream;
import java.lang.Number;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * A tool for annotating measures from the number contexts written by {@link NumberContextWriter}.
 *
 * @author Ben Knoll
 * @since 1.8.0
 */
public class MeasuresAnnotationTool {

  private static final Pattern SPACE_SPLIT = Pattern.compile(" ");

  private static final Pattern NON_NUMBER = Pattern.compile("[0-9 ]*");

  private static final NumberFormat numberFormat = NumberFormat.getIntegerInstance();

  private final Path inputDir;

  private final Path outputDir;

  public MeasuresAnnotationTool(Path inputDir, Path outputDir) {
    this.inputDir = inputDir;
    this.outputDir = outputDir;
  }

  public void run() throws IOException {
    Path resume = outputDir.resolve("resume.txt");

    String resumeFileTemp = null;
    Integer resumeOnExampleTemp = null;
    if (Files.exists(resume)) {
      try (BufferedReader bufferedReader = Files
          .newBufferedReader(resume, StandardCharsets.UTF_8)) {
        resumeFileTemp = bufferedReader.readLine();
        resumeOnExampleTemp = Integer.parseInt(bufferedReader.readLine());
      }
    }

    String resumeFile = resumeFileTemp;
    Integer resumeOnExample = resumeOnExampleTemp;

    try (Scanner scanner = new Scanner(System.in)) {

      Files.walkFileTree(inputDir, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

          if (!file.toString().endsWith(".txt")) {
            return FileVisitResult.CONTINUE;
          }

          Path relativePath = inputDir.relativize(file);

          Path outputFile = outputDir.resolve(relativePath);

          long lines = -1;
          if (Files.exists(outputFile)) {
            if (resumeFile == null) {
              throw new IllegalStateException("Output file exists and no resume information: "
                  + outputFile);
            }

            if (!resumeFile.equals(outputDir.relativize(outputFile).toString())) {
              return FileVisitResult.CONTINUE;
            }

            lines = Files.lines(outputFile).count();
          }

          System.out.println("File: " + relativePath.toString());

          try (BufferedReader bufferedReader = Files
              .newBufferedReader(file, StandardCharsets.UTF_8);
              BufferedWriter bufferedWriter = Files.newBufferedWriter(outputFile,
                  StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
            if (resumeFile != null) {
              if (lines != resumeOnExample) {
                throw new IllegalStateException(
                    "Resume information does not match the number of existing lines: "
                        + outputFile);
              }

              for (int i = 0; i < resumeOnExample; i++) {
                bufferedReader.readLine();
                bufferedReader.readLine();
                bufferedReader.readLine();
              }
            }

            int examples = resumeOnExample != null ? resumeOnExample : 0;

            while (true) {
              System.out.println("Example #" + (examples + 1));

              Example example = new Example();
              if (!example.readAndPrintNextContext(bufferedReader)) {
                break;
              }
              System.out.flush();

              while (true) {
                try {
                  String s = example.tryCollect(scanner);
                  bufferedWriter.write(s);
                  bufferedWriter.newLine();
                  break;
                } catch (ExampleResponseException exampleResponseException) {
                  //
                } catch (UserQuitException e) {
                  try (BufferedWriter resumeWriter = Files
                      .newBufferedWriter(resume, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                          StandardOpenOption.TRUNCATE_EXISTING)) {
                    resumeWriter.write(outputDir.relativize(outputFile).toString());
                    resumeWriter.newLine();
                    resumeWriter.write("" + examples);
                    resumeWriter.newLine();
                  }
                  return FileVisitResult.TERMINATE;
                }
              }

              System.out.println();
              examples++;
            }
          }

          return FileVisitResult.CONTINUE;
        }
      });
    }
  }

  public static void main(String[] args) {
    Path config = Paths.get("measures.properties");

    Properties properties = new Properties();
    try (InputStream inStream = Files.newInputStream(config, StandardOpenOption.READ)) {
      properties.load(inStream);
    } catch (IOException e) {
      e.printStackTrace();
    }

    String inputDirString = properties.getProperty("inputDir");
    if (inputDirString == null) {
      System.out.println("Missing input directory.");
      return;
    }
    Path inputDir = Paths.get(inputDirString);
    Path outputDir = Paths.get(properties.getProperty("outputDir", "."));

    MeasuresAnnotationTool tool = new MeasuresAnnotationTool(inputDir, outputDir);

    try {
      tool.run();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static class Example {

    private int tokenCount = 1;

    private int printedCount = 0;

    private StringBuilder lineBuilder;

    private StringBuilder numbersBuilder;

    private boolean retry = false;
    private String[] beforeTokens;
    private String[] afterTokens;

    boolean readAndPrintNextContext(BufferedReader bufferedReader) throws IOException {
      String line = bufferedReader.readLine();

      if (line == null) {
        return false;
      }

      beforeTokens = SPACE_SPLIT.split(line);
      String[] numberTokens = SPACE_SPLIT.split(bufferedReader.readLine());
      afterTokens = SPACE_SPLIT.split(bufferedReader.readLine());

      lineBuilder = new StringBuilder();
      numbersBuilder = new StringBuilder();

      printTokens(beforeTokens);

      for (String numberToken : numberTokens) {
        lineBuilder.append(numberToken).append(' ');
        numbersBuilder.append('N').append(' ');
        for (int i = numbersBuilder.length(); i < lineBuilder.length(); i++) {
          numbersBuilder.append(' ');
        }
        checkFive();
      }

      printTokens(afterTokens);

      if (lineBuilder.length() > 0) {
        System.out.println(lineBuilder.toString());
        System.out.println(numbersBuilder.toString());
        System.out.println();
      }
      return true;
    }

    void printTokens(String[] tokens) {
      for (String beforeToken : tokens) {
        lineBuilder.append(beforeToken).append(' ');
        numbersBuilder.append(tokenCount++).append(' ');
        for (int i = numbersBuilder.length(); i < lineBuilder.length(); i++) {
          numbersBuilder.append(' ');
        }
        checkFive();
      }
    }

    void checkFive() {
      while (lineBuilder.length() < numbersBuilder.length()) {
        lineBuilder.append(' ');
      }
      if (++printedCount % 100000 == 0) {
        System.out.println(lineBuilder.toString());
        System.out.println(numbersBuilder.toString());
        System.out.println();
        lineBuilder = new StringBuilder();
        numbersBuilder = new StringBuilder();
      }
    }

    String tryCollect(Scanner scanner)
        throws ExampleResponseException, IOException, UserQuitException {
      retry = false;
      StringBuilder measureBuilder = new StringBuilder();

      System.out.print("Is this a measure? ");
      String s = scanner.nextLine();
      char firstChar = s.charAt(0);
      if (firstChar == 'Y' || firstChar == 'y') {
        measureBuilder.append('y');

        while (true) {
          System.out.print("Which tokens are part of the unit of measure? ");
          List<Integer> tokenIndexes = collectIds(scanner, measureBuilder);
          if (tokenIndexes != null) {
            measureBuilder.append("\t").append(grabTokens(tokenIndexes));
            break;
          }
          if (retry) {
            return tryCollect(scanner);
          }
          System.out.println(
              "Write each token id that is part of the unit of measure, separated by a space, "
                  + "or the letter u to undo your previous answer");
        }

        while (true) {
          System.out.print("Which tokens are annotations? ");
          List<Integer> tokenIndexes = collectIds(scanner, measureBuilder);
          if (tokenIndexes != null) {
            measureBuilder.append("\t").append(grabTokens(tokenIndexes));
            break;
          }
          if (retry) {
            return tryCollect(scanner);
          }
          System.out
              .println("Write each token id that is an annotation, separated by a space, "
                  + "or the letter u to undo your previous answer");
        }
      } else if (firstChar == 'N' || firstChar == 'n') {
        measureBuilder.append('n');
      } else if (firstChar == 'U' || firstChar == 'u') {
        measureBuilder.append('u');
      } else if (firstChar == 'Q' || firstChar == 'q') {
        throw new UserQuitException();
      } else {
        System.out
            .println("Respond y for yes, n for no, u for don't know / ambiguous, q to quit");
        throw new ExampleResponseException();
      }

      return measureBuilder.toString();
    }

    private String grabTokens(List<Integer> indexes) {
      StringBuilder stringBuilder = new StringBuilder();

      for (Integer index : indexes) {
        if (stringBuilder.length() > 0) {
          stringBuilder.append(' ');
        }
        if (index <= beforeTokens.length) {
          stringBuilder.append(beforeTokens[index - 1]);
        } else {
          int i = index - beforeTokens.length;
          stringBuilder.append(afterTokens[i - 1]);
        }
      }

      return stringBuilder.toString();
    }

    private List<Integer> collectIds(Scanner scanner, StringBuilder measureBuilder) {
      String tokens = scanner.nextLine();

      if (tokens.length() == 0) {
        return Collections.emptyList();
      }

      if ("u".equals(tokens)) {
        retry = true;
        return null;
      }

      if (!NON_NUMBER.matcher(tokens).matches()) {
        return null;
      }

      String[] numbers = SPACE_SPLIT.split(tokens);
      List<Integer> results = new ArrayList<>(numbers.length);
      for (String number : numbers) {
        try {
          Number parse = numberFormat.parse(number);
          int i = parse.intValue();
          if (i > 0 && i < tokenCount) {
            results.add(i);
          } else {
            return null;
          }
        } catch (ParseException e) {
          return null;
        }
      }
      return results;
    }
  }

  private static class ExampleResponseException extends Exception {

  }

  private static class UserQuitException extends Exception {

  }
}
