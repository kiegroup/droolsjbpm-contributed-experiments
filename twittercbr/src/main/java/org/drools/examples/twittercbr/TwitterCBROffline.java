/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.examples.twittercbr;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.TimeUnit;

import org.drools.ClockType;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.conf.EventProcessingOption;
import org.drools.io.ResourceFactory;
import org.drools.runtime.KnowledgeSessionConfiguration;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.conf.ClockTypeOption;
import org.drools.runtime.rule.WorkingMemoryEntryPoint;
import org.drools.time.SessionPseudoClock;

import twitter4j.Status;
import twitter4j.StatusListener;
import twitter4j.TwitterException;

/**
 * TwitterCBR
 */
public class TwitterCBROffline {
    public static final boolean disableLog = true;

    /**
     * Main method
     */
    public static void main(String[] args) throws TwitterException, IOException{
        if( args.length == 0 ) {
            System.out.println("Please provide the rules file name to load.");
            System.exit( 0 );
        }
        
        // Creates a knowledge base
        final KnowledgeBase kbase = createKnowledgeBase( args[0] );
        
        // Creates a knowledge session
        final StatefulKnowledgeSession ksession = createKnowledgeSession( kbase );
        
        // Gets the stream entry point 
        final WorkingMemoryEntryPoint ep = ksession.getWorkingMemoryEntryPoint( "twitter" );
        
        new Thread( new Runnable() {
            @Override
            public void run() {
                // Starts to fire rules in Drools Fusion
                ksession.fireUntilHalt();
            }
        }).start();

        // reads the status stream and feeds it to the engine 
        feedEvents( ksession,
                    ep );
        
        ksession.halt();
        
    }

    /**
     * Feed events from the stream to the engine
     * @param ksession
     * @param ep
     */
    private static void feedEvents( final StatefulKnowledgeSession ksession,
                                    final WorkingMemoryEntryPoint ep ) {
        try {
            StatusListener listener = new TwitterStatusListener( ep );
            ObjectInputStream in = new ObjectInputStream( new FileInputStream( "src/main/resources/twitterstream.dump" ) );
            SessionPseudoClock clock = ksession.getSessionClock();
            
            for( int i = 0; ; i++ ) {
                try {
                    // Read an event
                    Status st = (Status) in.readObject();
                    // Using the pseudo clock, advance the clock
                    clock.advanceTime( st.getCreatedAt().getTime() - clock.getCurrentTime(), TimeUnit.MILLISECONDS );
                    // call the listener
                    listener.onStatus( st );
                } catch( IOException ioe ) {
                    break;
                }
            }
            in.close();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a Drools KnowledgeBase and adds the given rules file into it
     */
    private static KnowledgeBase createKnowledgeBase( final String rulesFile ) {
        // Parses and compiles the rules file
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add( ResourceFactory.newClassPathResource( rulesFile ), ResourceType.DRL );
        
        // Configures the Stream mode
        KnowledgeBaseConfiguration conf = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
        conf.setOption( EventProcessingOption.STREAM );
        
        // Creates the knowledge base and adds the rules
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase( conf );
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
        return kbase;
    }

    /**
     * Creates a Drools Stateful Knowledge Session
     */
    private static StatefulKnowledgeSession createKnowledgeSession( final KnowledgeBase kbase ) {
        final KnowledgeSessionConfiguration ksconf = KnowledgeBaseFactory.newKnowledgeSessionConfiguration();
        ksconf.setOption( ClockTypeOption.get( ClockType.PSEUDO_CLOCK.getId() ) );
        final StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession( ksconf, null );
        return ksession;
    }

    static {
        // disable twitter4j log
        System.setProperty( "twitter4j.loggerFactory", "twitter4j.internal.logging.NullLoggerFactory" );
    }
    
}
