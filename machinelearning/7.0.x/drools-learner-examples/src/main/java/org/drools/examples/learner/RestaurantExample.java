package org.drools.examples.learner;

import java.util.List;

import org.drools.examples.learner.models.Restaurant;
import org.drools.examples.learner.utils.CsvFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestaurantExample {

    private static final Logger LOG = LoggerFactory.getLogger( RestaurantExample.class );
    private static final boolean PRINT_DRL = true;

    public static void main( String... args ) throws Exception {

        String inputFile = System.getProperty( "user.dir" ) + "/src/main/resources/data/restauraunt/restauraunt.data.csv";
        List<Object> restaurantData = CsvFileReader.readObjects( inputFile, csv -> {
            Restaurant restauraunt = new Restaurant();
            restauraunt.setAlternate( Boolean.parseBoolean( csv.get( "alternate" ).trim() ) );
            restauraunt.setBar( Boolean.parseBoolean( csv.get( "bar" ).trim() ) );
            restauraunt.setFriSat( Boolean.parseBoolean( csv.get( "fri_sat" ).trim() ) );
            restauraunt.setHungry( Boolean.parseBoolean( csv.get( "hungry" ).trim() ) );
            restauraunt.setPatrons( csv.get( "patrons" ) );
            restauraunt.setPrice( Integer.parseInt( csv.get( "price" ).trim() ) );
            restauraunt.setRaining( Boolean.parseBoolean( csv.get( "raining" ) ) );
            restauraunt.setReservation( Boolean.parseBoolean( csv.get( "reservation" ).trim() ) );
            restauraunt.setType( csv.get( "type" ).trim() );
            restauraunt.setWaitEstimate( csv.get( "wait_estimate" ).trim() );
            restauraunt.setWillWait( Boolean.parseBoolean( csv.get( "will_wait" ).trim() ) );
            return restauraunt;
        } );

        DroolsLearnerExample carExample = new DroolsLearnerExample( DroolsLearnerExample.SINGLE_C45E );
        carExample.runTraningExample( Restaurant.class, restaurantData );

        if ( PRINT_DRL ) {
            System.out.println( carExample.getDrl() );
        }
        carExample.runGeneratedRules( restaurantData );
    }

}