package com.github.ram042.json;

import com.github.ram042.json.exceptions.JsonCastException;

import java.math.BigDecimal;

import static java.util.Objects.requireNonNull;

public final class JsonNumber extends Json implements Comparable<JsonNumber> {

    public final BigDecimal value;

    public JsonNumber(double value) {
        this(BigDecimal.valueOf(value));
    }

    public JsonNumber(long value) {
        this(BigDecimal.valueOf(value));
    }

    public JsonNumber(BigDecimal value) {
        this.value = requireNonNull(value);
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
        return value.compareTo(o.value);
    }

    public int intValue() {
        return value.intValue();
    }

    public float floatValue() {
        return value.floatValue();
    }

    public double doubleValue() {
        return value.doubleValue();
    }

    public byte byteValue() {
        return value.byteValue();
    }

    public short shortValue() {
        return value.shortValue();
    }

    public long longValue() {
        return value.longValue();
    }
}
