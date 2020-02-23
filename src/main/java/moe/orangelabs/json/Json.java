package moe.orangelabs.json;

import moe.orangelabs.json.exceptions.JsonCastException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import static moe.orangelabs.json.Json.InternalType.COMPLEX;
import static moe.orangelabs.json.Json.InternalType.PRIMITIVE;
import static moe.orangelabs.json.JsonType.ARRAY;
import static moe.orangelabs.json.JsonType.OBJECT;

public abstract class Json implements Cloneable {

    static final Marker ENTRY_SEPARATOR_TOKEN = new Marker(() -> ",");
    static final Marker KEY_VALUE_SEPARATOR_TOKEN = new Marker(() -> ":");
    static final Marker OBJECT_END_TOKEN = new Marker(() -> "}");
    static final Marker OBJECT_START_TOKEN = new Marker(() -> "{");
    static final Marker ARRAY_END_TOKEN = new Marker(() -> "]");
    static final Marker ARRAY_START_TOKEN = new Marker(() -> "[");

    public static Json parse(String data) {
        return JsonParser.decode(data);
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

    static String toString(List<Object> tokens) {
        Stack<List<Object>> stack = new Stack<>();
        stack.push(tokens);
        StringBuilder builder = new StringBuilder();
        while (stack.size() > 0) {
            List<Object> list = stack.peek();
            while (list.size() > 0) {
                Object item = list.get(0);

                if (item instanceof Json && ((Json) item).isComplex()) {
                    list.remove(0);
                    stack.push(((Json) item).deepStringTokenize());
                    break;
                }

                builder.append(list.remove(0));
            }
            if (list.size() == 0) {
                stack.pop();
            }
        }

        return builder.toString();
    }

    InternalType getInternalType() {
        JsonType type = getType();
        return type == OBJECT || type == ARRAY ? COMPLEX : PRIMITIVE;
    }

    boolean isPrimitive() {
        return getInternalType() == PRIMITIVE;
    }

    boolean isComplex() {
        return getInternalType() == COMPLEX;
    }

    List<Object> deepStringTokenize() {
        if (getInternalType() == COMPLEX) {
            throw new UnsupportedOperationException();
        }
        return Collections.singletonList(this);
    }

    @Override
    abstract public Json clone();

    /**
     * Because toString() does not use recursion, object with loop will execute forever.
     * Use this with to avoid such problems
     */
    public Future<String> toStringAsync() {
        return CompletableFuture.supplyAsync(this::toString);
    }

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
