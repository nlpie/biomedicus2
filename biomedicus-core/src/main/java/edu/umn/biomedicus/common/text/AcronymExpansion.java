package edu.umn.biomedicus.common.text;

import edu.umn.biomedicus.common.serialization.ProxySerializable;
import edu.umn.biomedicus.common.serialization.SerializationProxy;

public class AcronymExpansion implements ProxySerializable {
    private final String longform;

    public AcronymExpansion(String longform) {
        this.longform = longform;
    }

    public String getLongform() {
        return longform;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AcronymExpansion that = (AcronymExpansion) o;

        return longform.equals(that.longform);

    }

    @Override
    public int hashCode() {
        return longform.hashCode();
    }

    @Override
    public String toString() {
        return "AcronymExpansion(" + longform + ")";
    }

    @Override
    public SerializationProxy proxy() {
        Proxy proxy = new Proxy();
        proxy.setLongform(longform);
        return proxy;
    }

    public static class Proxy implements SerializationProxy {
        private String longform;

        public String getLongform() {
            return longform;
        }

        public Proxy setLongform(String longform) {
            this.longform = longform;
            return this;
        }

        @Override
        public Object deproxy() {
            return new AcronymExpansion(longform);
        }
    }
}
