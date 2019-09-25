/*
 * Copyright 2015 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package ec.util.completion;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 * @since 2.1.0
 */
public abstract class ExtAutoCompletionSource implements AutoCompletionSource {

    /**
     *
     * @param term
     * @return
     * @since 2.1.0
     */
    @NonNull
    abstract public Request getRequest(@NonNull String term);

    /**
     * @since 2.1.0
     */
    public static abstract class Request implements Callable<List<?>> {

        @NonNull
        abstract public String getTerm();

        @NonNull
        abstract public Behavior getBehavior();
    }

    /**
     *
     * @param source
     * @param term
     * @return
     * @since 2.1.0
     */
    @NonNull
    public static Request wrap(@NonNull AutoCompletionSource source, @NonNull String term) {
        Objects.requireNonNull(source);
        return new BasicRequest(term, () -> source.getBehavior(term), () -> source.getValues(term));
    }

    /**
     *
     * @param <T>
     * @param supplier
     * @return
     * @since 2.2.0
     */
    @NonNull
    public static <T> Builder<T> builder(@NonNull Callable<List<T>> supplier) {
        return builder(o -> supplier.call());
    }

    /**
     *
     * @param <T>
     * @param loader
     * @return
     * @since 2.2.0
     */
    @NonNull
    public static <T> Builder<T> builder(@NonNull Loader<T> loader) {
        return new BuilderImpl<>(loader);
    }

    /**
     * @param <T>
     * @since 2.2.0
     */
    public interface Builder<T> {

        @NonNull
        Builder<T> postProcessor(@NonNull BiFunction<List<T>, String, List<T>> processor);

        @NonNull
        Builder<T> behavior(@NonNull Function<? super String, Behavior> behavior);

        @NonNull
        default Builder<T> behavior(@NonNull Behavior behavior) {
            Objects.requireNonNull(behavior);
            return behavior(o -> behavior);
        }

        @NonNull
        Builder<T> valueToString(@NonNull Function<T, String> toString);

        @NonNull
        Builder<T> cache(
                @NonNull ConcurrentMap cache,
                @NonNull Function<? super String, Object> toKey,
                @NonNull Function<? super String, Behavior> behavior);

        @NonNull
        default Builder<T> cache(
                @NonNull ConcurrentMap cache,
                @NonNull Function<? super String, Object> toKey,
                @NonNull Behavior behavior) {
            Objects.requireNonNull(behavior);
            return cache(cache, toKey, o -> behavior);
        }

        @NonNull
        ExtAutoCompletionSource build();
    }

    /**
     * @param <T>
     * @since 2.2.0
     */
    public interface Loader<T> {

        @NonNull
        List<T> load(@NonNull String term) throws Exception;
    }

    /**
     * @param term
     * @return
     * @since 2.2.0
     */
    @NonNull
    public static Predicate<String> basicFilter(@NonNull String term) {
        String normalizedTerm = AutoCompletionSources.normalize(term);
        return value -> value != null && !value.isEmpty() && AutoCompletionSources.normalize(value).contains(normalizedTerm);
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    private static final class BasicRequest extends Request {

        private final String term;
        private final Supplier<Behavior> behavior;
        private final Callable<List<?>> callable;

        public BasicRequest(@NonNull String term, @NonNull Supplier<Behavior> behavior, @NonNull Callable<List<?>> callable) {
            this.term = Objects.requireNonNull(term);
            this.behavior = Objects.requireNonNull(behavior);
            this.callable = Objects.requireNonNull(callable);
        }

        @Override
        public String getTerm() {
            return term;
        }

        @Override
        public Behavior getBehavior() {
            return behavior.get();
        }

        @Override
        public List<?> call() throws Exception {
            return callable.call();
        }
    }

    private static final class BuilderImpl<T> implements Builder<T> {

        private final Loader<T> loader;
        private BiFunction<List<T>, String, List<T>> processor;
        private Function<? super String, Behavior> behavior;
        private Function<T, String> toString;
        private ConcurrentMap cache;
        private Function<? super String, Object> toKey;
        private Function<? super String, Behavior> cacheBehavior;

        public BuilderImpl(Loader<T> loader) {
            this.loader = loader;
            this.processor = (values, term) -> values;
            this.behavior = o -> Behavior.ASYNC;
            this.toString = Object::toString;
            this.cache = null;
            this.toKey = null;
            this.cacheBehavior = null;
        }

        @Override
        public Builder<T> postProcessor(BiFunction<List<T>, String, List<T>> processor) {
            this.processor = Objects.requireNonNull(processor);
            return this;
        }

        @Override
        public Builder<T> behavior(Function<? super String, Behavior> behavior) {
            this.behavior = Objects.requireNonNull(behavior);
            return this;
        }

        @Override
        public Builder<T> valueToString(Function<T, String> toString) {
            this.toString = Objects.requireNonNull(toString);
            return this;
        }

        @Override
        public Builder<T> cache(
                ConcurrentMap cache,
                Function<? super String, Object> toKey,
                Function<? super String, Behavior> behavior) {
            this.cache = Objects.requireNonNull(cache);
            this.toKey = Objects.requireNonNull(toKey);
            this.cacheBehavior = Objects.requireNonNull(behavior);
            return this;
        }

        @Override
        public ExtAutoCompletionSource build() {
            return cache != null
                    ? new CachedExtAutoCompletionSource<>(loader, processor, behavior, toString, cache, toKey, cacheBehavior)
                    : new DefaultExtAutoCompletionSource<>(loader, processor, behavior, toString);
        }
    }

    private static final class DefaultExtAutoCompletionSource<T> extends ExtAutoCompletionSource {

        private final Loader<T> loader;
        private final BiFunction<List<T>, String, List<T>> processor;
        private final Function<? super String, Behavior> behavior;
        private final Function<T, String> toString;

        public DefaultExtAutoCompletionSource(
                Loader<T> loader,
                BiFunction<List<T>, String, List<T>> consumer,
                Function<? super String, Behavior> behavior,
                Function<T, String> toString) {
            this.loader = loader;
            this.processor = consumer;
            this.behavior = behavior;
            this.toString = toString;
        }

        @Override
        public Request getRequest(String term) {
            return new BasicRequest(term, () -> behavior.apply(term), () -> processor.apply(loader.load(term), term));
        }

        @Override
        public Behavior getBehavior(String term) {
            return behavior.apply(term);
        }

        @Override
        public String toString(Object value) {
            return toString.apply((T) value);
        }

        @Override
        public List<?> getValues(String term) throws Exception {
            return processor.apply(loader.load(term), term);
        }
    }

    private static final class CachedExtAutoCompletionSource<T> extends ExtAutoCompletionSource {

        private final Loader<T> loader;
        private final BiFunction<List<T>, String, List<T>> processor;
        private final Function<? super String, Behavior> behavior;
        private final Function<T, String> toString;
        private final ConcurrentMap cache;
        private final Function<? super String, Object> toKey;
        private final Function<? super String, Behavior> cacheBehavior;

        public CachedExtAutoCompletionSource(Loader<T> loader, BiFunction<List<T>, String, List<T>> consumer,
                Function<? super String, Behavior> behavior, Function<T, String> toString,
                ConcurrentMap cache, Function<? super String, Object> toKey, Function<? super String, Behavior> cacheBehavior) {
            this.loader = loader;
            this.processor = consumer;
            this.behavior = behavior;
            this.toString = toString;
            this.cache = cache;
            this.toKey = toKey;
            this.cacheBehavior = cacheBehavior;
        }

        @Override
        public Request getRequest(String term) {
            Object key = toKey.apply(term);
            List<T> values = (List<T>) cache.get(key);
            if (values == null) {
                return new BasicRequest(term, () -> behavior.apply(term), () -> {
                    List<T> data = loader.load(term);
                    cache.put(key, data);
                    return processor.apply(data, term);
                });
            } else {
                return new BasicRequest(term, () -> cacheBehavior.apply(term), () -> processor.apply(values, term));
            }
        }

        @Override
        public Behavior getBehavior(String term) {
            return behavior.apply(term);
        }

        @Override
        public String toString(Object value) {
            return toString.apply((T) value);
        }

        @Override
        public List<?> getValues(String term) throws Exception {
            Object key = toKey.apply(term);
            List<T> values = (List<T>) cache.get(key);
            if (values == null) {
                values = loader.load(term);
                cache.put(key, values);
            }
            return processor.apply(values, term);
        }
    }
    //</editor-fold>
}
