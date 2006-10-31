package benchmarks.waltz;

import java.util.Iterator;
import java.util.List;

import benchmarks.Benchmark;
import benchmarks.BaseBenchmark.Stats;

import jess.Funcall;
import jess.RU;
import jess.Rete;
import jess.Value;

public class JessWaltz
    implements
    Benchmark {
    
    Rete rete = new Rete();

    public void init(String fileName, boolean buildStats) throws Exception {
        WaltzFile file = new WaltzFile();
        file.add( this.rete );        
        rete.executeCommand("(clear)");
        rete.executeCommand("(batch \"benchmarks/waltz/"+fileName+"\")");
        rete.executeCommand("(reset)");

    }
    
    public Stats getStats() {
        return null;
    }

    public void assertObjects(String fileName) throws Exception {
        rete.executeCommand("(load-facts \"benchmarks/waltz/" + fileName + "\")");
    }

    public void fireAllRules() throws Exception {
        rete.executeCommand( "(run)" );
    }


}