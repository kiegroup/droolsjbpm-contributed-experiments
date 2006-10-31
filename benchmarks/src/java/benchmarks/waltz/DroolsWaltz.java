package benchmarks.waltz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
import benchmarks.manners.Context;
import benchmarks.manners.DroolsManners;

public class DroolsWaltz
    implements
    Benchmark {
        
    private WorkingMemory workingMemory;
    private final Stats stats  = new Stats();

    public void init(String fileName, boolean buildStats) throws Exception {
        PackageBuilder builder = new PackageBuilder();
        builder.addPackageFromDrl( new InputStreamReader( DroolsWaltz.class.getResourceAsStream( fileName ) ) );
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

    public void assertObjects(String file) throws Exception {
        loadLines( this.workingMemory,
                   file );  
    }

    public void fireAllRules() throws Exception {
        workingMemory.fireAllRules();
    }

    private void loadLines(final WorkingMemory wm,
                           final String filename) throws IOException {
      final BufferedReader br = new BufferedReader( new InputStreamReader( DroolsWaltz.class.getResourceAsStream( filename ) ) );

        String textLine;
        while ( (textLine = br.readLine()) != null ) {
            if ( textLine.trim().length() == 0 || textLine.trim().startsWith( ";" ) ) {
                continue;
            }
            final StringTokenizer st = new StringTokenizer( textLine,
                                                            "() " );
            final String type = st.nextToken();

            if ( "line".equals( type ) ) {
                Line line = new Line();
                String  field = st.nextToken();
                if ( field.equals( "p1" ) ){
                    String value = st.nextToken(); 
                    line.setP1( Integer.parseInt( value ) );
                } 
                
                field = st.nextToken();
                if ( field.equals( "p2" ) ) {
                    String value = st.nextToken();
                    line.setP2( Integer.parseInt( value ) );
                } 

                wm.assertObject( line );
            }

            if ( "stage".equals( type ) ) {
                if ( !"value".equals( st.nextToken() ) ) {
                    throw new IOException( "expected 'value' in: " + textLine );
                }
                String value = st.nextToken();
                if ( "duplicate".equals( value ) ) {
                    wm.assertObject( new Stage( Stage.DUPLICATE ) );
                }
            }
        }
        br.close();

    }
}