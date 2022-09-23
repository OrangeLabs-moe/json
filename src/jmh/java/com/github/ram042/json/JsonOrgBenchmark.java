package com.github.ram042.json;

import com.github.ram042.json.utils.BenchmarkState;
import org.json.JSONObject;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

public class JsonOrgBenchmark {

    @Benchmark
    public void parse(BenchmarkState state, Blackhole blackhole) {
        blackhole.consume(new JSONObject(state.jsonString));
    }

    @Benchmark
    public void write(WriteState state, Blackhole blackhole) {
        blackhole.consume(state.json.toString());
    }

    public static class WriteState extends BenchmarkState {

        public JSONObject json;

        @Override
        public void setup() throws Exception {
            super.setup();
            json = new JSONObject(jsonString);
        }
    }

}
