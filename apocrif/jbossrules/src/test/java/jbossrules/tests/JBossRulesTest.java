package jbossrules.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.rule.Package;

import junit.framework.TestCase;
import apocrif.core.Ruleset;
import apocrif.engine.Driver;
import apocrif.engine.jbossrules.JBossRulesDriver;
import apocrif.engine.jbossrules.Rif2DrlTranslator;
import apocrif.io.DOMDeserializer;

public class JBossRulesTest extends TestCase {
    DOMDeserializer deserializer = new DOMDeserializer();

    public boolean testParser(String rifFileName,
                              String drlFileName,
                              String pkgName,
                              ClassLoader classLoader) throws Exception {
        Ruleset rifRuleset = deserializer.deserialize( new BufferedReader( new FileReader( new File( rifFileName ) ) ) );
        String actualDrlText = new Rif2DrlTranslator(classLoader).translateToString( rifRuleset, 
                                                                          pkgName );

        String expectedDrlText = readFileAsString( drlFileName );
        System.out.println( expectedDrlText );
        System.out.println( actualDrlText );

        assertEqualsIgnoreWhitespace( expectedDrlText,
                                      actualDrlText );

        return true;
    }

    public void testCreditscore1() throws Exception {
        final String XSD_FILE = "src/test/resources/xsd/mismo.xsd";
        final String DRL_FILE = "src/test/resources/drl/creditscore1.drl";
        final String RIF_FILE = "src/test/resources/xml/creditscore1.xml";
        //final String IMPLICIT_PCK = "mismo2";

//        File file = new File( "src/test/resources/creditscore.jar" );
//        ClassLoader classLoader = new URLClassLoader( new URL[] { file.toURL() } );
//        assertTrue( file.exists() );
        //classLoader.
        
        testParser( RIF_FILE,
                    DRL_FILE,
                    "creditscore",
                    Thread.currentThread().getContextClassLoader() );
    }

    public void testCreditscore2() throws Exception {
        final String XSD_FILE = "src/test/resources/xsd/mismo.xsd";
        final String DRL_FILE = "src/test/resources/drl/creditscore2.drl";
        final String RIF_FILE = "src/test/resources/xml/creditscore2.xml";
        //final String IMPLICIT_PCK = "mismo";

        testParser( RIF_FILE,
                    DRL_FILE,
                    "creditscore",
                    Thread.currentThread().getContextClassLoader() );
    }

    public void testCreditscore3() throws Exception {
        final String XSD_FILE = "src/test/resources/xsd/AUS_v2_4.xsd";
        //      final String XSD_FILE = "src/test/resources/xsd/CREDIT_RESPONSE_v2_3_1(MXCompliance).xsd";

        final String XML_FILE = "src/test/resources/xml/AUSMXARM.xml";
        final String DRL_FILE = "src/test/resources/drl/creditscore3.drl";
        final String RIF_FILE = "src/test/resources/xml/creditscore3.xml";
        //final String IMPLICIT_PCK = "mismo";

        testParser( RIF_FILE,
                    DRL_FILE,
                    "creditscore",
                    Thread.currentThread().getContextClassLoader() );
    }

    public void testMismo() throws Exception {
        final String XSD_FILE = "src/test/resources/xsd/AUS_v2_4.xsd";
        //		final String XSD_FILE = "data/xsd/CREDIT_RESPONSE_v2_3_1(MXCompliance).xsd";

        final String XML_FILE = "src/test/resources/xml/AUSMXARM.xml";
        final String IRL_FILE = "src/test/resources/irl/creditscore3.irl";
        final String RIF_FILE = "src/test/resources/xml/creditscore3.xml";
        //final String IMPLICIT_PCK = "mismo";

//        Reader reader = new BufferedReader( new FileReader( new File( RIF_FILE ) ) );
//        JBossRulesDriver driver = new JBossRulesDriver("mismo",
//                                                       Thread.currentThread().getContextClassLoader() ).toURL() } ) );
//        Package pkg = driver.readFromRifXml( reader );
//        RuleBase ruleBase = RuleBaseFactory.newRuleBase();
//        ruleBase.addPackage( pkg );
        
        JAXBContext jc = JAXBContext.newInstance("mismo");
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        
        Collection collection = (Collection)
        unmarshaller.unmarshal(new File( XML_FILE ) );
                        
        //executeDegradedRuleset(XSD_FILE, XML_FILE, IRL_FILE, RIF_FILE, IMPLICIT_PCK );
    }

    private void assertEqualsIgnoreWhitespace(final String expected,
                                              final String actual) {

        final String cleanExpected = expected.replaceAll( "\\s+",
                                                          "" );
        final String cleanActual = actual.replaceAll( "\\s+",
                                                      "" );

        assertEquals( cleanExpected,
                      cleanActual );
    }

    private static String readFileAsString(String filePath) throws java.io.IOException {
        StringBuffer fileData = new StringBuffer( 1000 );
        BufferedReader reader = new BufferedReader( new FileReader( filePath ) );
        char[] buf = new char[1024];
        int numRead = 0;
        while ( (numRead = reader.read( buf )) != -1 ) {
            String readresources = String.valueOf( buf,
                                                   0,
                                                   numRead );
            fileData.append( readresources );
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }
}