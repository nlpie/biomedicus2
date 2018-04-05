/*
 * Copyright (c) 2018 Regents of the University of Minnesota.
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

package edu.umn.biomedicus.uima.xmi;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.CasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.resource.ResourceInitializationException;
import org.xml.sax.SAXException;

/**
 *
 */
public class MongoDbXmiWriter extends CasAnnotator_ImplBase {

  private MongoClient mongoClient;

  private GridFSBucket gridFS;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);

    String mongoServer = (String) aContext.getConfigParameterValue("mongoServer");
    int mongoPort = (Integer) aContext.getConfigParameterValue("mongoPort");
    String mongoDbName = (String) aContext.getConfigParameterValue("mongoDbName");

    mongoClient = new MongoClient(mongoServer, mongoPort);

    MongoDatabase db = mongoClient.getDatabase(mongoDbName);



    gridFS = GridFSBuckets.create(db);
  }

  @Override
  public void destroy() {
    super.destroy();

    mongoClient.close();
  }

  @Override
  public void process(CAS aCAS) throws AnalysisEngineProcessException {
    Type documentIdType = aCAS.getTypeSystem()
        .getType("edu.umn.biomedicus.uima.type1_5.DocumentId");
    Feature docIdFeat = documentIdType.getFeatureByBaseName("documentId");

    String documentId = aCAS.getIndexRepository()
        .getAllIndexedFS(documentIdType)
        .get()
        .getStringValue(docIdFeat);

    if (documentId == null) {
      documentId = UUID.randomUUID().toString();
    }

    try (OutputStream outputStream = gridFS.openUploadStream(documentId + ".xmi")) {
      XmiCasSerializer.serialize(aCAS, outputStream);
    } catch (IOException | SAXException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }
}
