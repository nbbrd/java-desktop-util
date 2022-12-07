package _test;

import org.assertj.core.api.Condition;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.function.Function;

@lombok.experimental.UtilityClass
public class ImageUtil {

    public static BufferedImage getResourceAsImage(Class<?> anchor, String name) throws IOException {
        try (InputStream stream = anchor.getResourceAsStream(name)) {
            if (stream == null) {
                throw new IOException("Resource not found: " + name);
            }
            return ImageIO.read(stream);
        }
    }

    public static BufferedImage render(Icon icon, double scale) {
        BufferedImage result = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = result.createGraphics();
        AffineTransform transform = new AffineTransform();
        transform.scale(scale, scale);
        g2d.setTransform(transform);
        icon.paintIcon(null, g2d, 0, 0);
        g2d.dispose();
        return result;
    }

    public static byte[] toBytes(BufferedImage img) {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", stream);
            return stream.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static Condition<BufferedImage> imageBinaryContent(Icon icon) {
        return imageBinaryContent(render(icon, 1));
    }

    public static Condition<BufferedImage> imageBinaryContent(BufferedImage image) {
        return new Condition<>(value -> Arrays.equals(toBytes(value), toBytes(image)), "same image as " + image);
    }

    public static Condition<BufferedImage> imageBinaryContent(Class<?> anchor, String resourceName) {
        try {
            return imageBinaryContent(getResourceAsImage(anchor, resourceName));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static Condition<BufferedImage> imageSize(int size) {
        return new Condition<>(value -> value.getWidth() == size && value.getHeight() == size, "image size " + size);
    }

    public static Condition<Icon> iconSize(int size) {
        return new Condition<>(value -> value.getIconWidth() == size && value.getIconHeight() == size, "icon size " + size);
    }

    public static Function<Icon, BufferedImage> byRendering() {
        return icon -> ImageUtil.render(icon, 1);
    }

    public static Function<Icon, BufferedImage> byRenderingAtScale(double scale) {
        return icon -> ImageUtil.render(icon, scale);
    }
}
