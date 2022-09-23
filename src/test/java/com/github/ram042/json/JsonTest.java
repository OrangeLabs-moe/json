package com.github.ram042.json;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JsonTest {

    @DataProvider(name = "conversion")
    public Object[][] getConversion() {
        return new Object[][]{
                {
                        (JsonSerializable) () -> new JsonString("abc"),
                        new JsonString("abc")
                },
                {
                        (JsonSerializable) () -> null,
                        JsonNull.NULL
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
                        new HashSet<>(Collections.singletonList(1)),
                        new JsonArray(new JsonNumber(1))
                }

        };
    }

    @Test(dataProvider = "conversion")
    public void testConversionToJson(Object input, Json output) {
        assertThat(Json.toJson(input)).isEqualTo(output);
    }

    @Test
    public void testAsyncLoop() {
        JsonObject one = new JsonObject();
        JsonObject two = new JsonObject();

        one.castAndPut("two", two);
        two.castAndPut("one", one);

        assertThatThrownBy(() -> one.toStringAsync().get(2, TimeUnit.SECONDS))
                .isExactlyInstanceOf(TimeoutException.class);
    }

    @DataProvider(name = "typeProvider")
    public static Object[][] typeProvider() {
        return new Object[][]{
                {Json.object(), JsonType.OBJECT},
                {Json.array(), JsonType.ARRAY},
                {Json.string(""), JsonType.STRING},
                {Json.number(100), JsonType.NUMBER},
                {Json.bool(true), JsonType.BOOLEAN},
                {Json.aNull(), JsonType.NULL},
        };
    }

    @Test(dataProvider = "typeProvider")
    public void testTypes(Json object, JsonType expectedType) {
        TestUtils.testType(object, expectedType);
    }
}
