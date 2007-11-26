package benchmarks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public abstract class BaseBenchmark {
    public abstract void help();
      
    public abstract Benchmark initEngine(String engine);
    
    public void run(String[] args) {
        try {
            if(args.length != 3) {
                help();
            }
            String engine = args[0];
            String ruleFile = args[1];
            String dataFile = args[2];
                        
            Benchmark benchmark = initEngine( engine );             
            
            System.out.println("Using\nengine: " + engine + "\nrule file: " + ruleFile + "\ndata file: "+ dataFile);

            Runtime rt = Runtime.getRuntime();

            // initial stats
            long used1 = rt.totalMemory() - rt.freeMemory();
            long time1 = System.currentTimeMillis();

            boolean getStats = ruleFile.indexOf( "withprints" ) >= 0;
            
            // parse and load rulebase
            benchmark.init(ruleFile, getStats);
            long used2 = rt.totalMemory() - rt.freeMemory();
            long time2 = System.currentTimeMillis();
            
            // assert objects
            benchmark.assertObjects( dataFile );
            long time3 = System.currentTimeMillis();
            long used3 = rt.totalMemory() - rt.freeMemory();

            // fire rules
            benchmark.fireAllRules();
            long time4 = System.currentTimeMillis();
            long used4 = rt.totalMemory() - rt.freeMemory();
            
            // calling gc
            rt.gc();
            long time5 = System.currentTimeMillis();
            long used5 = rt.totalMemory() - rt.freeMemory();

            System.out.println("\n\n RESULTS:\n");
            System.out.println("    - Rules parsing time : "+new Long(time2-time1)+" ms   - Memory used:  + "+new Long((used2-used1)/1024)+" Kb\n");
            System.out.println("    - Assertion time     : " + new Long(time3-time2) +" ms   - Memory used:  + " + new Long((used3-used2)/1024)+" Kb\n");
            System.out.println("    - Rules firing time  : " + new Long(time4-time3) +" ms   - Memory used:  + " + new Long((used4-used3)/1024)+" Kb\n");
            System.out.println("----------------------------------------------------------------\n");
            System.out.println("    - Total time         : "+ new Long(time4-time1)+" ms   - Total memory: + "+new Long((used4-used1)/1024)+" Kb\n");
            System.out.println("    - GC Run time        : "+new Long(time5-time4)+" ms   - Mem after GC: + "+new Long((used5-used1)/1024)+" Kb\n\n");
            
            Stats stats = benchmark.getStats();
            if (  stats == null ) {
                return;
            }
                        
            Map created = stats.getCreated();
            System.out.println("Total Created:  " +  getTotal( created ) );
            System.out.println("--------------------" );
            for( Iterator  it = created.entrySet().iterator(); it.hasNext(); ) {
                Entry entry = (Entry) it.next();
                System.out.println("created: " + entry.getKey() + " : " + entry.getValue() );
            }
            System.out.println("");
            Map cancelled = stats.getCancelled();
            System.out.println("Total Cancelled:  " +   getTotal( cancelled ) );
            System.out.println("--------------------" );            
            for( Iterator  it = cancelled.entrySet().iterator(); it.hasNext(); ) {
                Entry entry = (Entry) it.next();
                System.out.println("cancelled: " + entry.getKey() + " : " + entry.getValue() );
            }  
            System.out.println("");
            Map fired = stats.getFired();
            System.out.println("Total Fired:  " +   getTotal( fired ) );
            System.out.println("--------------------" );            
            for( Iterator  it = fired.entrySet().iterator(); it.hasNext(); ) {
                Entry entry = (Entry) it.next();
                System.out.println("fired: " + entry.getKey() + " : " + entry.getValue() );
            }            
            System.out.println("");
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private int getTotal(Map map) {
    	int total = 0;
        for( Iterator  it = map.values().iterator(); it.hasNext(); ) {
        	Counter counter = (Counter) it.next();
        	total += counter.getValue();
        }
        return total;
    }
    
    public static class Stats {
        private Map created = new HashMap();
        private Map cancelled = new HashMap();
        private Map fired = new HashMap();
        
        public void created(String ruleName) {
            Counter counter = ( Counter ) created.get( ruleName );
            if  ( counter == null ) {
                counter = new Counter();
                created.put( ruleName, counter );
            }
            counter.increase();
        }
        
        public void cancelled(String ruleName) {
            Counter counter = ( Counter ) cancelled.get( ruleName );
            if  ( counter == null ) {
                counter = new Counter();
                cancelled.put( ruleName, counter );
            }
            counter.increase();
        }
        
        public void fired(String ruleName) {
            Counter counter = ( Counter ) fired.get( ruleName );
            if  ( counter == null ) {
                counter = new Counter();
                fired.put( ruleName, counter );
            }
            counter.increase();
        }

        public Map getCancelled() {
            return cancelled;
        }

        public Map getCreated() {
            return created;
        }

        public Map getFired() {
            return fired;
        }                          
    }
    
    public static class Counter {
        int value=0;
        
        public int getValue() {
        	return this.value;
        }
        
        public void increase(){
            value++;
        }       
        
        public String  toString() {
            return ""+this.value;
        }
    }    
}
