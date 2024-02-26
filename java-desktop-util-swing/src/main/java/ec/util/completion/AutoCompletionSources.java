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

import ec.util.completion.AutoCompletionSource.Behavior;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.NonNull;

public final class AutoCompletionSources {

    private AutoCompletionSources() {
        // static class
    }

    private static final AutoCompletionSource NONE = ExtAutoCompletionSource.builder(Collections::emptyList).behavior(Behavior.NONE).build();

    @NonNull
    public static AutoCompletionSource empty() {
        return NONE;
    }

    @NonNull
    public static <T> AutoCompletionSource of(boolean strict, @NonNull T... list) {
        return of(strict, Arrays.asList(list));
    }

    @NonNull
    public static <T> AutoCompletionSource of(boolean strict, @NonNull Iterable<T> list) {
        UnaryOperator<String> normalizer = strict ? UnaryOperator.identity() : AutoCompletionSources::normalize;
        return ExtAutoCompletionSource
                .builder(term -> {
                    String normalizedTerm = normalizer.apply(term);
                    return StreamSupport.stream(list.spliterator(), false)
                            .filter(o -> o != null && normalizer.apply(o.toString()).contains(normalizedTerm))
                            .sorted(Comparator.comparing(Object::toString))
                            .collect(Collectors.toList());
                })
                .behavior(Behavior.SYNC)
                .valueToString(Object::toString)
                .build();
    }

    /**
     * @see
     * http://www.drillio.com/en/software-development/java/removing-accents-diacritics-in-any-language/
     * @param input
     * @return
     */
    @NonNull
    public static String removeDiacritics(@NonNull String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    /**
     * Normalize a string by removing its diacritics and converting the result
     * to lowercase.
     *
     * @param input
     * @return
     */
    @NonNull
    public static String normalize(@NonNull String input) {
        return removeDiacritics(input).toLowerCase(Locale.ROOT);
    }
}
