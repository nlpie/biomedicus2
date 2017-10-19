/*
 * Copyright (c) 2017 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.framework;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Named;
import edu.umn.biomedicus.exc.BiomedicusException;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Responsible for running document sources.
 *
 * @author Ben Knoll
 * @since 1.7.0
 */
public final class DocumentSourceRunner extends ScopedWork implements AutoCloseable {

  @Nullable
  private Class<? extends DocumentSource> documentSourceClass;

  @Nullable
  private DocumentSource documentSource;

  @Inject
  public DocumentSourceRunner(
      Injector injector,
      @Named("globalSettings") Map<String, Object> globalSettings,
      SettingsTransformer settingsTransformer
  ) {
    super(injector, globalSettings, settingsTransformer);
  }

  public static DocumentSourceRunner create(Injector injector) {
    return injector.getInstance(DocumentSourceRunner.class);
  }

  public void setDocumentSourceClass(Class<? extends DocumentSource> documentSourceClass) {
    this.documentSourceClass = documentSourceClass;
  }

  public void setDocumentSourceClassName(String documentSourceClassName)
      throws ClassNotFoundException {
    this.documentSourceClass = Class.forName(documentSourceClassName)
        .asSubclass(DocumentSource.class);
  }

  @Override
  public void initialize(
      @Nullable Map<String, Object> processorSettings,
      @Nullable Map<Key<?>, Object> processorScopedObjects
  ) throws BiomedicusException {
    super.initialize(processorSettings, processorScopedObjects);

    Preconditions.checkNotNull(processorContext);
    Preconditions.checkNotNull(settingsInjector);
    Preconditions.checkNotNull(documentSourceClass);

    try {
      processorContext.call(() -> {
        documentSource = settingsInjector.getInstance(documentSourceClass);
        return null;
      });
    } catch (Exception e) {
      throw new BiomedicusException(e);
    }
  }

  public boolean hasNext() throws BiomedicusException {
    Preconditions.checkNotNull(processorContext);
    Preconditions.checkNotNull(documentSource);
    try {
      return processorContext.call(() -> documentSource.hasNext());
    } catch (Exception e) {
      throw new BiomedicusException(e);
    }
  }

  public void populateNext(DocumentBuilder documentBuilder) throws BiomedicusException {
    Preconditions.checkNotNull(processorContext);
    Preconditions.checkNotNull(documentSource);
    try {
      processorContext.call(() -> {
        documentSource.next(documentBuilder);
        return null;
      });
    } catch (Exception e) {
      throw new BiomedicusException(e);
    }
  }

  public long estimateTotal() throws BiomedicusException {
    Preconditions.checkNotNull(processorContext);
    Preconditions.checkNotNull(documentSource);
    try {
      return processorContext.call(() -> documentSource.estimateTotal());
    } catch (Exception e) {
      throw new BiomedicusException(e);
    }
  }

  @Override
  public void close() throws Exception {
    if (documentSource != null) {
      documentSource.close();
    }
  }
}
