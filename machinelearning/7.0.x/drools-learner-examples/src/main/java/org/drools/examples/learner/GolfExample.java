package org.drools.examples.learner;

import java.util.List;

import org.drools.examples.learner.models.Car;
import org.drools.examples.learner.models.Golf;
import org.drools.examples.learner.utils.CsvFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GolfExample {

    private static final Logger LOG = LoggerFactory.getLogger( GolfExample.class );
    private static final boolean PRINT_DRL = true;

    public static void main( String... args ) throws Exception {

        String inputFile = System.getProperty( "user.dir" ) + "/src/main/resources/data/golf/golf.data.csv";
        List<Object> golfData = CsvFileReader.readObjects( inputFile, csv -> {
            Golf golf = new Golf();
            golf.setOutlook( csv.get( "outlook" ).trim() );
            golf.setTemperature( Integer.parseInt( csv.get( "temperature" ).trim() ) );
            golf.setHumidity( Integer.parseInt( csv.get( "humidity" ).trim() ) );
            golf.setWindy( Boolean.parseBoolean( csv.get( "windy" ).trim() ) );
            golf.setDecision( csv.get( "decision" ).trim() );
            return golf;
        } );

        DroolsLearnerExample carExample = new DroolsLearnerExample( DroolsLearnerExample.SINGLE_C45E );
        carExample.runTraningExample( Golf.class, golfData );

        if ( PRINT_DRL ) {
            System.out.println( carExample.getDrl() );
        }

        carExample.runGeneratedRules( golfData );
    }

}