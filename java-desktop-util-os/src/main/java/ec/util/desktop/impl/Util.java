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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 *
 * @author Philippe Charles
 */
final class Util {

    private Util() {
        // static class
    }

    public static boolean isClassAvailable(@NonNull String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    public static boolean is64bit() {
        return "amd64".equals(System.getProperty("os.arch"));
    }

    @Nullable
    public static File fileFromPathname(@Nullable String pathname) {
        return pathname != null && !pathname.isEmpty() ? new File(pathname) : null;
    }

    /**
     * Checks if the file is a valid file and readable.
     *
     * @param file the file to check
     * @return the validated file
     * @throws NullPointerException if file is null
     * @throws IllegalArgumentException if file doesn't exist
     * @throws SecurityException If a security manager exists and its
     * {@link SecurityManager#checkRead(java.lang.String)} method denies read
     * access to the file
     */
    @NonNull
    public static File checkFileValidation(@NonNull File file) throws NullPointerException, IllegalArgumentException, SecurityException {
        Objects.requireNonNull(file, "File must not be null");
        if (!file.exists()) {
            throw new IllegalArgumentException("The file: " + file.getPath() + " doesn't exist.");
        }
        file.canRead();
        return file;
    }

    @NonNull
    public static File extractResource(@NonNull String resourceName, @NonNull String filePrefix, @NonNull String fileSuffix) throws IOException {
        File result = File.createTempFile(filePrefix, fileSuffix);
        result.deleteOnExit();
        try (InputStream in = AwtDesktop.class.getResourceAsStream(resourceName)) {
            Files.copy(in, result.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return result;
    }

    @NonNull
    public static File[] toFiles(@NonNull Process p, @NonNull Charset charset) throws IOException {
        List<File> result = toList(p, charset, File::new);
        return result.toArray(new File[result.size()]);
    }

    @NonNull
    public static <T> List<T> toList(@NonNull Process p, @NonNull Charset charset, @NonNull Function<String, T> func) throws IOException {
        List<T> result = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), charset))) {
            // we need the process to end, else we'll get an illegal Thread State Exception
            String line;
            while ((line = reader.readLine()) != null) {
                result.add(func.apply(line));
            }
            try {
                p.waitFor();
            } catch (InterruptedException ex) {
                // do nothing?
            }
        }

        return result;
    }

    public static final SortedMap<String, Object> EMPTY_SORTED_MAP = Collections.unmodifiableSortedMap(new TreeMap<String, Object>());
}
