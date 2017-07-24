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

package edu.umn.biomedicus.common.statistics;

public class ConfusionMatrix {

  private long truePositives;
  private long falsePositives;
  private long falseNegatives;
  private long trueNegatives;

  public ConfusionMatrix(long truePositives, long falsePositives, long falseNegatives,
      long trueNegatives) {
    this.truePositives = truePositives;
    this.falsePositives = falsePositives;
    this.falseNegatives = falseNegatives;
    this.trueNegatives = trueNegatives;
  }

  public ConfusionMatrix(ConfusionMatrix confusionMatrix) {
    truePositives = confusionMatrix.truePositives;
    falsePositives = confusionMatrix.falsePositives;
    falseNegatives = confusionMatrix.falseNegatives;
    trueNegatives = confusionMatrix.trueNegatives;
  }

  public ConfusionMatrix() {
    this(0, 0, 0, 0);
  }

  public long getTruePositives() {
    return truePositives;
  }

  public long getFalsePositives() {
    return falsePositives;
  }

  public long getFalseNegatives() {
    return falseNegatives;
  }

  public long getTrueNegatives() {
    return trueNegatives;
  }

  public void incrementTruePositives() {
    truePositives = Math.incrementExact(truePositives);
  }

  public void incrementTruePositives(long count) {
    truePositives = Math.addExact(truePositives, count);
  }

  public void incrementFalsePositives() {
    falsePositives = Math.incrementExact(falsePositives);
  }

  public void incrementFalsePositives(long count) {
    falsePositives = Math.addExact(falsePositives, count);
  }

  public void incrementFalseNegatives() {
    falseNegatives = Math.incrementExact(falseNegatives);
  }

  public void incrementFalseNegatives(long count) {
    falseNegatives = Math.addExact(falseNegatives, count);
  }

  public void incrementTrueNegatives() {
    trueNegatives = Math.incrementExact(trueNegatives);
  }

  public void incrementTrueNegatives(long count) {
    trueNegatives = Math.addExact(trueNegatives, count);
  }

  public double getPrecision() {
    return (double) truePositives / (double) Math.addExact(truePositives, falsePositives);
  }

  public double getRecall() {
    return (double) truePositives / (double) Math.addExact(truePositives, falseNegatives);
  }

  public double getFScore() {
    return 2.0 * truePositives / (2.0 * truePositives + (double) Math
        .addExact(falsePositives, falseNegatives));
  }

  public void add(ConfusionMatrix confusionMatrix) {
    truePositives = Math.addExact(truePositives, confusionMatrix.truePositives);
    falsePositives = Math.addExact(falsePositives, confusionMatrix.falsePositives);
    falseNegatives = Math.addExact(falseNegatives, confusionMatrix.falseNegatives);
    trueNegatives = Math.addExact(trueNegatives, confusionMatrix.trueNegatives);
  }
}
