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
package ec.util.desktop.impl;

import java.io.IOException;
import java.util.SortedMap;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Facade that allows retrieving values from the registry of Windows.
 *
 * @author Philippe Charles
 */
@lombok.extern.java.Log
public abstract class WinRegistry {

    public static enum Root {

        HKEY_LOCAL_MACHINE, HKEY_CURRENT_USER
    }

    abstract public boolean keyExists(@NonNull Root root, @NonNull String key) throws IOException;

    @Nullable
    abstract public Object getValue(@NonNull Root root, @NonNull String key, @NonNull String name) throws IOException;

    @NonNull
    abstract public SortedMap<String, Object> getValues(@NonNull Root root, @NonNull String key) throws IOException;

    @NonNull
    public static WinRegistry noOp() {
        return NoOpRegistry.INSTANCE;
    }

    @NonNull
    public static WinRegistry failing() {
        return FailingRegistry.INSTANCE;
    }

    @NonNull
    public static WinRegistry getDefault() {
        return LazyHolder.INSTANCE;
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation">
    /**
     * http://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom
     */
    private static final class LazyHolder {

        private static final WinRegistry INSTANCE = createInstance();

        private static WinRegistry createInstance() {
            // fallback
            log.log(Level.INFO, "Using RegRegistry");
            return new RegRegistry();
        }
    }

    private static final class NoOpRegistry extends WinRegistry {

        public static final NoOpRegistry INSTANCE = new NoOpRegistry();

        @Override
        public boolean keyExists(Root root, String key) throws IOException {
            return false;
        }

        @Override
        public Object getValue(Root root, String key, String name) throws IOException {
            return null;
        }

        @Override
        public SortedMap<String, Object> getValues(Root root, String key) throws IOException {
            return Util.EMPTY_SORTED_MAP;
        }
    }

    private static final class FailingRegistry extends WinRegistry {

        public static final FailingRegistry INSTANCE = new FailingRegistry();

        @Override
        public boolean keyExists(Root root, String key) throws IOException {
            throw new IOException();
        }

        @Override
        public Object getValue(Root root, String key, String name) throws IOException {
            throw new IOException();
        }

        @Override
        public SortedMap<String, Object> getValues(Root root, String key) throws IOException {
            throw new IOException();
        }
    }
    //</editor-fold>
}
