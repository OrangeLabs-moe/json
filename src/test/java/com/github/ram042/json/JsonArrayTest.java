package com.github.ram042.json;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonArrayTest {

    @Test
    public void test() {
        assertThat(new JsonArray().isEmpty()).isTrue();
        assertThat(new JsonArray(1, 2, 3).isEmpty()).isFalse();

        assertThat(new JsonArray().size()).isEqualTo(0);
        assertThat(new JsonArray(1).size()).isEqualTo(1);
        assertThat(new JsonArray(1, 2, 3).size()).isEqualTo(3);

        assertThat(new JsonArray()).isEqualTo(new JsonArray());
        assertThat(new JsonArray(1, 2, 3)).isEqualTo(new JsonArray(1, 2, 3));

        assertThat(new JsonArray()).isNotEqualTo(new JsonArray(1, 2, 3));
        assertThat(new JsonArray(3, 2, 1)).isNotEqualTo(new JsonArray(1, 2, 3));
    }

    @DataProvider(name = "conversion")
    public Object[][] getStringConversion() {
        return new Object[][]{
                {Json.array(1), "[1]"},
                {Json.array(1, 1), "[1,1]"}
        };
    }

    @Test(dataProvider = "conversion")
    public void testStringConversion(Json input, String output) {
        assertThat(input.toString()).isEqualTo(output);
    }

    @DataProvider(name = "parsing")
    public Object[][] getParsing() {
        return new Object[][]{
                {0, "[\"abc\",\"abc\"]", Json.array("abc", "abc")},
                {1, "[\"\\\"\",\"\\\"\"]", Json.array("\"", "\"")},
                {2, "[1]", Json.array(1)},
                {2, "[  1  ]", Json.array(1)}
        };
    }

    @Test(dataProvider = "parsing")
    public void testParsing(int testId, String input, JsonArray expected) {
        assertThat(Json.parse(input)).isEqualTo(expected);
    }

    @Test
    public void testGetting() {
        JsonArray array = Json.array(
                Json.object("1", 1),
                Json.array(1, 2, 3),
                "string",
                123,
                true,
                false
        );

        assertThat(array.getObject(0)).isEqualTo(Json.object("1", 1));
        assertThat(array.getArray(1)).isEqualTo(Json.array(1, 2, 3));
        assertThat(array.getString(2)).isEqualTo(Json.string("string"));
        assertThat(array.getNumber(3)).isEqualTo(Json.number(123));
        assertThat(array.getBoolean(4)).isEqualTo(JsonBoolean.TRUE);
        assertThat(array.getBoolean(5)).isEqualTo(JsonBoolean.FALSE);
    }
}
