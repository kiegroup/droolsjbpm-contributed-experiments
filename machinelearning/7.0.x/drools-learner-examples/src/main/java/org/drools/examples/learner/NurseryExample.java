package org.drools.examples.learner;

import java.util.List;

import org.drools.examples.learner.models.Nursery;
import org.drools.examples.learner.utils.CsvFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NurseryExample {

    private static final Logger LOG = LoggerFactory.getLogger( NurseryExample.class );
    private static final boolean PRINT_DRL = true;

    public static void main( String... args ) throws Exception {
        String inputFile = System.getProperty( "user.dir" ) + "/src/main/resources/data/nursery/nursery.data.csv";
        List<Object> nursureyData = CsvFileReader.readObjects( inputFile, csv -> {
            Nursery nursery = new Nursery();
            nursery.setParents( csv.get( "parents" ).trim() );
            nursery.setHas_nurs( csv.get( "has_nurs" ).trim() );
            nursery.setChildren( csv.get( "children" ).trim() );
            nursery.setForm( csv.get( "form" ).trim() );
            nursery.setHousing( csv.get( "housing" ).trim() );
            nursery.setFinance( csv.get( "finance" ).trim() );
            nursery.setSocial( csv.get( "social" ).trim() );
            nursery.setHealth( csv.get( "health" ).trim() );
            nursery.setClassnursery( csv.get( "classnursery" ) );
            return nursery;
        } );

        DroolsLearnerExample nurseryExample = new DroolsLearnerExample( DroolsLearnerExample.SINGLE_C45E );
        nurseryExample.runTraningExample( Nursery.class, nursureyData );

        if ( PRINT_DRL ) {
            System.out.println( nurseryExample.getDrl() );
        }

        nurseryExample.runGeneratedRules( nursureyData );
    }
}