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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.impl.XmiCasSerializer;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 *
 */
public class ActiveMqXmiWriter extends JCasAnnotator_ImplBase {

  private static final String PARAM_ACTIVE_MQ_URL = "activeMqUrl";
  private static final String PARAM_ACTIVE_MQ_QUEUE_NAME = "queueName";
  private static final Logger LOGGER = LoggerFactory.getLogger(ActiveMqXmiWriter.class);
  private Connection connection;
  private Session session;
  private MessageProducer messageProducer;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);

    String activeMqUrl = (String) aContext.getConfigParameterValue(PARAM_ACTIVE_MQ_URL);
    LOGGER.info("Starting connection to ActiveMQ server: {}", activeMqUrl);
    String queueName = (String) aContext.getConfigParameterValue(PARAM_ACTIVE_MQ_QUEUE_NAME);

    ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory(
        activeMqUrl);
    try {
      connection = activeMQConnectionFactory.createConnection();
      session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

      Queue queue = session.createQueue(queueName);
      messageProducer = session.createProducer(queue);
    } catch (JMSException e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  public void process(JCas aJCas) throws AnalysisEngineProcessException {
    String message;
    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
      XmiCasSerializer.serialize(aJCas.getCas(), byteArrayOutputStream);
      message = byteArrayOutputStream.toString();
    } catch (IOException | SAXException e) {
      throw new AnalysisEngineProcessException(e);
    }

    try {
      TextMessage textMessage = session.createTextMessage(message);
      messageProducer.send(textMessage);
    } catch (JMSException e) {
      throw new AnalysisEngineProcessException(e);
    }
  }

  @Override
  public void destroy() {
    super.destroy();

    try {
      connection.close();
    } catch (JMSException e) {
      throw new RuntimeException(e);
    }
  }
}
