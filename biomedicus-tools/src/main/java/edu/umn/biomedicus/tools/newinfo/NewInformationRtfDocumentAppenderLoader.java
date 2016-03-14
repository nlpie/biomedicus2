package edu.umn.biomedicus.tools.newinfo;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import edu.umn.biomedicus.application.BiomedicusConfiguration;
import edu.umn.biomedicus.application.DataLoader;
import edu.umn.biomedicus.exc.BiomedicusException;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 *
 */
@Singleton
public class NewInformationRtfDocumentAppenderLoader extends DataLoader<NewInformationRtfDocumentAppender> {

    private final Path newInfoConfigPath;

    @Inject
    public NewInformationRtfDocumentAppenderLoader(BiomedicusConfiguration biomedicusConfiguration) {
        newInfoConfigPath = biomedicusConfiguration.getConfigDir().resolve("newinfo-rtfrewriter.yml");
    }

    @Override
    protected NewInformationRtfDocumentAppender loadModel() throws BiomedicusException {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> newInfoConfig = (Map<String, Object>) new Yaml().load(Files.newBufferedReader(newInfoConfigPath));

            @SuppressWarnings("unchecked")
            Map<String, String> beginMap = (Map<String, String>) newInfoConfig.get("beginMap");

            @SuppressWarnings("unchecked")
            Map<String, String> endMap = (Map<String, String>) newInfoConfig.get("endMap");

            String colorTableMarker = (String) newInfoConfig.get("colorTableMarker");

            String stylesheetMarker = (String) newInfoConfig.get("stylesheetMarker");

            return new NewInformationRtfDocumentAppender(beginMap, endMap, colorTableMarker, stylesheetMarker);
        } catch (IOException e) {
            throw new BiomedicusException(e);
        }
    }
}
