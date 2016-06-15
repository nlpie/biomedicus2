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

package edu.umn.biomedicus.application;

import com.google.inject.AbstractModule;
import edu.umn.biomedicus.annotations.DocumentScoped;
import edu.umn.biomedicus.annotations.ProcessorScoped;
import edu.umn.biomedicus.common.text.Document;

/**
 *
 */
class BiomedicusModule extends AbstractModule {
    @Override
    protected void configure() {
        bindScope(DocumentScoped.class, BiomedicusScopes.DOCUMENT_SCOPE);
        bindScope(ProcessorScoped.class, BiomedicusScopes.PROCESSOR_SCOPE);

        bind(Document.class).toProvider(BiomedicusScopes.providedViaSeeding()).in(BiomedicusScopes.DOCUMENT_SCOPE);
    }
}
