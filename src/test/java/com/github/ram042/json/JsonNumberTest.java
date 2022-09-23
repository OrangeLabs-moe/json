package com.github.ram042.json;

import org.testng.annotations.Test;

import static com.github.ram042.json.Json.number;
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
