package edu.umn.biomedicus.application;

import com.google.inject.*;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 *
 */
public class BiomedicusScopes {
    public static final Scope DOCUMENT_SCOPE = new Scope() {
        @Override
        public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped) {
            return () -> {
                DocumentContext documentContext = DOCUMENT_CONTEXT.get();
                if (null != documentContext) {
                    @SuppressWarnings("unchecked")
                    T t = (T) documentContext.seededObjects.get(key);

                    if (t == null) {
                        t = unscoped.get();
                        if (!Scopes.isCircularProxy(t)) {
                            documentContext.seededObjects.put(key, t);
                        }
                    }

                    return t;
                } else {
                    throw new OutOfScopeException("Not currently in a document scope");
                }
            };
        }
    };

    private static final ThreadLocal<DocumentContext> DOCUMENT_CONTEXT = new ThreadLocal<>();

    public static void runInDocumentScope(Runnable runnable, Map<Key<?>, Object> seededObjects) throws Exception {
        runInDocumentScope(() -> {
            runnable.run();
            return null;
        }, seededObjects);
    }

    public static <T> T runInDocumentScope(Callable<T> callable, Map<Key<?>, Object> seededObjects) throws Exception {
        boolean invalidSeeds = Objects.requireNonNull(seededObjects, "Seeded objects should not be null")
                .entrySet()
                .stream()
                .anyMatch(entry -> {
                    if (entry.getValue() == null) {
                        return true;
                    }
                    if (!entry.getKey().getTypeLiteral().getRawType().isInstance(entry.getValue())) {
                        return true;
                    }
                    return false;
                });

        if (invalidSeeds) {
            throw new IllegalArgumentException("Invalid seed map, contains null objects or objects which do not match their keys");
        }

        Map<Key<?>, Object> seedsCopy = seededObjects.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        DocumentContext documentContext = new DocumentContext(seedsCopy);
        return documentContext.call(callable);
    }

    public static <T> Provider<T> providedViaSeeding() {
        return () -> {
            throw new IllegalStateException("This provider should not be called, the object should be seeded when entering the document scope");
        };
    }

    private static final class DocumentContext {
        private final Map<Key<?>, Object> seededObjects;

        public DocumentContext(Map<Key<?>, Object> seededObjects) {
            this.seededObjects = seededObjects;
        }

        private synchronized <T> T call(Callable<T> callable) throws Exception {
            if (DOCUMENT_CONTEXT.get() != null) {
                throw new IllegalStateException("Document scope already in progress");
            }
            DOCUMENT_CONTEXT.set(this);
            try {
                return callable.call();
            } finally {
                DOCUMENT_CONTEXT.remove();
            }
        }
    }
}
