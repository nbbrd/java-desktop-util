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
package ec.util.chart;

import org.checkerframework.checker.nullness.qual.NonNull;


/**
 *
 * @author Philippe Charles
 */
public abstract class SeriesPredicate {

    public abstract boolean apply(int series);

    @NonNull
    public SeriesFunction<Boolean> asFunction() {
        return new AsFunction(this);
    }

    @NonNull
    public static SeriesPredicate alwaysTrue() {
        return TRUE;
    }

    @NonNull
    public static SeriesPredicate alwaysFalse() {
        return FALSE;
    }

    //<editor-fold defaultstate="collapsed" desc="Internal implementation">
    private static final SeriesPredicate TRUE = new SeriesPredicate() {
        @Override
        public boolean apply(int series) {
            return true;
        }
    };

    private static final SeriesPredicate FALSE = new SeriesPredicate() {
        @Override
        public boolean apply(int series) {
            return false;
        }
    };

    private static final class AsFunction extends SeriesFunction<Boolean> {

        private final SeriesPredicate predicate;

        public AsFunction(SeriesPredicate predicate) {
            this.predicate = predicate;
        }

        @Override
        public Boolean apply(int series) {
            return predicate.apply(series);
        }
    }
    //</editor-fold>
}
