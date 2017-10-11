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

import edu.umn.biomedicus.numbers.NumberType;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.immutables.value.Value;

/**
 * Represents a number in text, either appearing in english form or in numeral form.
 *
 * @since 1.8.0
 */
@Value.Immutable
public interface Number {

  /**
   * The numerator value of the number normalized to numeral forum in the {@link BigDecimal} string
   * format.
   *
   * @return {@link BigDecimal#toString()} formatted numerator
   */
  String numerator();

  /**
   * The denominator value of the number normalized to numeral for in the {@link BigDecimal} string
   * format.
   *
   * @return {@link BigDecimal#toString()} formatted denominator
   */
  String denominator();

  /**
   * The type of number that this is.
   *
   * @return whether this number is cardinal, ordinal, fraction, or decimal
   */
  NumberType numberType();

  /**
   * Turns the value into a {@link BigInteger} object.
   *
   * @return BigInteger object with the value of this number.
   */
  default BigDecimal value() {
    return new BigDecimal(numerator()).divide(new BigDecimal(denominator()),
        BigDecimal.ROUND_HALF_UP);
  }
}
