package edu.umn.biomedicus.common.serialization;

import java.io.Serializable;

public interface SerializationProxy extends Serializable {
    Object deproxy();
}
