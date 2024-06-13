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

import lombok.AccessLevel;
import lombok.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

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
        return result.toArray(new File[0]);
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
            try {
                PowerShellSearch result = PowerShellSearch.init();
                log.log(Level.INFO, "Using PowerShellSearch");
                return result;
            } catch (IOException ex) {
                log.log(Level.INFO, "Cannot load PowerShellSearch", ex);
            }

            try {
                VbsSearch result = VbsSearch.init();
                log.log(Level.INFO, "Using VbsSearch");
                return result;
            } catch (IOException ex) {
                log.log(Level.INFO, "Cannot load VbsSearch", ex);
            }

            // fallback
            log.log(Level.INFO, "Using NoOpSearch");
            return noOp();
        }
    }

    private static final class NoOpSearch extends WinSearch {

        private static final WinSearch INSTANCE = new NoOpSearch();

        @Override
        public @NonNull List<File> getFilesByName(@NonNull String query) throws IOException {
            return Collections.emptyList();
        }
    }

    private static final class FailingSearch extends WinSearch {

        public static final FailingSearch INSTANCE = new FailingSearch();

        @Override
        public @NonNull List<File> getFilesByName(@NonNull String query) throws IOException {
            throw new IOException();
        }
    }

    @lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
    static final class VbsSearch extends WinSearch {

        public static @NonNull VbsSearch init() throws IOException {
            return new VbsSearch(
                    WinScriptHost.getDefault(), Util.extractResource("winsearch.vbs", "winsearch", ".vbs")
            );
        }

        private static final String QUOTE = "\"";

        private final @NonNull WinScriptHost wsh;
        private final @NonNull File searchScript;

        @Override
        public @NonNull List<File> getFilesByName(@NonNull String query) throws IOException {
            String quotedQuery = quote(query.replace(QUOTE, ""));
            Process p = wsh.exec(searchScript, quotedQuery);
            return Util.toList(p, Charset.defaultCharset(), File::new);
        }

        @NonNull
        private static String quote(@NonNull String input) {
            return QUOTE + input + QUOTE;
        }
    }

    @lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
    static final class PowerShellSearch extends WinSearch {

        public static @NonNull PowerShellSearch init() throws IOException {
            return new PowerShellSearch(
                    Util.extractResource("winsearch.ps1", "winsearch", ".ps1")
            );
        }

        private static final String QUOTE = "\"";

        private final @NonNull File searchScript;

        @Override
        public @NonNull List<File> getFilesByName(@NonNull String query) throws IOException {
            String quotedQuery = quote(query.replace(QUOTE, ""));
            Process p = new ProcessBuilder("powershell", "-file", searchScript.getAbsolutePath(), quotedQuery)
                    .redirectError(ProcessBuilder.Redirect.INHERIT)
                    .start();
            return Util.toList(p, Charset.defaultCharset(), File::new);
        }

        @NonNull
        private static String quote(@NonNull String input) {
            return QUOTE + input + QUOTE;
        }
    }
    //</editor-fold>
}
