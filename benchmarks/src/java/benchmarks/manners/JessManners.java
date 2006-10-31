package benchmarks.manners;

import benchmarks.Benchmark;
import benchmarks.BaseBenchmark.Stats;

import jess.Activation;
import jess.Defrule;
import jess.JessEvent;
import jess.JessException;
import jess.JessListener;
import jess.Rete;

public class JessManners
    implements
    Benchmark {    
    Rete rete = new Rete();
    private final Stats stats  = new Stats();

    public void init(String fileName, boolean buildStats) throws Exception {
        rete.executeCommand("(clear)");
        rete.executeCommand("(batch \"benchmarks/manners/"+fileName+"\")");
        rete.executeCommand("(reset)");
        
        JessListener listener  = new JessListener()  {
            public void eventHappened(JessEvent event) throws JessException {
                switch ( event.getTag() ) {
                    case JessEvent.DEFRULE_FIRED:
                        Defrule rule =  ( Defrule ) event.getSource();
                        stats.fired( rule.getName() );
                        break;
                    case JessEvent.ACTIVATION:
                        Activation activation = (Activation) event.getSource();
                        if ( activation.isInactive() ) {
                            stats.cancelled( activation.getRule().getName() );
                        } else  {
                            stats.created( activation.getRule().getName() );
                        }
                        break;
                }                
            }            
        };
        
        if( buildStats ) {
            rete.addJessListener( listener );
        }
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