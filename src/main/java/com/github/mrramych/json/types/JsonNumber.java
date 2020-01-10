package com.github.mrramych.json.types;

import com.github.mrramych.json.Json;
import com.github.mrramych.json.JsonCastException;
import com.github.mrramych.json.JsonType;

import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkNotNull;

public final class JsonNumber implements Comparable<JsonNumber>, Json {

    public final BigDecimal value;

    public JsonNumber(double value) {
        this(BigDecimal.valueOf(value));
    }

    public JsonNumber(long value) {
        this(BigDecimal.valueOf(value));
    }

    public JsonNumber(BigDecimal value) {
        checkNotNull(value);
        this.value = value;
    }

    public JsonNumber(String value) {
        this.value = new BigDecimal(value);
    }

    @Override
    public JsonType getType() {
        return JsonType.NUMBER;
    }

    @Override
    public JsonNumber getAsNumber() throws JsonCastException {
        return this;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public JsonNumber clone() {
        return this;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof JsonNumber) && (value.equals(((JsonNumber) obj).value));
    }

    @Override
    public int compareTo(JsonNumber o) {
        return value.compareTo(checkNotNull(o).value);
    }

    public JsonNumber add(JsonNumber value) {
        checkNotNull(value);
        return new JsonNumber(this.value.add(value.value));
    }

    public JsonNumber substract(JsonNumber value) {
        checkNotNull(value);
        return new JsonNumber(this.value.subtract(value.value));
    }
}
