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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

/**
 *
 * @author Philippe Charles
 */
@lombok.RequiredArgsConstructor(staticName = "of")
public final class ColorIcon implements Icon {

    private final Color color;
    private final int size;

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Color savedColor = g.getColor();
        
        g.setColor(c.getForeground());
        g.drawRect(x, y, getIconWidth(), getIconHeight());

        g.setColor(color);
        g.fillRect(x + 1, y + 1, getIconWidth() - 1, getIconHeight() - 1);

        g.setColor(savedColor);
    }

    @Override
    public int getIconWidth() {
        return size;
    }

    @Override
    public int getIconHeight() {
        return size;
    }
}
