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

package edu.umn.biomedicus.common.terms;

import java.io.IOException;
import org.jetbrains.annotations.NotNull;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.serializer.GroupSerializer;
import org.mapdb.serializer.GroupSerializerObjectArray;

/**
 *
 */
public class TermsBagSerializer extends GroupSerializerObjectArray<TermsBag> {

  @Override
  public void serialize(@NotNull DataOutput2 out, @NotNull TermsBag value) throws IOException {
    out.packInt(value.size());
    for (IndexedTerm indexedTerm : value) {
      out.packInt(indexedTerm.termIdentifier());
    }
  }

  @Override
  public TermsBag deserialize(@NotNull DataInput2 input, int available) throws IOException {
    return null;
  }
}
