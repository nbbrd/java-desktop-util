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

import ec.util.desktop.Desktop.Factory;
import java.util.AbstractMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 *
 * @author Philippe Charles
 */
public enum DesktopFactoryProc implements UnaryOperator<Stream<Factory>> {

    INSTANCE;

    @Override
    public Stream<Factory> apply(Stream<Factory> t) {
        String osArch = System.getProperty("os.arch");
        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");

        return t
                .map(o -> new AbstractMap.SimpleEntry<>(o.getSupportType(osArch, osName, osVersion), o))
                .filter(DesktopFactoryProc::isSupported)
                .sorted(DesktopFactoryProc::compareByLevelOfSupport)
                .map(Map.Entry::getValue);
    }

    private static boolean isSupported(Map.Entry<Factory.SupportType, Factory> entry) {
        return !entry.getKey().equals(Factory.SupportType.NONE);
    }

    /**
     * The comparator used to select the best factory through their level of
     * support.
     */
    private static int compareByLevelOfSupport(Map.Entry<Factory.SupportType, Factory> l, Map.Entry<Factory.SupportType, Factory> r) {
        return r.getKey().compareTo(l.getKey());
    }
}
