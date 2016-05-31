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

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;
import edu.umn.biomedicus.type.ClinicalNoteAnnotation;
import edu.umn.biomedicus.uima.common.Views;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JFSIndexRepository;
import org.apache.uima.resource.ResourceInitializationException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 *
 */
public class MongoDbXmiWriter extends JCasAnnotator_ImplBase {

    /**
     * The mongo server to connect to.
     */
    public static final String PARAM_MONGO_SERVER = "mongoServer";

    /**
     * The mongo port to connect to.
     */
    public static final String PARAM_MONGO_PORT = "mongoPort";

    /**
     * The mongo db to use on the server.
     */
    public static final String PARAM_MONGO_DB_NAME = "mongoDbName";

    private MongoClient mongoClient;
    private GridFS gridFS;

    @Override
    public void initialize(UimaContext aContext) throws ResourceInitializationException {
        super.initialize(aContext);

        String mongoServer = (String) aContext.getConfigParameterValue(PARAM_MONGO_SERVER);
        int mongoPort = (Integer) aContext.getConfigParameterValue(PARAM_MONGO_PORT);
        String mongoDbName = (String) aContext.getConfigParameterValue(PARAM_MONGO_DB_NAME);

        try {
            mongoClient = new MongoClient(mongoServer, mongoPort);
        } catch (UnknownHostException e) {
            throw new ResourceInitializationException(e);
        }

        DB db = mongoClient.getDB(mongoDbName);

        gridFS = new GridFS(db);
    }

    @Override
    public void process(JCas jCas) throws AnalysisEngineProcessException {
        JCas systemView;
        try {
            systemView = jCas.getView(Views.SYSTEM_VIEW);
        } catch (CASException e) {
            throw new AnalysisEngineProcessException(e);
        }

        String documentId = null;
        JFSIndexRepository jfsIndexRepository = systemView.getJFSIndexRepository();
        if (jfsIndexRepository != null) {
            FSIterator it = jfsIndexRepository.getAllIndexedFS(ClinicalNoteAnnotation.type);
            if (it.hasNext()) {
                ClinicalNoteAnnotation documentAnnotation = (ClinicalNoteAnnotation) it.next();
                documentId = documentAnnotation.getDocumentId();
            }
        }

        if (documentId == null) {
            documentId = UUID.randomUUID().toString();
        }

        GridFSInputFile file = gridFS.createFile(documentId + ".xmi");

        try (OutputStream outputStream = file.getOutputStream()) {
            XmiCasSerializer.serialize(jCas.getCas(), outputStream);
        } catch (IOException | SAXException e) {
            throw new AnalysisEngineProcessException(e);
        }

    }

    @Override
    public void destroy() {
        super.destroy();

        mongoClient.close();
    }
}
