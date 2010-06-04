package org.drools.examples.lotrc;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.audit.WorkingMemoryFileLogger;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.examples.lotrc.model.Card;
import org.drools.examples.lotrc.model.Game;
import org.drools.examples.lotrc.model.Region;
import org.drools.examples.lotrc.player.FellowshipPlayer;
import org.drools.examples.lotrc.player.Player;
import org.drools.examples.lotrc.player.SauronPlayer;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;

/**
 * The main entry point for the Lord of the Rings: Confrontation game
 * 
 * @author etirelli
 */
public class Main {
    static final Logger                     logger = Logger.getLogger( "Game" );

    private static KnowledgeBase            kbase;
    private static StatefulKnowledgeSession ksession;

    private static WorkingMemoryFileLogger audit;

    public static void main(String[] args) {
        initResources();
        initKnowledgeBase();
        initSession();
        run();
    }

    private static void initResources() {
        PropertyConfigurator.configure(Main.class.getResource( "/log4j.properties" ));
    }

    private static void initKnowledgeBase() {
        logger.debug( "Creating knowledge base" );
        KnowledgeBuilderConfiguration conf = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
        KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder( conf );
        String[] resources = new String[] { "game/combat.rf", "game/gameflow.rf", "game/setup.drl", 
                                            "game/move.drl", "game/combat.drl", "game/victory.drl", 
                                            "game/misc.drl" };
        addGameResources( builder, resources );
        if ( builder.hasErrors() ) {
            for ( KnowledgeBuilderError error : builder.getErrors() ) {
                logger.error( error );
            }
            System.exit( 0 );
        }
        logger.debug( "Knowledge packages successfuly compiled." );
        kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( builder.getKnowledgePackages() );
        logger.debug( "Knowledge Base successfuly created" );
    }

    private static void addGameResources(KnowledgeBuilder builder, String[] resources ) {
        int previous = 0;
        for( String resource : resources ) {
            //logger.debug( "Loading "+resource );
            builder.add( ResourceFactory.newClassPathResource( resource ),
                         resource.endsWith( ".rf" ) ? ResourceType.DRF : ResourceType.DRL );
            checkErrors(builder);
            int newSize = builder.getKnowledgePackages().iterator().next().getRules().size();
            logger.info( "Loaded: "+resource+" with "+(newSize-previous)+" rules." );
            previous = newSize;
        }
        logger.info( "All resources loaded. Total of "+previous+" rules." );
    }

    private static void checkErrors(KnowledgeBuilder builder) {
        if( builder.hasErrors() ) {
            for (KnowledgeBuilderError knowledgeBuilderError : builder.getErrors()) {
                System.err.println(knowledgeBuilderError.toString());
            }
            System.exit(0);
        }
    }

    private static void initSession() {
        logger.debug( "Creating Knowledge Session" );
        ksession = kbase.newStatefulKnowledgeSession();
        ksession.setGlobal( "logger", logger );
        
        audit = new WorkingMemoryFileLogger( ksession );
        audit.setFileName( "game" );
                
        logger.debug( "Creating Game objects" );
        Player sauron = new SauronPlayer();
        Player fellowship = new FellowshipPlayer();
        Game game = new Game(sauron, fellowship);
        
        logger.debug( "Initializing session with game data" );
        ksession.insert( sauron );
        ksession.insert( fellowship );
        ksession.insert( game );
        ksession.insert( game.getBoard() );
        Region[][] regions = game.getBoard().getRegions();
        for( Region[] row : regions ) {
            for( Region site : row ) {
                ksession.insert( site );
            }
        }
        for( org.drools.examples.lotrc.model.Character character : game.getCharacters() ) {
            ksession.insert( character );
        }
        for( Card card : game.getCards() ) {
            ksession.insert( card );
        }
    }

    private static void run() {
        try {
            logger.debug( "Starting Game Flow" );
            ksession.startProcess( "Game Flow" );
            logger.debug( "Firing rules" );
            ksession.fireAllRules();
            logger.debug( "Session finished" );
        } finally {
            if( audit != null ) {
                audit.writeToDisk();
            }
        }
    }

}
