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
package internal;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Philippe Charles
 */
public class InternalUtilTest {

    @Test
    public void testGetLazyResource() {
        AtomicInteger factory = new AtomicInteger(0);

        Supplier<Integer> lazyResource = InternalUtil.getLazyResource(factory::incrementAndGet);
        assertThat(factory.get()).isEqualTo(0);

        assertThat(lazyResource.get()).isEqualTo(1);
        assertThat(factory.get()).isEqualTo(1);

        assertThat(lazyResource.get()).isEqualTo(1);
        assertThat(factory.get()).isEqualTo(1);
    }
}
