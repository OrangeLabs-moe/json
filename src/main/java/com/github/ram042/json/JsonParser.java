package com.github.ram042.json;

import com.github.ram042.json.exceptions.ParseException;

import java.nio.Buffer;
import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

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

    public static Json decode(String data) {
        requireNonNull(data);
        data = data.trim();
        LinkedList<Object> tokens = tokenize(CharBuffer.wrap(data));
        if (tokens.get(0) == Json.OBJECT_START_TOKEN || tokens.get(0) == Json.ARRAY_START_TOKEN) {
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
                    tokens.add(Json.OBJECT_START_TOKEN);
                    break;
                case '}':
                    buffer.get();
                    tokens.add(Json.OBJECT_END_TOKEN);
                    break;
                case '[':
                    buffer.get();
                    tokens.add(Json.ARRAY_START_TOKEN);
                    break;
                case ']':
                    buffer.get();
                    tokens.add(Json.ARRAY_END_TOKEN);
                    break;
                case ':':
                    buffer.get();
                    tokens.add(Json.KEY_VALUE_SEPARATOR_TOKEN);
                    break;
                case ',':
                    buffer.get();
                    tokens.add(Json.ENTRY_SEPARATOR_TOKEN);
                    break;
                default:
                    tokens.add(fromStringSimple(buffer));
                    break;
            }
        }

        return tokens;
    }

    private static int checkOpenCloseTokens(List<Object> tokens) {
        int objectStartCount = 0;
        int objectEndCount = 0;
        int arrayStartsCount = 0;
        int arrayEndsCount = 0;

        for (Object o : tokens) {
            if (o == Json.OBJECT_START_TOKEN) {
                objectStartCount++;
            }
            if (o == Json.OBJECT_END_TOKEN) {
                objectEndCount++;
            }
            if (o == Json.ARRAY_START_TOKEN) {
                arrayStartsCount++;
            }
            if (o == Json.ARRAY_END_TOKEN) {
                arrayEndsCount++;
            }
        }
        if (objectStartCount != objectEndCount || arrayStartsCount != arrayEndsCount) {
            throw new ParseException("Unequal count of open/close brackets");
        }
        return objectStartCount + arrayStartsCount;
    }

    private static Json parseStructure(LinkedList<Object> tokens) {
        final Stack<Json> stack = new Stack<>();
        stack.ensureCapacity(checkOpenCloseTokens(tokens));

        Json root;

        if (tokens.get(0) == Json.OBJECT_START_TOKEN) {
            root = stack.push(new JsonObject());
            tokens.removeFirst();
        } else if (tokens.get(0) == Json.ARRAY_START_TOKEN) {
            root = stack.push(new JsonArray());
            tokens.removeFirst();
        } else {
            throw new ParseException("Expected structure");
        }

        while (tokens.size() > 0) {
            Json last = stack.peek();
            if (last.isArray()) {
                JsonArray lastArray = last.getAsArray();
                Object first = tokens.removeFirst();

                if (first instanceof Json) {
                    lastArray.add((Json) first);

                    if (tokens.size() == 0) {
                        throw new ParseException("Unexpected end");
                    }
                    Object nextToken = tokens.getFirst();
                    if (nextToken == Json.ENTRY_SEPARATOR_TOKEN) {
                        if (tokens.size() < 2) {
                            throw new ParseException("Expected more tokens");
                        }
                        Object nextNextToken = tokens.get(1);
                        if (!(nextNextToken instanceof Json || nextNextToken == Json.OBJECT_START_TOKEN || nextNextToken == Json.ARRAY_START_TOKEN)) {
                            throw new ParseException("Unexpected value");
                        }
                        tokens.removeFirst();
                    } else if (nextToken != Json.ARRAY_END_TOKEN) {
                        throw new ParseException("Unexpected token");
                    }

                } else if (first == Json.ARRAY_START_TOKEN) {
                    lastArray.add(stack.push(new JsonArray()));
                } else if (first == Json.OBJECT_START_TOKEN) {
                    lastArray.add(stack.push(new JsonObject()));
                } else if (first == Json.ARRAY_END_TOKEN) {
                    stack.pop();
                    if (tokens.size() > 0 && tokens.getFirst() == Json.ENTRY_SEPARATOR_TOKEN) {
                        tokens.removeFirst();
                    }
                } else {
                    throw new ParseException("Expected json/arrayEnd/arrayStart/object/start");
                }
            } else if (last.isObject()) {
                JsonObject lastObject = last.getAsObject();
                Object first = tokens.removeFirst();

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

                    if (tokens.removeFirst() != Json.KEY_VALUE_SEPARATOR_TOKEN) {
                        throw new ParseException("Expected ':'");
                    }

                    Object value = tokens.removeFirst();
                    if (value instanceof Json) {
                        lastObject.put(key, (Json) value);

                        if (tokens.size() == 0) {
                            throw new ParseException("Unexpected end");
                        }
                        Object nextToken = tokens.getFirst();
                        if (nextToken == Json.ENTRY_SEPARATOR_TOKEN) {
                            if (tokens.size() < 2) {
                                throw new ParseException("Expected more tokens");
                            }
                            Object nextNextToken = tokens.get(1);
                            if (!(nextNextToken instanceof Json || nextNextToken == Json.OBJECT_START_TOKEN || nextNextToken == Json.ARRAY_START_TOKEN)) {
                                throw new ParseException("Unexpected value");
                            }
                            tokens.removeFirst();
                        } else if (nextToken != Json.OBJECT_END_TOKEN) {
                            throw new ParseException("Unexpected token");
                        }

                    } else if (value == Json.OBJECT_START_TOKEN) {
                        lastObject.put(key, stack.push(new JsonObject()));
                    } else if (value == Json.ARRAY_START_TOKEN) {
                        lastObject.put(key, stack.push(new JsonArray()));
                    } else {
                        throw new ParseException("Expected value");
                    }
                } else if (first == Json.OBJECT_END_TOKEN) {
                    stack.pop();
                    if (tokens.size() > 0 && tokens.get(0) == Json.ENTRY_SEPARATOR_TOKEN) {
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
            if (thisChar == '\\') {
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
