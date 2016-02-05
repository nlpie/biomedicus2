package edu.umn.biomedicus.sections;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import edu.umn.biomedicus.common.utilities.Patterns;

import java.util.regex.Pattern;

/**
 * Guice module for sections.
 *
 * @since 1.4
 */
public class SectionsModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Pattern.class).annotatedWith(Names.named("sectionHeaders"))
                .toInstance(Patterns.loadPatternByJoiningLines("edu/umn/biomedicus/config/section/headers.txt"));
    }
}
