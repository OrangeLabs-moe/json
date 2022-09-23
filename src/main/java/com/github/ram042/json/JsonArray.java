package com.github.ram042.json;

import com.github.ram042.json.exceptions.JsonCastException;

import java.util.*;

import static java.util.Objects.requireNonNull;

public class JsonArray extends Json implements List<Json> {

    private final LinkedList<Json> array;

    public JsonArray(Object... objects) {
        array = new LinkedList<>();
        for (Object object : objects) {
            array.add(toJson(object));
        }
    }

    public JsonArray(List<Object> objects) {
        array = new LinkedList<>();
        objects.forEach(o -> array.add(toJson(o)));
    }

    private JsonArray(LinkedList<Json> list) {
        this.array = list;
    }

    public JsonArray(Iterator<Object> iterator) {
        array = new LinkedList<>();
        iterator.forEachRemaining(o -> array.add(toJson(o)));
    }

    public JsonArray(Iterable<Object> iterable) {
        array = new LinkedList<>();
        iterable.forEach(o -> array.add(toJson(o)));
    }

    @Override
    public int size() {
        return array.size();
    }

    @Override
    public boolean isEmpty() {
        return array.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return array.contains(o);
    }

    @Override
    public Iterator<Json> iterator() {
        return array.iterator();
    }

    @Override
    public Object[] toArray() {
        return array.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return array.toArray(a);
    }

    @Override
    public boolean add(Json json) {
        return array.add(requireNonNull(json));
    }

    @Override
    public boolean remove(Object o) {
        return array.remove(toJson(o));
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return array.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Json> c) {
        c.forEach(Objects::requireNonNull);
        return array.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Json> c) {
        c.forEach(Objects::requireNonNull);
        return array.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return array.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return array.retainAll(c);
    }

    @Override
    public void clear() {
        array.clear();
    }

    @Override
    public Json get(int index) {
        return array.get(index);
    }

    public JsonObject getObject(int index) {
        return array.get(index).getAsObject();
    }

    public JsonArray getArray(int index) {
        return array.get(index).getAsArray();
    }

    public JsonString getString(int index) {
        return array.get(index).getAsString();
    }

    public JsonNumber getNumber(int index) {
        return array.get(index).getAsNumber();
    }

    public JsonBoolean getBoolean(int index) {
        return array.get(index).getAsBoolean();
    }

    @Override
    public Json set(int index, Json element) {
        return array.set(index, requireNonNull(element));
    }

    @Override
    public void add(int index, Json element) {
        array.add(index, requireNonNull(element));
    }

    @Override
    public Json remove(int index) {
        return array.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return array.indexOf(toJson(o));
    }

    @Override
    public int lastIndexOf(Object o) {
        return array.lastIndexOf(toJson(o));
    }

    @Override
    public ListIterator<Json> listIterator() {
        return array.listIterator();
    }

    @Override
    public ListIterator<Json> listIterator(int index) {
        return array.listIterator(index);
    }

    @Override
    public JsonArray subList(int fromIndex, int toIndex) {
        return new JsonArray(array.subList(fromIndex, toIndex));
    }

    @Override
    public JsonArray getAsArray() throws JsonCastException {
        return this;
    }

    @Override
    public JsonType getType() {
        return JsonType.ARRAY;
    }

    @Override
    public Json clone() {
        return new JsonArray(array);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonArray array1 = (JsonArray) o;
        return Objects.equals(array, array1.array);
    }

    @Override
    public int hashCode() {
        return array.hashCode();
    }

    @Override
    List<Object> deepStringTokenize() {
        List<Object> tokens = new LinkedList<>();
        tokens.add(ARRAY_START_TOKEN);
        for (int i = 0; i < array.size(); i++) {
            tokens.add(array.get(i));
            if (i < array.size() - 1) {
                tokens.add(ENTRY_SEPARATOR_TOKEN);
            } else {
                tokens.add(ARRAY_END_TOKEN);
            }

        }
        if (array.size() == 0) {
            tokens.add(ARRAY_END_TOKEN);
        }
        return tokens;
    }

    @Override
    public String toString() {
        return Json.toString(deepStringTokenize());
    }
}
