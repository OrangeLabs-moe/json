package com.github.ram042.json;

import com.github.ram042.json.exceptions.JsonCastException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.github.ram042.json.Json.InternalType.COMPLEX;
import static com.github.ram042.json.Json.InternalType.PRIMITIVE;

public abstract class Json implements Cloneable {

    static final Marker ENTRY_SEPARATOR_TOKEN = new Marker(() -> ",");
    static final Marker KEY_VALUE_SEPARATOR_TOKEN = new Marker(() -> ":");
    static final Marker OBJECT_END_TOKEN = new Marker(() -> "}");
    static final Marker OBJECT_START_TOKEN = new Marker(() -> "{");
    static final Marker ARRAY_END_TOKEN = new Marker(() -> "]");
    static final Marker ARRAY_START_TOKEN = new Marker(() -> "[");

    public static Json parse(String json) {
        return JsonParser.parse(json);
    }

    public static JsonObject parseObject(String json) {
        return parse(json).getAsObject();
    }

    public static JsonArray parseArray(String json) {
        return parse(json).getAsArray();
    }

    public static JsonNumber parseNumber(String json) {
        return parse(json).getAsNumber();
    }

    public static JsonString parseString(String json) {
        return parse(json).getAsString();
    }

    public static JsonBoolean parseBoolean(String json) {
        return parse(json).getAsBoolean();
    }

    public static JsonNull parseNull(String json) {
        return parse(json).getAsNull();
    }

    /**
     * Try to convert object to ProtoObj equivalent.
     *
     * @param value Object to
     * @return Casted object
     * @throws IllegalArgumentException if object can not be casted
     */
    public static Json toJson(Object value) throws IllegalArgumentException {
        if (value == null) {
            return JsonNull.NULL;
        } else if (value instanceof Json) {
            return (Json) value;
        } else if (value instanceof JsonSerializable) {
            Json serialized = ((JsonSerializable) value).serialize();
            return serialized == null ? JsonNull.NULL : serialized;
        } else if (value instanceof Boolean) {
            return new JsonBoolean((Boolean) value);
        } else if (value instanceof Object[]) {
            return new JsonArray((Object[]) value);
        } else if (value instanceof List) {
            return new JsonArray((List) value);
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
            return new JsonArray((Iterator) value);
        } else if (value instanceof String) {
            return new JsonString((String) value);
        } else {
            throw new IllegalArgumentException("Can not convert " + value.toString() + " to JSON");
        }
    }

    public static JsonString string(String string) {
        return new JsonString(string);
    }

    public static JsonNumber number(long value) {
        return new JsonNumber(value);
    }

    public static JsonNumber number(BigDecimal value) {
        return new JsonNumber(value);
    }

    public static JsonNumber number(BigInteger value) {
        return new JsonNumber(new BigDecimal(value));
    }

    public static JsonNumber number(float value) {
        return new JsonNumber(value);
    }

    public static JsonBoolean bool(boolean value) {
        return value ? JsonBoolean.TRUE : JsonBoolean.FALSE;
    }

    public static JsonObject object(Object... keyAndValues) {
        return new JsonObject(keyAndValues);
    }

    public static JsonArray array(Object... elements) {
        return new JsonArray(elements);
    }

    public static JsonNull aNull() {
        return JsonNull.NULL;
    }

    abstract JsonType getType();

    public JsonBoolean getAsBoolean() throws JsonCastException {
        throw new JsonCastException();
    }

    public JsonArray getAsArray() throws JsonCastException {
        throw new JsonCastException();
    }

    public JsonNumber getAsNumber() throws JsonCastException {
        throw new JsonCastException();
    }

    public JsonObject getAsObject() throws JsonCastException {
        throw new JsonCastException();
    }

    public JsonNull getAsNull() throws JsonCastException {
        throw new JsonCastException();
    }

    public JsonString getAsString() throws JsonCastException {
        throw new JsonCastException();
    }

    public boolean isBoolean() {
        return getType().equals(JsonType.BOOLEAN);
    }

    public boolean isArray() {
        return getType().equals(JsonType.ARRAY);
    }

    public boolean isNumber() {
        return getType().equals(JsonType.NUMBER);
    }

    public boolean isObject() {
        return getType().equals(JsonType.OBJECT);
    }

    public boolean isNull() {
        return getType().equals(JsonType.NULL);
    }

    public boolean isString() {
        return getType().equals(JsonType.STRING);
    }

    static String toString(Json json) {
        Set<Json> processedSet = Collections.newSetFromMap(new IdentityHashMap<>());
        LinkedList<String> tokens = new LinkedList<>();
        Stack<List<Object>> stack = new Stack<>();

        processedSet.add(json);
        stack.push(json.tokenize());

        while (!stack.isEmpty()) {
            List<Object> list = stack.peek();
            while (!list.isEmpty()) {
                Object item = list.get(0);

                if (item instanceof Json && ((Json) item).isComplex()) {
                    list.remove(0);
                    if (processedSet.contains(item)) {
                        throw new IllegalStateException("Loop found");
                    }
                    processedSet.add(json);
                    stack.push(((Json) item).tokenize());
                    break;
                }

                tokens.add(list.remove(0).toString());
            }
            if (list.isEmpty()) {
                stack.pop();
            }
        }

        return tokens.parallelStream()
                .collect(Collectors.joining());
    }

    InternalType getInternalType() {
        JsonType type = getType();
        return type == JsonType.OBJECT || type == JsonType.ARRAY ? COMPLEX : PRIMITIVE;
    }

    boolean isPrimitive() {
        return getInternalType() == PRIMITIVE;
    }

    boolean isComplex() {
        return getInternalType() == COMPLEX;
    }

    List<Object> tokenize() {
        if (getInternalType() == COMPLEX) {
            throw new UnsupportedOperationException();
        }
        return Collections.singletonList(this);
    }

    @Override
    abstract public Json clone();

    enum InternalType {
        COMPLEX, PRIMITIVE
    }

    static class Marker {

        final String name;

        Marker(Supplier<String> supplier) {
            name = supplier.get();
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public Json clone() {
            throw new UnsupportedOperationException();
        }
    }
}
