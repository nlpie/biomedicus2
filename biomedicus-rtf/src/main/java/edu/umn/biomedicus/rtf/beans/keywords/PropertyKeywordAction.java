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

import edu.umn.biomedicus.rtf.exc.RtfReaderException;
import edu.umn.biomedicus.rtf.reader.KeywordAction;
import edu.umn.biomedicus.rtf.reader.State;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 */
@XmlRootElement
@XmlType
public class PropertyKeywordAction extends AbstractKeywordAction {
    private String propertyGroup;

    private String propertyName;

    private boolean alwaysUseDefault;

    private int defaultValue;

    @XmlElement(required = true)
    public String getPropertyGroup() {
        return propertyGroup;
    }

    public void setPropertyGroup(String propertyGroup) {
        this.propertyGroup = propertyGroup;
    }

    @XmlElement(required = true)
    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    @XmlElement(required = true)
    public boolean isAlwaysUseDefault() {
        return alwaysUseDefault;
    }

    public void setAlwaysUseDefault(boolean alwaysUseDefault) {
        this.alwaysUseDefault = alwaysUseDefault;
    }

    @XmlElement(required = true)
    public int getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(int defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public void executeKeyword(State state) throws RtfReaderException {
        int value = (!alwaysUseDefault && hasParameter()) ? getParameter() : defaultValue;
        state.setPropertyValue(propertyGroup, propertyName, value);
    }

    @Override
    public KeywordAction copy() {
        PropertyKeywordAction propertyKeywordAction = new PropertyKeywordAction();
        propertyKeywordAction.setPropertyGroup(propertyGroup);
        propertyKeywordAction.setPropertyName(propertyName);
        propertyKeywordAction.setAlwaysUseDefault(alwaysUseDefault);
        propertyKeywordAction.setDefaultValue(defaultValue);
        return propertyKeywordAction;
    }
}
