package com.github.ram042.json;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonStringTest {

    @DataProvider(name = "encoding")
    public Object[][] getEncoding() {
        return new Object[][]{
                {0, "abc", "\"abc\""},
                {1, "Sample string", "\"Sample string\""},
                {2, "\u22a0", "\"\u22a0\""},
                {3, "abc\"\\/\b\f\n\r\tabc", "\"abc\\\"\\\\\\/\\b\\f\\n\\r\\tabc\""},
                {4, "\uD834\uDD1E", "\"\uD834\uDD1E\""}
        };
    }

    @Test(dataProvider = "encoding")
    public void testEncoding(int testNumber, String input, String expected) {
        assertThat(new JsonString(input).toString()).isEqualToIgnoringCase(expected);
    }

    @DataProvider(name = "decoding")
    public Object[][] getDecoding() {
        return new Object[][]{
                {0, "\"abc\"", "abc"},
                {1, "\"Sample string\"", "Sample string"},
                {2, "\"\u22a0\"", "\u22a0"},
                {3, "\"\\\"\"", "\""},
                {4, "\"\\\"\\\"\"", "\"\""},
                {5, "\"abc \\\" \\\\ \\/ \\b \\f \\n \\r \\t abc\"",
                        "abc \" \\ / \b \f \n \r \t abc"},
                {6, "\"\\u003b\"", ";"},
                {7, "\"\\uD834\\uDD1E\"", "\uD834\uDD1E"}
        };
    }

    @Test(dataProvider = "decoding")
    public void testParsing(int testNumber, String input, String expected) {
        assertThat(Json.parse(input).getAsString().string).isEqualTo(expected);
    }
}
