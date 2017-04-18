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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
            List<Node> parse = ptbReader.parse();
            System.out.println(parse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Node> parse() throws IOException {
        List<Node> completed = new ArrayList<>();
        Node current = null;

        int in;
        while (true) {
            if (current == null) {
                // we need a node
                in = reader.read();
                if (in == -1) {
                    return completed;
                } else if (in == '(') {
                    current = new Node(null);
                } else if (!Character.isWhitespace(in)) {
                    throw new IllegalStateException("Unexpected character \'" + in + "\'");
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
                    Node child = new Node(current);
                    current.children.add(child);
                    current = child;
                } else if (!Character.isWhitespace(in)) {
                    current.word = readWord(in);
                    current = current.parent;
                } else {
                    throw new IllegalStateException("Unexpected character: \'" + in + "\'");
                }
            } else {
                // we need another child or a end to the current node
                do {
                    in = reader.read();
                } while (Character.isWhitespace(in));
                if (in == '(') {
                    Node child = new Node(current);
                    current.children.add(child);
                    current = child;
                } else if (in == ')') {
                    if (current.parent == null) {
                        completed.add(current);
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

        String replacement= WORD_REPLACEMENTS.get(word);
        if (replacement != null) {
            word = WORD_REPLACEMENTS.get(word);
        }

        return word;
    }

    public static class Node {
        @Nullable private final Node parent;
        private final List<Node> children = new ArrayList<>();
        private String label;
        private String word;

        Node(@Nullable Node parent) {
            this.parent = parent;
        }

        @Override
        public String toString() {
            StringBuilder childBuilder = new StringBuilder();
            for (Node child : children) {
                childBuilder.append(child.toString());
            }
            return "(" + label + " " + ((word != null) ? word : "") + childBuilder + ")";
        }

        public List<Node> getLeaves() {
            LinkedList<Node> stack = new LinkedList<>();
            stack.push(this);
            List<Node> leaves = new ArrayList<>();
            while (!stack.isEmpty()) {
                Node current = stack.poll();
                if (current.children.isEmpty()) {
                    leaves.add(this);
                } else {
                    for (int i = current.children.size() - 1; i >= 0; i--) {
                        stack.push(current.children.get(i));
                    }
                }
            }
            return leaves;
        }

        @Nullable
        public Node getParent() {
            return parent;
        }

        public List<Node> getChildren() {
            return children;
        }

        public String getLabel() {
            return label;
        }

        public String getWord() {
            return word;
        }
    }
}
