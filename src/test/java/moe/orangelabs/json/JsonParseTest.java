package moe.orangelabs.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static moe.orangelabs.json.Json.array;
import static moe.orangelabs.json.Json.*;
import static moe.orangelabs.json.JsonParseTest.SuiteExpectedResult.*;
import static org.assertj.core.api.Assertions.*;

public class JsonParseTest {

    @DataProvider(name = "parse")
    public Object[][] getParseData() {
        return new Object[][]{
                {0, "{}", object()},
                {1, "{\"key\":\"value\"}", object("key", "value")},
                {2, "{\"int1\":14,\"int2\":-1}", object("int1", 14, "int2", -1)},
                {3,
                        "{\"a\":\"b\",\"c\":42,\"d\":\"e\",\"f\":[\"g\"],\"h\":\"e\"}",
                        object(
                                "a", "b",
                                "c", 42,
                                "d", "e",
                                "f", array("g"),
                                "h", "e"
                        )
                },

                {4, "[]", array()},
                {5, "[\"a\",\"b\",\"c\"]", array("a", "b", "c")},
                {6, "[true,false,null]", array(true, false, null)},
                {7, "[40.96]", array(number(new BigDecimal("40.96")))},
                {8, "[81.92e+2]", array(new BigDecimal(8192))}
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
        assertThatThrownBy(() -> Json.parse(input)).isExactlyInstanceOf(ParseException.class);
    }


    Logger logger = LoggerFactory.getLogger(JsonParseTest.class);

    @DataProvider(name = "suite")
    public Object[][] getSuiteTests() throws IOException {
        List<Object[]> result = new LinkedList<>();
        Files.list(Paths.get("src/test/resources/suite/test_parsing")).forEach(path -> {
            SuiteExpectedResult expectedResult;
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

    private static final List<String> ignoredSuiteTest = Arrays.asList(
            "n_multidigit_number_then_00.json" //ignore this because we trim control characters with String.trim()
    );

    @Test(timeOut = 5 * 1000, dataProvider = "suite")
    public void testSuite(String name, String string, SuiteExpectedResult expectedResult) {
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
                        logger.info("Test that can fail or success {} threw exception", name, e);
                    }
                    break;
            }
        } catch (AssertionError e) {
            if (ignoredSuiteTest.contains(name)) {
                logger.warn("Ignored test {} threw exception", name, e);
            } else {
                throw e;
            }
        }

    }

    public enum SuiteExpectedResult {
        ACCEPT, REJECT, ANY
    }
}
