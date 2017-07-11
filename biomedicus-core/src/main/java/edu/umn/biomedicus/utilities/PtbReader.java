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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Class for parsing penn treebank style parse trees into a structured tree.
 *
 * @author Ben Knoll
 * @since 1.5.0
 */
public class PtbReader {

  private static final Map<String, String> WORD_REPLACEMENTS;

  static {
    WORD_REPLACEMENTS = new HashMap<>();
    WORD_REPLACEMENTS.put("-LRB-", "(");
    WORD_REPLACEMENTS.put("-RRB-", ")");
    WORD_REPLACEMENTS.put("-LCB-", "{");
    WORD_REPLACEMENTS.put("-RCB-", "}");
    WORD_REPLACEMENTS.put("-LSB-", "[");
    WORD_REPLACEMENTS.put("-RSB-", "]");
    WORD_REPLACEMENTS.put("``", "\"");
    WORD_REPLACEMENTS.put("''", "\"");
  }

  private final Reader reader;

  private PtbReader(Reader reader) {
    this.reader = reader;
  }

  /**
   * Creates a new {@code PtbReader} which from the reader.
   *
   * @param reader a "fresh" reader of the PTB text.
   * @return {@code PtbReader} which can be used to retrieve the sentence
   */
  public static PtbReader create(Reader reader) {
    return new PtbReader(reader);
  }

  public static PtbReader create(InputStream inputStream) {
    return new PtbReader(new InputStreamReader(inputStream));
  }

  public static PtbReader create(String string) {
    return new PtbReader(new StringReader(string));
  }

  public static PtbReader createFromFile(Path path) throws IOException {
    return new PtbReader(Files.newBufferedReader(path));
  }

  public static PtbReader createFromFile(String path) throws IOException {
    return new PtbReader(Files.newBufferedReader(Paths.get(path)));
  }

  public static PtbReader createFromFile(File file) throws IOException {
    return new PtbReader(Files.newBufferedReader(file.toPath()));
  }

  public static void main(String args[]) {
    Path path = Paths.get(args[0]);
    try (BufferedReader bufferedReader = Files.newBufferedReader(path)) {
      PtbReader ptbReader = new PtbReader(bufferedReader);
      Optional<Node> nextNode;
      while ((nextNode = ptbReader.nextNode()).isPresent()) {
        System.out.println(nextNode);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Optional<Node> nextNode() throws IOException {
    Node current = null;

    int in;
    while (true) {
      if (current == null) {
        // we need a node
        in = reader.read();
        if (in == -1) {
          return Optional.empty();
        } else if (in == '(') {
          current = new Node();
        } else if (!Character.isWhitespace(in)) {
          throw new IllegalStateException("Unexpected character \'" + (char) in + "\'");
        }
      } else if (current.label == null) {
        // we need a label
        StringBuilder stringBuilder = new StringBuilder();
        while (!Character.isWhitespace(in = reader.read())) {
          stringBuilder.append((char) in);
        }
        current.label = stringBuilder.toString();
      } else if (current.children.size() == 0) {
        // we need a value or a first child
        in = reader.read();
        if (in == '(') {
          Node child = new Node();
          child.parent = current;
          current.children.add(child);
          current = child;
        } else if (!Character.isWhitespace(in)) {
          current.word = readWord(in);
          current = current.parent;
        } else {
          throw new IllegalStateException("Unexpected character: \'" + in + "\'");
        }
      } else {
        // we need another child or an end to the current node
        do {
          in = reader.read();
        } while (Character.isWhitespace(in));
        if (in == '(') {
          Node child = new Node();
          child.parent = current;
          current.children.add(child);
          current = child;
        } else if (in == ')') {
          if (current.parent == null) {
            return Optional.of(current);
          }
          current = current.parent;
        } else {
          throw new IllegalStateException("Unexpected character: \'" + in + "\'");
        }
      }
    }
  }

  @Nonnull
  private String readWord(int in) throws IOException {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append((char) in);
    while ((in = reader.read()) != ')') {
      if (Character.isWhitespace(in)) {
        throw new IllegalStateException("Unexpected whitespace");
      }
      stringBuilder.append((char) in);
    }
    String word = stringBuilder.toString();

    String replacement = WORD_REPLACEMENTS.get(word);
    if (replacement != null) {
      word = WORD_REPLACEMENTS.get(word);
    }

    return word;
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
      for (Node child : children) {
        childBuilder.append(child.toString());
      }
      return "(" + label + " " + ((word != null) ? word : "") + childBuilder + ")";
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
