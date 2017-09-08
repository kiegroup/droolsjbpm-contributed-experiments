package org.drools.examples.learner;

import java.util.List;
import java.util.function.Function;

import org.apache.commons.csv.CSVRecord;
import org.drools.examples.learner.models.Poker;
import org.drools.examples.learner.utils.CsvFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Drools Learner example using a poker data set obtained from: https://archive.ics.uci.edu/ml/datasets/Poker+Hand
 */
public class PokerExampleInfiniteLoop {

    private static final Logger LOG = LoggerFactory.getLogger( PokerExampleInfiniteLoop.class );
    private static final boolean PRINT_DRL = true;

    public static void main( String... args ) throws Exception {

        String inputFile = System.getProperty( "user.dir" ) + "/src/main/resources/data/poker/poker.training.data.partial.csv";

        List<Object> pokerTrainingData = CsvFileReader.readObjects( inputFile, csv -> {
            Poker pokerHand = new Poker();
            pokerHand.setFirstCardSuit( Integer.parseInt( csv.get( "card_1_suit" ).trim() ) );
            pokerHand.setFirstCardRank( Integer.parseInt( csv.get( "card_1_rank" ).trim() ) );
            pokerHand.setSecondCardSuit( Integer.parseInt( csv.get( "card_2_suit" ).trim() ) );
            pokerHand.setSecondCardRank( Integer.parseInt( csv.get( "card_2_rank" ).trim() ) );
            pokerHand.setThirdCardSuit( Integer.parseInt( csv.get( "card_3_rank" ).trim() ) );
            pokerHand.setThirdCardRank( Integer.parseInt( csv.get( "card_3_rank" ).trim() ) );
            pokerHand.setFourthCarSuit( Integer.parseInt( csv.get( "card_4_suit" ).trim() ) );
            pokerHand.setFourthCardRank( Integer.parseInt( csv.get( "card_4_rank" ).trim() ) );
            pokerHand.setFifthCardSuit( Integer.parseInt( csv.get( "card_5_suit" ).trim() ) );
            pokerHand.setFifthCardRank( Integer.parseInt( csv.get( "card_5_rank" ).trim() ) );
            pokerHand.setClassification( Integer.parseInt( csv.get( "classification" ).trim() ) );
            return pokerHand;
        } );

        List<Object> pokerTestData = CsvFileReader.readObjects( inputFile, csv -> {
            Poker pokerHand = new Poker();
            pokerHand.setFirstCardSuit( Integer.parseInt( csv.get( "card_1_suit" ).trim() ) );
            pokerHand.setFirstCardRank( Integer.parseInt( csv.get( "card_1_rank" ).trim() ) );
            pokerHand.setSecondCardSuit( Integer.parseInt( csv.get( "card_2_suit" ).trim() ) );
            pokerHand.setSecondCardRank( Integer.parseInt( csv.get( "card_2_rank" ).trim() ) );
            pokerHand.setThirdCardSuit( Integer.parseInt( csv.get( "card_3_rank" ).trim() ) );
            pokerHand.setThirdCardRank( Integer.parseInt( csv.get( "card_3_rank" ).trim() ) );
            pokerHand.setFourthCarSuit( Integer.parseInt( csv.get( "card_4_suit" ).trim() ) );
            pokerHand.setFourthCardRank( Integer.parseInt( csv.get( "card_4_rank" ).trim() ) );
            pokerHand.setFifthCardSuit( Integer.parseInt( csv.get( "card_5_suit" ).trim() ) );
            pokerHand.setFifthCardRank( Integer.parseInt( csv.get( "card_5_rank" ).trim() ) );
            pokerHand.setClassification( Integer.parseInt( csv.get( "classification" ).trim() ) );
            return pokerHand;
        } );

        DroolsLearnerExample pokerExample = new DroolsLearnerExample( DroolsLearnerExample.SINGLE_C45E );
        pokerExample.runTraningExample( Poker.class, pokerTrainingData );

        if ( PRINT_DRL ) {
            System.out.println( pokerExample.getDrl() );
        }

        pokerExample.runGeneratedRules( pokerTestData );
    }
}