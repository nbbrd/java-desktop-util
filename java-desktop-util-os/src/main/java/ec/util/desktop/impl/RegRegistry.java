/*
 * Copyright 2020 National Bank of Belgium
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

import ec.util.desktop.impl.InternalCopyOfRegWrapper.RegValue;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 *
 * @author Philippe Charles
 */
final class RegRegistry extends WinRegistry {

    private static final String KEY_SEPARATOR = "\\";

    private List<InternalCopyOfRegWrapper.RegValue> getValuesOrNull(WinRegistry.Root root, String key) throws IOException {
        Objects.requireNonNull(root);
        Objects.requireNonNull(key);
        String keyName = root.name() + KEY_SEPARATOR + key;
        return InternalCopyOfRegWrapper.query(keyName, false).get(keyName);
    }

    @Override
    public boolean keyExists(WinRegistry.Root root, String key) throws IOException {
        List<InternalCopyOfRegWrapper.RegValue> data = getValuesOrNull(root, key);
        return data != null;
    }

    @Override
    public Object getValue(WinRegistry.Root root, String key, String name) throws IOException {
        List<InternalCopyOfRegWrapper.RegValue> data = getValuesOrNull(root, key);
        Objects.requireNonNull(name);
        return data != null
                ? data
                        .stream()
                        .filter(regValue -> regValue.getName().equals(name))
                        .map(regValue -> regValue.getValue())
                        .findFirst()
                        .orElse(null)
                : null;
    }

    @Override
    public SortedMap<String, Object> getValues(WinRegistry.Root root, String key) throws IOException {
        List<InternalCopyOfRegWrapper.RegValue> data = getValuesOrNull(root, key);
        return data != null
                ? data.stream().collect(Collectors.toMap(RegValue::getName, regValue -> (Object) regValue.getValue(), (l, r) -> l, TreeMap::new))
                : Collections.emptySortedMap();
    }
}
