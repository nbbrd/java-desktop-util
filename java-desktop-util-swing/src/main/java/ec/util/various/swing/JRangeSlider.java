/*
 * Copyright 2026 National Bank of Belgium
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
package ec.util.various.swing;

import lombok.NonNull;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * A Swing slider with two thumbs representing a low and high value within a
 * bounded range. Inspired by ControlsFX {@code RangeSlider}.
 *
 * <p>Bound properties:</p>
 * <ul>
 *   <li>{@link #MINIMUM_PROPERTY}</li>
 *   <li>{@link #MAXIMUM_PROPERTY}</li>
 *   <li>{@link #LOW_VALUE_PROPERTY}</li>
 *   <li>{@link #HIGH_VALUE_PROPERTY}</li>
 *   <li>{@link #ORIENTATION_PROPERTY}</li>
 *   <li>{@link #PAINT_TICKS_PROPERTY}</li>
 *   <li>{@link #MAJOR_TICK_SPACING_PROPERTY}</li>
 *   <li>{@link #MINOR_TICK_SPACING_PROPERTY}</li>
 *   <li>{@link #SNAP_TO_TICKS_PROPERTY}</li>
 * </ul>
 *
 * @author Philippe Charles
 */
public final class JRangeSlider extends JComponent {

    // <editor-fold defaultstate="collapsed" desc="API definition">
    public static final String MINIMUM_PROPERTY = "minimum";
    public static final String MAXIMUM_PROPERTY = "maximum";
    public static final String LOW_VALUE_PROPERTY = "lowValue";
    public static final String HIGH_VALUE_PROPERTY = "highValue";
    public static final String ORIENTATION_PROPERTY = "orientation";
    public static final String PAINT_TICKS_PROPERTY = "paintTicks";
    public static final String MAJOR_TICK_SPACING_PROPERTY = "majorTickSpacing";
    public static final String MINOR_TICK_SPACING_PROPERTY = "minorTickSpacing";
    public static final String SNAP_TO_TICKS_PROPERTY = "snapToTicks";
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Visual constants">
    private static final int THUMB_SIZE = 14;
    private static final int TRACK_THICKNESS = 4;
    private static final int MAJOR_TICK_LENGTH = 7;
    private static final int MINOR_TICK_LENGTH = 4;
    private static final int PREFERRED_LENGTH = 200;
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Fields">
    private int minimum;
    private int maximum;
    private int lowValue;
    private int highValue;
    private int orientation;
    private boolean paintTicks;
    private int majorTickSpacing;
    private int minorTickSpacing;
    private boolean snapToTicks;

    private enum Thumb {NONE, LOW, HIGH, RANGE}

    private Thumb activeThumb = Thumb.NONE;
    private Thumb hoveredThumb = Thumb.NONE;
    private Thumb focusedThumb = Thumb.LOW;
    private int dragOffset;
    private int dragRangeWidth;
    // </editor-fold>

    public JRangeSlider() {
        this.minimum = 0;
        this.maximum = 100;
        this.lowValue = 25;
        this.highValue = 75;
        this.orientation = SwingConstants.HORIZONTAL;
        this.paintTicks = false;
        this.majorTickSpacing = 25;
        this.minorTickSpacing = 5;
        this.snapToTicks = false;

        setOpaque(false);
        setFocusable(true);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                Thumb hit = hitTest(e.getX(), e.getY());
                if (hit != Thumb.NONE) {
                    activeThumb = hit;
                    if (hit != Thumb.RANGE) focusedThumb = hit;
                    int pixel = (orientation == SwingConstants.HORIZONTAL) ? e.getX() : e.getY();
                    dragOffset = pixel - valueToPixel(hit == Thumb.LOW ? lowValue : highValue);
                    if (hit == Thumb.RANGE) {
                        dragOffset = pixel - valueToPixel(lowValue);
                        dragRangeWidth = highValue - lowValue;
                    }
                    repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (activeThumb == Thumb.NONE) return;
                int pixel = (orientation == SwingConstants.HORIZONTAL) ? e.getX() : e.getY();
                if (activeThumb == Thumb.RANGE) {
                    int newLow = pixelToValue(pixel - dragOffset);
                    newLow = Math.max(minimum, Math.min(newLow, maximum - dragRangeWidth));
                    setRange(newLow, newLow + dragRangeWidth);
                    return;
                }
                int newVal = pixelToValue(pixel - dragOffset + THUMB_SIZE / 2);
                if (snapToTicks && minorTickSpacing > 0) {
                    newVal = Math.round((float) newVal / minorTickSpacing) * minorTickSpacing;
                } else if (snapToTicks && majorTickSpacing > 0) {
                    newVal = Math.round((float) newVal / majorTickSpacing) * majorTickSpacing;
                }
                if (activeThumb == Thumb.LOW) {
                    setLowValue(Math.min(newVal, highValue));
                } else {
                    setHighValue(Math.max(newVal, lowValue));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                activeThumb = Thumb.NONE;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoveredThumb = Thumb.NONE;
                setCursor(Cursor.getDefaultCursor());
                repaint();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Thumb hit = hitTest(e.getX(), e.getY());
                hoveredThumb = hit;
                setCursor(hit == Thumb.RANGE
                        ? Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR)
                        : Cursor.getDefaultCursor());
                repaint();
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int step = (minorTickSpacing > 0) ? minorTickSpacing
                        : (majorTickSpacing > 0) ? majorTickSpacing : 1;
                boolean horizontal = orientation == SwingConstants.HORIZONTAL;
                boolean decrement = horizontal ? e.getKeyCode() == KeyEvent.VK_LEFT : e.getKeyCode() == KeyEvent.VK_DOWN;
                boolean increment = horizontal ? e.getKeyCode() == KeyEvent.VK_RIGHT : e.getKeyCode() == KeyEvent.VK_UP;
                boolean switchThumb = e.getKeyCode() == KeyEvent.VK_TAB;
                if (switchThumb) {
                    focusedThumb = (focusedThumb == Thumb.LOW) ? Thumb.HIGH : Thumb.LOW;
                    repaint();
                } else if (decrement) {
                    if (focusedThumb == Thumb.LOW) setLowValue(Math.max(lowValue - step, minimum));
                    else setHighValue(Math.max(highValue - step, lowValue));
                } else if (increment) {
                    if (focusedThumb == Thumb.LOW) setLowValue(Math.min(lowValue + step, highValue));
                    else setHighValue(Math.min(highValue + step, maximum));
                }
            }
        });

        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    // <editor-fold defaultstate="collapsed" desc="Painting">
    @Override
    public void updateUI() {
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean horizontal = orientation == SwingConstants.HORIZONTAL;
        int w = getWidth();
        int h = getHeight();

        // --- Sizes from FlatLaf / fallback to constants ---
        int trackW  = uiInt("Slider.trackWidth", TRACK_THICKNESS);
        int trackArc = uiInt("Slider.trackArc", trackW);
        Dimension thumbDim = uiDimension("Slider.thumbSize", new Dimension(THUMB_SIZE, THUMB_SIZE));
        int tW = thumbDim.width;
        int tH = thumbDim.height;

        // --- Colors from FlatLaf / fallback chain ---
        Color bg          = getBackground();
        Color fg          = getForeground();
        Color trackColor  = uiColor("Slider.trackColor",        "controlShadow",   bg.darker());
        Color rangeColor  = uiColor("Slider.trackValueColor",   "textHighlight",   fg);
        Color thumbColor  = uiColor("Slider.thumbColor",        null,              rangeColor);
        Color thumbHover  = uiColor("Slider.thumbHoverColor",   null,              thumbColor.brighter());
        Color thumbPress  = uiColor("Slider.thumbPressedColor", null,              thumbColor.brighter().brighter());
        Color thumbFocus  = uiColor("Slider.thumbFocusedColor", null,              thumbColor);
        Color focusRing   = uiColor("Slider.focusedColor",      "textHighlight",   fg);
        Color tickColor   = uiColor("Slider.tickColor",         "controlDkShadow", trackColor.darker());

        // Track centre line
        int cx = horizontal ? 0     : (w - tW) / 2 + tW / 2;
        int cy = horizontal ? (h - tH) / 2 + tH / 2 : 0;
        if (paintTicks) {
            if (horizontal) cy = (h - tH - MAJOR_TICK_LENGTH - 4) / 2 + tH / 2;
            else            cx = (w - tW - MAJOR_TICK_LENGTH - 4) / 2 + tW / 2;
        }

        int pad = focusPad();
        int trackLen   = horizontal ? w - tW - 2 * pad : h - tH - 2 * pad;
        int trackStart = horizontal ? tW / 2 + pad      : tH / 2 + pad;

        // Full track
        int tx = horizontal ? trackStart       : cx - trackW / 2;
        int ty = horizontal ? cy  - trackW / 2 : trackStart;
        int tw = horizontal ? trackLen         : trackW;
        int th = horizontal ? trackW           : trackLen;
        g2.setColor(trackColor);
        g2.fill(new RoundRectangle2D.Float(tx, ty, tw, th, trackArc, trackArc));

        // Range fill
        int lowPx  = valueToPixelEx(lowValue,  tW, tH);
        int highPx = valueToPixelEx(highValue, tW, tH);
        int rx = horizontal ? lowPx        : cx - trackW / 2;
        int ry = horizontal ? cy - trackW / 2 : lowPx;
        int rw = horizontal ? highPx - lowPx  : trackW;
        int rh = horizontal ? trackW          : highPx - lowPx;
        g2.setColor(rangeColor);
        g2.fill(new RoundRectangle2D.Float(rx, ry, rw, rh, trackArc, trackArc));

        // Tick marks
        if (paintTicks) {
            g2.setColor(tickColor);
            if (minorTickSpacing > 0) {
                for (int v = minimum; v <= maximum; v += minorTickSpacing) {
                    int px = valueToPixelEx(v, tW, tH);
                    if (horizontal) g2.drawLine(px, cy + trackW, px, cy + trackW + MINOR_TICK_LENGTH);
                    else            g2.drawLine(cx + trackW, px, cx + trackW + MINOR_TICK_LENGTH, px);
                }
            }
            if (majorTickSpacing > 0) {
                for (int v = minimum; v <= maximum; v += majorTickSpacing) {
                    int px = valueToPixelEx(v, tW, tH);
                    if (horizontal) g2.drawLine(px, cy + trackW, px, cy + trackW + MAJOR_TICK_LENGTH);
                    else            g2.drawLine(cx + trackW, px, cx + trackW + MAJOR_TICK_LENGTH, px);
                }
            }
        }

        // Thumbs
        boolean focused = isFocusOwner();
        paintThumb(g2, lowPx,  horizontal ? cy : cx, horizontal, tW, tH,
                thumbColor, thumbHover, thumbPress, thumbFocus, focusRing,
                focusedThumb == Thumb.LOW  && focused,
                hoveredThumb == Thumb.LOW,
                activeThumb  == Thumb.LOW);
        paintThumb(g2, highPx, horizontal ? cy : cx, horizontal, tW, tH,
                thumbColor, thumbHover, thumbPress, thumbFocus, focusRing,
                focusedThumb == Thumb.HIGH && focused,
                hoveredThumb == Thumb.HIGH,
                activeThumb  == Thumb.HIGH);

        g2.dispose();
    }

    private void paintThumb(Graphics2D g2, int center, int cross, boolean horizontal,
                             int tW, int tH,
                             Color base, Color hover, Color press, Color focused, Color focusRing,
                             boolean isFocused, boolean isHovered, boolean isPressed) {
        int x = horizontal ? center - tW / 2 : cross - tW / 2;
        int y = horizontal ? cross  - tH / 2 : center - tH / 2;

        // Focus ring (drawn behind the thumb)
        if (isFocused) {
            int fw = uiInt("Slider.focusWidth", 2);
            g2.setColor(focusRing);
            g2.setStroke(new BasicStroke(fw));
            g2.drawOval(x - fw, y - fw, tW + 2 * fw, tH + 2 * fw);
        }

        // Thumb fill
        Color fill = isPressed ? press : isHovered ? hover : isFocused ? focused : base;
        g2.setColor(fill);
        g2.fillOval(x, y, tW, tH);

        // Thumb border (1 px, same as FlatLaf thumb border)
        Color border = uiColor("Slider.thumbBorderColor", null, fill.darker());
        g2.setColor(border);
        g2.setStroke(new BasicStroke(1f));
        g2.drawOval(x, y, tW, tH);
    }

    @Override
    public Dimension getPreferredSize() {
        boolean horizontal = orientation == SwingConstants.HORIZONTAL;
        // THUMB_SIZE + 2*fw (focus ring outset each side) + 4 (stroke half-width + margin)
        int fw = uiInt("Slider.focusWidth", 2);
        int cross = THUMB_SIZE + 2 * fw + 4;
        if (paintTicks) cross += MAJOR_TICK_LENGTH + 2;
        return horizontal ? new Dimension(PREFERRED_LENGTH, cross) : new Dimension(cross, PREFERRED_LENGTH);
    }

    @Override
    public Dimension getMinimumSize() {
        boolean horizontal = orientation == SwingConstants.HORIZONTAL;
        int fw = uiInt("Slider.focusWidth", 2);
        int cross = THUMB_SIZE + 2 * fw + 4;
        if (paintTicks) cross += MAJOR_TICK_LENGTH + 2;
        return horizontal ? new Dimension(2 * THUMB_SIZE, cross) : new Dimension(cross, 2 * THUMB_SIZE);
    }

    // UIManager helpers with fallback chain
    private static Color uiColor(String flatKey, String fallbackKey, Color def) {
        Color c = UIManager.getColor(flatKey);
        if (c != null) return c;
        if (fallbackKey != null) { c = UIManager.getColor(fallbackKey); if (c != null) return c; }
        return def;
    }

    private static int uiInt(String key, int def) {
        Object v = UIManager.get(key);
        return (v instanceof Integer) ? (Integer) v : def;
    }

    private static Dimension uiDimension(String key, Dimension def) {
        Object v = UIManager.get(key);
        return (v instanceof Dimension) ? (Dimension) v : def;
    }

    /** Inset on each end of the track axis to keep the focus ring inside the component bounds. */
    private static int focusPad() {
        int fw = uiInt("Slider.focusWidth", 2);
        return fw + (fw + 1) / 2 + 1; // ring outset + half stroke-width + 1px margin
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Geometry helpers">
    /**
     * Converts a value to a pixel coordinate (thumb centre) using a given thumb size.
     */
    private int valueToPixelEx(int value, int tW, int tH) {
        int range = maximum - minimum;
        boolean horiz = orientation == SwingConstants.HORIZONTAL;
        int halfThumb = (horiz ? tW : tH) / 2;
        int pad = focusPad();
        int lead = halfThumb + pad;
        int trackLen = (horiz ? getWidth() : getHeight()) - 2 * halfThumb - 2 * pad;
        if (range == 0 || trackLen <= 0) return lead;
        return lead + (value - minimum) * trackLen / range;
    }

    /**
     * Converts a value to a pixel coordinate (thumb centre) along the track axis.
     */
    private int valueToPixel(int value) {
        return valueToPixelEx(value, THUMB_SIZE, THUMB_SIZE);
    }

    /**
     * Converts a pixel coordinate (thumb centre) to a clamped model value.
     */
    private int pixelToValue(int pixel) {
        boolean horiz = orientation == SwingConstants.HORIZONTAL;
        int halfThumb = THUMB_SIZE / 2;
        int pad = focusPad();
        int lead = halfThumb + pad;
        int trackLen = (horiz ? getWidth() : getHeight()) - 2 * halfThumb - 2 * pad;
        if (trackLen <= 0) return minimum;
        int range = maximum - minimum;
        int value = minimum + (pixel - lead) * range / trackLen;
        return Math.max(minimum, Math.min(maximum, value));
    }

    private Thumb hitTest(int mx, int my) {
        int center = orientation == SwingConstants.HORIZONTAL ? mx : my;
        int lowPx = valueToPixel(lowValue);
        int highPx = valueToPixel(highValue);
        // Prefer the closer thumb on overlap
        boolean hitLow = Math.abs(center - lowPx) <= THUMB_SIZE / 2 + 1;
        boolean hitHigh = Math.abs(center - highPx) <= THUMB_SIZE / 2 + 1;
        if (hitLow && hitHigh) {
            return Math.abs(center - lowPx) <= Math.abs(center - highPx) ? Thumb.LOW : Thumb.HIGH;
        }
        if (hitLow) return Thumb.LOW;
        if (hitHigh) return Thumb.HIGH;
        // Range fill body: between the two thumb centres
        if (center > lowPx && center < highPx) return Thumb.RANGE;
        return Thumb.NONE;
    }

    /** Moves both thumbs atomically, bypassing individual clamping setters. */
    private void setRange(int newLow, int newHigh) {
        int oldLow = this.lowValue;
        int oldHigh = this.highValue;
        this.lowValue = newLow;
        this.highValue = newHigh;
        if (oldLow != newLow) firePropertyChange(LOW_VALUE_PROPERTY, oldLow, newLow);
        if (oldHigh != newHigh) firePropertyChange(HIGH_VALUE_PROPERTY, oldHigh, newHigh);
        repaint();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Getters/Setters">
    public int getMinimum() {
        return minimum;
    }

    public void setMinimum(int minimum) {
        int old = this.minimum;
        this.minimum = minimum;
        if (lowValue < minimum) setLowValue(minimum);
        if (highValue < minimum) setHighValue(minimum);
        firePropertyChange(MINIMUM_PROPERTY, old, this.minimum);
        repaint();
    }

    public int getMaximum() {
        return maximum;
    }

    public void setMaximum(int maximum) {
        int old = this.maximum;
        this.maximum = maximum;
        if (lowValue > maximum) setLowValue(maximum);
        if (highValue > maximum) setHighValue(maximum);
        firePropertyChange(MAXIMUM_PROPERTY, old, this.maximum);
        repaint();
    }

    public int getLowValue() {
        return lowValue;
    }

    public void setLowValue(int lowValue) {
        int clamped = Math.max(minimum, Math.min(lowValue, highValue));
        int old = this.lowValue;
        this.lowValue = clamped;
        firePropertyChange(LOW_VALUE_PROPERTY, old, this.lowValue);
        repaint();
    }

    public int getHighValue() {
        return highValue;
    }

    public void setHighValue(int highValue) {
        int clamped = Math.max(lowValue, Math.min(highValue, maximum));
        int old = this.highValue;
        this.highValue = clamped;
        firePropertyChange(HIGH_VALUE_PROPERTY, old, this.highValue);
        repaint();
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        if (orientation != SwingConstants.HORIZONTAL && orientation != SwingConstants.VERTICAL) {
            throw new IllegalArgumentException("orientation must be HORIZONTAL or VERTICAL");
        }
        int old = this.orientation;
        this.orientation = orientation;
        firePropertyChange(ORIENTATION_PROPERTY, old, this.orientation);
        revalidate();
        repaint();
    }

    public boolean isPaintTicks() {
        return paintTicks;
    }

    public void setPaintTicks(boolean paintTicks) {
        boolean old = this.paintTicks;
        this.paintTicks = paintTicks;
        firePropertyChange(PAINT_TICKS_PROPERTY, old, this.paintTicks);
        revalidate();
        repaint();
    }

    public int getMajorTickSpacing() {
        return majorTickSpacing;
    }

    public void setMajorTickSpacing(int majorTickSpacing) {
        int old = this.majorTickSpacing;
        this.majorTickSpacing = majorTickSpacing;
        firePropertyChange(MAJOR_TICK_SPACING_PROPERTY, old, this.majorTickSpacing);
        repaint();
    }

    public int getMinorTickSpacing() {
        return minorTickSpacing;
    }

    public void setMinorTickSpacing(int minorTickSpacing) {
        int old = this.minorTickSpacing;
        this.minorTickSpacing = minorTickSpacing;
        firePropertyChange(MINOR_TICK_SPACING_PROPERTY, old, this.minorTickSpacing);
        repaint();
    }

    public boolean isSnapToTicks() {
        return snapToTicks;
    }

    public void setSnapToTicks(boolean snapToTicks) {
        boolean old = this.snapToTicks;
        this.snapToTicks = snapToTicks;
        firePropertyChange(SNAP_TO_TICKS_PROPERTY, old, this.snapToTicks);
    }

    /**
     * Adds a {@link ChangeListener} that is notified whenever {@code lowValue}
     * or {@code highValue} changes.
     */
    public void addChangeListener(@NonNull ChangeListener l) {
        addPropertyChangeListener(LOW_VALUE_PROPERTY, evt -> l.stateChanged(new ChangeEvent(this)));
        addPropertyChangeListener(HIGH_VALUE_PROPERTY, evt -> l.stateChanged(new ChangeEvent(this)));
    }
    // </editor-fold>
}
