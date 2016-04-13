package edu.umn.biomedicus.application;

import edu.umn.biomedicus.annotations.ProcessorSetting;
import edu.umn.biomedicus.annotations.Setting;

import java.io.Serializable;
import java.lang.annotation.Annotation;

/**
 */
public class ProcessorSettingImpl implements ProcessorSetting, Serializable {
    private final String value;

    public ProcessorSettingImpl(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Setting key cannot be null");
        }

        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public int hashCode() {
        return (127 * "value".hashCode()) ^ value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ProcessorSetting)) {
            return false;
        }
        ProcessorSetting other = (ProcessorSetting) obj;
        return value.equals(other.value());
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return ProcessorSetting.class;
    }

    @Override
    public String toString() {
        return "ProcessorSetting(" + value + ")";
    }
}
