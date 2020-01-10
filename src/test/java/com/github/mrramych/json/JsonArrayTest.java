package com.github.mrramych.json;

import com.github.mrramych.json.types.JsonArray;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.github.mrramych.json.Json.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JsonArrayTest {

    @Test
    public void testType() {
        assertThat(new JsonArray().getType()).isEqualTo(JsonType.ARRAY);


        assertThat(new JsonArray().isArray()).isTrue();

        assertThat(new JsonArray().isBoolean()).isFalse();
        assertThat(new JsonArray().isNumber()).isFalse();
        assertThat(new JsonArray().isNull()).isFalse();
        assertThat(new JsonArray().isObject()).isFalse();
        assertThat(new JsonArray().isString()).isFalse();


        assertThat(new JsonArray().getAsArray()).isExactlyInstanceOf(JsonArray.class);

        assertThatThrownBy(() -> new JsonArray().getAsBoolean()).isExactlyInstanceOf(JsonCastException.class);
        assertThatThrownBy(() -> new JsonArray().getAsNumber()).isExactlyInstanceOf(JsonCastException.class);
        assertThatThrownBy(() -> new JsonArray().getAsNull()).isExactlyInstanceOf(JsonCastException.class);
        assertThatThrownBy(() -> new JsonArray().getAsObject()).isExactlyInstanceOf(JsonCastException.class);
        assertThatThrownBy(() -> new JsonArray().getAsString()).isExactlyInstanceOf(JsonCastException.class);
    }

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
                {array(1), "[1]"},
                {array(1, 1), "[1,1]"}
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
        };
    }

    @Test(dataProvider = "parsing")
    public void testParsing(int testId, String input, JsonArray expected) {
        assertThat(Json.parse(input)).isEqualTo(expected);
    }

    @Test
    public void testGetting() {
        JsonArray array = array(
                object("1", 1),
                array(1, 2, 3),
                "string",
                123,
                true,
                false
        );

        assertThat(array.getObject(0)).isEqualTo(object("1", 1));
        assertThat(array.getArray(1)).isEqualTo(array(1, 2, 3));
        assertThat(array.getString(2)).isEqualTo(string("string"));
        assertThat(array.getNumber(3)).isEqualTo(number(123));
        assertThat(array.getBoolean(4)).isEqualTo(TRUE);
        assertThat(array.getBoolean(5)).isEqualTo(FALSE);
    }
}
