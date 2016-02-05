/*
 * Copyright (c) 2015 Regents of the University of Minnesota.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.umn.biomedicus.uima.xmi;

import org.apache.uima.cas.TypeSystem;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.TypeSystemUtil;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Semaphore;

/**
 * Implementation class for writing the type system.
 *
 * @author Ben Knoll
 * @since 1.3.0
 */
public class TypeSystemWriterResourceImpl implements TypeSystemWriterResource, SharedResourceObject {
    /**
     * Semaphore which prevents the type system from being written more than once.
     */
    private Semaphore writeOnce = new Semaphore(1);

    /**
     * {@inheritDoc}
     * <p>Writes the type system to the path if it hasn't already been written. Uses the semaphore with 1 permit
     * {@code writeOnce}.</p>
     */
    @Override
    public void writeToPath(Path path, TypeSystem typeSystem) throws IOException, SAXException {
        if (writeOnce.tryAcquire()) {
            Path folder = path.getParent();
            Files.createDirectories(folder);
            TypeSystemDescription typeSystemDescription = TypeSystemUtil.typeSystem2TypeSystemDescription(typeSystem);
            typeSystemDescription.toXML(Files.newOutputStream(path));
        }
    }

    @Override
    public void load(DataResource aData) throws ResourceInitializationException {

    }
}
