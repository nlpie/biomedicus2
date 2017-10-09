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

package edu.umn.biomedicus.serialization;

import edu.umn.biomedicus.common.dictionary.BidirectionalDictionary;
import edu.umn.biomedicus.common.dictionary.StringIdentifier;
import edu.umn.biomedicus.common.dictionary.StringsBag;
import edu.umn.biomedicus.common.tuples.PosCap;
import edu.umn.biomedicus.common.types.syntax.PartOfSpeech;
import edu.umn.biomedicus.common.types.syntax.PartsOfSpeech;
import edu.umn.biomedicus.concepts.CUI;
import edu.umn.biomedicus.concepts.SUI;
import edu.umn.biomedicus.concepts.TUI;
import java.util.Arrays;
import java.util.Collection;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/**
 *
 */
public final class YamlSerialization {

  private YamlSerialization() {
    throw new UnsupportedOperationException();
  }

  public static Yaml createYaml() {
    return createYaml(null);
  }

  public static Yaml createYaml(BidirectionalDictionary bidirectionalDictionary) {
    Yaml yaml = new Yaml(constructor(bidirectionalDictionary), representer(bidirectionalDictionary));
    yaml.addImplicitResolver(new Tag("!cui"), CUI.CUI_PATTERN, "C");
    yaml.addImplicitResolver(new Tag("!tui"), TUI.TUI_PATTERN, "T");
    yaml.addImplicitResolver(new Tag("!sui"), SUI.SUI_PATTERN, "S");
    return yaml;
  }

  private static Constructor constructor(BidirectionalDictionary bidirectionalDictionary) {
    return new Constructor() {
      {
        yamlConstructors.put(new Tag("!pc"), new AbstractConstruct() {
          @Override
          public Object construct(Node node) {
            String value = (String) constructScalar((ScalarNode) node);
            boolean isCapitalized = value.charAt(0) == 'C';
            PartOfSpeech partOfSpeech = PartsOfSpeech.forTag(value.substring(1));
            return PosCap.create(partOfSpeech, isCapitalized);
          }
        });
        yamlConstructors.put(new Tag("!pos"), new AbstractConstruct() {
          @Override
          public Object construct(Node node) {
            String value = (String) constructScalar((ScalarNode) node);
            return PartsOfSpeech.forTag(value);
          }
        });
        yamlConstructors.put(new Tag("!cui"), new AbstractConstruct() {
          @Override
          public Object construct(Node node) {
            String val = (String) constructScalar((ScalarNode) node);
            return new CUI(val);
          }
        });
        yamlConstructors.put(new Tag("!tui"), new AbstractConstruct() {
          @Override
          public Object construct(Node node) {
            String val = (String) constructScalar((ScalarNode) node);
            return new TUI(val);
          }
        });
        yamlConstructors.put(new Tag("!sui"), new AbstractConstruct() {
          @Override
          public Object construct(Node node) {
            String val = (String) constructScalar((ScalarNode) node);
            return new SUI(val);
          }
        });
        if (bidirectionalDictionary != null) {
          yamlConstructors.put(new Tag("!t"), new AbstractConstruct() {
            @Override
            public Object construct(Node node) {
              String val = (String) constructScalar((ScalarNode) node);
              return bidirectionalDictionary.getTermIdentifier(val);
            }
          });
          yamlConstructors.put(new Tag("!tv"), new AbstractConstruct() {
            @Override
            public Object construct(Node node) {
              String[] val = (String[]) constructArray((SequenceNode) node);
              return bidirectionalDictionary.getTermsBag(Arrays.asList(val));
            }
          });
        }
      }
    };
  }

  private static Representer representer(BidirectionalDictionary bidirectionalDictionary) {
    return new Representer() {
      {
        representers.put(PosCap.class, o -> {
          PosCap posCap = (PosCap) o;
          String value = (posCap.isCapitalized() ? "C" : 'l') + posCap.getPartOfSpeech().toString();
          return representScalar(new Tag("!pc"), value);
        });
        representers.put(PartOfSpeech.class, o -> {
          PartOfSpeech partOfSpeech = (PartOfSpeech) o;
          String value = partOfSpeech.toString();
          return representScalar(new Tag("!pos"), value);
        });
        representers.put(CUI.class, o -> {
          CUI cui = (CUI) o;
          String value = cui.toString();
          return representScalar(new Tag("!cui"), value);
        });
        representers.put(TUI.class, o -> {
          TUI tui = (TUI) o;
          String value = tui.toString();
          return representScalar(new Tag("!tui"), value);
        });
        representers.put(SUI.class, o -> {
          SUI sui = (SUI) o;
          String value = sui.toString();
          return representScalar(new Tag("!sui"), value);
        });
        if (bidirectionalDictionary != null) {
          representers.put(StringIdentifier.class, o -> {
            StringIdentifier it = (StringIdentifier) o;
            String value = bidirectionalDictionary.getTerm(it);
            return representScalar(new Tag("!t"), value);
          });
          representers.put(StringsBag.class, o -> {
            StringsBag tv = (StringsBag) o;
            Collection<String> expanded = bidirectionalDictionary.getTerms(tv);
            return representSequence(new Tag("!tv"), expanded, null);
          });
        }
      }
    };
  }
}
