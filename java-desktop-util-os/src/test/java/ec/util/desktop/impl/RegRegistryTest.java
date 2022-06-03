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

import java.io.IOException;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Philippe Charles
 */
public class RegRegistryTest {

    @Test
    public void testKeyExists() throws IOException {
        WinRegistryTest.testKeyExists(new RegRegistry());
    }

    @Test
    public void testGetValue() throws IOException {
        WinRegistryTest.testGetValue(new RegRegistry());
    }

    @Test
    public void testGetValues() throws IOException {
        WinRegistryTest.testGetValues(new RegRegistry());
    }
}
