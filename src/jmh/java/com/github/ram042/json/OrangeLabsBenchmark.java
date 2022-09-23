package com.github.ram042.json;

import com.github.ram042.json.utils.BenchmarkState;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

public class OrangeLabsBenchmark {

    @Benchmark
    public void parse(BenchmarkState state, Blackhole blackhole) {
        blackhole.consume(Json.parse(state.jsonString));
    }

    @Benchmark
    public void write(WriteState state, Blackhole blackhole) {
        blackhole.consume(state.json.toString());
    }

    public static class WriteState extends BenchmarkState {

        public Json json;

        @Override
        public void setup() throws Exception {
            super.setup();
            json = Json.parse(jsonString);
        }
    }

    public static void main(String[] args) throws Exception {
        WriteState state = new WriteState();
        state.difficulty = 50000;
        state.deep = false;
        state.setup();
        new OrangeLabsBenchmark().write(state,
                new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous."));
    }

}
