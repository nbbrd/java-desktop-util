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

import nbbrd.io.win.RegWrapper;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Philippe Charles
 */
final class RegRegistry extends WinRegistry {

    private static final String KEY_SEPARATOR = "\\";

    private List<RegWrapper.RegValue> getValuesOrEmpty(WinRegistry.Root root, String key) throws IOException {
        Objects.requireNonNull(root);
        Objects.requireNonNull(key);
        String keyName = root.name() + KEY_SEPARATOR + key;
        try {
            return RegWrapper.query(keyName, false).getOrDefault(keyName, Collections.emptyList());
        } catch (IOException ex) {
            if (ex.getMessage().contains("Invalid exit value: 1")) {
                return Collections.emptyList();
            }
            throw ex;
        }
    }

    @Override
    public boolean keyExists(WinRegistry.Root root, String key) throws IOException {
        return !getValuesOrEmpty(root, key).isEmpty();
    }

    @Override
    public Object getValue(WinRegistry.Root root, String key, String name) throws IOException {
        List<RegWrapper.RegValue> data = getValuesOrEmpty(root, key);
        Objects.requireNonNull(name);
        return data
                .stream()
                .filter(regValue -> regValue.getName().equals(name))
                .map(regValue -> regValue.getValue())
                .findFirst()
                .orElse(null);
    }

    @Override
    public SortedMap<String, Object> getValues(WinRegistry.Root root, String key) throws IOException {
        List<RegWrapper.RegValue> data = getValuesOrEmpty(root, key);
        return data.stream().collect(Collectors.toMap(RegWrapper.RegValue::getName, RegWrapper.RegValue::getValue, (l, r) -> l, TreeMap::new));
    }
}
