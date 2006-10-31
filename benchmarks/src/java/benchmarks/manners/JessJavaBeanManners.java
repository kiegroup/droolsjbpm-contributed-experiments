package benchmarks.manners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import org.drools.WorkingMemory;

import benchmarks.Benchmark;
import benchmarks.BaseBenchmark.Stats;

import jess.Activation;
import jess.Defrule;
import jess.Funcall;
import jess.JessEvent;
import jess.JessException;
import jess.JessListener;
import jess.RU;
import jess.Rete;
import jess.Value;

public class JessJavaBeanManners
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
                System.out.println( "help" );
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

    public void assertObjects(String fileName) throws Exception {
        loadLines( this.rete,
                   fileName );
    }
    
    public Stats getStats() {
        return null;
    }

    public void fireAllRules() throws Exception {
        rete.executeCommand( "(run)" );
    }

    private void loadLines(Rete rete,
                           String filename) throws Exception {
        BufferedReader br = new BufferedReader( new InputStreamReader( DroolsManners.class.getResourceAsStream( filename ) ) );

        String line;
        while ( (line = br.readLine()) != null ) {
            if ( line.trim().length() == 0 || line.trim().startsWith( ";" ) ) {
                continue;
            }
            StringTokenizer st = new StringTokenizer( line,
                                                      "() " );
            String type = st.nextToken();
            
            if ( "guest".equals( type ) ) {
                if ( !"name".equals( st.nextToken() ) ) {
                    throw new IOException( "expected 'name' in: " + line );
                }
                String name = st.nextToken();
                if ( !"sex".equals( st.nextToken() ) ) {
                    throw new IOException( "expected 'sex' in: " + line );
                }
                String sex = st.nextToken();
                if ( !"hobby".equals( st.nextToken() ) ) {
                    throw new IOException( "expected 'hobby' in: " + line );
                }
                String hobby = st.nextToken();

                Guest guest = new Guest( name,
                                         Sex.resolve( sex ),
                                         Hobby.resolve( hobby ) );

                rete.definstance( "guest", guest,  false );            
            }

            if ( "last_seat".equals( type ) ) {
                if ( !"seat".equals( st.nextToken() ) ) {
                    throw new IOException( "expected 'seat' in: " + line );
                }
                LastSeat seat =  new LastSeat( new Integer( st.nextToken() ).intValue() );
                rete.definstance( "last_seat", seat,  false );
            }

            if ( "context".equals( type ) ) {
                if ( !"name".equals( st.nextToken() ) ) {
                    throw new IOException( "expected 'state' in: " + line );
                }
                Context context = new Context( st.nextToken() );
                rete.definstance( "context", context,  false );              
            }
            
            if ( "count".equals( type ) ) {
                if ( !"c".equals( st.nextToken() ) ) {
                    throw new IOException( "expected 'value' in: " + line );
                }
                Count count = new Count( new Integer( st.nextToken() ).intValue() );
                rete.definstance( "count", count,  false );
            }            
        }
        br.close();
    }    

}