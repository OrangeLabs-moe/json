package moe.orangelabs.json;

import moe.orangelabs.json.types.JsonObject;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static moe.orangelabs.json.Json.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JsonObjectTest {

    @Test
    public void testType() {
        assertThat(new JsonObject().getType()).isEqualTo(JsonType.OBJECT);

        assertThat(new JsonObject().isObject()).isTrue();

        assertThat(new JsonObject().isArray()).isFalse();
        assertThat(new JsonObject().isBoolean()).isFalse();
        assertThat(new JsonObject().isNumber()).isFalse();
        assertThat(new JsonObject().isNull()).isFalse();
        assertThat(new JsonObject().isString()).isFalse();

        assertThat(new JsonObject().getAsObject()).isExactlyInstanceOf(JsonObject.class);

        assertThatThrownBy(() -> new JsonObject().getAsArray()).isExactlyInstanceOf(JsonCastException.class);
        assertThatThrownBy(() -> new JsonObject().getAsBoolean()).isExactlyInstanceOf(JsonCastException.class);
        assertThatThrownBy(() -> new JsonObject().getAsNumber()).isExactlyInstanceOf(JsonCastException.class);
        assertThatThrownBy(() -> new JsonObject().getAsNull()).isExactlyInstanceOf(JsonCastException.class);
        assertThatThrownBy(() -> new JsonObject().getAsString()).isExactlyInstanceOf(JsonCastException.class);
    }

    @Test
    public void testClone() {
        Json a = object("123", object("321", "123"));
        Json b = a.clone();
        assertThat(a.equals(b)).isTrue();
    }

    @DataProvider(name = "objMapEqualsProvider")
    public Object[][] getEquals() {
        return new Object[][]{
                {object(), object(), true},
                {object("abc", true), object("abc", true), true},
                {object("abc", true, "12", null), object("abc", true, "12", null), true},
        };
    }

    @Test(dataProvider = "objMapEqualsProvider")
    public void testEquals(Json valueA, Json valueB, boolean expected) {
        assertThat(valueA.equals(valueB)).isEqualTo(expected);
    }

    @Test
    public void testGetting() {
        JsonObject object = object(
                "object", object("1", "1"),
                "array", array(1, 2, 3),
                "string", "string",
                "number", 123,
                "boolean", false,
                "null", null
        );

        assertThat(object.getObject("object")).isEqualTo(object("1", "1"));
        assertThat(object.getArray("array")).isEqualTo(array(1, 2, 3));
        assertThat(object.getString("string")).isEqualTo(string("string"));
        assertThat(object.getNumber("number")).isEqualTo(number(123));
        assertThat(object.getBoolean("boolean")).isEqualTo(bool(false));
        assertThat(object.getNull("null")).isEqualTo(aNull());
    }

    @Test
    public void testGetOrDefault() {
        JsonObject object = object(
                "object", object("1", "1"),
                "array", array(1, 2, 3),
                "string", "string",
                "number", 123,
                "boolean", true,
                "null", null
        );

        assertThat(object.getObjectOrDefault("object", object("2", "2"))).isEqualTo(object("1", "1"));
        assertThat(object.getObjectOrDefault("object2", object("2", "2"))).isEqualTo(object("2", "2"));

        assertThat(object.getArrayOrDefault("array", array(4, 5, 6))).isEqualTo(array(1, 2, 3));
        assertThat(object.getArrayOrDefault("array2", array(4, 5, 6))).isEqualTo(array(4, 5, 6));

        assertThat(object.getStringOrDefault("string", "something")).isEqualTo(string("string"));
        assertThat(object.getStringOrDefault("string2", "something")).isEqualTo(string("something"));

        assertThat(object.getNumberOrDefault("number", 456)).isEqualTo(number(123));
        assertThat(object.getNumberOrDefault("number2", 456)).isEqualTo(number(456));

        assertThat(object.getBooleanOrDefault("boolean", false)).isEqualTo(bool(true));
        assertThat(object.getBooleanOrDefault("boolean2", false)).isEqualTo(bool(false));
    }

    @Test
    public void testContains() {
        JsonObject object = object(
                "object", object("1", "1"),
                "array", array(1, 2, 3),
                "string", "string",
                "number", 123,
                "boolean", true,
                "null", null
        );

        assertThat(object.containsKey("object")).isTrue();
        assertThat(object.containsKey("object2")).isFalse();

        assertThat(object.containsValue(array(1, 2, 3))).isTrue();
        assertThat(object.containsValue(array())).isFalse();

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
