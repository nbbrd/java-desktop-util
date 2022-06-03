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

import lombok.NonNull;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FileAutoCompletionSource implements AutoCompletionSource {

    protected final boolean strict;
    protected final FileFilter fileFilter;
    protected final File[] paths;

    public FileAutoCompletionSource() {
        this(false, null, new File[0]);
    }

    public FileAutoCompletionSource(boolean strict, FileFilter fileFilter, File[] paths) {
        this.strict = strict;
        this.fileFilter = fileFilter;
        this.paths = paths;
    }

    @Override
    public @NonNull Behavior getBehavior(@NonNull String term) {
        return Behavior.ASYNC;
    }

    @Override
    public @NonNull String toString(@NonNull Object value) {
        return ((File) value).getPath();
    }

    @Override
    public @NonNull List<File> getValues(final @NonNull String term) throws IOException {
        // case 1: absolute path
        {
            File file = new File(term);
            // is a directory -> get all children
            if (file.exists() && file.isDirectory()) {
                return children(file, fileFilter);
            }
            File parent = file.getParentFile();
            // is not a directory but has parent -> get siblings
            if (parent != null && parent.exists()) {
                return children(parent, normalizedFilter(term));
            }
        }
        // case 2: relative path
        {
            for (File path : paths) {
                File file = new File(path, term);
                // is a directory -> get all children
                if (file.exists() && file.isDirectory()) {
                    return toRelativeFiles(children(file, fileFilter), path);
                }
                File parent = file.getParentFile();
                // is not a directory but has parent -> get siblings
                if (parent != null && parent.exists()) {
                    return toRelativeFiles(children(parent, normalizedFilter(file.getAbsolutePath())), path);
                }
            }
        }
        return Collections.emptyList();
    }

    List<File> toRelativeFiles(List<File> files, File path) {
        files.replaceAll(file -> new File(file.getPath().substring(path.getPath().length() + 1)));
        return files;
    }

    FileFilter normalizedFilter(String term) {
        final String normalizedTerm = getNormalizedString(term);
        return (File o) -> (fileFilter == null || fileFilter.accept(o)) && getNormalizedString(o.getPath()).startsWith(normalizedTerm);
    }

    String getNormalizedString(String input) {
        return strict ? input : AutoCompletionSources.normalize(input);
    }

    static List<File> children(File folder, FileFilter fileFilter) {
        File[] result = folder.listFiles(fileFilter);
        // result == null => An I/O exception occured
        return result != null ? Arrays.asList(result) : Collections.emptyList();
    }
}
