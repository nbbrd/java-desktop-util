/*
 * Copyright 2019 National Bank of Belgium
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
package ec.util.desktop.impl;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
@lombok.extern.java.Log
abstract class Trash {

    abstract public boolean hasTrash();

    abstract public void moveToTrash(@NonNull File... files) throws IOException;

    @NonNull
    public static Trash getDefault() {
        return LazyHolder.INSTANCE;
    }

    /**
     * http://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom
     */
    private static final class LazyHolder {

        private static final Trash INSTANCE = createInstance();

        private static Trash createInstance() {
            if (Util.isClassAvailable("com.sun.jna.platform.FileUtils")) {
                log.log(Level.INFO, "Using JnaTrash");
                return new JnaTrash();
            }
            // fallback
            log.log(Level.INFO, "Using NoOpTrash");
            return new NoOpTrash();
        }
    }

    private static final class NoOpTrash extends Trash {

        @Override
        public boolean hasTrash() {
            return false;
        }

        @Override
        public void moveToTrash(File... files) throws IOException {
            throw new UnsupportedOperationException();
        }
    }
}
