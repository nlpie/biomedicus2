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

package edu.umn.biomedicus.measures;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Number;
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

  private String resumeFile;

  private int resumeIndex;

  private DocumentAnnotations lastDocument;

  public MeasuresAnnotationTool(Path inputDir, Path outputDir) {
    this.inputDir = inputDir;
    this.outputDir = outputDir;
  }

  public void run() throws IOException {
    resumeFile = null;
    if (Files.exists(resumeFile())) {
      try (BufferedReader bufferedReader = Files.newBufferedReader(resumeFile(), UTF_8)) {
        resumeFile = bufferedReader.readLine();
        String s = bufferedReader.readLine();
        resumeIndex = Integer.parseInt(s);
      }
    }

    try (Scanner scanner = new Scanner(System.in)) {
      Files.walkFileTree(inputDir, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          if (!file.toString().endsWith(".txt")) {
            return FileVisitResult.CONTINUE;
          }

          lastDocument = new DocumentAnnotations(file, inputDir, outputDir);
          if (!lastDocument.checkResume(resumeFile, resumeIndex)) {
            if (lastDocument.terminate) {
              System.out.println("Resume data is inconsistent with existing files in the output "
                  + "directory. Seek assistance to prevent data loss.");
              return FileVisitResult.TERMINATE;
            }
            return FileVisitResult.CONTINUE;
          }
          if (resumeFile != null) {
            // resume information has been passed to document instance.
            resumeFile = null;
          }

          try {
            lastDocument.open();
            return lastDocument.runFile(scanner);
          } finally {
            lastDocument.close();
          }
        }
      });
    } finally {
      if (lastDocument != null) {
        lastDocument.writeResume(resumeFile());
      }
    }
  }

  private Path resumeFile() {
    return outputDir.resolve("resume.txt");
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

  private static class DocumentAnnotations implements Closeable {

    private final Path relativePath;

    private final Path inputFile;

    private final Path outputFile;

    private long examples = 0;

    private boolean terminate = false;

    private BufferedReader bufferedReader;

    private BufferedWriter bufferedWriter;

    private String prev;

    DocumentAnnotations(Path inputFile, Path inputDir, Path outputDir) {
      this.relativePath = inputDir.relativize(inputFile);
      this.inputFile = inputFile;
      outputFile = outputDir.resolve(relativePath);
    }

    private boolean checkResume(String resumeFile, int resumeIndex) throws IOException {
      if (Files.exists(outputFile)) {
        if (resumeFile == null) {
          terminate = true;
          return false;
        }

        if (!resumeFile.equals(relativePath.toString())) {
          return false;
        }

        long lines = Files.exists(outputFile) ? Files.lines(outputFile).count() : -1;

        if (lines != resumeIndex) {
          terminate = true;
          return false;
        }

        examples = lines;
      }
      return true;
    }

    private void open() throws IOException {
      openReader();
      bufferedWriter = Files.newBufferedWriter(outputFile, UTF_8, APPEND, CREATE);
    }

    private void openReader() throws IOException {
      bufferedReader = Files.newBufferedReader(inputFile, UTF_8);
      for (int i = 0; i < examples; i++) {
        bufferedReader.readLine();
        bufferedReader.readLine();
        bufferedReader.readLine();
      }
    }

    private FileVisitResult runFile(Scanner scanner) throws IOException {
      System.out.println("File: " + relativePath.toString());

      REDO:
      while (true) {
        ExampleQuery exampleQuery = new ExampleQuery(examples);

        if (!exampleQuery.readAndPrintNextContext(bufferedReader)) {
          if (prev != null) {
            bufferedWriter.write(prev);
            bufferedWriter.newLine();
          }
          break;
        }
        System.out.flush();

        while (true) {
          String s = exampleQuery.tryCollect(scanner);
          if (s != null) {
            if (prev != null) {
              bufferedWriter.write(prev);
              bufferedWriter.newLine();
            }
            prev = s;
            break;
          }

          if (exampleQuery.redo) {
            prev = null;
            examples--;
            bufferedReader.close();
            openReader();
            continue REDO;
          }

          if (exampleQuery.quit) {
            if (prev != null) {
              bufferedWriter.write(prev);
              bufferedWriter.newLine();
            }
            return FileVisitResult.TERMINATE;
          }
        }

        System.out.println();
        examples++;
      }
      examples = 0;

      return FileVisitResult.CONTINUE;
    }

    private void writeResume(Path resumeFile) throws IOException {
      try (BufferedWriter resumeWriter = Files.newBufferedWriter(resumeFile, UTF_8, CREATE,
          TRUNCATE_EXISTING)) {
        resumeWriter.write(relativePath.toString());
        resumeWriter.newLine();
        resumeWriter.write("" + examples);
        resumeWriter.newLine();
      }
    }

    @Override
    public void close() throws IOException {
      bufferedReader.close();
      bufferedWriter.close();
    }
  }

  private static class ExampleQuery {

    private final long exampleNumber;

    private StringBuilder lineBuilder;

    private StringBuilder numbersBuilder;

    private String[] beforeTokens;

    private String[] afterTokens;

    private int tokenCount = 1;

    private int printedCount = 0;

    private boolean retry = false;

    private boolean quit = false;

    private boolean redo = false;

    ExampleQuery(long exampleNumber) {
      this.exampleNumber = exampleNumber;
    }

    private boolean readAndPrintNextContext(BufferedReader bufferedReader) throws IOException {
      String line = bufferedReader.readLine();

      if (line == null) {
        return false;
      }

      System.out.println("Example #" + (exampleNumber + 1));

      beforeTokens = SPACE_SPLIT.split(line);
      String[] numberTokens = SPACE_SPLIT.split(bufferedReader.readLine());
      afterTokens = SPACE_SPLIT.split(bufferedReader.readLine());

      lineBuilder = new StringBuilder();
      numbersBuilder = new StringBuilder();

      printTokens(beforeTokens);

      for (String numberToken : numberTokens) {
        lineBuilder.append(numberToken).append(' ');
        numbersBuilder.append('#').append(' ');
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

    private void printTokens(String[] tokens) {
      for (String beforeToken : tokens) {
        lineBuilder.append(beforeToken).append(' ');
        numbersBuilder.append(tokenCount++).append(' ');
        for (int i = numbersBuilder.length(); i < lineBuilder.length(); i++) {
          numbersBuilder.append(' ');
        }
        checkFive();
      }
    }

    private void checkFive() {
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

    private String tryCollect(Scanner scanner) {
      StringBuilder measureBuilder = new StringBuilder();

      System.out.print("Is this a measure? ");
      String s = scanner.nextLine();
      if (s.isEmpty()) {
        return null;
      }

      char firstChar = s.charAt(0);
      if (firstChar == 'Y' || firstChar == 'y') {
        measureBuilder.append('y');

        while (true) {
          System.out.print("Which tokens are units? ");
          List<Integer> tokenIndexes = collectIds(scanner);
          if (tokenIndexes != null) {
            measureBuilder.append("\t").append(String.join(" ",
                (Iterable<String>) () -> tokenIndexes.stream().map(i -> "" + i).iterator()));
            measureBuilder.append("\t").append(grabTokens(tokenIndexes));
            break;
          }
          if (retry) {
            return tryCollect(scanner);
          }
          System.out.println(
              "Write each token id that is part of the unit of measure, separated by a space, "
                  + "or the letter u to undo your previous answer.");
        }

        while (true) {
          System.out.print("Which tokens are annotations? ");
          List<Integer> tokenIndexes = collectIds(scanner);
          if (tokenIndexes != null) {
            measureBuilder.append("\t").append(String.join(" ",
                (Iterable<String>) () -> tokenIndexes.stream().map(i -> "" + i).iterator()));
            measureBuilder.append("\t").append(grabTokens(tokenIndexes));
            break;
          }
          if (retry) {
            return tryCollect(scanner);
          }
          System.out.println("Write each token id that is an annotation, separated by a space, "
              + "or the letter u to undo your previous answer");
        }
      } else if (firstChar == 'N' || firstChar == 'n') {
        measureBuilder.append('n');
      } else if (firstChar == 'U' || firstChar == 'u') {
        measureBuilder.append('u');
      } else if (firstChar == 'Q' || firstChar == 'q') {
        quit = true;
        return null;
      } else if (firstChar == 'R' || firstChar == 'r') {
        redo = true;
        return null;
      } else {
        System.out
            .println("Respond y for yes, n for no, u for don't know / ambiguous, or q to quit");
        return null;
      }

      return measureBuilder.toString();
    }

    private List<Integer> collectIds(Scanner scanner) {
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
  }
}
