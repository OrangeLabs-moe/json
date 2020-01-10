package com.github.mrramych.json;

import com.github.mrramych.json.types.JsonArray;
import com.github.mrramych.json.types.JsonNumber;
import com.github.mrramych.json.types.JsonObject;
import com.github.mrramych.json.types.JsonString;
import org.assertj.core.api.Assertions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

public class JsonTest {

    @DataProvider(name = "conversion")
    public Object[][] getConversion() {
        return new Object[][]{
                {
                        (JsonSerializable) () -> new JsonString("abc"),
                        new JsonString("abc")
                },
                {
                        new Object[]{1, new JsonNumber(2), new JsonNumber(3)},
                        new JsonArray(new JsonNumber(1), new JsonNumber(2), new JsonNumber(3))
                },
                {
                        Arrays.asList(1, new JsonNumber(2)),
                        new JsonArray(new JsonNumber(1), new JsonNumber(2))
                },
                {
                        new BigDecimal(12),
                        new JsonNumber(new BigDecimal(12))
                },
                {
                        1.6f,
                        new JsonNumber(1.6f)
                },
                {
                        3.2d,
                        new JsonNumber(3.2d)
                },
                {
                        Collections.singletonMap("a", new JsonString("b")),
                        new JsonObject(new JsonString("a"), new JsonString("b"))
                },
                {
                        Arrays.stream(new Object[]{1, new JsonNumber(2), new JsonNumber(3)}).iterator(),
                        new JsonArray(new JsonNumber(1), new JsonNumber(2), new JsonNumber(3))
                },
                {
                        new HashSet<>(Arrays.asList(1)),
                        new JsonArray(new JsonNumber(1))
                }

        };
    }

    @Test(dataProvider = "conversion")
    public void testConversion(Object input, Json output) {
        Assertions.assertThat(Json.toJson(input)).isEqualTo(output);
    }

    @Test
    public void testNull() {
        Assertions.assertThat(Json.aNull()).isEqualTo(Json.NULL);
    }
}
