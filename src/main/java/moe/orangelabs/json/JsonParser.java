package moe.orangelabs.json;

import moe.orangelabs.json.types.JsonArray;
import moe.orangelabs.json.types.JsonNumber;
import moe.orangelabs.json.types.JsonObject;
import moe.orangelabs.json.types.JsonString;

import java.nio.Buffer;
import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import static java.util.Objects.requireNonNull;

final class JsonParser {

    private static final Marker objectStart = new Marker(() -> "OBJECT_START");
    private static final Marker objectEnd = new Marker(() -> "OBJECT_END");
    private static final Marker arrayStart = new Marker(() -> "ARRAY_START");
    private static final Marker arrayEnd = new Marker(() -> "ARRAY_END");
    /**
     * ':'
     */
    private static final Marker keyValueSeparator = new Marker(() -> "KEY_VALUE_SEPARATOR");
    /**
     * ','
     */
    private static final Marker separator = new Marker(() -> "SEPARATOR");
    private static final String escapedCharacters = "\"\\/bfnrt";
    /**
     * Chars from escapedCharacters string are replaced with chars from this string
     */
    private static final String escapedSequence = "\"\\/\b\f\n\r\t";

    private JsonParser() {
    }

    public static Json decode(String data) {
        requireNonNull(data);
        data = data.trim();
        List<Object> tokens = tokenize(CharBuffer.wrap(data));
        if (tokens.get(0) == objectStart || tokens.get(0) == arrayStart) {
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
                    tokens.add(objectStart);
                    break;
                case '}':
                    buffer.get();
                    tokens.add(objectEnd);
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
                    tokens.add(keyValueSeparator);
                    break;
                case ',':
                    buffer.get();
                    tokens.add(separator);
                    break;
                default:
                    tokens.add(fromStringSimple(buffer));
                    break;
            }
        }

        return tokens;
    }

    private static void checkOpenCloseTokens(List<Object> tokens) {
        LinkedList<Object> structTokens = new LinkedList<>();

        if (tokens.get(0) == objectStart || tokens.get(0) == arrayStart) {
            structTokens.add(tokens.get(0));
        } else {
            throw new ParseException("Expected array start or object start");
        }

        for (int i = 1, tokensSize = tokens.size(); i < tokensSize; i++) {
            Object o = tokens.get(i);
            if (o == objectStart || o == arrayStart) {
                if (structTokens.size() == 0) {
                    throw new ParseException("Expected exactly one structure");
                }
                structTokens.add(o);
            }
            if (o == objectEnd) {
                if (structTokens.getLast() == objectStart) {
                    structTokens.removeLast();
                } else {
                    throw new ParseException("Unaligned object");
                }
            }
            if (o == arrayEnd) {
                if (structTokens.getLast() == arrayStart) {
                    structTokens.removeLast();
                } else {
                    throw new ParseException("Unaligned object");
                }
            }
            if (structTokens.size() == 0) {
                if (i < tokensSize - 1) {
                    throw new ParseException("Extra tokens");
                }
                return;
            }
        }
        if (structTokens.size() > 0) {
            throw new ParseException("Unaligned structures");
        }
    }

    private static Json parseStructure(List<Object> tokens) {
        checkOpenCloseTokens(tokens);

        final Stack<Json> stack = new Stack<>();

        Json root;

        if (tokens.get(0) == objectStart) {
            root = stack.push(new JsonObject());
            tokens.remove(0);
        } else if (tokens.get(0) == arrayStart) {
            root = stack.push(new JsonArray());
            tokens.remove(0);
        } else {
            throw new ParseException("Expected structure");
        }

        while (tokens.size() > 0) {
            Json last = stack.peek();
            if (last.isArray()) {
                JsonArray lastArray = last.getAsArray();
                Object first = tokens.remove(0);

                if (first instanceof Json) {
                    lastArray.add((Json) first);

                    if (tokens.size() == 0) {
                        throw new ParseException("Unexpected end");
                    }
                    Object nextToken = tokens.get(0);
                    if (nextToken == separator) {
                        if (tokens.size() < 2) {
                            throw new ParseException("Expected more tokens");
                        }
                        Object nextNextToken = tokens.get(1);
                        if (!(nextNextToken instanceof Json || nextNextToken == objectStart || nextNextToken == arrayStart)) {
                            throw new ParseException("Unexpected value");
                        }
                        tokens.remove(0);
                    } else if (nextToken != arrayEnd) {
                        throw new ParseException("Unexpected token");
                    }

                } else if (first == arrayStart) {
                    lastArray.add(stack.push(new JsonArray()));
                } else if (first == objectStart) {
                    lastArray.add(stack.push(new JsonObject()));
                } else if (first == arrayEnd) {
                    stack.pop();
                    if (tokens.size() > 0 && tokens.get(0) == separator) {
                        tokens.remove(0);
                    }
                } else {
                    throw new ParseException("Expected json/arrayEnd/arrayStart/object/start");
                }
            } else if (last.isObject()) {
                JsonObject lastObject = last.getAsObject();
                Object first = tokens.remove(0);

                if (first instanceof Json) {
                    if (tokens.size() < 3) {
                        throw new ParseException("Expected more tokens");
                    }

                    JsonString key;
                    if (((Json) first).isString()) {
                        key = (JsonString) first;
                    } else {
                        throw new ParseException("Key must be string");
                    }

                    if (tokens.remove(0) != keyValueSeparator) {
                        throw new ParseException("Expected ':'");
                    }

                    Object value = tokens.remove(0);
                    if (value instanceof Json) {
                        lastObject.put(key, (Json) value);

                        if (tokens.size() == 0) {
                            throw new ParseException("Unexpected end");
                        }
                        Object nextToken = tokens.get(0);
                        if (nextToken == separator) {
                            if (tokens.size() < 2) {
                                throw new ParseException("Expected more tokens");
                            }
                            Object nextNextToken = tokens.get(1);
                            if (!(nextNextToken instanceof Json || nextNextToken == objectStart || nextNextToken == arrayStart)) {
                                throw new ParseException("Unexpected value");
                            }
                            tokens.remove(0);
                        } else if (nextToken != objectEnd) {
                            throw new ParseException("Unexpected token");
                        }

                    } else if (value == objectStart) {
                        lastObject.put(key, stack.push(new JsonObject()));
                    } else if (value == arrayStart) {
                        lastObject.put(key, stack.push(new JsonArray()));
                    } else {
                        throw new ParseException("Expected value");
                    }
                } else if (first == objectEnd) {
                    stack.pop();
                    if (tokens.size() > 0 && tokens.get(0) == separator) {
                        tokens.remove(0);
                    }
                } else {
                    throw new ParseException("Unexpected token");
                }
            } else {
                throw new ParseException("Expected structure");
            }
        }

        return root;
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

            if (objectTokens.get(i * 4 + 1) != keyValueSeparator) {
                throw new ParseException("Key separator was expected");
            }

            Json value;
            if (objectTokens.get(0) instanceof Json) {
                value = ((Json) objectTokens.get(i * 4 + 2));
            } else {
                throw new ParseException("Expected json value");
            }

            if (!(i == (objectTokens.size() / 4) || objectTokens.get(i * 4 + 3) == separator)) {
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

            if (!(i == (arrayTokens.size() / 2) || arrayTokens.get(i * 2 + 1) == separator)) {
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
