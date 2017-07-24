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

package edu.umn.biomedicus.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for parsing penn treebank style parse trees into a structured tree.
 *
 * @author Ben Knoll
 * @since 1.5.0
 */
public class PtbReader {

  private static final Logger LOGGER = LoggerFactory.getLogger(PtbReader.class);

  private final BufferedReader reader;

  private int line = 0;
  private Node current;

  private PtbReader(BufferedReader reader) {
    this.reader = reader;
  }

  /**
   * Creates a new {@code PtbReader} from a reader.
   *
   * @param reader a "fresh" reader of the PTB text.
   * @return {@code PtbReader} which can be used to retrieve nodes in the penn tree
   */
  public static PtbReader create(Reader reader) {
    return new PtbReader(new BufferedReader(reader));
  }

  /**
   * Creates a new ptb reader from an input stream, uses default charset.
   *
   * @param inputStream the inputstream to read from
   * @return {@code PtbReader} which can be used to retrieve nodes in the penn tree
   */
  public static PtbReader create(InputStream inputStream) {
    return new PtbReader(new BufferedReader(new InputStreamReader(inputStream)));
  }

  /**
   * Creates a new ptb reader from an input stream and a charset
   *
   * @param inputStream the input stream to read from
   * @param charset the charset to use
   * @return {@code PtbReader} which can be used to retrieve nodes in a penn tree
   */
  public static PtbReader create(InputStream inputStream, Charset charset) {
    return new PtbReader(new BufferedReader(new InputStreamReader(inputStream, charset)));
  }

  /**
   * Creates a new ptb reader directly from a string.
   *
   * @param string string to read from
   * @return {@code PtbReader} which can be used to retrieve nodes in a penn tree
   */
  public static PtbReader create(String string) {
    return new PtbReader(new BufferedReader(new StringReader(string)));
  }

  /**
   * Creates a new ptb reader from a path to a file to read from, using the default charset
   *
   * @param path the path to the file to read from
   * @return {@code PtbReader} which can be used to retrieve nodes in a penn tree
   * @throws IOException if there is a failure with creating the reader
   */
  public static PtbReader createFromFile(Path path) throws IOException {
    return new PtbReader(Files.newBufferedReader(path));
  }

  /**
   * Creates a new ptb reader from a path to a file to read from, using a specified charset.
   *
   * @param path the path to the file to read from
   * @param charset the charset to use
   * @return {@code PtbReader} which can be used to retrieve nodes in a penn tree
   * @throws IOException if there is a failure with creating the reader
   */
  public static PtbReader createFromFile(Path path, Charset charset) throws IOException {
    return new PtbReader(Files.newBufferedReader(path, charset));
  }

  /**
   * Create a new ptb reader from a string path to a file to read from, using the default charset.
   *
   * @param path the path to the file to read from.
   * @return {@code PtbReader} which can be used to retrieve nodes in a penn tree
   * @throws IOException if there is a failure with creating the reader
   */
  public static PtbReader createFromFile(String path) throws IOException {
    return new PtbReader(Files.newBufferedReader(Paths.get(path)));
  }

  /**
   * Create a new ptb reader from a string path to a file to read from, using a specified charset.
   *
   * @param path the string path to the file to read from.
   * @param charset the specified charset to use
   * @return {@code PtbReader} which can be used to retrieve nodes in a penn tree
   * @throws IOException if there is a failure with creating the reader
   */
  public static PtbReader createFromFile(String path, Charset charset) throws IOException {
    return new PtbReader(Files.newBufferedReader(Paths.get(path), charset));
  }

  /**
   * Creates a new ptb reader by reading from file, using the default charset.
   *
   * @param file the file to read from
   * @return {@code PtbReader} which can be used to retrieve nodes in a penn tree
   * @throws IOException if there is an error reading from the file.
   */
  public static PtbReader createFromFile(File file) throws IOException {
    return new PtbReader(Files.newBufferedReader(file.toPath()));
  }

  /**
   * Creates a new ptb reader by reading from the file, using a specified from a charset
   *
   * @param file the file to read from
   * @param charset the charset to use
   * @return {@code PtbReader} which can be used to retrieve nodes in a penn tree
   * @throws IOException if there is an error reading from the file.
   */
  public static PtbReader createFromFile(File file, Charset charset) throws IOException {
    return new PtbReader(Files.newBufferedReader(file.toPath(), charset));
  }

  public static void main(String args[]) {
    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {
      PtbReader ptbReader = new PtbReader(bufferedReader);
      Optional<Node> nextNode;
      while ((nextNode = ptbReader.nextNode()).isPresent()) {
        System.out.println(nextNode.get());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Returns the next top level node in the penn tree set.
   *
   * @return a top level node.
   * @throws IOException if there is a failure reading.
   */
  public Optional<Node> nextNode() throws IOException {
    current = null;

    int in;
    while (true) {
      if (current == null) {
        // we need a node
        in = readCountingLines();
        if (in == '*') {
          reader.readLine();
          line++;
        } else if (in == -1) {
          return Optional.empty();
        } else if (in == '(') {
          current = new Node();
        } else if (!Character.isWhitespace(in)) {
          LOGGER.error("Failed on line: {} on character: {}", line, (char) in);
          throw new IOException("Unexpected character \'" + (char) in + "\'");
        }
      } else if (current.label == null) {
        // we need a label
        StringBuilder stringBuilder = new StringBuilder();
        in = readCountingLines();
        if (goChildNode(in)) continue;
        if (in == ')') {
          if (current.parent == null) {
            return Optional.of(current);
          }
          current = current.parent;
        } else {
          while (!Character.isWhitespace(in)) {
            stringBuilder.append((char) in);
            in = readCountingLines();
          }
          current.label = stringBuilder.toString();
        }
      } else if (current.children.size() == 0) {
        // we need a value or a first child
        in = readCountingLines();
        if (goChildNode(in)) continue;
        if (!Character.isWhitespace(in)) {
          current.word = readWord(in);
          current = current.parent;
        }
      } else {
        // we need another child or an end to the current node
        do {
          in = readCountingLines();
          if (in == -1) {
            throw new IOException("Unexpected end to document at line: " + line);
          }
        } while (Character.isWhitespace(in));
        if (goChildNode(in)) continue;
        if (in == ')') {
          if (current.parent == null) {
            return Optional.of(current);
          }
          current = current.parent;
        } else {
          throw new IOException("Unexpected character: \'" + in + "\'");
        }
      }
    }
  }

  private boolean goChildNode(int in) {
    if (in == '(') {
      Node child = new Node();
      child.parent = current;
      current.children.add(child);
      current = child;
      return true;
    }
    return false;
  }

  private int readCountingLines() throws IOException {
    int in = reader.read();
    if (in == '\n') line++;
    return in;
  }

  @Nullable
  private String readWord(int in) throws IOException {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append((char) in);
    while ((in = readCountingLines()) != ')') {
      if (Character.isWhitespace(in)) {
        throw new IOException("Unexpected whitespace");
      }
      stringBuilder.append((char) in);
    }
    String word = stringBuilder.toString();

    switch (word) {
      case "-LRB-":
        return "(";
      case "-RRB-":
        return ")";
      case "-LCB-":
        return "{";
      case "-RCB-":
        return "}";
      case "-LSB-":
        return "[";
      case "-RSB-":
        return "]";
      case "``":
        return "\"";
      case "''":
        return "\"";
      case "-NONE-":
        return null;
      default:
        return word;
    }
  }

  /**
   * A node in a penn treebank tree. For the node "(NNS patients)" the parent is the immediate node
   * containing that node, the children are empty, the label is
   */
  public static class Node {

    @Nullable
    private Node parent;

    private List<Node> children = new ArrayList<>();

    @Nullable
    private String label;

    @Nullable
    private String word;

    @Override
    public String toString() {
      StringBuilder childBuilder = new StringBuilder();
      boolean prev = false;
      for (Node child : children) {
        if (prev) {
          childBuilder.append(" ");
        }
        childBuilder.append(child.toString());
        prev = true;
      }
      return "(" + (label != null ? label + " " : "") + ((word != null) ? word : "") + childBuilder
          + ")";
    }

    /**
     * Gets all of the leaves underneath this node.
     *
     * @return a list of all the leaves underneath this node, or a list containing this node if this
     * is a leaf node.
     */
    public List<Node> getLeaves() {
      List<Node> leaves = new ArrayList<>();
      leaves.add(this);
      int ptr = 0;
      while (ptr < leaves.size()) {
        Node current = leaves.get(ptr);
        if (current.children.isEmpty()) {
          ptr++;
        } else {
          leaves.remove(ptr);
          leaves.addAll(current.children);
        }
      }
      return leaves;
    }

    /**
     * Returns an iterator which goes through all the leafs underneath this node.
     *
     * @return the iterator of leafs
     */
    public Iterator<Node> leafIterator() {
      return new Iterator<Node>() {
        @Nullable
        Node next;

        {
          next = firstLeaf();
        }

        void advance() {
          assert next != null : "next should never be null when advance is called";

          Node ptr = next;
          while (true) {
            Node parent = ptr.parent;
            if (parent == null) {
              next = null;
              return;
            }

            int index = parent.children.indexOf(ptr);
            if (index + 1 == parent.children.size()) {
              ptr = parent;
            } else {
              next = parent.children.get(index + 1).firstLeaf();
              return;
            }
          }
        }

        @Override
        public boolean hasNext() {
          return next != null;
        }

        @Override
        public Node next() {
          if (next == null) {
            throw new NoSuchElementException("All leafs have been returned");
          }
          Node next = this.next;
          advance();
          return next;
        }
      };
    }

    /**
     * Returns an optional of the parent of this node.
     *
     * @return empty if this is a top-level node, otherwise contains the parent node.
     */
    public Optional<Node> getParent() {
      return Optional.ofNullable(parent);
    }

    /**
     * Returns an optional of the children of this node.
     *
     * @return empty if this node is a leaf node, otherwise contains any nodes of this node.
     */
    public List<Node> getChildren() {
      return children;
    }

    /**
     * Returns this node's label
     */
    public String getLabel() {
      assert label != null : "By the time label passes out of PtbReader it should never be null";
      return label;
    }

    public Optional<String> getWord() {
      return Optional.ofNullable(word);
    }

    /**
     * Returns the word that this
     */
    public String leafGetWord() {
      if (word == null) {
        throw new IllegalStateException("Leaves should always have words.");
      }
      return word;
    }

    boolean isLastChild(Node node) {
      return children.indexOf(node) == children.size() - 1;
    }

    Node firstLeaf() {
      Node ptr = this;
      while (!ptr.children.isEmpty()) {
        ptr = ptr.children.get(0);
      }
      return ptr;
    }
  }
}
