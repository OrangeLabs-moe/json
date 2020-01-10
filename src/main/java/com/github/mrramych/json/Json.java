package com.github.mrramych.json;

import com.github.mrramych.json.types.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface Json extends Cloneable {

    JsonNull NULL = new JsonNull();
    JsonNumber ZERO = new JsonNumber(BigDecimal.ZERO);
    JsonNumber ONE = new JsonNumber(BigDecimal.ONE);
    JsonBoolean TRUE = new JsonBoolean(true);
    JsonBoolean FALSE = new JsonBoolean(false);

    static Json parse(String data) {
        return JsonDecoder.decode(data);
    }

    /**
     * Try to convert object to ProtoObj equivalent.
     *
     * @param value Object to
     * @return Casted object
     * @throws IllegalArgumentException if object can not be casted
     */
    static Json toJson(Object value) throws IllegalArgumentException {
        if (value == null) {
            return NULL;
        } else if (value instanceof Json) {
            return (Json) value;
        } else if (value instanceof JsonSerializable) {
            return ((JsonSerializable) value).serialize();
        } else if (value instanceof Boolean) {
            return new JsonBoolean((Boolean) value);
        } else if (value instanceof Object[]) {
            return new JsonArray((Object[]) value);
        } else if (value instanceof List) {
            return new JsonArray(((List) value));
        } else if (value instanceof Number) {
            if (value instanceof BigInteger)
                return new JsonNumber(new BigDecimal(((BigInteger) value)));
            else if (value instanceof BigDecimal)
                return new JsonNumber(((BigDecimal) value));
            else if (value instanceof Float)
                return new JsonNumber(BigDecimal.valueOf(((Float) value)));
            else if (value instanceof Double)
                return new JsonNumber(BigDecimal.valueOf(((Double) value)));
            else
                return new JsonNumber(((Number) value).longValue());
        } else if (value instanceof Map) {
            return new JsonObject((Map) value);
        } else if (value instanceof Iterable) {
            return new JsonArray((Iterable) value);
        } else if (value instanceof Iterator) {
            return new JsonArray(((Iterator) value));
        } else if (value instanceof String) {
            return new JsonString((String) value);
        } else {
            throw new IllegalArgumentException("Invalid key " + value.toString());
        }
    }

    static JsonString string(String string) {
        return new JsonString(string);
    }

    static JsonNumber number(long value) {
        return new JsonNumber(value);
    }

    static JsonNumber number(BigDecimal value) {
        return new JsonNumber(value);
    }

    static JsonNumber number(BigInteger value) {
        return new JsonNumber(new BigDecimal(value));
    }

    static JsonNumber number(float value) {
        return new JsonNumber(value);
    }

    static JsonBoolean bool(boolean value) {
        return value ? TRUE : FALSE;
    }

    static JsonObject object(Object... keyAndValues) {
        return new JsonObject(keyAndValues);
    }

    static JsonArray array(Object... elements) {
        return new JsonArray(elements);
    }

    static JsonNull aNull() {
        return NULL;
    }

    JsonType getType();

    default JsonBoolean getAsBoolean() throws JsonCastException {
        throw new JsonCastException();
    }

    default JsonArray getAsArray() throws JsonCastException {
        throw new JsonCastException();
    }

    default JsonNumber getAsNumber() throws JsonCastException {
        throw new JsonCastException();
    }

    default JsonObject getAsObject() throws JsonCastException {
        throw new JsonCastException();
    }

    default JsonNull getAsNull() throws JsonCastException {
        throw new JsonCastException();
    }

    default JsonString getAsString() throws JsonCastException {
        throw new JsonCastException();
    }

    default boolean isBoolean() {
        return getType().equals(JsonType.BOOLEAN);
    }

    default boolean isArray() {
        return getType().equals(JsonType.ARRAY);
    }

    default boolean isNumber() {
        return getType().equals(JsonType.NUMBER);
    }

    default boolean isObject() {
        return getType().equals(JsonType.OBJECT);
    }

    default boolean isNull() {
        return getType().equals(JsonType.NULL);
    }

    default boolean isString() {
        return getType().equals(JsonType.STRING);
    }

    Json clone();
}
