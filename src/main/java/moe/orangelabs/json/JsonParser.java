package moe.orangelabs.json;

import moe.orangelabs.json.types.JsonArray;
import moe.orangelabs.json.types.JsonNumber;
import moe.orangelabs.json.types.JsonObject;
import moe.orangelabs.json.types.JsonString;

import java.nio.Buffer;
import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.base.Preconditions.checkNotNull;

class JsonParser {

    private static final Marker mapStart = new Marker(() -> "MAP_START");
    private static final Marker mapEnd = new Marker(() -> "MAP_END");
    private static final Marker arrayStart = new Marker(() -> "ARRAY_START");
    private static final Marker arrayEnd = new Marker(() -> "ARRAY_END");
    private static final Marker keySeparator = new Marker(() -> "KEY_SEPARATOR");
    private static final Marker pairSeparator = new Marker(() -> "PAIR_SEPARATOR");
    private static final String escapedCharacters = "\"\\/bfnrt";
    /**
     * Chars from escapedCharacters string are replaced with chars from this string
     */
    private static final String escapedSequence = "\"\\/\b\f\n\r\t";

    private JsonParser() {
    }

    public static Json decode(String data) {
        checkNotNull(data);
        data = data.trim();
        List<Object> tokens = tokenize(CharBuffer.wrap(data));
        if (tokens.get(0) == mapStart || tokens.get(0) == arrayStart) {
            return parseStructure(tokens);
        } else if (tokens.size() == 1 && tokens.get(0) instanceof Json) {
            return (Json) tokens.get(0);
        } else {
            throw new ParseException("Unexpected first token");
        }

    }

    private static List<Object> tokenize(CharBuffer buffer) {
        List<Object> tokens = new LinkedList<>();
        while (buffer.hasRemaining()) {
            char ch = buffer.get(buffer.position());
            switch (ch) {
                case ' ':
                case '\n':
                case '\r':
                case '\t':
                    skipBuffer(buffer, 1);
                    break;
                case '{':
                    buffer.get();
                    tokens.add(mapStart);
                    break;
                case '}':
                    buffer.get();
                    tokens.add(mapEnd);
                    break;
                case '[':
                    buffer.get();
                    tokens.add(arrayStart);
                    break;
                case ']':
                    buffer.get();
                    tokens.add(arrayEnd);
                    break;
                case ':':
                    buffer.get();
                    tokens.add(keySeparator);
                    break;
                case ',':
                    buffer.get();
                    tokens.add(pairSeparator);
                    break;
                default:
                    tokens.add(fromStringSimple(buffer));
                    break;
            }
        }

        return tokens;
    }

    private static void checkOpenCloseTokenCount(List<Object> tokens) {
        AtomicInteger mapStartsCount = new AtomicInteger();
        AtomicInteger mapEndsCount = new AtomicInteger();
        AtomicInteger arrayStartsCount = new AtomicInteger();
        AtomicInteger arrayEndsCount = new AtomicInteger();

        tokens.forEach(
                o -> {
                    if (o == mapStart) {
                        mapStartsCount.getAndIncrement();
                    }
                    if (o == mapEnd) {
                        mapEndsCount.getAndIncrement();
                    }
                    if (o == arrayStart) {
                        arrayStartsCount.getAndIncrement();
                    }
                    if (o == arrayEnd) {
                        arrayEndsCount.getAndIncrement();
                    }
                });
        if (mapStartsCount.get() != mapEndsCount.get() || arrayStartsCount.get() != arrayEndsCount.get()) {
            throw new ParseException("Unequal count of open/close brackets");
        }
    }

    private static Json parseStructure(List<Object> tokens) {
        checkOpenCloseTokenCount(tokens);

        //while we has not combined top level structure
        while (tokens.get(0) == mapStart || tokens.get(0) == arrayStart) {
            //find first deepest array/object
            int start = -1;
            int end = -1;
            for (int i = 0; i < tokens.size(); i++) {
                if (tokens.get(i) == mapStart || tokens.get(i) == arrayStart) {
                    start = i;
                }

                if (tokens.get(i) == mapEnd || tokens.get(i) == arrayEnd) {
                    end = i;
                }

                if (start != -1 && end != -1 && end > start) {
                    if (tokens.get(start) == mapStart && tokens.get(end) == arrayEnd) {
                        throw new ParseException("Unaligned close/open tokens");
                    }
                    if (tokens.get(start) == arrayStart && tokens.get(end) == mapEnd) {
                        throw new ParseException("Unaligned close/open tokens");
                    }


                    if (tokens.get(start) == mapStart) {
                        List<Object> mapTokens = tokens.subList(start, end + 1);
                        mapTokens.remove(0);
                        mapTokens.remove(mapTokens.size() - 1);

                        tokens.add(start, populateObject(mapTokens));
                    } else {
                        List<Object> arrayTokens = tokens.subList(start, end + 1);
                        arrayTokens.remove(0);
                        arrayTokens.remove(arrayTokens.size() - 1);

                        tokens.add(start, populateArray(arrayTokens));
                    }

                    start = -1;
                    end = -1;
                }

            }
        }

        if (tokens.size() > 1) {
            throw new ParseException("Expected only one structure");
        }

        return (Json) tokens.get(0);
    }

    /**
     * @param objectTokens items and separators to add to object. Does not include '{' and '}' symbols.
     */
    private static JsonObject populateObject(List<Object> objectTokens) {
        JsonObject object = new JsonObject();

        if (objectTokens.size() == 0) {
            return object;
        }

        if (objectTokens.size() % 2 == 0) {
            throw new ParseException("Number of elements in object must be odd number or 0");
        }

        if (!((objectTokens.size() == 3) || ((objectTokens.size() - 3) % 4 == 0))) {
            throw new ParseException("Number of elements in array must be odd number");
        }

        for (int i = 0; i <= objectTokens.size() / 4; i++) {
            JsonString key;
            if (objectTokens.get(i * 4) instanceof JsonString) {
                key = (JsonString) objectTokens.get(i * 4);
            } else {
                throw new ParseException("Object key must be string");
            }

            if (objectTokens.get(i * 4 + 1) != keySeparator) {
                throw new ParseException("Key separator was expected");
            }

            Json value;
            if (objectTokens.get(0) instanceof Json) {
                value = ((Json) objectTokens.get(i * 4 + 2));
            } else {
                throw new ParseException("Expected json value");
            }

            if (!(i == (objectTokens.size() / 4) || objectTokens.get(i * 4 + 3) == pairSeparator)) {
                throw new ParseException("Separator expected");
            }

            object.put(key, value);
        }

        objectTokens.clear();
        return object;
    }

    /**
     * @param arrayTokens items and separators to add to array. Does not include '[' and ']' symbols.
     */
    private static JsonArray populateArray(List<Object> arrayTokens) {
        JsonArray array = new JsonArray();

        if (arrayTokens.size() == 0) {
            return array;
        }

        if (arrayTokens.size() % 2 == 0) {
            throw new ParseException("Number of elements in array must be odd number or 0");
        }

        for (int i = 0; i <= arrayTokens.size() / 2; i++) {
            if (arrayTokens.get(i * 2) instanceof Json) {
                array.add((Json) arrayTokens.get(i * 2));
            } else {
                throw new ParseException("Expected json value");
            }

            if (!(i == (arrayTokens.size() / 2) || arrayTokens.get(i * 2 + 1) == pairSeparator)) {
                throw new ParseException("Separator expected");
            }
        }

        arrayTokens.clear();
        return array;
    }

    private static boolean charBufferStartsWith(CharBuffer buffer, String data) {
        char[] chars = data.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (buffer.get(buffer.position() + i) != chars[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return Json, chars to skip
     */
    private static Json fromStringSimple(CharBuffer buffer) {
        if (charBufferStartsWith(buffer, "\"")) {
            return parseString(buffer);
        } else if (charBufferStartsWith(buffer, "true")) {
            skipBuffer(buffer, 4);
            return Json.TRUE;
        } else if (charBufferStartsWith(buffer, "false")) {
            skipBuffer(buffer, 5);
            return Json.FALSE;
        } else if (charBufferStartsWith(buffer, "null")) {
            skipBuffer(buffer, 4);
            return Json.NULL;
        } else {
            return parseNumber(buffer);
        }
    }

    private static Json parseNumber(CharBuffer buffer) {
        int i = 0;

        while (((buffer.position() + i) < buffer.capacity())
                && "+-0123456789.eE".indexOf(buffer.get(buffer.position() + i)) >= 0) {
            i++;
        }

        if (i == 0) {
            throw new ParseException("Could not parse number");
        }
        String number = buffer.toString().substring(0, i);
        skipBuffer(buffer, i);

        return new JsonNumber(validateNumber(number));
    }

    private static String validateNumber(String number) {
        //https://stackoverflow.com/questions/13340717/json-numbers-regular-expression

        if (!number.matches("-?(?:0|[1-9]\\d*)(?:\\.\\d+)?(?:[eE][+-]?\\d+)?")) {
            throw new NumberFormatException();
        }

        return number;
    }

    private static JsonString parseString(CharBuffer buffer) {
        skipBuffer(buffer, 1);
        int i = 0;
        while (true) {
            if ((buffer.get(buffer.position() + i) == '"') && (buffer.get(buffer.position() + i - 1) != '\\')) {
                break;
            } else {
                i++;
            }
        }
        JsonString result = new JsonString(unescapeString(buffer.toString().substring(0, i)));
        skipBuffer(buffer, i + 1);
        return result;
    }

    private static String unescapeString(String input) {
        StringBuilder out = new StringBuilder();
        char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if ((int) chars[i] <= 0x1f) {
                throw new ParseException("Unexpected control character");
            }
            if (chars[i] == '\\') {
                if (escapedCharacters.indexOf(chars[i + 1]) >= 0) {
                    out.append(escapedSequence.charAt(escapedCharacters.indexOf(chars[i + 1])));
                    i++;
                } else if (chars[i + 1] == 'u') {
                    char first = (char) Integer.parseInt(input.substring(i + 2, i + 2 + 4), 16);

                    if (chars.length - i - 6 >= 6) {
                        if (chars[i + 6] == '\\' && chars[i + 7] == 'u') {
                            char second = (char) Integer.parseInt(input.substring(i + 2 + 6, i + 2 + 4 + 6), 16);

                            if (Character.isSurrogatePair(first, second)) {
                                out.append(Character.toChars(Character.toCodePoint(first, second)));
                                i += 11;
                            }
                        }
                    } else {
                        out.append(Character.toChars(first));
                        i += 5;
                    }

                } else {
                    throw new ParseException("Unexpected backslash character");
                }
            } else {
                out.append(chars[i]);
            }
        }
        return out.toString();
    }

    private static void skipBuffer(Buffer buffer, int count) {
        buffer.position(buffer.position() + count);
    }

    @FunctionalInterface
    private interface NameProvider {
        String getName();
    }

    private static class Marker {

        private final NameProvider nameProvider;

        Marker(NameProvider nameProvider) {
            this.nameProvider = nameProvider;
        }

        @Override
        public String toString() {
            return nameProvider.getName();
        }
    }

}
