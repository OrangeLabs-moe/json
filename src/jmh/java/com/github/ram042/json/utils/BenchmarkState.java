package com.github.ram042.json.utils;

import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class BenchmarkState {

    public String jsonString;

    @Param({"1000", "10000", "100000"})
    public int difficulty;

    @Param({"true", "false"})
    public boolean deep;

    @Setup
    public void setup() throws Exception {
        jsonString = JsonGenerator.getJson(difficulty, deep);
    }
}
