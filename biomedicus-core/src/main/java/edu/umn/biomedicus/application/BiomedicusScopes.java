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

                    if (t == null || t instanceof Key) {
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

    static final Scope PROCESSOR_SCOPE = new Scope() {
        @Override
        public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped) {
            return () -> {
                ProcessorContext processorContext = PROCESSOR_CONTEXT.get();
                if (null != processorContext) {
                    @SuppressWarnings("unchecked")
                    T t = (T) processorContext.seededObjects.get(key);

                    if (t == null || t instanceof Key) {
                        t = unscoped.get();
                        if (!Scopes.isCircularProxy(t)) {
                            processorContext.seededObjects.put(key, t);
                        }
                    }
                    return t;
                } else {
                    throw new OutOfScopeException("Not currently in a processor scope");
                }
            };
        }
    };

    private static final ThreadLocal<ProcessorContext> PROCESSOR_CONTEXT = new ThreadLocal<>();

    private static final ThreadLocal<DocumentContext> DOCUMENT_CONTEXT = new ThreadLocal<>();

    private static Map<Key<?>, Object> checkForInvalidSeedsAndCopy(Map<Key<?>, Object> seededObjects) {
        boolean invalidSeeds = Objects.requireNonNull(seededObjects, "Seeded objects should not be null")
                .entrySet()
                .stream()
                .anyMatch(entry -> entry.getValue() == null || (!entry.getKey().getTypeLiteral().getRawType().isInstance(entry.getValue()) && !(entry.getValue() instanceof Key)));

        if (invalidSeeds) {
            throw new IllegalArgumentException("Invalid seed map, contains null objects or objects which do not match their keys");
        }

        return seededObjects.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    static <T> T runInProcessorScope(Callable<T> callable, Map<Key<?>, Object> seededObjects) throws Exception {
        ProcessorContext processorContext = new ProcessorContext(checkForInvalidSeedsAndCopy(seededObjects));
        return processorContext.call(callable);
    }

    static <T> T runInDocumentScope(Callable<T> callable, Map<Key<?>, Object> seededObjects) throws Exception {
        DocumentContext documentContext = new DocumentContext(checkForInvalidSeedsAndCopy(seededObjects));
        return documentContext.call(callable);
    }

    public static <T> Provider<T> providedViaSeeding() {
        return () -> {
            throw new IllegalStateException("This provider should not be called, the object should be seeded when entering the document scope");
        };
    }

    private static final class ProcessorContext {
        private final Map<Key<?>, Object> seededObjects;

        ProcessorContext(Map<Key<?>, Object> seededObjects) {
            this.seededObjects = seededObjects;
        }

        private synchronized <T> T call(Callable<T> callable) throws Exception {
            if (PROCESSOR_CONTEXT.get() != null) {
                throw new IllegalStateException("Processor scope already in progress");
            }
            PROCESSOR_CONTEXT.set(this);
            try {
                return callable.call();
            } finally {
                PROCESSOR_CONTEXT.remove();
            }
        }
    }

    private static final class DocumentContext {
        private final Map<Key<?>, Object> seededObjects;

        DocumentContext(Map<Key<?>, Object> seededObjects) {
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
