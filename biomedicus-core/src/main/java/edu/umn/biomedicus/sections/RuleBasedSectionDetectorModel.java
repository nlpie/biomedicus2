package edu.umn.biomedicus.sections;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import edu.umn.biomedicus.common.utilities.Patterns;
import edu.umn.biomedicus.exc.BiomedicusException;

import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 *
 */
@Singleton
class RuleBasedSectionDetectorModel {
    private final Pattern sectionHeaderPattern;

    @Inject
    RuleBasedSectionDetectorModel(@Named("sections.headers") Path path) throws BiomedicusException {
        sectionHeaderPattern = Patterns.loadPatternByJoiningLines(path);
    }

    Pattern getSectionHeaderPattern() {
        return sectionHeaderPattern;
    }
}
