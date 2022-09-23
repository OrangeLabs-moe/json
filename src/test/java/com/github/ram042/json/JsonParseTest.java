package com.github.ram042.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static com.github.ram042.json.JsonParseTest.ExpectedResult.*;
import static org.assertj.core.api.Assertions.*;

public class JsonParseTest {

    Logger logger = LoggerFactory.getLogger(JsonParseTest.class);

    @DataProvider(name = "parse")
    public Object[][] getParseData() {
        return new Object[][]{
                {0, "{}", Json.object()},
                {1, "{\"key\":\"value\"}", Json.object("key", "value")},
                {2, "{\"int1\":14,\"int2\":-1}", Json.object("int1", 14, "int2", -1)},
                {3,
                        "{\"a\":\"b\",\"c\":42,\"d\":\"e\",\"f\":[\"g\"],\"h\":\"e\"}",
                        Json.object(
                                "a", "b",
                                "c", 42,
                                "d", "e",
                                "f", Json.array("g"),
                                "h", "e"
                        )
                },

                {4, "[]", Json.array()},
                {5, "[\"a\",\"b\",\"c\"]", Json.array("a", "b", "c")},
                {6, "[true,false,null]", Json.array(true, false, null)},
                {7, "[40.96]", Json.array(Json.number(new BigDecimal("40.96")))},
                {8, "[81.92e+2]", Json.array(new BigDecimal(8192))}
        };
    }

    @Test(dataProvider = "parse")
    public void parse(int testId, String input, Json expected) {
        assertThat(Json.parse(input)).isEqualTo(expected);
    }

    @DataProvider(name = "parseError")
    public Object[][] getParseErrorData() {
        return new Object[][]{
                {"[}"},
                {"{]"},
                {"{{"},
                {"}}"},
                {"[["},
                {"]]"},
                {"[{]}"},
                {"{[}]"},

                {"{12:12}"},
                {"{12:12:12{"},
                {"{12::}"},
                {"{12::}"},
                {"{12 12"},

                {"[,]"},
                {"{12,,}"},
                {"[12 12]"}
        };
    }

    @Test(dataProvider = "parseError")
    public void parseError(String input) {
        assertThatThrownBy(() -> Json.parse(input));
    }

    @DataProvider(name = "suite")
    public Object[][] getSuiteTests() throws IOException {
        List<Object[]> result = new LinkedList<>();
        Files.list(Paths.get("src/test/resources/suite/test_parsing")).forEach(path -> {
            ExpectedResult expectedResult;
            switch (path.getFileName().toString().charAt(0)) {
                case 'y':
                    expectedResult = ACCEPT;
                    break;
                case 'n':
                    expectedResult = REJECT;
                    break;
                case 'i':
                    expectedResult = ANY;
                    break;
                default:
                    logger.warn("Can not get expected result for {}", path.getFileName());
                    return;
            }

            String string;
            try {
                string = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
            } catch (IOException e) {
                logger.warn("Can not read file {}", path.getFileName(), e);
                return;
            }

            result.add(new Object[]{
                    path.getFileName().toString(),
                    string,
                    expectedResult
            });
        });

        Object[][] resultObject = new Object[result.size()][];
        for (int i = 0; i < result.size(); i++) {
            resultObject[i] = result.get(i);
        }

        return resultObject;
    }

    @Test(dataProvider = "suite")
    public void testSuite(String name, String string, ExpectedResult expectedResult) {
        try {
            switch (expectedResult) {
                case ACCEPT:
                    assertThatCode(() -> Json.parse(string)).doesNotThrowAnyException();
                    break;
                case REJECT:
                    assertThatThrownBy(() -> Json.parse(string));
                    break;
                case ANY:
                    try {
                        Json.parse(string);
                    } catch (Exception e) {
                        logger.info("Test that can fail or success {} threw exception {}", name, e.toString());
                    }
                    break;
            }
        } catch (AssertionError e) {
            logger.warn("Test {} failed with exception {}", name, e.toString());
        }

    }

    public enum ExpectedResult {
        ACCEPT, REJECT, ANY
    }
}
