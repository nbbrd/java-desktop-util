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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import javax.swing.Icon;

/**
 *
 * @author Philippe Charles
 */
public final class SpinningIcon implements Icon {

    private static final int DURATION = 2000;

    private final Animation animation;
    private final Icon icon;

    public SpinningIcon(Component c, Icon icon) {
        this.animation = new Animation(c, DURATION);
        this.icon = icon;
        Animator.INSTANCE.register(animation);
    }

    private double getAngle() {
        return Math.PI * 2 * animation.getPosition();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        AffineTransform trans = new AffineTransform();
        trans.translate(x, y);
        trans.rotate(getAngle(), getIconWidth() / 2d, getIconHeight() / 2d);
        g2d.transform(trans);

        icon.paintIcon(c, g2d, 0, 0);

        g2d.dispose();
    }

    @Override
    public int getIconWidth() {
        return icon.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return icon.getIconHeight();
    }
}
