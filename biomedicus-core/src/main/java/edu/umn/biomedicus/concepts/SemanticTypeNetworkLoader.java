package edu.umn.biomedicus.concepts;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.umn.biomedicus.application.BiomedicusConfiguration;
import edu.umn.biomedicus.application.DataLoader;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.io.IOException;
import java.nio.file.Path;

/**
 *
 */
@Singleton
public class SemanticTypeNetworkLoader extends DataLoader<SemanticTypeNetwork> {

    private final Path srdefPath;

    private final Path semgroupsPath;

    @Inject
    public SemanticTypeNetworkLoader(BiomedicusConfiguration biomedicusConfiguration) {
        srdefPath = biomedicusConfiguration.resolveDataFile("semanticNetwork.srdef.path");
        semgroupsPath = biomedicusConfiguration.resolveDataFile("semanticNetwork.semgroups.path");
    }

    @Override
    protected SemanticTypeNetwork loadModel() throws BiomedicusException {
        try {
            return SemanticTypeNetwork.loadFromFiles(srdefPath, semgroupsPath);
        } catch (IOException e) {
            throw new BiomedicusException(e);
        }
    }
}
