package com.github.ram042.json;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;
import com.github.ram042.json.utils.BenchmarkState;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

public class MinimalJsonBenchmark {

    @Benchmark
    public void parse(BenchmarkState state, Blackhole blackhole) {
        blackhole.consume(Json.parse(state.jsonString));
    }

    @Benchmark
    public void write(WriteState state, Blackhole blackhole) {
        blackhole.consume(state.json.toString());
    }

    public static class WriteState extends BenchmarkState {

        public JsonValue json;

        @Override
        public void setup() throws Exception {
            super.setup();
            json = Json.parse(jsonString);
        }
    }

}
