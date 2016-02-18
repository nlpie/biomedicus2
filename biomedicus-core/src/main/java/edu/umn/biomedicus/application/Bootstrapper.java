package edu.umn.biomedicus.application;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import edu.umn.biomedicus.sections.SectionsModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class Bootstrapper {

    private final Injector injector;

    public Bootstrapper(Module... additionalModules) {
        List<Module> modules = new ArrayList<>();
        modules.add(new BiomedicusModule());
        modules.add(new SectionsModule());
        modules.addAll(Arrays.asList(additionalModules));
        injector = Guice.createInjector(modules.toArray(new Module[modules.size()]));
    }

    public Injector injector() {
        return injector;
    }
}
