package com.github.ram042.json;

import com.github.ram042.json.exceptions.JsonCastException;

public final class JsonNull extends Json {

    public static final JsonNull NULL = new JsonNull();

    @Override
    public JsonType getType() {
        return JsonType.NULL;
    }

    @Override
    public JsonNull getAsNull() throws JsonCastException {
        return this;
    }

    @Override
    public String toString() {
        return "null";
    }

    @Override
    public JsonNull clone() {
        return this;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof JsonNull);
    }
}
