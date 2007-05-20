package jbossrules.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import mismo.LOANAPPLICATION;
import mismo.ObjectFactory;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.StatelessSession;
import org.drools.StatelessSessionResult;
import org.drools.rule.Package;

import com.sun.xml.bind.v2.runtime.JAXBContextImpl;

import junit.framework.TestCase;
import apocrif.core.Ruleset;
import apocrif.engine.Driver;
import apocrif.engine.jbossrules.JBossRulesDriver;
import apocrif.engine.jbossrules.Rif2DrlTranslator;
import apocrif.io.DOMDeserializer;
import javax.xml.namespace.QName;

public class JBossRulesTest extends TestCase {
    DOMDeserializer deserializer = new DOMDeserializer();

    public boolean testParser(String rifFileName,
                              String drlFileName,
                              String pkgName,
                              Class objectTypeFactory,
                              ClassLoader classLoader) throws Exception {
        Ruleset rifRuleset = deserializer.deserialize( new BufferedReader( new FileReader( new File( rifFileName ) ) ) );
        String actualDrlText = new Rif2DrlTranslator( classLoader ).translateToString( rifRuleset,
                                                                                       pkgName,
                                                                                       objectTypeFactory );

        String expectedDrlText = readFileAsString( drlFileName );
//        System.out.println( expectedDrlText );
//        System.out.println( actualDrlText );

        assertEqualsIgnoreWhitespace( expectedDrlText,
                                      actualDrlText );

        return true;
    }

    public void testCreditscore1() throws Exception {
        final String DRL_FILE = "src/test/resources/drl/creditscore1.drl";
        final String RIF_FILE = "src/test/resources/xml/creditscore1.xml";

        testParser( RIF_FILE,
                    DRL_FILE,
                    "creditscore",
                    creditscore.ObjectFactory.class,
                    Thread.currentThread().getContextClassLoader() );
    }

    public void testCreditscore2() throws Exception {
        final String DRL_FILE = "src/test/resources/drl/creditscore2.drl";
        final String RIF_FILE = "src/test/resources/xml/creditscore2.xml";

        testParser( RIF_FILE,
                    DRL_FILE,
                    "creditscore",
                    creditscore.ObjectFactory.class,
                    Thread.currentThread().getContextClassLoader() );
    }

    public void testCreditscore3() throws Exception {
        final String DRL_FILE = "src/test/resources/drl/creditscore3.drl";
        final String RIF_FILE = "src/test/resources/xml/creditscore3.xml";

        testParser( RIF_FILE,
                    DRL_FILE,
                    "creditscore",
                    creditscore.ObjectFactory.class,
                    Thread.currentThread().getContextClassLoader() );
    }

    public void testMismo() throws Exception {
        final String XML_FILE = "src/test/resources/xml/AUSMXARM.xml";
        final String RIF_FILE = "src/test/resources/xml/simpleTest.xml";

        JAXBContextImpl jc = (JAXBContextImpl) JAXBContext.newInstance( "mismo" );
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        LOANAPPLICATION application = (LOANAPPLICATION) unmarshaller.unmarshal( new File( XML_FILE ) );

        JBossRulesDriver driver = new JBossRulesDriver( "mismo",
                                                        mismo.ObjectFactory.class,
                                                        Thread.currentThread().getContextClassLoader() );

        Reader reader = new BufferedReader( new FileReader( new File( RIF_FILE ) ) );

        Package pkg = driver.readFromRifXml( reader );
        RuleBase ruleBase = RuleBaseFactory.newRuleBase();
        ruleBase.addPackage( pkg );

        // check values before executing the rule session
        assertEquals( "B1", application.getREOPROPERTY().getBorrowerID() );
        
        StatelessSession session = ruleBase.newStatelessSession();
        StatelessSessionResult results = session.executeWithResults( new Object[] { application }  );

        LOANAPPLICATION returnedApplication = ( LOANAPPLICATION ) results.iterateObjects().next();
        
        // check vlaues after executing the rule session
        assertEquals( "B2", returnedApplication.getREOPROPERTY().getBorrowerID() );
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