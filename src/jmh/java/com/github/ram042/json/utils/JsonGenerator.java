package com.github.ram042.json.utils;

import com.github.ram042.json.Json;
import com.github.ram042.json.JsonArray;
import com.github.ram042.json.JsonObject;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import static com.github.ram042.json.Json.array;

public class JsonGenerator {

    public static String getJson(int difficulty, boolean deep) throws IOException {
        Path file = new File(difficulty + " deep-" + deep + ".json").toPath();
        System.out.println("Benchmark data at " + file.toAbsolutePath().toString());
        if (Files.exists(file)) {
            return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        } else {
            String json = createJson(difficulty, deep);
            Files.write(file, json.getBytes(StandardCharsets.UTF_8));
            return json;
        }
    }

    /**
     * Generate test data
     * <pre>
     *     {
     *         "lots of fields": "lots of longs, floats and booleans",
     *         "one long array with many objects": [
     *              {
     *                  "some fields": "floats, longs, big numbers, strings and small object with array inside"
     *              }
     *         ]
     *         "very deep object"
     *     }
     * </pre>
     */
    public static String createJson(int difficulty, boolean deep) {
        Random random = new Random();
        JsonObject rootObject = new JsonObject();
        JsonArray longArray = new JsonArray();
        JsonObject deepObject = new JsonObject();
        rootObject.castAndPut("deep object", deepObject);
        BigInteger bigValue = BigInteger.valueOf(Long.MAX_VALUE);
        for (int i = 0; i < difficulty; i++) {
            longArray.add(Json.object(
                    "x", random.nextFloat(),
                    "y", random.nextBoolean(),
                    "z", random.nextLong(),
                    "bigz", BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.valueOf(random.nextLong())),
                    "null", "null",
                    "name", random.ints(5, 'a', 'z').limit(5)
                            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString()
                            + " " + random.nextFloat(),
                    "opts", Json.object("1", array(1, true))
            ));
            rootObject.castAndPut(Float.toString(random.nextFloat()), random.nextInt());
            rootObject.castAndPut(Float.toString(random.nextFloat()), random.nextFloat());
            rootObject.castAndPut(Float.toString(random.nextFloat()), random.nextBoolean());

            if (deep) {
                JsonObject nextDeepObject = new JsonObject();
                deepObject.castAndPut(Integer.toString(i), nextDeepObject);
                deepObject = nextDeepObject;
            }

        }
        rootObject.castAndPut("very long array", longArray);
        String jsonString = rootObject.toString();

        System.out.println("Sample length is " + jsonString.length() * 2 + " bytes");
        return jsonString;
    }

}
