package moe.orangelabs.json;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import moe.orangelabs.json.utils.BenchmarkState;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

public class GsonBenchmark {

    @Benchmark
    public void parse(BenchmarkState state, Blackhole blackhole) {
        blackhole.consume(JsonParser.parseString(state.jsonString));
    }

    @Benchmark
    public void write(WriteState state, Blackhole blackhole) {
        blackhole.consume(state.json.toString());
    }

    public static class WriteState extends BenchmarkState {

        public JsonObject json;

        @Override
        public void setup() throws Exception {
            super.setup();
            json = (JsonObject) JsonParser.parseString(jsonString);
        }
    }

}
