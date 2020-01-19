package moe.orangelabs.json;

import moe.orangelabs.json.types.JsonNumber;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonNumberTest {

    @Test
    public void testCompareEqual() {
        assertThat(new JsonNumber(1)).isEqualByComparingTo(new JsonNumber(1));
    }

    @Test
    public void testCompareGreater() {
        assertThat(new JsonNumber(10)).isGreaterThan(new JsonNumber(1));
    }

    @Test
    public void testCompareLess() {
        assertThat(new JsonNumber(1)).isLessThan(new JsonNumber(10));
    }
}
