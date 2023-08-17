package com.github.ram042.json;

import com.github.ram042.json.exceptions.ParseException;

import java.nio.Buffer;
import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.Stack;

import static com.github.ram042.json.Json.*;
import static java.util.Objects.requireNonNull;

final class JsonParser {

    private static final String escapedCharacters = "\"\\/bfnrt";
    /**
     * Chars from escapedCharacters string are replaced with chars from this string
     */
    private static final String escapedSequence = "\"\\/\b\f\n\r\t";
    private static final char[] STRING_PREFIX = "\"".toCharArray();
    private static final char[] TRUE_PREFIX = "true".toCharArray();
    private static final char[] FALSE_PREFIX = "false".toCharArray();
    private static final char[] NULL_PREFIX = "null".toCharArray();
    private static final String NUMBER = "0123456789.-+eE";

    private JsonParser() {
    }

    static Json parse(String json) {
        requireNonNull(json);
        json = json.trim();
        LinkedList<Object> tokens = tokenize(CharBuffer.wrap(json));
        if (tokens.get(0) == OBJECT_START_TOKEN || tokens.get(0) == ARRAY_START_TOKEN) {
            return parseStructure(tokens);
        } else if (tokens.size() == 1 && tokens.get(0) instanceof Json) {
            return (Json) tokens.get(0);
        } else {
            throw new ParseException("Unexpected first token");
        }
    }

    private static LinkedList<Object> tokenize(CharBuffer buffer) {
        LinkedList<Object> tokens = new LinkedList<>();
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
                    tokens.add(OBJECT_START_TOKEN);
                    break;
                case '}':
                    buffer.get();
                    tokens.add(OBJECT_END_TOKEN);
                    break;
                case '[':
                    buffer.get();
                    tokens.add(ARRAY_START_TOKEN);
                    break;
                case ']':
                    buffer.get();
                    tokens.add(ARRAY_END_TOKEN);
                    break;
                case ':':
                    buffer.get();
                    tokens.add(KEY_VALUE_SEPARATOR_TOKEN);
                    break;
                case ',':
                    buffer.get();
                    tokens.add(ENTRY_SEPARATOR_TOKEN);
                    break;
                default:
                    tokens.add(fromStringSimple(buffer));
                    break;
            }
        }

        return tokens;
    }

    private static Json parseStructure(LinkedList<Object> tokens) {
        final Stack<Json> stack = new Stack<>();
//        stack.ensureCapacity(checkOpenCloseTokens(tokens));
        Json root;

        Object firstToken = tokens.removeFirst();
        if (firstToken == OBJECT_START_TOKEN) {
            root = stack.push(new JsonObject());
        } else if (firstToken == ARRAY_START_TOKEN) {
            root = stack.push(new JsonArray());
        } else {
            throw new ParseException("Expected structure");
        }

        while (!tokens.isEmpty()) {
            Json current = stack.peek();
            if (current.isArray()) {
                collectArray(stack, tokens);
            } else if (current.isObject()) {
                collectObject(stack, tokens);
            } else {
                throw new ParseException("Expected structure");
            }
        }
        if (!stack.isEmpty()) {
            throw new ParseException("Last element not closed");
        }

        return root;
    }

    private static void collectArray(Stack<Json> stack, LinkedList<Object> tokens) {
        JsonArray array = stack.peek().getAsArray();
        while (!tokens.isEmpty()) {
            Object next = tokens.removeFirst();
            if (next instanceof Json) {
                array.add((Json) next);
                next = tokens.getFirst();
                if (next == ENTRY_SEPARATOR_TOKEN) {
                    tokens.removeFirst();
                } else if (next != ARRAY_END_TOKEN) {
                    throw new IllegalArgumentException("Unexpected token");
                }
            } else if (next == ARRAY_END_TOKEN) {
                stack.pop();
                if (!stack.isEmpty()) {
                    next = tokens.getFirst();
                    if (next == ENTRY_SEPARATOR_TOKEN) {
                        tokens.removeFirst();
                    }
                }
                return;
            } else if (next == ARRAY_START_TOKEN) {
                JsonArray newArray = new JsonArray();
                array.add(stack.push(newArray));
                return;
            } else if (next == OBJECT_START_TOKEN) {
                JsonObject newObject = new JsonObject();
                array.add(stack.push(newObject));
                return;
            } else {
                throw new IllegalArgumentException("Unexpected token");
            }
        }
    }

    private static void collectObject(Stack<Json> stack, LinkedList<Object> tokens) {
        JsonObject object = stack.peek().getAsObject();
        while (!tokens.isEmpty()) {
            Object next = tokens.removeFirst();
            if (next instanceof JsonString) {
                JsonString key = ((JsonString) next).getAsString();
                if (tokens.removeFirst() != KEY_VALUE_SEPARATOR_TOKEN) {
                    throw new ParseException("Unexpected token");
                }

                next = tokens.removeFirst();
                if (next instanceof Json) {
                    Json value = (Json) next;
                    object.put(key, value);

                    next = tokens.getFirst();
                    if (next == ENTRY_SEPARATOR_TOKEN) {
                        tokens.removeFirst();
                    } else if (next != OBJECT_END_TOKEN) {
                        throw new IllegalArgumentException("Unexpected token");
                    }
                } else if (next == ARRAY_START_TOKEN) {
                    JsonArray value = new JsonArray();
                    stack.push(value);
                    object.put(key, value);
                    return;
                } else if (next == OBJECT_START_TOKEN) {
                    JsonObject value = new JsonObject();
                    stack.push(value);
                    object.put(key, value);
                    return;
                } else {
                    throw new ParseException("Unexpected token");
                }
            } else if (next == OBJECT_END_TOKEN) {
                stack.pop();
                if (!stack.isEmpty()) {
                    next = tokens.getFirst();
                    if (next == ENTRY_SEPARATOR_TOKEN) {
                        tokens.removeFirst();
                    }
                }
                return;
            } else {
                throw new IllegalArgumentException("Unexpected token");
            }
        }
    }

    private static boolean charBufferStartsWith(CharBuffer buffer, char[] chars) {
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
        if (charBufferStartsWith(buffer, STRING_PREFIX)) {
            return parseString(buffer);
        } else if (charBufferStartsWith(buffer, TRUE_PREFIX)) {
            skipBuffer(buffer, 4);
            return JsonBoolean.TRUE;
        } else if (charBufferStartsWith(buffer, FALSE_PREFIX)) {
            skipBuffer(buffer, 5);
            return JsonBoolean.FALSE;
        } else if (charBufferStartsWith(buffer, NULL_PREFIX)) {
            skipBuffer(buffer, 4);
            return JsonNull.NULL;
        } else {
            return parseNumber(buffer);
        }
    }

    private static Json parseNumber(CharBuffer buffer) {
        StringBuilder builder = new StringBuilder();
        boolean process = true;
        while (process && buffer.remaining() > 0) {
            char nextChar = buffer.get();
            if (NUMBER.indexOf(nextChar) >= 0) {
                builder.append(nextChar);
            } else {
                skipBuffer(buffer, -1);
                process = false;
            }
        }
        return new JsonNumber(builder.toString());
    }

    private static JsonString parseString(CharBuffer buffer) {
        skipBuffer(buffer, 1);
        StringBuilder builder = new StringBuilder();
        boolean process = true;
        while (process && buffer.remaining() > 0) {
            char thisChar = buffer.get();
            if (thisChar <= 0x1f) {
                throw new ParseException("Unexpected escape character");
            } else if (thisChar == '\\') {
                char nextChar = buffer.get();
                if (escapedCharacters.indexOf(nextChar) >= 0) {
                    builder.append(escapedSequence.charAt(escapedCharacters.indexOf(nextChar)));
                } else if (nextChar == 'u') {
                    char[] hex = new char[4];
                    buffer.get(hex);
                    builder.append((char) Integer.parseInt(new String(hex), 16));
                } else {
                    throw new ParseException("Unexpected escape character");
                }
            } else if (thisChar == '"') {
                process = false;
            } else {
                builder.append(thisChar);
            }
        }
        return new JsonString(builder.toString());
    }

    private static void skipBuffer(Buffer buffer, int count) {
        buffer.position(buffer.position() + count);
    }

}
