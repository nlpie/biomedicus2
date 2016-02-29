package edu.umn.biomedicus.tnt;

import edu.umn.biomedicus.model.semantics.PartOfSpeech;
import edu.umn.biomedicus.model.tuples.PosCap;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;

/**
 *
 */
public class PartOfSpeechConstructor extends Constructor {
    public PartOfSpeechConstructor() {
        this.yamlConstructors.put(new Tag("!pc"), new ConstructPosCap());
        this.yamlConstructors.put(new Tag("!pos"), new ConstructPartOfSpeech());
    }

    private class ConstructPosCap extends AbstractConstruct {

        @Override
        public Object construct(Node node) {
            String value = (String) constructScalar((ScalarNode) node);
            boolean isCapitalized = value.charAt(0) == 'C';
            PartOfSpeech partOfSpeech = PartOfSpeech.MAP.get(value.substring(1));
            return PosCap.create(partOfSpeech, isCapitalized);
        }
    }

    private class ConstructPartOfSpeech extends AbstractConstruct {
        @Override
        public Object construct(Node node) {
            String value = (String) constructScalar((ScalarNode) node);
            return PartOfSpeech.MAP.get(value);
        }
    }
}
