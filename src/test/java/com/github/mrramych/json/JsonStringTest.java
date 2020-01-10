package com.github.mrramych.json;

import com.github.mrramych.json.types.JsonString;
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
                {3, "abc\"\\/\b\f\n\r\tabc", "\"abc\\\"\\\\\\/\\b\\f\\n\\r\\tabc\""}
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
                        "abc \" \\ / \b \f \n \r \t abc"}
        };
    }

    @Test(dataProvider = "decoding")
    public void testDecoding(int testNumber, String input, String expected) {
        //here we try to parse string as it was part of array
        assertThat(Json.parse("[" + input + "]").getAsArray().get(0).getAsString().string)
                .isEqualTo(expected);
    }
}
