package org.drools.learner.examples;

import java.io.IOException;
import java.util.List;

import org.drools.examples.learner.DroolsLearnerExample;
import org.drools.examples.learner.models.Poker;
import org.drools.examples.learner.utils.CsvFileReader;
import org.junit.Ignore;
import org.junit.Test;

public class PokerInfiniteLoopTest {

    @Test
    public void pokerExampleDoesNotCauseInfiniteLoop() throws Exception {
        String trainingDataFile = System.getProperty( "user.dir" ) + "/src/main/resources/data/poker/poker.training.data.1000.csv";
        List<Object> pokerTrainingData = readCSV( trainingDataFile );

        DroolsLearnerExample pokerExample = new DroolsLearnerExample( DroolsLearnerExample.SINGLE_C45E );
        pokerExample.runTraningExample( Poker.class, pokerTrainingData );
    }

    @Test
    @Ignore
    public void pokerExampleCausesInfiniteLoop() throws Exception {
        String trainingDataFile = System.getProperty( "user.dir" ) + "/src/main/resources/data/poker/poker.training.data.1150.csv";
        List<Object> pokerTrainingData = readCSV( trainingDataFile );

        DroolsLearnerExample pokerExample = new DroolsLearnerExample( DroolsLearnerExample.SINGLE_C45E );
        pokerExample.runTraningExample( Poker.class, pokerTrainingData );
    }

    private List<Object> readCSV( String inputFile ) throws IOException {
        List<Object> pokerData = CsvFileReader.readObjects( inputFile, csv -> {
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

        return pokerData;
    }
}
