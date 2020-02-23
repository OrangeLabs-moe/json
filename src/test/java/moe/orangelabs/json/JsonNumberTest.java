package moe.orangelabs.json;

import org.testng.annotations.Test;

import static moe.orangelabs.json.Json.number;
import static moe.orangelabs.json.JsonType.ARRAY;
import static moe.orangelabs.json.JsonType.NUMBER;
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
