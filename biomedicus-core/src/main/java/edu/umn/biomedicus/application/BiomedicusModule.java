package edu.umn.biomedicus.application;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.name.Names;
import edu.umn.biomedicus.annotations.DocumentScoped;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

/**
 *
 */
class BiomedicusModule extends AbstractModule {

    private final Map<String, Object> settings;

    private final Collection<NamedImplementationBinding> namedImplementationBindings;

    BiomedicusModule(Map<String, Object> settings, Collection<NamedImplementationBinding> namedImplementationBindings) {
        this.settings = settings;
        this.namedImplementationBindings = namedImplementationBindings;
    }

    @Override
    protected void configure() {
        bindScope(DocumentScoped.class, BiomedicusScopes.DOCUMENT_SCOPE);

        settings.forEach((key, value) -> {
            bindSetting(value.getClass(), key, value);
        });

        for (NamedImplementationBinding namedImplementationBinding : namedImplementationBindings) {
            namedImplementationBinding.bind(binder());
        }

    }

    private <T> void bindSetting(Class<T> tClass, String key, Object value) {
        if (value instanceof Path) {
            tClass = (Class<T>) Path.class;
        }
        bind(tClass).annotatedWith(Names.named(key)).toInstance(tClass.cast(value));
    }

    static class NamedImplementationBinding<T> {
        private Class<T> superclass;
        private String key;
        private Class<? extends T> aClass;

        NamedImplementationBinding(Class<T> superclass, String key, Class<? extends T> aClass) {
            this.superclass = superclass;
            this.key = key;
            this.aClass = aClass;
        }

        static <T> NamedImplementationBinding<T> create(Class<T> superclass, String key, Class<?> aClass) {
            Class<? extends T> tClass = aClass.asSubclass(superclass);
            return new NamedImplementationBinding<>(superclass, key, tClass);
        }

        public void bind(Binder binder) {
            binder.bind(superclass).annotatedWith(Names.named(key)).to(aClass);
        }
    }
}
