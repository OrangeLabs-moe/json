package com.github.mrramych.json.types;

import com.github.mrramych.json.Json;
import com.github.mrramych.json.JsonCastException;
import com.github.mrramych.json.JsonType;

public final class JsonNull implements Json {

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
