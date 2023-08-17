package com.github.ram042.json;

import com.github.ram042.json.exceptions.JsonCastException;

import static java.util.Objects.requireNonNull;

public final class JsonString extends Json {

    private static final String charactersToEscape = "\"\\/\b\f\n\r\t";
    private static final String escaped = "\"\\/bfnrt";

    public final String string;

    public JsonString(String string) {
        this.string = requireNonNull(string);
    }

    @Override
    public JsonType getType() {
        return JsonType.STRING;
    }

    @Override
    public JsonString getAsString() throws JsonCastException {
        return this;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("\"");
        for (char thisChar : string.toCharArray()) {
            int index;
            if ((index = charactersToEscape.indexOf(thisChar)) >= 0 || thisChar <= 0x1f) {
                if (index >= 0) {
                    out.append('\\');
                    out.append(escaped.charAt(index));
                } else {
                    out.append("\\u00");
                    out.append(toHex(thisChar / 16));
                    out.append(toHex(thisChar % 16));
                }
            } else {
                out.append(thisChar);
            }
        }
        out.append('\"');
        return out.toString();
    }

    private static char toHex(int i) {
        if (i < 10) {
            return (char) ('0' + i);
        } else {
            return (char) ('a' + i - 10);
        }
    }

    @Override
    public JsonString clone() {
        return this;
    }

    @Override
    public int hashCode() {
        return string.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof JsonString) && (string.equals(((JsonString) obj).string));
    }
}
