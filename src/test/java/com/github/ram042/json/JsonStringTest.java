package com.github.ram042.json;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonStringTest {

    @DataProvider(name = "encoding")
    public Object[][] getEncoding() {
        return new Object[][]{
                {"Simple string", "abc", "\"abc\""},
                {"Simple string", "Simple string", "\"Simple string\""},
                {"Unicode character", "\u22a0", "\"\u22a0\""},

                {"Quotation mark", "\"", "\"\\\"\""},
                {"Reverse solidus", "\\", "\"\\\\\""},
                {"Solidus", "/", "\"\\/\""},
                {"Backspace", "\b", "\"\\b\""},
                {"Form feed", "\f", "\"\\f\""},
                {"Line feed", "\n", "\"\\n\""},
                {"Carriage return", "\r", "\"\\r\""},
                {"Tab", "\t", "\"\\t\""},

                {"Control character 0x00", "\u0000", "\"\\u0000\""},
                {"Control character 0x0f", "\u000f", "\"\\u000f\""},
                {"Control character 0x10", "\u0010", "\"\\u0010\""},
                {"Control character 0x11", "\u0011", "\"\\u0011\""},
                {"Control character 0x19", "\u0019", "\"\\u0019\""},
                {"Control character 0x1a", "\u001a", "\"\\u001a\""},
                {"Control character 0x1f", "\u001f", "\"\\u001f\""},

                {"UTF-16", "\uD834\uDD1E", "\"\uD834\uDD1E\""},
        };
    }

    @Test(dataProvider = "encoding")
    public void testEncoding(String testDescription, String input, String expected) {
        assertThat(new JsonString(input).toString()).isEqualToIgnoringCase(expected);
    }

    @Test(dataProvider = "encoding")
    public void testParsing(String testDescription, String expected, String input) {
        assertThat(Json.parse(input).getAsString().string).isEqualTo(expected);
    }
}
