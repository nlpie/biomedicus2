package edu.umn.biomedicus.application;

import edu.umn.biomedicus.annotations.Setting;

import java.io.Serializable;
import java.lang.annotation.Annotation;

/**
 *
 */
class SettingImpl implements Setting, Serializable {
    private final String value;

    SettingImpl(String value) {
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
        if (!(obj instanceof Setting)) {
            return false;
        }
        Setting other = (Setting) obj;
        return value.equals(other.value());
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Setting.class;
    }

    @Override
    public String toString() {
        return "Setting(" + value + ")";
    }
}
