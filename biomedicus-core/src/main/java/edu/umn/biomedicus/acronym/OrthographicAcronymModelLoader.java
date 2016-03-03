package edu.umn.biomedicus.acronym;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.umn.biomedicus.application.BiomedicusConfiguration;
import edu.umn.biomedicus.application.ModelLoader;
import edu.umn.biomedicus.common.vocabulary.CharacterSet;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * Loads the orthographic model.
 *
 * @since 1.5.0
 */
@Singleton
public class OrthographicAcronymModelLoader extends ModelLoader<OrthographicAcronymModel> {
    private final Path orthographicModel;

    private CharacterSet symbols;

    @Inject
    OrthographicAcronymModelLoader(BiomedicusConfiguration biomedicusConfiguration) {
        orthographicModel = biomedicusConfiguration.resolveDataFile("acronym.orthographicModel.path");
    }

    @Override
    protected OrthographicAcronymModel loadModel() throws BiomedicusException {
        Yaml yaml = new Yaml();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> serObj = (Map<String, Object>) yaml.load(Files.newBufferedReader(orthographicModel));
            boolean caseSensitive = (Boolean) serObj.get("caseSensitive");
            symbols = caseSensitive ? OrthographicAcronymModel.CASE_SENS_SYMBOLS : OrthographicAcronymModel.CASE_INSENS_SYMBOLS;
            @SuppressWarnings("unchecked")
            Map<String, Double> abbrevProbsMap = (Map<String, Double>) serObj.get("abbrevProbs");
            double[][][] abbrevProbs = expandProbs(abbrevProbsMap);
            @SuppressWarnings("unchecked")
            Map<String, Double> longformProbsMap = (Map<String, Double>) serObj.get("longformProbs");
            double[][][] longformProbs = expandProbs(longformProbsMap);
            Set<String> longformsLower = new HashSet<>();
            @SuppressWarnings("unchecked")
            List<String> longformsLowerList = (List<String>) serObj.get("longformsLower");
            longformsLower.addAll(longformsLowerList);
            return new OrthographicAcronymModel(abbrevProbs, longformProbs, caseSensitive, longformsLower);
        } catch (IOException e) {
            throw new BiomedicusException(e);
        }
    }

    private double[][][] expandProbs(Map<String, Double> collapsedProbs) {
        double[][][] probs = new double[symbols.size()][symbols.size()][symbols.size()];
        for (Map.Entry<String, Double> entry : collapsedProbs.entrySet()) {
            String key = entry.getKey();
            probs[symbols.indexOf(key.charAt(0))][symbols.indexOf(key.charAt(1))][symbols.indexOf(key.charAt(2))] = entry.getValue();
        }
        return probs;
    }
}
