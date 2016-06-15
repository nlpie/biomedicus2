/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.uima.pos;

import java.io.IOException;
import java.io.Writer;

/**
 *
 */
public class ConfusionMatrix {
    private int truePositives;
    private int falseNegatives;
    private int falsePositives;

    public ConfusionMatrix(int truePositives, int falseNegatives, int falsePositives) {
        this.truePositives = truePositives;
        this.falseNegatives = falseNegatives;
        this.falsePositives = falsePositives;
    }

    public ConfusionMatrix() {
        this(0, 0, 0);
    }

    public void incrementTruePositives() {
        truePositives++;
    }

    public void incrementFalseNegatives() {
        falseNegatives++;
    }

    public void incrementFalsePositives() {
        falsePositives++;
    }

    public void write(Writer writer, int total) throws IOException {
        writer.write(truePositives + "," + (truePositives + falseNegatives) + ",");
    }

    public void add(ConfusionMatrix other) {
        truePositives += other.truePositives;
        falseNegatives += other.falseNegatives;
        falsePositives += other.falsePositives;
    }
}
