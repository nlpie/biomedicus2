package edu.umn.biomedicus.concepts;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.umn.biomedicus.annotations.Setting;
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
    public SemanticTypeNetworkLoader(@Setting("semanticNetwork.srdef.path") Path srdefPath,
                                     @Setting("semanticNetwork.semgroups.path") Path semgroupsPath) {
        this.srdefPath = srdefPath;
        this.semgroupsPath = semgroupsPath;
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
