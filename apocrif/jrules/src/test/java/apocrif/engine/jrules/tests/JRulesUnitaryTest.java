package apocrif.engine.jrules.tests;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringWriter;

import junit.framework.Assert;
import junit.framework.TestCase;
import apocrif.engine.jrules.JRulesDriver;

/**
 * JUnit Test for mismo jrules implementation
 */
public class JRulesUnitaryTest extends TestCase {

    protected IlrReflect buildXmlReflect(String xsdName,
                                         String implicitPackage) throws IlrXmlErrorException,
                                                                FileNotFoundException {
        IlrXmlSchemaDriver xsdDriver = new IlrXmlDefaultDataDriver();
        xsdDriver.loadModel( IlrXmlHelper.openFile( xsdName ),
                             IlrXmlHelper.getNamespaceResolver( implicitPackage ) );
        return xsdDriver.getModel();
    }

    protected IlrRuleset roundTripRuleset(String xsdFile,
                                          String irlFile,
                                          String rifFile,
                                          String implicitPck) {
        try {
            IlrReflect reflect = buildXmlReflect( xsdFile,
                                                  implicitPck );
            IlrRuleset ruleset = new IlrRuleset( reflect );

            if ( ruleset.parseFileName( irlFile ) ) {
                boolean printIntermediateIrl = false;
                JRulesDriver driver = new JRulesDriver( reflect,
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
            } else {
                Assert.fail( "Error in IRL file" );
            }
        } catch ( Exception e ) {
            Assert.fail( "Exception raised " + e );
        }
        return null;
    }

    protected void executeRuleset(String xsdFile,
                                  String xmlDataFile,
                                  String irlFile,
                                  String rifFile,
                                  String implicitPck) {
        IlrRuleset ruleset = roundTripRuleset( xsdFile,
                                               irlFile,
                                               rifFile,
                                               implicitPck );
        try {
            IlrXmlDocumentDriver dataDriver = new IlrXmlDefaultDataDriver( ruleset.getReflect() );
            IlrXmlObject xmlObject = dataDriver.readObject( new FileReader( xmlDataFile ) );
            IlrContext engine = new IlrContext( ruleset );
            engine.insert( xmlObject );
            engine.fireAllRules();
        } catch ( Exception e ) {
            Assert.fail( "Exception raised " + e );
        }
    }

    protected void executeDegradedRuleset(String xsdFile,
                                          String xmlDataFile,
                                          String irlFile,
                                          String rifFile,
                                          String implicitPck) {
        try {
            IlrXmlDataDriver dataDriver = new IlrXmlDefaultDataDriver();
            dataDriver.loadModel( IlrXmlHelper.openFile( xsdFile ),
                                  IlrXmlHelper.getNamespaceResolver( implicitPck ) );
            IlrXmlObject xmlObject = dataDriver.readObject( new FileReader( xmlDataFile ) );
        } catch ( Exception e ) {
            Assert.fail( "Exception raised " + e );
        }

    }

    public void testSimplifiedMismoRules() {
        final String XSD_FILE = "data/xsd/mismo.xsd";
        final String IRL_FILE = "data/irl/mismo.irl";
        final String RIF_FILE = "data/xml/mismo.xml";
        final String IMPLICIT_PCK = "mismo";

        roundTripRuleset( XSD_FILE,
                          IRL_FILE,
                          RIF_FILE,
                          IMPLICIT_PCK );
    }

    public void testSimplifiedMismo2Rules() {
        final String XSD_FILE = "data/xsd/mismo.xsd";
        final String IRL_FILE = "data/irl/mismo2.irl";
        final String RIF_FILE = "data/xml/mismo2.xml";
        final String IMPLICIT_PCK = "mismo2";

        roundTripRuleset( XSD_FILE,
                          IRL_FILE,
                          RIF_FILE,
                          IMPLICIT_PCK );
    }

    public void testSimplifiedMismoJBossRules() {
        final String XSD_FILE = "data/xsd/mismo.xsd";
        final String IRL_FILE = "data/irl/mismoJBoss.irl";
        final String RIF_FILE = "data/xml/mismoJBoss.xml";
        final String IMPLICIT_PCK = "mismo";

        roundTripRuleset( XSD_FILE,
                          IRL_FILE,
                          RIF_FILE,
                          IMPLICIT_PCK );
    }

    public void testMismoRules() {
        final String XSD_FILE = "data/xsd/AUS_v2_4.xsd";
        //		final String XSD_FILE = "data/xsd/CREDIT_RESPONSE_v2_3_1(MXCompliance).xsd";

        final String XML_FILE = "data/xml/AUSMXARM.xml";
        final String IRL_FILE = "data/irl/mismo.irl";
        final String RIF_FILE = "data/xml/mismo.xml";
        final String IMPLICIT_PCK = "mismo";

        executeDegradedRuleset( XSD_FILE,
                                XML_FILE,
                                IRL_FILE,
                                RIF_FILE,
                                IMPLICIT_PCK );
    }

    public static void main(String[] args) {
        JRulesUnitaryTest test = new JRulesUnitaryTest();
    }
}
