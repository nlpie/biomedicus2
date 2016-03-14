package edu.umn.biomedicus.serialization;

import edu.umn.biomedicus.common.semantics.PartOfSpeech;
import edu.umn.biomedicus.common.terms.TermIndex;
import edu.umn.biomedicus.common.terms.TermVector;
import edu.umn.biomedicus.common.tuples.PosCap;
import edu.umn.biomedicus.common.terms.IndexedTerm;
import edu.umn.biomedicus.concepts.CUI;
import edu.umn.biomedicus.concepts.SUI;
import edu.umn.biomedicus.concepts.TUI;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.util.List;

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

    public static Yaml createYaml(TermIndex termIndex) {
        Yaml yaml = new Yaml(constructor(termIndex), representer(termIndex));
        yaml.addImplicitResolver(new Tag("!cui"), CUI.CUI_PATTERN, "C");
        yaml.addImplicitResolver(new Tag("!tui"), TUI.TUI_PATTERN, "T");
        yaml.addImplicitResolver(new Tag("!sui"), SUI.SUI_PATTERN, "S");
        return yaml;
    }

    private static Constructor constructor(TermIndex termIndex) {
        return new Constructor() {
            {
                yamlConstructors.put(new Tag("!pc"), new AbstractConstruct() {
                    @Override
                    public Object construct(Node node) {
                        String value = (String) constructScalar((ScalarNode) node);
                        boolean isCapitalized = value.charAt(0) == 'C';
                        PartOfSpeech partOfSpeech = PartOfSpeech.MAP.get(value.substring(1));
                        return PosCap.create(partOfSpeech, isCapitalized);
                    }
                });
                yamlConstructors.put(new Tag("!pos"), new AbstractConstruct() {
                    @Override
                    public Object construct(Node node) {
                        String value = (String) constructScalar((ScalarNode) node);
                        return PartOfSpeech.MAP.get(value);
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
                if (termIndex != null) {
                    yamlConstructors.put(new Tag("!t"), new AbstractConstruct() {
                        @Override
                        public Object construct(Node node) {
                            String val = (String) constructScalar((ScalarNode) node);
                            return termIndex.lookup(val);
                        }
                    });
                    yamlConstructors.put(new Tag("!tv"), new AbstractConstruct() {
                        @Override
                        public Object construct(Node node) {
                            String[] val = (String[]) constructArray((SequenceNode) node);
                            return termIndex.lookup(val);
                        }
                    });
                }
            }
        };
    }

    private static Representer representer(TermIndex termIndex) {
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
                if (termIndex != null) {
                    representers.put(IndexedTerm.class, o -> {
                        IndexedTerm it = (IndexedTerm) o;
                        String value = termIndex.getString(it);
                        return representScalar(new Tag("!t"), value);
                    });
                    representers.put(TermVector.class, o -> {
                        TermVector tv = (TermVector) o;
                        List<String> expanded = termIndex.getStrings(tv);
                        return representSequence(new Tag("!tv"), expanded, null);
                    });
                }
            }
        };
    }
}
