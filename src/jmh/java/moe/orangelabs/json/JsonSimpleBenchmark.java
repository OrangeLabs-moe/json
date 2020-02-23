package moe.orangelabs.json;

import moe.orangelabs.json.utils.BenchmarkState;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

public class JsonSimpleBenchmark {

    @Benchmark
    public void parse(BenchmarkState state, Blackhole blackhole) throws ParseException {
        blackhole.consume(new JSONParser().parse(state.jsonString));
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
            json = (JSONObject) new JSONParser().parse(jsonString);
        }
    }

}
