package edu.umn.biomedicus.application;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import edu.umn.biomedicus.sections.SectionsModule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class Bootstrapper {

    private final Injector injector;

    public Bootstrapper(Module... additionalModules) throws IOException {
        List<Module> modules = new ArrayList<>();
        modules.add(new BiomedicusModule());
        modules.add(new SectionsModule());
        modules.addAll(Arrays.asList(additionalModules));
        injector = Guice.createInjector(modules.toArray(new Module[modules.size()]));
    }

    public static Bootstrapper create(Module... additionalModules) throws IOException {
        return new Bootstrapper(additionalModules);
    }

    public Injector injector() {
        return injector;
    }

    public <T> T getInstance(Class<T> tClass) {
        return injector.getInstance(tClass);
    }
}
