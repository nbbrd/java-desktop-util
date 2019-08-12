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
package ec.util.desktop.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * http://en.wikipedia.org/wiki/Windows_Search
 * https://docs.microsoft.com/en-us/windows/desktop/search/windows-search
 *
 * @author Philippe Charles
 */
@lombok.extern.java.Log
abstract class WinSearch {

    @NonNull
    public File[] search(@NonNull String query) throws IOException {
        List<File> result = getFilesByName(query);
        return result.toArray(new File[result.size()]);
    }

    @NonNull
    abstract public List<File> getFilesByName(@NonNull String query) throws IOException;

    @NonNull
    public static WinSearch noOp() {
        return NoOpSearch.INSTANCE;
    }

    @NonNull
    public static WinSearch getDefault() {
        return LazyHolder.INSTANCE;
    }

    @NonNull
    static WinSearch failing() {
        return FailingSearch.INSTANCE;
    }

    //<editor-fold defaultstate="collapsed" desc="Implementation details">
    /**
     * http://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom
     */
    private static final class LazyHolder {

        private static final WinSearch INSTANCE = createInstance();

        private static WinSearch createInstance() {
            if (Util.is64bit() && Util.isClassAvailable("com.sun.jna.platform.win32.COM.util.Factory")) {
                log.log(Level.INFO, "Using JnaSearch");
                return new JnaSearch();
            }
            File searchScript = extractSearchScript();
            if (searchScript != null) {
                log.log(Level.INFO, "Using VbsSearch");
                return new VbsSearch(WinScriptHost.getDefault(), searchScript);
            }
            // fallback
            log.log(Level.INFO, "Using NoOpSearch");
            return noOp();
        }

        @Nullable
        private static File extractSearchScript() {
            try {
                return Util.extractResource("winsearch.vbs", "winsearch", ".vbs");
            } catch (IOException ex) {
                log.log(Level.INFO, "Cannot load search script", ex);
                return null;
            }
        }
    }

    private static final class NoOpSearch extends WinSearch {

        private static final WinSearch INSTANCE = new NoOpSearch();

        @Override
        public List<File> getFilesByName(String query) throws IOException {
            return Collections.emptyList();
        }
    }

    private static final class FailingSearch extends WinSearch {

        public static final FailingSearch INSTANCE = new FailingSearch();

        @Override
        public List<File> getFilesByName(String query) throws IOException {
            throw new IOException();
        }
    }

    static final class VbsSearch extends WinSearch {

        private static final String QUOTE = "\"";

        private final WinScriptHost wsh;
        private final File searchScript;

        public VbsSearch(@NonNull WinScriptHost wsh, @NonNull File searchScript) {
            this.wsh = wsh;
            this.searchScript = searchScript;
        }

        @Override
        public List<File> getFilesByName(String query) throws IOException {
            String quotedQuery = quote(query.replace(QUOTE, ""));
            Process p = wsh.exec(searchScript, quotedQuery);
            return Util.toList(p, Charset.defaultCharset(), File::new);
        }

        @NonNull
        private static String quote(@NonNull String input) {
            return QUOTE + input + QUOTE;
        }
    }
    //</editor-fold>
}
