package moe.orangelabs.json;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;

import static moe.orangelabs.json.Json.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

}
