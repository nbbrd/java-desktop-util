/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ec.util.completion;

import java.util.Arrays;
import java.util.List;

import lombok.NonNull;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Philippe Charles
 */
public class AbstractAutoCompletionSourceTest {

    static final List<Class<?>> VALUES = Arrays.asList(Integer.class, Double.class);

    static class TestSource extends AbstractAutoCompletionSource<Class<?>> {

        @Override
        protected @NonNull Iterable<Class<?>> getAllValues() throws Exception {
            return VALUES;
        }
    }

    @Test
    public void testToString() {
        AutoCompletionSource s1 = new TestSource();
        Assert.assertEquals("class java.lang.Integer", s1.toString(VALUES.get(0)));

        AutoCompletionSource s2 = new TestSource() {
            @Override
            protected @NonNull String getValueAsString(@NonNull Class<?> value) {
                return value.getSimpleName();
            }
        };
        Assert.assertEquals("Integer", s2.toString(VALUES.get(0)));
    }

    @Test
    public void testGetValues() throws Exception {
        AutoCompletionSource s1 = new TestSource();
        Assert.assertArrayEquals(new Object[]{Double.class, Integer.class}, s1.getValues("").toArray());
        Assert.assertArrayEquals(new Object[]{Double.class}, s1.getValues("oüBl").toArray());
        Assert.assertArrayEquals(new Object[]{Double.class, Integer.class}, s1.getValues("lang").toArray());
    }

    @Test
    public void testGetValueAsString() throws Exception {
        AutoCompletionSource s2 = new TestSource() {
            @Override
            protected @NonNull String getValueAsString(@NonNull Class<?> value) {
                return value.getSimpleName();
            }
        };
        Assert.assertArrayEquals(new Object[]{Double.class, Integer.class}, s2.getValues("").toArray());
        Assert.assertArrayEquals(new Object[]{Double.class}, s2.getValues("oüBl").toArray());
        Assert.assertArrayEquals(new Object[]{}, s2.getValues("lang").toArray());
    }

    @Test
    public void testGetNormalizedString() throws Exception {
        AutoCompletionSource s3 = new TestSource() {
            @Override
            protected @NonNull String getNormalizedString(@NonNull String input) {
                return input;
            }
        };
        Assert.assertArrayEquals(new Object[]{Double.class, Integer.class}, s3.getValues("").toArray());
        Assert.assertArrayEquals(new Object[]{}, s3.getValues("oüBl").toArray());
        Assert.assertArrayEquals(new Object[]{Double.class, Integer.class}, s3.getValues("lang").toArray());
    }

    @Test
    public void testCompare() throws Exception {
        AutoCompletionSource s3 = new TestSource() {
            @Override
            public int compare(Class<?> left, Class<?> right) {
                return super.compare(right, left);
            }
        };
        Assert.assertArrayEquals(new Object[]{Integer.class, Double.class}, s3.getValues("").toArray());
    }

    @Test
    public void testGetLimitSize() throws Exception {
        AutoCompletionSource s3 = new TestSource() {
            @Override
            protected int getLimitSize() {
                return 1;
            }
        };
        Assert.assertArrayEquals(new Object[]{Integer.class}, s3.getValues("").toArray());
    }

    @Test
    public void testMatches() throws Exception {
        AutoCompletionSource s3 = new TestSource() {
            @Override
            protected boolean matches(@NonNull String normalizedTerm, @NonNull String normalizedInput) {
                return normalizedTerm.equals(normalizedInput);
            }
        };
        Assert.assertArrayEquals(new Object[]{}, s3.getValues("").toArray());
        Assert.assertArrayEquals(new Object[]{}, s3.getValues("Integer").toArray());
        Assert.assertArrayEquals(new Object[]{Integer.class}, s3.getValues("class java.lang.Integer").toArray());
    }

    @Test
    public void testMatches2() throws Exception {
        AutoCompletionSource s3 = new TestSource() {
            @Override
            protected boolean matches(@NonNull TermMatcher termMatcher, @NonNull Class<?> input) {
                return termMatcher.matches("" + input.getSimpleName().length());
            }
        };
        Assert.assertArrayEquals(new Object[]{Double.class, Integer.class}, s3.getValues("").toArray());
        Assert.assertArrayEquals(new Object[]{}, s3.getValues("Integer").toArray());
        Assert.assertArrayEquals(new Object[]{Integer.class}, s3.getValues("7").toArray());
    }
}
