package moe.orangelabs.json;

import moe.orangelabs.json.exceptions.JsonCastException;

import java.util.function.Function;
import java.util.function.Supplier;

import static moe.orangelabs.json.JsonType.*;
import static org.assertj.core.api.Assertions.assertThat;

public class TestUtils {

    public static void testType(Json json, JsonType expectedType) {
        assertThat(json.getType()).isEqualTo(expectedType);

        assertThat(json.isObject()).isEqualTo(expectedType == OBJECT);
        assertThat(json.isArray()).isEqualTo(expectedType == ARRAY);

        assertThat(json.isString()).isEqualTo(expectedType == STRING);
        assertThat(json.isNumber()).isEqualTo(expectedType == NUMBER);
        assertThat(json.isBoolean()).isEqualTo(expectedType == BOOLEAN);
        assertThat(json.isNull()).isEqualTo(expectedType == NULL);

        assertThat(smartThing.apply(json::getAsObject))
                .isExactlyInstanceOf(expectedType == OBJECT ? getClassForType(expectedType) : JsonCastException.class)
                .matches(o -> o instanceof JsonCastException || o == json);
        assertThat(smartThing.apply(json::getAsArray))
                .isExactlyInstanceOf(expectedType == ARRAY ? getClassForType(expectedType) : JsonCastException.class)
                .matches(o -> o instanceof JsonCastException || o == json);

        assertThat(smartThing.apply(json::getAsString))
                .isExactlyInstanceOf(expectedType == STRING ? getClassForType(expectedType) : JsonCastException.class)
                .matches(o -> o instanceof JsonCastException || o == json);
        assertThat(smartThing.apply(json::getAsNumber))
                .isExactlyInstanceOf(expectedType == NUMBER ? getClassForType(expectedType) : JsonCastException.class)
                .matches(o -> o instanceof JsonCastException || o == json);
        assertThat(smartThing.apply(json::getAsBoolean))
                .isExactlyInstanceOf(expectedType == BOOLEAN ? getClassForType(expectedType) : JsonCastException.class)
                .matches(o -> o instanceof JsonCastException || o == json);
        assertThat(smartThing.apply(json::getAsNull))
                .isExactlyInstanceOf(expectedType == NULL ? getClassForType(expectedType) : JsonCastException.class)
                .matches(o -> o instanceof JsonCastException || o == json);
    }

    private static final Function<Supplier<Object>, Object> smartThing = objectSupplier -> {
        try {
            return objectSupplier.get();
        } catch (JsonCastException e) {
            return e;
        }
    };

    static Class<? extends Json> getClassForType(JsonType type) {
        switch (type) {
            case OBJECT:
                return JsonObject.class;
            case ARRAY:
                return JsonArray.class;
            case STRING:
                return JsonString.class;
            case NUMBER:
                return JsonNumber.class;
            case BOOLEAN:
                return JsonBoolean.class;
            case NULL:
                return JsonNull.class;
            default:
                throw new IllegalArgumentException("Unexpected value: " + type);
        }
    }

}
