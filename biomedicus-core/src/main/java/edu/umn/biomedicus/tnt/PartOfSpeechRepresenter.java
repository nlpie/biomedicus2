package edu.umn.biomedicus.tnt;

import edu.umn.biomedicus.common.semantics.PartOfSpeech;
import edu.umn.biomedicus.common.tuples.PosCap;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

/**
 *
 */
public class PartOfSpeechRepresenter extends Representer {
    public PartOfSpeechRepresenter() {
        this.representers.put(PosCap.class, new RepresentPosCap());
        this.representers.put(PartOfSpeech.class, new RepresentPartOfSpeech());
    }

    private class RepresentPosCap implements Represent {

        @Override
        public Node representData(Object o) {
            PosCap posCap = (PosCap) o;
            String value = (posCap.isCapitalized() ? "C" : 'l') + posCap.getPartOfSpeech().toString();
            return representScalar(new Tag("!pc"), value);
        }
    }

    private class RepresentPartOfSpeech implements Represent {
        @Override
        public Node representData(Object o) {
            PartOfSpeech partOfSpeech = (PartOfSpeech) o;
            String value = partOfSpeech.toString();
            return representScalar(new Tag("!pos"), value);
        }
    }
}
