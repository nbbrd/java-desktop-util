/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
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

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.checkerframework.checker.index.qual.NonNegative;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An implementation of AutoCompletionSource that allows to quickly construct a
 * source by overriding a few methods.
 *
 * @author Philippe Charles
 * @param <T>
 * @Deprecated use {@link ec.util.completion.ExtAutoCompletionSource} instead
 */
@Deprecated
public abstract class AbstractAutoCompletionSource<T> extends ExtAutoCompletionSource implements Comparator<T> {

    //<editor-fold defaultstate="collapsed" desc="AutoCompletionSource">
    @Override
    public final @NonNull String toString(@NonNull Object value) {
        return getValueAsString((T) value);
    }

    @Override
    public @NonNull Behavior getBehavior(@NonNull String term) {
        return Behavior.SYNC;
    }

    @Override
    public @NonNull List<?> getValues(@NonNull String term) throws Exception {
        return getValues(term, getAllValues());
    }

    @Override
    public @NonNull Request getRequest(@NonNull String term) {
        return wrap(this, term);
    }
    //</editor-fold>

    /**
     * Returns a view on all possible values. This view will be filtered and
     * sorted later on.
     *
     * @return
     * @throws Exception
     */
    @NonNull
    abstract protected Iterable<T> getAllValues() throws Exception;

    /**
     * Format a value as a string.<br>Default behavior uses
     * {@link Object#toString()}.
     *
     * @param value the value to be formatted
     * @return
     */
    @NonNull
    protected String getValueAsString(@NonNull T value) {
        return value.toString();
    }

    /**
     * Returns a normalized string used by filtering criteria.<br>Default
     * behavior uses {@link AutoCompletionSources#normalize(java.lang.String)}.
     *
     * @param input the string to be normalized
     * @return a normalized string
     */
    @NonNull
    protected String getNormalizedString(@NonNull String input) {
        return AutoCompletionSources.normalize(input);
    }

    /**
     * Checks if a normalized input matches a normalized term.<br>Default
     * behavior uses {@link String#contains(java.lang.CharSequence)}.
     *
     * @param normalizedTerm
     * @param normalizedInput
     * @return true if the input matches the term
     */
    protected boolean matches(@NonNull String normalizedTerm, @NonNull String normalizedInput) {
        return normalizedInput.contains(normalizedTerm);
    }

    /**
     * Checks if an input matches a term matcher.<br>Default behavior uses
     * {@link #getValueAsString(java.lang.Object)}.
     *
     * @param termMatcher
     * @param input
     * @return true if the input matches the term matcher
     */
    protected boolean matches(@NonNull TermMatcher termMatcher, @NonNull T input) {
        return termMatcher.matches(getValueAsString(input));
    }

    /**
     * Returns the size used to limit the number of values provided by this
     * source.<br>Default behavior uses {@link Integer#MAX_VALUE}.
     *
     * @return
     */
    @NonNegative
    protected int getLimitSize() {
        return Integer.MAX_VALUE;
    }

    /**
     * Compares two values in order to sort them.<br>Default behavior uses
     * {@link #getValueAsString(java.lang.Object)}.
     *
     * @param left
     * @param right
     * @return
     */
    @Override
    public int compare(T left, T right) {
        return getValueAsString(left).compareTo(getValueAsString(right));
    }

    @NonNull
    protected List<?> getValues(@NonNull String term, @NonNull Iterable<T> allValues) {
        TermMatcher termFilter = createTermMatcher(term);
        return StreamSupport.stream(allValues.spliterator(), false)
                .filter(o -> matches(termFilter, o))
                .limit(getLimitSize())
                .sorted(this)
                .collect(Collectors.toList());
    }

    @NonNull
    protected Request createCachedRequest(@NonNull final String term, @NonNull final Iterable<T> allValues) {
        return new Request() {
            @Override
            public @NonNull String getTerm() {
                return term;
            }

            @Override
            public @NonNull Behavior getBehavior() {
                return Behavior.SYNC;
            }

            @Override
            public List<?> call() throws Exception {
                return getValues(term, allValues);
            }
        };
    }

    @NonNull
    protected TermMatcher createTermMatcher(@NonNull final String term) {
        final String normalizedTerm = getNormalizedString(term);
        return o -> o != null && matches(normalizedTerm, getNormalizedString(o));
    }

    @FunctionalInterface
    public interface TermMatcher {

        boolean matches(@Nullable String input);
    }
}
