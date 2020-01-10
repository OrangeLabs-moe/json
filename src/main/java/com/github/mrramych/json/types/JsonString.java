package com.github.mrramych.json.types;

import com.github.mrramych.json.Json;
import com.github.mrramych.json.JsonCastException;
import com.github.mrramych.json.JsonType;

import static com.google.common.base.Preconditions.checkNotNull;

public final class JsonString implements Json {

    private static final String charactersToEscape = "\"\\/\b\f\n\r\t";

    public final String string;

    public JsonString(String string) {
        this.string = checkNotNull(string);
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
        StringBuilder out = new StringBuilder();
        out.append("\"");
        for (char c : string.toCharArray()) {
            if (charactersToEscape.indexOf(c) < 0) {
                out.append(c);
            } else {
                out.append("\\");
                switch (c) {
                    case '\"':
                        out.append("\"");
                        break;
                    case '\\':
                        out.append("\\");
                        break;
                    case '/':
                        out.append("/");
                        break;
                    case '\b':
                        out.append("b");
                        break;
                    case '\f':
                        out.append("f");
                        break;
                    case '\n':
                        out.append("n");
                        break;
                    case '\r':
                        out.append("r");
                        break;
                    case '\t':
                        out.append("t");
                        break;
                    default:
                        //this should never happen
                        throw new IllegalStateException("Unexecpected symbol");
                }
            }
        }
        out.append("\"");
        return out.toString();
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
