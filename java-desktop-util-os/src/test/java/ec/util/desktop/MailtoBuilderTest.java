/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.util.desktop;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Philippe Charles
 */
public class MailtoBuilderTest {

    @Test
    public void testBuild() {
        MailtoBuilder b = new MailtoBuilder();
        Assertions.assertEquals(
                b.clear().build().toString(),
                "mailto:?");
        Assertions.assertEquals(
                b.clear().to("email@example.com").build().toString(),
                "mailto:?to=email%40example.com");
        Assertions.assertEquals(
                b.clear().to("\"Tim Jones\" <tim@example.com>").build().toString(),
                "mailto:?to=%22Tim%20Jones%22%20%3Ctim%40example.com%3E");
    }
}
