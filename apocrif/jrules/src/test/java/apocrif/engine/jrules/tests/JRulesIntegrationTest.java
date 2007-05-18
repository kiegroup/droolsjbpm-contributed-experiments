package apocrif.engine.jrules.tests;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.jar.JarInputStream;

import junit.framework.Assert;
import junit.framework.TestCase;
import apocrif.engine.jrules.JRulesDriver;

/**
 * JUnit Test for mismo jrules implementation
 */
public class JRulesIntegrationTest extends TestCase {

    protected IlrRuleset createJarRuleset(String filename) {
        try {
            JarInputStream is = new JarInputStream( new FileInputStream( filename ) );
            IlrRulesetArchiveLoader rulesetloader = new IlrJarArchiveLoader( is );
            IlrRulesetArchiveParser rulesetparser = new IlrRulesetArchiveParser();
            boolean parsed = rulesetparser.parseArchive( rulesetloader );
            if ( !parsed ) {
                System.out.println( "** Errors detected for archive " + filename );
                return null;
            }
            return rulesetparser.getRuleset();
        } catch ( IOException e ) {
            System.out.println( "** Error: File " + filename + " loadind raises the exception " + e );
            return null;
        }

    }

    protected IlrRuleset roundTripRuleset(IlrRuleset ruleset,
                                          String rifFile) {
        try {
            System.out.println( "--------Original ruleset--------" );
            System.out.println( ruleset.makeFactory().toLanguage() );
            boolean printIntermediateIrl = true;
            JRulesDriver driver = new JRulesDriver( ruleset.getReflect(),
                                                    printIntermediateIrl );
            Ruleset rifRuleset = driver.writeToRif( ruleset );
            Assert.assertNotNull( "Test 1",
                                  rifRuleset );

            IlrRuleset ruleset2 = driver.readFromRif( rifRuleset );
            Assert.assertNotNull( "Test 2",
                                  ruleset2 );
            System.out.println( "--------Meta-model round tripping--------" );
            System.out.println( ruleset2.makeFactory().toLanguage() );

            // round trip
            StringWriter strw = new StringWriter();
            FileWriter fw = new FileWriter( rifFile );
            driver.writeToRifXml( ruleset,
                                  strw );
            driver.writeToRifXml( ruleset,
                                  fw );
            fw.flush();
            System.out.println( "--------XML document--------" );
            System.out.println( strw.toString() );
            FileReader fr = new FileReader( rifFile );
            IlrRuleset ruleset3 = driver.readFromRifXml( fr );
            Assert.assertNotNull( "Test 4",
                                  ruleset3 );
            System.out.println( "--------Meta-model + XML round tripping--------" );
            System.out.println( ruleset3.makeFactory().toLanguage() );
            return ruleset3;

        } catch ( Exception e ) {
            Assert.fail( "Exception raised " + e );
        }
        return null;
    }

    protected void executeRuleset(String archiveFile,
                                  String xmlDataFile,
                                  String rifFile) {
        IlrRuleset ruleset = createJarRuleset( archiveFile );
        IlrRuleset ruleset2 = roundTripRuleset( ruleset,
                                                rifFile );
        try {
            IlrXmlDocumentDriver dataDriver = new IlrXmlDefaultDataDriver( ruleset.getReflect() );
            IlrContext engine = new IlrContext( ruleset2 );

            if ( xmlDataFile != null ) {
                IlrXmlObject xmlObject = dataDriver.readObject( new FileReader( xmlDataFile ) );
                engine.insert( xmlObject );
            }
            engine.fireAllRules();
        } catch ( Exception e ) {
            Assert.fail( "Exception raised " + e );
        }
    }

    public void testAUPricingRules() {
        executeRuleset( "data/archive/AUPricing5RulesetArchive.jar",
                        null,
                        "data/xml/AUPricing.xml" );
    }

    public static void main(String[] args) {
        JRulesIntegrationTest test = new JRulesIntegrationTest();
    }
}
