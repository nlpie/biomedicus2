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
import edu.umn.biomedicus.application.DataLoader;
import edu.umn.biomedicus.exc.BiomedicusException;
import edu.umn.biomedicus.plugins.AbstractPlugin;
import org.apache.uima.resource.Resource_ImplBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Guice injector resource implementation.
 *
 * @author Ben Knoll
 * @since 1.4
 */
public class GuiceInjector extends Resource_ImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuiceInjector.class);

    private final Injector injector;

    public GuiceInjector() {
        LOGGER.info("Initializing Guice Injector Resource");
        try {
            UimaBootstrapper uimaBootstrapper = UimaBootstrapper.create();
            injector = uimaBootstrapper.getInjector();

            for (AbstractPlugin plugin : uimaBootstrapper.getPlugins()) {
                for (Class<DataLoader> loaderClass : plugin.dataLoaders()) {
                    DataLoader dataLoader = injector.getInstance(loaderClass);
                    dataLoader.eagerLoad();
                }
            }
        } catch (BiomedicusException e) {
            throw new IllegalStateException(e);
        }
    }

    public Injector getInjector() {
        return injector;
    }
}
