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
package _demo;

import java.awt.Color;
import java.awt.Font;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.IkonHandler;
import org.kordamp.ikonli.swing.IkonResolver;

/**
 *
 * @author Philippe Charles
 */
@lombok.experimental.UtilityClass
class Ikons {

    public internal.FontIcon of(Ikon icon, float size) {
        return of(icon, size, null);
    }

    public internal.FontIcon of(Ikon icon, float size, Color color) {
        IkonHandler handler = IkonResolver.getInstance().resolve(icon.getDescription());
        Font font = ((Font) handler.getFont()).deriveFont(size);
        return internal.FontIcon.of(icon.getCode(), font, color, 0);
    }
}
