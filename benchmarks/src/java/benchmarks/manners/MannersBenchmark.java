package benchmarks.manners;

import benchmarks.BaseBenchmark;
import benchmarks.Benchmark;
import benchmarks.waltz.DroolsWaltz;
import benchmarks.waltz.JessWaltz;

/**
 * This is a sample file to launch a rule package from a rule source file.
 */
public class MannersBenchmark extends BaseBenchmark {
    public void help() {
        System.out.println("USAGE: java benchmark.manners.MannersBenchmark <engine> <ruleFile> <ruleData>");
        System.exit( 0 );
    }
    
    
    public Benchmark initEngine(String engine) {
        Benchmark benchmark = null;
        if  ( engine.equals( "drools" ) ) {
            benchmark = new DroolsManners();
        } else if ( engine.equals( "jess" ) ) {
            benchmark = new JessManners();
        }  else if ( engine.equals( "jess-javabean" ) ) {
            benchmark = new JessJavaBeanManners();
        }      
        return benchmark;
    }
    
    public static void main(String[] args) {
        MannersBenchmark benchmark = new MannersBenchmark();
        benchmark.run( args );
    }
}
