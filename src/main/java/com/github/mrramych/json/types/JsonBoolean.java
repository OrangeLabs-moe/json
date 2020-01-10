package com.github.mrramych.json.types;

import com.github.mrramych.json.Json;
import com.github.mrramych.json.JsonCastException;
import com.github.mrramych.json.JsonType;

public final class JsonBoolean implements Json {

    public final boolean value;

    public JsonBoolean(boolean value) {
        this.value = value;
    }

    @Override
    public JsonType getType() {
        return JsonType.BOOLEAN;
    }

    @Override
    public JsonBoolean getAsBoolean() throws JsonCastException {
        return this;
    }

    @Override
    public String toString() {
        if (value) {
            return "true";
        } else {
            return "false";
        }
    }

    @Override
    public JsonBoolean clone() {
        return this;
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(value);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof JsonBoolean) && (value == ((JsonBoolean) obj).value);
    }
}
