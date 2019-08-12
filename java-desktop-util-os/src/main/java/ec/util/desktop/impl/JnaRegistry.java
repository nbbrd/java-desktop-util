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

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;
import java.io.IOException;
import java.util.SortedMap;

/**
 *
 * @author Philippe Charles
 */
final class JnaRegistry extends WinRegistry {

    @Override
    public boolean keyExists(Root root, String key) throws IOException {
        try {
            return Advapi32Util.registryKeyExists(convert(root), key);
        } catch (Win32Exception | UnsatisfiedLinkError ex) {
            throw new IOException("While checking key existence", ex);
        }
    }

    @Override
    public Object getValue(Root root, String key, String name) throws IOException {
        try {
            WinReg.HKEY hkey = convert(root);
            return Advapi32Util.registryValueExists(hkey, key, name) ? Advapi32Util.registryGetValue(hkey, key, name) : null;
        } catch (Win32Exception | UnsatisfiedLinkError ex) {
            throw new IOException("While getting string value", ex);
        }
    }

    @Override
    public SortedMap<String, Object> getValues(Root root, String key) throws IOException {
        try {
            WinReg.HKEY hkey = convert(root);
            return Advapi32Util.registryKeyExists(hkey, key) ? Advapi32Util.registryGetValues(hkey, key) : Util.EMPTY_SORTED_MAP;
        } catch (Win32Exception | UnsatisfiedLinkError ex) {
            throw new IOException("While getting values", ex);
        }
    }

    private WinReg.HKEY convert(Root root) {
        switch (root) {
            case HKEY_CURRENT_USER:
                return WinReg.HKEY_CURRENT_USER;
            case HKEY_LOCAL_MACHINE:
                return WinReg.HKEY_LOCAL_MACHINE;
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
