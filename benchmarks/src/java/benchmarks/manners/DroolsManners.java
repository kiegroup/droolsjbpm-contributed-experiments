package benchmarks.manners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.WorkingMemory;
import org.drools.compiler.PackageBuilder;
import org.drools.event.ActivationCancelledEvent;
import org.drools.event.ActivationCreatedEvent;
import org.drools.event.AfterActivationFiredEvent;
import org.drools.event.AgendaEventListener;
import org.drools.event.DefaultAgendaEventListener;
import org.drools.rule.Package;

import benchmarks.Benchmark;
import benchmarks.BaseBenchmark.Stats;

public class DroolsManners
    implements
    Benchmark {

    private WorkingMemory workingMemory;
    private final Stats stats  = new Stats();

    public void init(String fileName, boolean buildStats) throws Exception {
        PackageBuilder builder = new PackageBuilder();
        builder.addPackageFromDrl( new InputStreamReader( DroolsManners.class.getResourceAsStream( fileName ) ) );
        Package pkg = builder.getPackage();

        RuleBase ruleBase = RuleBaseFactory.newRuleBase();
        ruleBase.addPackage( pkg );
        workingMemory = ruleBase.newWorkingMemory();
                
        
        AgendaEventListener listener = new DefaultAgendaEventListener() {
            public void activationCreated(ActivationCreatedEvent event,
                                          WorkingMemory workingMemory) {
                stats.created( event.getActivation().getRule().getName() );
            }
            
            public void activationCancelled(ActivationCancelledEvent event,
                                            WorkingMemory workingMemory) {
                stats.cancelled( event.getActivation().getRule().getName() );
            }           
            
            public void afterActivationFired(AfterActivationFiredEvent event,
                                             WorkingMemory workingMemory) {
                stats.fired( event.getActivation().getRule().getName() );
            }            
        };
        
        if( buildStats ) {
            workingMemory.addEventListener( listener );
        }
    }   
    
    public Stats getStats() {
        return this.stats;
    }
    
    public void assertObjects(String fileName) throws Exception {
        loadLines( this.workingMemory,
                   fileName );
    }

    public void fireAllRules() throws Exception {
        workingMemory.fireAllRules();
    }

    private void loadLines(WorkingMemory wm,
                           String filename) throws IOException {
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

                wm.assertObject( guest );
            }

            if ( "last_seat".equals( type ) ) {
                if ( !"seat".equals( st.nextToken() ) ) {
                    throw new IOException( "expected 'seat' in: " + line );
                }
                wm.assertObject( new LastSeat( new Integer( st.nextToken() ).intValue() ) );
            }

            if ( "context".equals( type ) ) {
                if ( !"name".equals( st.nextToken() ) ) {
                    throw new IOException( "expected 'state' in: " + line );
                }
                wm.assertObject( new Context( st.nextToken() ) );
            }
            
            if ( "count".equals( type ) ) {
                if ( !"c".equals( st.nextToken() ) ) {
                    throw new IOException( "expected 'value' in: " + line );
                }
                wm.assertObject( new Count( new Integer( st.nextToken() ).intValue() ) );
            }            
        }
        br.close();
    }
}