package benchmarks;

import java.util.List;

import benchmarks.BaseBenchmark.Stats;

public interface Benchmark {

    public void init(String fileName, boolean buildStats) throws Exception;

    public Stats getStats();
    
    public void assertObjects(String fileName) throws Exception;

    public void fireAllRules() throws Exception;

}