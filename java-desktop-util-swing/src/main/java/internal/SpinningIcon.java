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
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 *
 * @author Philippe Charles
 */
public final class SpinningIcon implements Icon {

    @NonNull
    public static SpinningIcon of(@NonNull Icon icon) {
        return new SpinningIcon(icon);
    }

    @NonNull
    public static SpinningIcon of(@NonNull Icon icon, @NonNull Component animated) {
        SpinningIcon result = new SpinningIcon(icon);
        result.init(animated);
        return result;
    }

    private static final int DURATION = 2000;

    private final Animation animation;
    private final Icon icon;

    private Component animated;
    private double angle;

    private SpinningIcon(Icon icon) {
        this.animation = Animation.cycle(DURATION, this::refresh);
        this.icon = icon;
        this.animated = null;
        this.angle = 0;
    }

    private void refresh(double position) {
        double newAngle = Math.PI * 2 * position;
        if (angle != newAngle) {
            angle = newAngle;
            if (animated != null) {
                animated.repaint();
            }
        }
    }

    private void init(Component c) {
        Animator.INSTANCE.register(animation);
        animated = c;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (animated == null) {
            init(c);
        }

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        AffineTransform trans = new AffineTransform();
        trans.translate(x, y);
        trans.rotate(angle, getIconWidth() / 2d, getIconHeight() / 2d);
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
