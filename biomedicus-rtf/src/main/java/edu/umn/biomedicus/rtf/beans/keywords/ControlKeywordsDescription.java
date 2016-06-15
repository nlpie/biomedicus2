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

package edu.umn.biomedicus.rtf.beans.keywords;

import com.google.common.base.Preconditions;
import edu.umn.biomedicus.rtf.reader.KeywordAction;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 */
@XmlRootElement
@XmlType
public class ControlKeywordsDescription {
    private List<ControlKeyword> controlKeywords;

    @XmlElementWrapper(required = true)
    @XmlElement(name = "controlKeyword")
    public List<ControlKeyword> getControlKeywords() {
        return controlKeywords;
    }

    public void setControlKeywords(List<ControlKeyword> controlKeywords) {
        this.controlKeywords = controlKeywords;
    }

    public static ControlKeywordsDescription loadFromFile(String classpath) {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(classpath);
        return JAXB.unmarshal(inputStream, ControlKeywordsDescription.class);
    }

    public Map<String, KeywordAction> getKeywordActionsAsMap() {
        assert controlKeywords != null;
        Preconditions.checkNotNull(controlKeywords);

        return controlKeywords.stream()
                .collect(Collectors.toMap(ControlKeyword::getKeyword, ControlKeyword::getKeywordAction));
    }
}
