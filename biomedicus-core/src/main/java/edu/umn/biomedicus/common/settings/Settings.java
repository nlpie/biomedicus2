package edu.umn.biomedicus.common.settings;

import java.nio.file.Path;

/**
 * Created by benknoll on 4/1/16.
 */
public interface Settings {
    String get(String key);

    boolean containsSetting(String key);

    boolean getAsBoolean(String key);

    int getAsInt(String key);

    long getAsLong(String key);

    float getAsFloat(String key);

    double getAsDouble(String key);

    Path getAsPath(String key);
}
