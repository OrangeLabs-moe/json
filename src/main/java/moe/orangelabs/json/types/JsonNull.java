package moe.orangelabs.json.types;

import moe.orangelabs.json.Json;
import moe.orangelabs.json.JsonCastException;
import moe.orangelabs.json.JsonType;

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
