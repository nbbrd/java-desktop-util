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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.LongConsumer;
import javax.swing.Icon;
import javax.swing.Timer;

/**
 *
 * @author Philippe Charles
 */
public final class SpinningIcon implements Icon {

    private static final int DURATION = 2000;

    private final Component component;
    private final Icon icon;
    private double position;

    public SpinningIcon(Component c, Icon icon) {
        this.component = c;
        this.icon = icon;
        Animator.INSTANCE.register(this::refresh);
    }

    private void refresh(long timeInMillis) {
        position = 1f * (timeInMillis % DURATION) / DURATION;
        component.repaint();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        double angle = Math.PI * 2 * position;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
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

    private enum Animator implements ActionListener {

        INSTANCE;

        private static final int FPS = 60;

        private final Timer timer;
        private final List<WeakReference<LongConsumer>> items;

        private Animator() {
            this.timer = new Timer(1000 / FPS, this);
            timer.start();
            this.items = new ArrayList<>();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            long time = System.currentTimeMillis();
            Iterator<WeakReference<LongConsumer>> iterator = items.iterator();
            while (iterator.hasNext()) {
                WeakReference<LongConsumer> ref = iterator.next();
                LongConsumer o = ref.get();
                if (o != null) {
                    o.accept(time);
                } else {
                    iterator.remove();
                }
            }
        }

        public void register(LongConsumer stuff) {
            items.add(new WeakReference(stuff));
        }
    }
}
