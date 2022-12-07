package _test;

import lombok.NonNull;
import org.checkerframework.checker.index.qual.NonNegative;

import javax.swing.*;
import java.awt.*;

@lombok.Value
public class MonochromeIcon implements Icon {

    @NonNull Color color;

    @NonNegative int size;

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setPaint(color);
        g2d.fillRect(x, y, getIconWidth(), getIconHeight());
        g2d.dispose();
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
