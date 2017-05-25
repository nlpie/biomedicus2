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

package edu.umn.biomedicus.framework;

import com.google.inject.*;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Contains the Guice scopes and contexts that store their scoped objects.<br/>
 * <br/>
 * There are two primary scopes:<br/>
 * <ul>
 * <li>
 * Processor Scope: contains objects that are one instance per configured document processor. For example, any processor
 * settings.
 * </li>
 * <li>
 * Document Scope: contains objects that are one instance per document, for example a document, or any data that is
 * specific to and has one instance per document.
 * </li>
 * </ul>
 * <br/>
 * The primary methods for running code in scopes are {@link #createProcessorContext(Map)} and
 * {@link #runInDocumentScope(Map, Callable)}.
 *
 * @author Ben Knoll
 * @since 1.6.0
 */
public final class BiomedicusScopes {
    private static final ThreadLocal<Context> PROCESSOR_CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Context> DOCUMENT_CONTEXT = new ThreadLocal<>();

    /**
     * The processor scope. Used when binding objects to cause them to have a single instance per processor type.
     */
    public static final Scope PROCESSOR_SCOPE = new ContextScope(PROCESSOR_CONTEXT);

    /**
     * The document scope. Used when binding objects to cause them to have a single instance per document.
     */
    public static final Scope DOCUMENT_SCOPE = new ContextScope(DOCUMENT_CONTEXT);

    private static Map<Key<?>, Object> checkForInvalidSeedsAndCopy(Map<Key<?>, Object> seededObjects) {
        for (Map.Entry<Key<?>, Object> entry : seededObjects.entrySet()) {
            Key<?> key = entry.getKey();
            Object value = entry.getValue();
            if (value == null) {
                throw new IllegalArgumentException("Seeded objects contains null key. Key: " + key);
            }
            Class<?> rawType = key.getTypeLiteral().getRawType();
            if (!rawType.isInstance(value)) {
                if (!(value instanceof Key)) {
                    throw new IllegalArgumentException("Seeded object is not instance of key type. " +
                            "Key: " + key + ". Value: " + value);
                }
                Class<?> valueRawType = ((Key) value).getTypeLiteral().getRawType();
                if (!rawType.isAssignableFrom(valueRawType)) {
                    throw new IllegalArgumentException("Chained value key type does not extend the key type. " +
                            "Key Key: " + key + ". Value Key: " + value);
                }
            }
        }

        return seededObjects.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Creates and returns a processor context that can be used to re-enter a processor scope multiple times.
     *
     * @param seededObjects the original objects to seed into the context.
     * @return a context object that can be used to run code in a processor scope.
     */
    public static Context createProcessorContext(Map<Key<?>, Object> seededObjects) {
        return new Context(PROCESSOR_CONTEXT, checkForInvalidSeedsAndCopy(seededObjects));
    }

    /**
     * Creates and uses a document scope to run a callable.
     *
     * @param <T>           the return type of the callable to run.
     * @param seededObjects the original objects to seed into the document context.
     * @param callable      a {@link Callable} to run.
     * @return the result of the callable.
     * @throws Exception any exception throw while executing the callable
     */
    public static <T> T runInDocumentScope(Map<Key<?>, Object> seededObjects, Callable<T> callable) throws Exception {
        Context documentContext = new Context(DOCUMENT_CONTEXT, checkForInvalidSeedsAndCopy(seededObjects));
        return documentContext.call(callable);
    }

    /**
     * A provider which represents an object that will be provided by seeding into the context via the map arguments on
     * {@link #createProcessorContext(Map)} or {@link #runInDocumentScope(Map, Callable)}. Modules can bind objects to
     * this provider in order to satisfy injector dependencies prior to the creation of the scopes. The binding can only
     * be injected inside a scope, and any provision request outside of a {@link ContextScope} will cause an
     * {@link IllegalStateException}.
     *
     * @param <T> the provided type.
     * @return a provider
     */
    public static <T> Provider<T> providedViaSeeding() {
        return () -> {
            throw new IllegalStateException("This provider should not be called, " +
                    "the object should be seeded when entering a scope");
        };
    }

    /**
     * A context object, stores a ref to the context {@link ThreadLocal} and the map of objects to provide within the
     * scope.<br/>
     * <br/>
     * {@link #call(Callable)} causes the current thread to enter the scope so that objects from the map are provided
     * when scoped objects are needed.
     */
    public static final class Context {
        private final ThreadLocal<Context> contextRef;
        private final Map<Key<?>, Object> objectsMap;

        private Context(ThreadLocal<Context> contextRef, Map<Key<?>, Object> objectsMap) {
            this.contextRef = contextRef;
            this.objectsMap = objectsMap;
        }

        /**
         * Runs the callable inside of a context, providing objects from that context for injected types within the
         * context.
         *
         * @param callable the {@link Callable} to run.
         * @param <T>      the return type of the callable.
         * @return the value of T returned by the callable.
         * @throws Exception any exception thrown by the callable.
         */
        public synchronized <T> T call(Callable<T> callable) throws Exception {
            if (contextRef.get() != null) {
                throw new IllegalStateException("Processor scope already in progress");
            }
            contextRef.set(this);
            try {
                return callable.call();
            } finally {
                contextRef.remove();
            }
        }
    }

    /**
     * The context scope. Provides scoped objects from the {@link Context} object for the current thread.
     */
    private static class ContextScope implements Scope {
        private final ThreadLocal<Context> contextRef;

        private ContextScope(ThreadLocal<Context> contextRef) {
            this.contextRef = contextRef;
        }

        @Override
        public <T> Provider<T> scope(Key<T> key, Provider<T> unscoped) {
            return () -> {
                Context context = contextRef.get();
                if (null != context) {
                    @SuppressWarnings("unchecked")
                    T t = (T) context.objectsMap.get(key);

                    if (t == null) {
                        t = unscoped.get();
                        if (!Scopes.isCircularProxy(t)) {
                            context.objectsMap.put(key, t);
                        }
                    }
                    return t;
                } else {
                    throw new OutOfScopeException("Not currently in a document scope");
                }
            };
        }
    }
}
