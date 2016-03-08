package edu.umn.biomedicus.serialization;

import edu.umn.biomedicus.common.semantics.PartOfSpeech;
import edu.umn.biomedicus.common.tuples.PosCap;
import edu.umn.biomedicus.concepts.CUI;
import edu.umn.biomedicus.concepts.TUI;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
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
        Yaml yaml = new Yaml(CONSTRUCTOR, REPRESENTER);
        yaml.addImplicitResolver(new Tag("!cui"), CUI.CUI_PATTERN, "C");
        yaml.addImplicitResolver(new Tag("!tui"), TUI.TUI_PATTERN, "T");
        return yaml;
    }

    private static final Constructor CONSTRUCTOR = new Constructor() {
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
        }
    };

    private static final Representer REPRESENTER = new Representer() {
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
                String value = cui.getText();
                return representScalar(new Tag("!cui"), value);
            });
            representers.put(TUI.class, o -> {
                TUI tui = (TUI) o;
                String value = tui.getText();
                return representScalar(new Tag("!tui"), value);
            });
        }
    };
}
