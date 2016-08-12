/*
 * Copyright (c) 2016 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.uima.adapter;

import com.google.inject.Injector;
import com.google.inject.Module;
import edu.umn.biomedicus.application.BiomedicusFiles;
import edu.umn.biomedicus.application.Bootstrapper;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.plugins.AbstractPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class UimaBootstrapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(UimaBootstrapper.class);
    private final Injector injector;
    private final List<AbstractPlugin> plugins;

    private UimaBootstrapper(Injector injector, List<AbstractPlugin> plugins) {
        this.injector = injector;
        this.plugins = plugins;
    }

    public static UimaBootstrapper create(Module... additionalModules) throws BiomedicusException {
        Injector injector = Bootstrapper.create(additionalModules).injector();
        BiomedicusFiles biomedicusFiles = injector.getInstance(BiomedicusFiles.class);
        Path uimaPluginsFile = biomedicusFiles.confFolder().resolve("uimaPlugins.txt");
        List<String> pluginClassNames;
        try {
            pluginClassNames = Files.lines(uimaPluginsFile).collect(Collectors.toList());
        } catch (IOException e) {
            throw new BiomedicusException(e);
        }
        List<AbstractPlugin> plugins = new ArrayList<>();
        List<Module> modules = new ArrayList<>();

        modules.add(new UimaModule());

        for (String pluginClassName : pluginClassNames) {
            if (pluginClassName.isEmpty()) {
                continue;
            }
            LOGGER.debug("Loading modules from plugin: {}", pluginClassName);

            Class<? extends AbstractPlugin> pluginClass;
            try {
                pluginClass = Class.forName(pluginClassName).asSubclass(AbstractPlugin.class);
            } catch (ClassNotFoundException e) {
                throw new BiomedicusException(e);
            }
            AbstractPlugin abstractPlugin = injector.getInstance(pluginClass);
            plugins.add(abstractPlugin);
            Collection<? extends Module> pluginModules = abstractPlugin.modules();
            if (LOGGER.isDebugEnabled()) {
                for (Module pluginModule : pluginModules) {
                    LOGGER.debug("Plugin module: {}", pluginModule.toString());
                }
            }
            modules.addAll(pluginModules);
        }

        Injector uimaInjector = injector.createChildInjector(modules);
        return new UimaBootstrapper(uimaInjector, plugins);
    }

    public <T> T createClass(Class<T> tClass) {
        return injector.getInstance(tClass);
    }

    public Injector getInjector() {
        return injector;
    }

    public List<AbstractPlugin> getPlugins() {
        return plugins;
    }
}
