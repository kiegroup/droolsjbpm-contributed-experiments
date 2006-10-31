package benchmarks.waltz;

import benchmarks.BaseBenchmark;
import benchmarks.Benchmark;
import benchmarks.manners.DroolsManners;
import benchmarks.manners.MannersBenchmark;

/**
 * This is a sample file to launch a rule package from a rule source file.
 */
public class WaltzBenchmark  extends BaseBenchmark {
    public void help() {
        System.out.println("USAGE: java benchmark.waltz.WaltzBenchmark <engine> <ruleFile> <ruleData>");
        System.exit( 0 );
    }
    
    public Benchmark initEngine(String engine) {
        Benchmark benchmark = null;
        if  ( engine.equals( "drools" ) ) {
            benchmark = new DroolsWaltz();
        } else if ( engine.equals( "jess" ) ) {
            benchmark = new JessWaltz();
        }        
        return benchmark;
    }
    
    public static void main(String[] args) {
        WaltzBenchmark benchmark = new WaltzBenchmark();
        benchmark.run( args );
    }
}
