package com.github.ram042.json;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JsonObjectTest {

    @Test
    public void testClone() {
        Json a = Json.object("123", Json.object("321", "123"));
        Json b = a.clone();
        assertThat(a.equals(b)).isTrue();
    }

    @DataProvider(name = "objMapEqualsProvider")
    public Object[][] getEquals() {
        return new Object[][]{
                {Json.object(), Json.object(), true},
                {Json.object("abc", true), Json.object("abc", true), true},
                {Json.object("abc", true, "12", null), Json.object("abc", true, "12", null), true},
        };
    }

    @Test(dataProvider = "objMapEqualsProvider")
    public void testEquals(Json valueA, Json valueB, boolean expected) {
        assertThat(valueA.equals(valueB)).isEqualTo(expected);
    }

    @Test
    public void testGetting() {
        JsonObject object = Json.object(
                "object", Json.object("1", "1"),
                "array", Json.array(1, 2, 3),
                "string", "string",
                "number", 123,
                "boolean", false,
                "null", null
        );

        assertThat(object.getObject("object")).isEqualTo(Json.object("1", "1"));
        assertThat(object.getArray("array")).isEqualTo(Json.array(1, 2, 3));
        assertThat(object.getString("string")).isEqualTo(Json.string("string"));
        assertThat(object.getNumber("number")).isEqualTo(Json.number(123));
        assertThat(object.getBoolean("boolean")).isEqualTo(Json.bool(false));
        assertThat(object.getNull("null")).isEqualTo(Json.aNull());
    }

    @Test
    public void testGetOrDefault() {
        JsonObject object = Json.object(
                "object", Json.object("1", "1"),
                "array", Json.array(1, 2, 3),
                "string", "string",
                "number", 123,
                "boolean", true,
                "null", null
        );

        assertThat(object.getObjectOrDefault("object", Json.object("2", "2"))).isEqualTo(Json.object("1", "1"));
        assertThat(object.getObjectOrDefault("object2", Json.object("2", "2"))).isEqualTo(Json.object("2", "2"));

        assertThat(object.getArrayOrDefault("array", Json.array(4, 5, 6))).isEqualTo(Json.array(1, 2, 3));
        assertThat(object.getArrayOrDefault("array2", Json.array(4, 5, 6))).isEqualTo(Json.array(4, 5, 6));

        assertThat(object.getStringOrDefault("string", "something")).isEqualTo(Json.string("string"));
        assertThat(object.getStringOrDefault("string2", "something")).isEqualTo(Json.string("something"));

        assertThat(object.getNumberOrDefault("number", 456)).isEqualTo(Json.number(123));
        assertThat(object.getNumberOrDefault("number2", 456)).isEqualTo(Json.number(456));

        assertThat(object.getBooleanOrDefault("boolean", false)).isEqualTo(Json.bool(true));
        assertThat(object.getBooleanOrDefault("boolean2", false)).isEqualTo(Json.bool(false));
    }

    @Test
    public void testContains() {
        JsonObject object = Json.object(
                "object", Json.object("1", "1"),
                "array", Json.array(1, 2, 3),
                "string", "string",
                "number", 123,
                "boolean", true,
                "null", null
        );

        assertThat(object.containsKey("object")).isTrue();
        assertThat(object.containsKey("object2")).isFalse();

        assertThat(object.containsValue(Json.array(1, 2, 3))).isTrue();
        assertThat(object.containsValue(Json.array())).isFalse();

        assertThat(object.containsObject("object")).isTrue();
        assertThat(object.containsObject("object2")).isFalse();
        assertThat(object.containsObject("null")).isFalse();

        assertThat(object.containsArray("array")).isTrue();
        assertThat(object.containsArray("array2")).isFalse();
        assertThat(object.containsArray("null")).isFalse();

        assertThat(object.containsString("string")).isTrue();
        assertThat(object.containsString("string2")).isFalse();
        assertThat(object.containsString("null")).isFalse();

        assertThat(object.containsNumber("number")).isTrue();
        assertThat(object.containsNumber("number2")).isFalse();
        assertThat(object.containsNumber("null")).isFalse();

        assertThat(object.containsBoolean("boolean")).isTrue();
        assertThat(object.containsBoolean("boolean2")).isFalse();
        assertThat(object.containsBoolean("null")).isFalse();

        assertThat(object.containsNull("null")).isTrue();
        assertThat(object.containsNull("boolean2")).isFalse();
        assertThat(object.containsNull("string")).isFalse();
    }

}
