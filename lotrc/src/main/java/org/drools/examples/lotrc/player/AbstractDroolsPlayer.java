package org.drools.examples.lotrc.player;

import org.apache.log4j.Logger;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.examples.lotrc.action.CharacterDefeatedAction;
import org.drools.examples.lotrc.action.CombatAction;
import org.drools.examples.lotrc.action.MoveAction;
import org.drools.examples.lotrc.action.PlaceCharacterAction;
import org.drools.examples.lotrc.action.PlayCardAction;
import org.drools.examples.lotrc.model.Allegiance;
import org.drools.examples.lotrc.model.Board;
import org.drools.examples.lotrc.model.CardAbility;
import org.drools.examples.lotrc.model.CardImpl;
import org.drools.examples.lotrc.model.CardName;
import org.drools.examples.lotrc.model.CharacterAbility;
import org.drools.examples.lotrc.model.CharacterImpl;
import org.drools.examples.lotrc.model.CharacterName;
import org.drools.examples.lotrc.model.Region;
import org.drools.examples.lotrc.player.command.MakeAMove;
import org.drools.examples.lotrc.player.command.PlayACard;
import org.drools.examples.lotrc.player.command.SetupCharacter;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;

public abstract class AbstractDroolsPlayer
    implements
    Player {

    protected Logger                   logger;
    protected KnowledgeBase            kbase;
    protected StatefulKnowledgeSession ksession;

    public AbstractDroolsPlayer(String identifier,
                                String[] ruleFiles) {
        logger = Logger.getLogger( identifier );
        initRulebase( ruleFiles );
        initSession();
    }

    public abstract Allegiance getAllegiance();

    public void notify(PlaceCharacterAction action) {
        updateSession( action );
    }

    public void notify(MoveAction action) {
        updateSession( action );
    }

    public void notify(PlayCardAction action) {
        updateSession( action );
    }

    public void notify(CharacterDefeatedAction action) {
        updateSession( action );
    }

    private void updateSession(Object fact) {
        FactHandle handle = ksession.insert( fact );
        ksession.fireAllRules();
        ksession.retract( handle );
    }

    public PlaceCharacterAction setupCharacter(CharacterName character) {
        SetupCharacter sc = new SetupCharacter( character );
        updateSession( sc );
        return sc.getAction();
    }

    public MoveAction makeAMove() {
        MakeAMove mam = new MakeAMove();
        updateSession( mam );
        return mam.getAction();
    }

    public PlayCardAction playACard(CombatAction combat) {
        PlayACard pac = new PlayACard( combat );
        updateSession( pac );
        return pac.getAction();
    }

    public void printBoard() {
        ksession.insert( "Print Board" );
        ksession.fireAllRules();
    }

    private void initRulebase(String[] ruleFiles) {
        logger.debug( "Creating knowledge base" );
        KnowledgeBuilderConfiguration conf = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
        KnowledgeBuilder builder = KnowledgeBuilderFactory.newKnowledgeBuilder( conf );
        for ( String ruleFile : ruleFiles ) {
            logger.debug( "Loading: " + ruleFile );
            builder.add( ResourceFactory.newClassPathResource( ruleFile ),
                         ResourceType.DRL );
        }
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

    private void initSession() {
        logger.debug( "Creating Knowledge Session" );
        ksession = kbase.newStatefulKnowledgeSession();
        ksession.setGlobal( "logger",
                            logger );
        logger.debug( "Initializing session with game data" );
        Board board = new Board();
        ksession.insert( board );
        Region[][] regions = board.getRegions();
        for ( Region[] row : regions ) {
            for ( Region region : row ) {
                ksession.insert( region );
            }
        }
        // Sauron characters
        ksession.insert( new CharacterImpl( Allegiance.SAURON,
                                            CharacterName.WARG,
                                            2,
                                            CharacterAbility.IGNORE_TEXT ) );
        ksession.insert( new CharacterImpl( Allegiance.SAURON,
                                            CharacterName.SHELOB,
                                            5,
                                            CharacterAbility.RETURN_TO_ROHAN ) );
        ksession.insert( new CharacterImpl( Allegiance.SAURON,
                                            CharacterName.ORCS,
                                            2,
                                            CharacterAbility.DEFEAT_ON_ATTACK ) );
        ksession.insert( new CharacterImpl( Allegiance.SAURON,
                                            CharacterName.FLYING_NAZGUL,
                                            3,
                                            CharacterAbility.FLY_TO_PREY ) );
        ksession.insert( new CharacterImpl( Allegiance.SAURON,
                                            CharacterName.CAVE_TROLL,
                                            9,
                                            CharacterAbility.IGNORE_CARD ) );
        ksession.insert( new CharacterImpl( Allegiance.SAURON,
                                            CharacterName.BLACK_RIDER,
                                            3,
                                            CharacterAbility.CHARGE_TO_ATTACK ) );
        ksession.insert( new CharacterImpl( Allegiance.SAURON,
                                            CharacterName.WITCH_KING,
                                            5,
                                            CharacterAbility.ATTACK_SIDEWAYS ) );
        ksession.insert( new CharacterImpl( Allegiance.SAURON,
                                            CharacterName.SARUMAN,
                                            4,
                                            CharacterAbility.NO_CARDS_ON_COMBAT ) );
        ksession.insert( new CharacterImpl( Allegiance.SAURON,
                                            CharacterName.BALROG,
                                            5,
                                            CharacterAbility.GUARDS_MORIA_TUNEL ) );
        // create the characters for the Fellowship
        ksession.insert( new CharacterImpl( Allegiance.FELLOWSHIP,
                                            CharacterName.FRODO,
                                            1,
                                            CharacterAbility.RETREAT_SIDEWAYS ) );
        ksession.insert( new CharacterImpl( Allegiance.FELLOWSHIP,
                                            CharacterName.SAM,
                                            2,
                                            CharacterAbility.PROTECTS_FRODO ) );
        ksession.insert( new CharacterImpl( Allegiance.FELLOWSHIP,
                                            CharacterName.ARAGORN,
                                            4,
                                            CharacterAbility.ATTACK_ADJACENT ) );
        ksession.insert( new CharacterImpl( Allegiance.FELLOWSHIP,
                                            CharacterName.PIPPIN,
                                            1,
                                            CharacterAbility.RETREAT_ON_ATTACK ) );
        ksession.insert( new CharacterImpl( Allegiance.FELLOWSHIP,
                                            CharacterName.MERRY,
                                            2,
                                            CharacterAbility.DEFEAT_WITCH_KING ) );
        ksession.insert( new CharacterImpl( Allegiance.FELLOWSHIP,
                                            CharacterName.LEGOLAS,
                                            3,
                                            CharacterAbility.DEFEAT_NAZGUL ) );
        ksession.insert( new CharacterImpl( Allegiance.FELLOWSHIP,
                                            CharacterName.GIMLI,
                                            3,
                                            CharacterAbility.DEFEAT_ORCS ) );
        ksession.insert( new CharacterImpl( Allegiance.FELLOWSHIP,
                                            CharacterName.GANDALF,
                                            5,
                                            CharacterAbility.PLAY_CARD_LAST ) );
        ksession.insert( new CharacterImpl( Allegiance.FELLOWSHIP,
                                            CharacterName.BOROMIR,
                                            0,
                                            CharacterAbility.SACRIFICE ) );

        // create the cards for Sauron
        ksession.insert( new CardImpl( Allegiance.SAURON,
                                       CardName.P1,
                                       1,
                                       CardAbility.NONE ) );
        ksession.insert( new CardImpl( Allegiance.SAURON,
                                       CardName.P2,
                                       2,
                                       CardAbility.NONE ) );
        ksession.insert( new CardImpl( Allegiance.SAURON,
                                       CardName.P3,
                                       3,
                                       CardAbility.NONE ) );
        ksession.insert( new CardImpl( Allegiance.SAURON,
                                       CardName.P4,
                                       4,
                                       CardAbility.NONE ) );
        ksession.insert( new CardImpl( Allegiance.SAURON,
                                       CardName.P5,
                                       5,
                                       CardAbility.NONE ) );
        ksession.insert( new CardImpl( Allegiance.SAURON,
                                       CardName.P6,
                                       6,
                                       CardAbility.NONE ) );
        ksession.insert( new CardImpl( Allegiance.SAURON,
                                       CardName.MAGIC,
                                       0,
                                       CardAbility.MAGIC ) );
        ksession.insert( new CardImpl( Allegiance.SAURON,
                                       CardName.EYE_OF_SAURON,
                                       0,
                                       CardAbility.IGNORE_TEXT ) );
        ksession.insert( new CardImpl( Allegiance.SAURON,
                                       CardName.RETREAT,
                                       0,
                                       CardAbility.RETREAT_SIDEWAYS ) );

        // create the cards for the Fellowship
        ksession.insert( new CardImpl( Allegiance.FELLOWSHIP,
                                       CardName.P1,
                                       1,
                                       CardAbility.NONE ) );
        ksession.insert( new CardImpl( Allegiance.FELLOWSHIP,
                                       CardName.P2,
                                       2,
                                       CardAbility.NONE ) );
        ksession.insert( new CardImpl( Allegiance.FELLOWSHIP,
                                       CardName.P3,
                                       3,
                                       CardAbility.NONE ) );
        ksession.insert( new CardImpl( Allegiance.FELLOWSHIP,
                                       CardName.P4,
                                       4,
                                       CardAbility.NONE ) );
        ksession.insert( new CardImpl( Allegiance.FELLOWSHIP,
                                       CardName.P5,
                                       5,
                                       CardAbility.NONE ) );
        ksession.insert( new CardImpl( Allegiance.FELLOWSHIP,
                                       CardName.NOBLE_SACRIFICE,
                                       0,
                                       CardAbility.SACRIFICE_BOTH ) );
        ksession.insert( new CardImpl( Allegiance.FELLOWSHIP,
                                       CardName.MAGIC,
                                       0,
                                       CardAbility.MAGIC ) );
        ksession.insert( new CardImpl( Allegiance.FELLOWSHIP,
                                       CardName.ELVEN_CLOAK,
                                       0,
                                       CardAbility.IGNORE_POWER ) );
        ksession.insert( new CardImpl( Allegiance.FELLOWSHIP,
                                       CardName.RETREAT,
                                       0,
                                       CardAbility.RETREAT_BACKWARDS ) );

        logger.debug( "Done." );
    }

}