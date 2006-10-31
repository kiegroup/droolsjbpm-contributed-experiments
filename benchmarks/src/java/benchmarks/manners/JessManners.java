package benchmarks.manners;

import benchmarks.Benchmark;
import benchmarks.BaseBenchmark.Stats;

import jess.Rete;

public class JessManners
    implements
    Benchmark {    
    Rete rete = new Rete();

    public void init(String fileName, boolean buildStats) throws Exception {
        rete.executeCommand("(clear)");
        rete.executeCommand("(batch \"benchmarks/manners/"+fileName+"\")");
        rete.executeCommand("(reset)");
    }
    
    public Stats getStats() {
        return null;
    }

    public void assertObjects(String fileName) throws Exception {
        rete.executeCommand("(load-facts \"benchmarks/manners/" + fileName + "\")");
    }

    public void fireAllRules() throws Exception {
        rete.executeCommand( "(run)" );
    }


}