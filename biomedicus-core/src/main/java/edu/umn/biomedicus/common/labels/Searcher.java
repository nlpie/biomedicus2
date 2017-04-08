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

package edu.umn.biomedicus.common.labels;

import edu.umn.biomedicus.common.types.text.Document;
import edu.umn.biomedicus.common.types.text.Span;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.PatternSyntaxException;

/**
 *
 */
public class Searcher {
    static final Node TAIL = new Node();
    final Node root;
    final int numberGroups;
    final int numberLocals;
    final Map<String, Integer> groupNames;

    public Searcher(Node root,
                    int numberGroups,
                    int numberLocals,
                    Map<String, Integer> groupNames) {
        this.root = root;
        this.numberGroups = numberGroups;
        this.numberLocals = numberLocals;
        this.groupNames = groupNames;
    }

    public static Searcher parse(LabelAliases labelAliases, String pattern) {
        return new Parser(labelAliases, pattern).compile();
    }

    public Search search(Document document) {
        return new DefaultSearch(document, document.getDocumentSpan());
    }

    public Search search(Document document, Span span) {
        return new DefaultSearch(document, span);
    }

    static class Return {
        final Node head;
        final Node demarc;
        final Node tail;

        Return(Node head,
               Node demarc,
               Node tail) {
            this.head = head;
            this.demarc = demarc;
            this.tail = tail;
        }
    }

    private static class Parser {
        private final LabelAliases labelAliases;
        private final String pattern;
        private final char[] arr;
        private final Map<String, Integer> groupNames = new HashMap<>();
        private final Map<Integer, Class<?>> groupTypes = new HashMap<>();
        private int index = 0;
        private int numberGroups = 0;
        private int numberBranches = 0;
        private int localsCount = 0;

        private Parser(LabelAliases labelAliases,
                       String pattern) {
            this.labelAliases = labelAliases;
            this.pattern = pattern;
            arr = pattern.toCharArray();
        }

        private Searcher compile() {
            Return expr = alts(TAIL);
            Node root = expr.head;
            return new Searcher(root, numberGroups, numberBranches, groupNames);
        }

        private Return alts(Node end) {
            Node prev = null;
            Node firstDemarc = null;
            Node firstTail = null;
            Branch branch = null;
            Node join = null;

            while (true) {
                Return sequence = seqs(end);
                if (prev == null) {
                    prev = sequence.head;
                    firstDemarc = sequence.demarc;
                    firstTail = sequence.tail;
                } else {
                    if (join == null) {
                        join = new Noop();
                        join.next = end;
                    }
                    if (prev == branch) {
                        // branch has been created already, add new path
                        branch.add(sequence.head);
                    } else {
                        // created new branch, need to add first branch
                        prev = branch = new Branch(join);
                        if (prev == end) {
                            // first path is an empty path
                            branch.optional = true;
                        } else {
                            // add first path to branch
                            Node newFirstTail = createSaveAndLoad(firstDemarc,
                                    firstTail);
                            newFirstTail.next = join;
                            branch.add(prev);
                        }
                    }
                    if (sequence.head == end) {
                        branch.optional = true;
                    } else {
                        // insert the node responsible for saving the begin
                        Node newTail = createSaveAndLoad(sequence.demarc,
                                sequence.tail);
                        newTail.next = join;
                    }
                }
                if (peek() != '|') {
                    if (join == null) {
                        return sequence;
                    } else {
                        return new Return(prev, join, join);
                    }
                }
                read();
            }
        }

        private Return seqs(Node end) {
            Node head = null;
            Node demarc = null;
            Node tail = null;
            Node node;
            LOOP:
            while (true) {
                int ch = peekPastWhiteSpace();
                switch (ch) {
                    case '(':
                    case '[':
                        Return group = groupLike();
                        node = group.head;
                        if (node == null) {
                            continue;
                        }
                        if (head == null) {
                            head = node;
                        } else {
                            if (demarc == null) {
                                demarc = group.demarc;
                            }
                            tail.next = node;
                        }
                        tail = group.tail;
                        continue;
                    case '|':
                    case ')':
                    case ']':
                    case '&':
                        break LOOP;
                    default:
                        node = type();
                        break;
                }

                node = trailing(node);

                if (head == null) {
                    head = demarc = tail = node;
                } else {
                    if (demarc == null) {
                        demarc = node;
                    }
                    tail.next = node;
                    tail = node;
                }
            }
            if (head == null) {
                return new Return(end, end, end);
            }
            tail.next = end;
            return new Return(head, demarc, tail);
        }

        private Return groupLike() {
            if (read() == '(') {
                return group();
            } else {
                // ch is '['
                return pinning();
            }
        }

        private Return group() {
            Node head = null;
            Node tail = null;
        }

        private Return pinning() {

        }

        private Return intersection(Node end) {

        }

        private Node type() {
            TypeMatch node;
            int ch = read();
            boolean seek = false;
            if (ch == '>') {
                seek = true;
                ch = read();
            }
            if (!Character.isAlphabetic(ch) && !Character.isDigit(ch))
                throw error("Illegal identifier");

            String first = readAlphanumeric();
            String variable = null;
            String type;
            ch = read();
            if (ch == ':') {
                variable = first;
                type = readAlphanumeric();
                ch = read();
            } else {
                type = first;
            }
            Class<?> aClass = labelAliases.getLabelable(type);
            if (aClass == null) {
                try {
                    aClass = Class.forName(type);
                } catch (ClassNotFoundException e) {
                    throw error("Couldn't find a type with alias or name "
                            + type);
                }
            }
            int group = -1;
            if (variable != null) {
                group = numberGroups++;
                groupTypes.put(group, aClass);
            }
            node = new TypeMatch(aClass, seek, group);
            ch = read();
            if (ch == '{') {
                do {
                    parseProperty(node);
                    if (peek() == ',') {
                        read();
                        parseProperty(node);
                    } else {
                        break;
                    }
                } while (true);
            }
            return node;
        }

        private Node trailing(Node node) {
            return null;
        }

        Node createSaveAndLoad(Node demarc, Node tail) {
            if (demarc == tail) {
                // we don't need to save and load
                return tail;
            }

            SaveBegin save = new SaveBegin(localsCount);
            LoadBegin load = new LoadBegin(localsCount++);

            save.next = demarc.next;
            demarc.next = save;

            load.next = tail.next;
            tail.next = load;

            return load;
        }

        String readAlphanumeric() {
            StringBuilder groupName = new StringBuilder();
            int ch;
            while (Character.isAlphabetic(ch = peek())
                    || Character.isDigit(ch)) {
                groupName.append(ch);
                read();
            }
            return groupName.toString();
        }

        String parseTypeName(int ch) {
            StringBuilder typeName = new StringBuilder();
            typeName.append(ch);
            read();
            while (Character.isAlphabetic(ch = peek())
                    || Character.isDigit(ch)) {
                typeName.append(ch);
            }

            return typeName.toString();
        }

        String parsePropertyStringValue() {
            StringBuilder vb = new StringBuilder();
            int ch;
            boolean escaped = false;
            while ((ch = read()) != '"' || escaped) {
                if (escaped) {
                    escaped = false;
                    vb.append(ch);
                } else if (ch == '\\') {
                    escaped = true;
                } else {
                    vb.append(ch);
                }
            }
            return vb.toString();
        }

        Object parseNumber(int ch) {
            StringBuilder nb = new StringBuilder();
            nb.append(ch);
            boolean isDouble = false;
            while (Character.isDigit(ch = peek()) || (!isDouble && ch == '.')) {
                if (ch == '.') {
                    isDouble = true;
                }
                nb.append(ch);
                read();
            }

            String digitString = nb.toString();
            return isDouble ? Double.parseDouble(digitString)
                    : Long.parseLong(digitString);
        }

        String parseBackreferenceGroupName() {
            StringBuilder bsb = new StringBuilder();
            read();
            int ch;
            while (Character.isAlphabetic(ch = peek())
                    || Character.isDigit(ch)) {
                bsb.append(ch);
                read();
            }
            return bsb.toString();
        }

        void parseProperty(TypeMatch typeMatch) {
            StringBuilder pnsb = new StringBuilder();
            int ch;
            while (Character.isAlphabetic(ch = read()) || Character.isDigit(ch))
                pnsb.append(ch);
            if (ch != '=') throw error("Invalid property value format");
            String propertyName = pnsb.toString();
            ch = read();
            if (ch == '"') {
                typeMatch.addPropertyMatch(propertyName,
                        parsePropertyStringValue());
            } else if (Character.isDigit(ch)) {
                typeMatch.addPropertyMatch(propertyName, parseNumber(ch));
            } else if (Character.isAlphabetic(ch)) {
                Object value;
                if (ch == 't' || ch == 'T' || ch == 'y' || ch == 'Y') {
                    value = true;
                } else if (ch == 'f' || ch == 'F' || ch == 'n' || ch == 'N') {
                    value = false;
                } else {
                    throw error("Invalid property value");
                }
                while (Character.isAlphabetic(peek()))
                    read();
                typeMatch.addPropertyMatch(propertyName, value);
            } else if (ch == '$') {
                String backReferenceGroup = parseBackreferenceGroupName();
                ch = peek();
                if (ch == '.') {
                    Class<?> type = groupTypes.get(backReferenceGroup);
                    String backPropertyName = parseTypeName(read());
                    try {
                        Method method = type.getMethod(backPropertyName);
                        typeMatch.addPropertyValueBackReference(propertyName,
                                backReferenceGroup, method);
                    } catch (NoSuchMethodException e) {
                        throw error(e.getLocalizedMessage());
                    }
                } else {
                    typeMatch.addSpanBackReference(propertyName,
                            backReferenceGroup);
                }
            } else {
                throw error("Illegal property value");
            }
        }

        PatternSyntaxException error(String desc) {
            return new PatternSyntaxException(desc, pattern, index);
        }

        boolean atEnd() {
            return index == arr.length;
        }

        int read() {
            return arr[index++];
        }

        int readPastWhitespace() {
            int ch;
            do {
                ch = read();
            } while (Character.isWhitespace(ch));
            return ch;
        }

        int peekPastWhiteSpace() {
            int ch;
            while (Character.isWhitespace(ch = peek())) {
                read();
            }
            return ch;
        }

        int peek() {
            return arr[index];
        }

        void unwind() {
            index--;
        }

        boolean consume(int ch) {
            if (peek() == ch) {
                read();
                return true;
            } else {
                return false;
            }
        }

        boolean consumePastWhiteSpace(int ch) {
            int peek = peekPastWhiteSpace();
            if (peek == ch) {
                read();
                return true;
            } else {
                return false;
            }
        }
    }

    static class Node {
        Node next;

        Node() {
            next = TAIL;
        }

        boolean search(DefaultSearch search) {
            return true;
        }
    }

    static class SaveBegin extends Node {
        final int local;

        SaveBegin(int local) {
            this.local = local;
        }

        @Override
        boolean search(DefaultSearch search) {
            search.locals[local] = search.begin;
            return next.search(search);
        }
    }

    static class LoadBegin extends Node {
        final int local;

        LoadBegin(int local) {
            this.local = local;
        }

        @Override
        public boolean search(DefaultSearch search) {
            search.begin = search.locals[local];
            return next.search(search);
        }
    }

    static class Branch extends Node {
        Node[] paths = new Node[2];
        int size = 0;
        Node join;
        boolean optional = false;

        Branch(Node branchJoin) {
            join = branchJoin;
        }

        void add(Node node) {
            if (size >= paths.length) {
                Node[] tmp = new Node[paths.length * 2];
                System.arraycopy(paths, 0, tmp, 0, paths.length);
                paths = tmp;
            }
            paths[size++] = node;
        }

        @Override
        public boolean search(DefaultSearch search) {
            for (int i = 0; i < size; i++) {
                if (paths[i].search(search)) {
                    return true;
                }
            }
            return optional && join.search(search);
        }
    }

    static class Noop extends Node {
        @Override
        public boolean search(DefaultSearch search) {
            return next.search(search);
        }
    }

    static class Intersection extends Node {
        final int localAddr;
        Node[] conditions = new Node[2];
        int size = 0;

        Intersection(int localAddr) {
            this.localAddr = localAddr;
        }

        @Override
        public boolean search(DefaultSearch search) {
            return false;
        }
    }

    static class TypeMatch extends Node {
        final Class<?> labelType;
        final List<PropertyMatch> requiredProperties = new ArrayList<>();
        final boolean seek;
        final int group;

        TypeMatch(Class labelType, boolean seek, int group) {
            this.labelType = labelType;
            this.seek = seek;
            this.group = group;
        }

        @Override
        public boolean search(DefaultSearch search) {
            LabelIndex<?> labelIndex = search.document.getLabelIndex(labelType)
                    .insideSpan(new Span(search.end, search.limit));
            if (!seek) {
                Optional<? extends Label<?>> labelOp = labelIndex.first();
                if (!labelOp.isPresent()) return false;
                Label<?> label = labelOp.get();
                if (!propertiesMatch(search, label)) return false;
                search.end = label.getEnd();
                if (group != -1) {
                    search.groups[group * 2] = label.getBegin();
                    search.groups[group * 2 + 1] = label.getEnd();
                }
                if (next.search(search)) {
                    search.begin = label.getBegin();
                    return true;
                }
            }
            for (Label<?> label : labelIndex) {
                if (!propertiesMatch(search, label)) continue;
                Span span = label.toSpan();
                search.end = span.getEnd();
                if (group != -1) {
                    search.groups[group * 2] = label.getBegin();
                    search.groups[group * 2 + 1] = label.getEnd();
                }
                if (next.search(search)) {
                    search.begin = span.getBegin();
                    return true;
                }
            }
            return false;
        }

        boolean propertiesMatch(Search search, Label label) {
            for (PropertyMatch requiredProperty : requiredProperties) {
                if (!requiredProperty.doesMatch(search, label)) return false;
            }
            return true;
        }

        void addPropertyMatch(String name, Object value) {
            requiredProperties.add(new ValuedPropertyMatch(name, value));
        }

        void addPropertyValueBackReference(String name,
                                           String group,
                                           Method backrefMethod) {
            requiredProperties.add(new PropertyValueBackReference(name, group,
                    backrefMethod));
        }

        void addSpanBackReference(String name, String group) {
            requiredProperties.add(new SpanBackReference(name, group));
        }

        abstract class PropertyMatch {
            final String name;
            final Method readMethod;

            PropertyMatch(String name) {
                this.name = name;
                try {
                    readMethod = new PropertyDescriptor(name, labelType)
                            .getReadMethod();
                } catch (IntrospectionException e) {
                    throw new IllegalStateException(e);
                }
            }

            abstract boolean doesMatch(Search search, Label<?> label);
        }


        class ValuedPropertyMatch extends PropertyMatch {
            final Object value;

            ValuedPropertyMatch(String name, Object value) {
                super(name);
                this.value = value;
            }

            @Override
            boolean doesMatch(Search search, Label<?> label) {
                try {
                    return value.equals(readMethod.invoke(label.getValue()));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalStateException("");
                }
            }
        }

        class PropertyValueBackReference extends PropertyMatch {
            private final String group;
            private final Method backrefMethod;

            PropertyValueBackReference(String name,
                                       String group,
                                       Method backrefMethod) {
                super(name);
                this.group = group;
                this.backrefMethod = backrefMethod;
            }

            @Override
            boolean doesMatch(Search search, Label<?> label) {
                Label<?> groupLabel = search.getLabel(group);
                try {
                    Object value = backrefMethod.invoke(groupLabel.getValue());
                    return value.equals(readMethod.invoke(label.getValue()));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalStateException("");
                }
            }
        }

        class SpanBackReference extends PropertyMatch {

            private final String group;

            SpanBackReference(String name, String group) {
                super(name);
                this.group = group;
            }

            @Override
            boolean doesMatch(Search search, Label<?> label) {
                Span span = search.getSpan(group);
                try {
                    return span.equals(readMethod.invoke(label.getValue()));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalStateException("");
                }
            }
        }
    }

    /**
     *
     */
    class DefaultSearch implements Search {
        final Document document;
        final Label[] labels;
        final int[] groups;
        final int[] locals;
        boolean found;
        int begin;
        int end;
        int limit;

        DefaultSearch(Document document, Span span) {
            this.document = document;
            labels = new Label[numberGroups];
            groups = new int[numberGroups];
            locals = new int[numberLocals];
            found = root.search(this);
            begin = end = span.getBegin();
            limit = span.getEnd();
        }

        @Override
        public Label<?> getLabel(String name) {
            return labels[groupNames.get(name)];
        }

        @Override
        public Span getSpan(String name) {
            Integer integer = groupNames.get(name);
            if (integer == null) {
                throw new IllegalArgumentException("Name not found");
            }
            return new Span(groups[integer * 2], groups[integer * 2 + 1]);
        }

        @Override
        public boolean foundMatch() {
            return found;
        }

        @Override
        public boolean findNext() {
            if (!found) {
                throw new IllegalStateException();
            }

            Arrays.fill(groups, -1);
            Arrays.fill(labels, null);

            return found = root.search(this);
        }

        @Override
        public Span getSpan() {
            return new Span(groups[0], groups[1]);
        }

        @Override
        public Optional<Label<?>> getTopLabel() {
            return Optional.ofNullable((Label<?>) labels[0]);
        }
    }
}
