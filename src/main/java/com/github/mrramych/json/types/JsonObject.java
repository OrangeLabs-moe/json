package com.github.mrramych.json.types;

import com.github.mrramych.json.Json;
import com.github.mrramych.json.JsonCastException;
import com.github.mrramych.json.JsonNotFoundException;
import com.github.mrramych.json.JsonType;
import org.apache.commons.collections4.map.UnmodifiableEntrySet;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.mrramych.json.Json.toJson;
import static com.github.mrramych.json.JsonType.OBJECT;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class JsonObject implements Map<JsonString, Json>, Json {

    private static final Object MAP_START_TOKEN = new Object();
    private static final Object MAP_END_TOKEN = new Object();
    private static final Object KEY_VALUE_SEPARATOR_TOKEN = new Object();
    private static final Object ENTRY_SEPARATOR_TOKEN = new Object();

    private final Map<JsonString, Json> map = new HashMap<>();

    public JsonObject() {
    }

    public JsonObject(Map<Object, Object> data) {
        checkNotNull(data);
        data.forEach(this::castAndPut);
    }

    public JsonObject(Iterable<Object> data) {
        checkNotNull(data);
        AtomicInteger counter = new AtomicInteger(0);
        data.forEach(o -> castAndPut(counter.getAndIncrement(), o));
    }

    public JsonObject(Object... keysAndValues) {
        checkArgument(keysAndValues.length % 2 == 0);
        for (int i = 0; i < keysAndValues.length / 2; i++) {
            castAndPut(keysAndValues[i * 2], keysAndValues[i * 2 + 1]);
        }
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) throws IllegalArgumentException {
        return map.containsKey(toJson(key).getAsString());
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsKey(toJson(value));
    }

    @Override
    public Json get(Object key) throws JsonNotFoundException, IllegalArgumentException {
        Json result = map.get(toJson(key).getAsString());
        if (result == null) {
            throw new JsonNotFoundException();
        }
        return result;
    }

    public Json put(JsonString key, Json value) {
        checkNotNull(key);
        checkNotNull(value);
        return map.put(key, value);
    }

    @Override
    public Json remove(Object key) {
        return map.remove(toJson(key).getAsString());
    }

    @Override
    public void putAll(Map<? extends JsonString, ? extends Json> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<JsonString> keySet() {
        return Collections.unmodifiableSet(map.keySet());
    }

    @Override
    public Collection<Json> values() {
        return Collections.unmodifiableCollection(map.values());
    }

    @Override
    public Set<Entry<JsonString, Json>> entrySet() {
        return UnmodifiableEntrySet.unmodifiableEntrySet(map.entrySet());
    }

    public Json castAndPut(Object key, Object value) {
        return put(toJson(key).getAsString(), toJson(value));
    }

    @Override
    public JsonType getType() {
        return OBJECT;
    }

    @Override
    public JsonObject getAsObject() throws JsonCastException {
        return this;
    }

    public List<Object> deepStringTokenize() {
        List<Object> tokenList = new LinkedList<>();
        tokenList.add(MAP_START_TOKEN);
        Iterator<Entry<JsonString, Json>> entryIterator = map.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Entry<JsonString, Json> entry = entryIterator.next();
            tokenList.add(entry.getKey());
            tokenList.add(KEY_VALUE_SEPARATOR_TOKEN);
            if (entry.getValue().isObject()) {
                tokenList.addAll(entry.getValue().getAsObject().deepStringTokenize());
            } else {
                tokenList.add(entry.getValue());
            }
            if (entryIterator.hasNext()) {
                tokenList.add(ENTRY_SEPARATOR_TOKEN);
            }
        }
        tokenList.add(MAP_END_TOKEN);
        return tokenList;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        deepStringTokenize()
                .forEach(
                        o -> {
                            if (o == MAP_START_TOKEN) {
                                builder.append("{");
                            } else if (o == MAP_END_TOKEN) {
                                builder.append("}");
                            } else if (o == KEY_VALUE_SEPARATOR_TOKEN) {
                                builder.append(":");
                            } else if (o == ENTRY_SEPARATOR_TOKEN) {
                                builder.append(",");
                            } else {
                                builder.append(o.toString());
                            }
                        });
        return builder.toString();
    }

    /**
     * One level clone.
     */
    @Override
    public JsonObject clone() {
        JsonObject map = new JsonObject();
        forEach(map::put);
        return map;
    }

    public JsonObject deepClone() {
        JsonObject result = new JsonObject();
        forEach(
                (key, value) -> {
                    if (value.isObject()) {
                        result.put(key, value.getAsObject().deepClone());
                    } else {
                        result.put(key, value);
                    }
                });
        return result;
    }

    public JsonObject getObject(Object key) throws JsonNotFoundException, JsonCastException {
        return get(key).getAsObject();
    }

    public JsonArray getArray(Object key) throws JsonNotFoundException, JsonCastException {
        return get(key).getAsArray();
    }

    public JsonString getString(Object key) throws JsonNotFoundException, JsonCastException {
        return get(key).getAsString();
    }

    public JsonNumber getNumber(Object key) throws JsonNotFoundException, JsonCastException {

        return get(key).getAsNumber();
    }

    public JsonBoolean getBoolean(Object key) throws JsonNotFoundException, JsonCastException {
        return get(key).getAsBoolean();
    }

    public JsonNull getNull(Object key) throws JsonNotFoundException, JsonCastException {
        return get(key).getAsNull();
    }

    @Override
    public Json getOrDefault(Object key, Json defaultValue) {
        return containsKey(key) ? get(key) : defaultValue;
    }

    public JsonObject getObjectOrDefault(Object key, Object expected) throws JsonCastException {
        return containsKey(key) ? get(key).getAsObject() : Json.toJson(expected).getAsObject();
    }

    public JsonArray getArrayOrDefault(Object key, Object expected) throws JsonCastException {
        return containsKey(key) ? get(key).getAsArray() : Json.toJson(expected).getAsArray();
    }

    public JsonString getStringOrDefault(Object key, Object expected) throws JsonCastException {
        return containsKey(key) ? get(key).getAsString() : Json.toJson(expected).getAsString();
    }

    public JsonNumber getNumberOrDefault(Object key, Object expected) throws JsonCastException {
        return containsKey(key) ? get(key).getAsNumber() : Json.toJson(expected).getAsNumber();
    }

    public JsonBoolean getBooleanOrDefault(Object key, Object expected) throws JsonCastException {
        return containsKey(key) ? get(key).getAsBoolean() : Json.toJson(expected).getAsBoolean();
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof JsonObject) && (map.equals(((JsonObject) obj).map));
    }
}
