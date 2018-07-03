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

package edu.umn.biomedicus.examples;

import edu.umn.biomedicus.annotations.Setting;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ExampleJavaResource {

  private final List<String> values;

  public ExampleJavaResource(List<String> values) {
    this.values = values;
  }

  @Inject
  public ExampleJavaResource(@Setting("example.valuesFile") Path valuesFile) throws IOException {
    this(Files.readAllLines(valuesFile));
  }

  public boolean isInValues(CharSequence cs) {
    return values.stream().anyMatch(s -> s.contentEquals(cs));
  }
}
