package jbossrules.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import mismo.AUSBORROWERType;
import mismo.AUSLOANAPPLICATIONType;
import mismo.AUSLoanDocumentationTypeEnumerated;

import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.StatelessSession;
import org.drools.StatelessSessionResult;
import org.drools.event.DebugAgendaEventListener;
import org.drools.rule.Package;

import apocrif.core.Ruleset;
import apocrif.engine.jbossrules.JBossRulesDriver;
import apocrif.engine.jbossrules.Rif2DrlTranslator;
import apocrif.io.DOMDeserializer;

import com.sun.xml.bind.v2.runtime.JAXBContextImpl;

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

    public void testAUSMXFix() throws Exception {
        final String XML_FILE = "src/test/resources/xml/AUSMXFix.xml";
        final String RIF_FILE = "src/test/resources/xml/AUPricing_RIF_6.5.2.xml";

        JAXBContextImpl jc = (JAXBContextImpl) JAXBContext.newInstance( "mismo" );
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        JAXBElement element = ( JAXBElement ) unmarshaller.unmarshal( new File( XML_FILE ) );
        AUSLOANAPPLICATIONType application = ( AUSLOANAPPLICATIONType) element.getValue();

        JBossRulesDriver driver = new JBossRulesDriver( "mismo",
                                                        mismo.ObjectFactory.class,
                                                        Thread.currentThread().getContextClassLoader() );

        Reader reader = new BufferedReader( new FileReader( new File( RIF_FILE ) ) );

        Package pkg = driver.readFromRifXml( reader );
        RuleBase ruleBase = RuleBaseFactory.newRuleBase();
        ruleBase.addPackage( pkg );
        
        assertEquals( new Double( 6.750 ), application.getMORTGAGETERMS().getRequestedInterestRatePercent() );
        StatelessSession session = ruleBase.newStatelessSession();
        session.addEventListener( new DebugAgendaEventListener() );
        
        StatelessSessionResult results = session.executeWithResults( new Object[] { application }  );        

        AUSLOANAPPLICATIONType returnedApplication = ( AUSLOANAPPLICATIONType ) results.iterateObjects().next();
        
        assertEquals( new Double( 11.9 ), returnedApplication.getMORTGAGETERMS().getRequestedInterestRatePercent() );
        
        // This is how you "write" the results back to a stream using jaxb marshalling.
        Marshaller marshaller = jc.createMarshaller();        
        marshaller.marshal( new JAXBElement( new QName("mismo", "AUSLOANAPPLICATIONType"),returnedApplication.getClass(), returnedApplication ), System.out );
    }

    public void testAUSMXARM() throws Exception {
        final String XML_FILE = "src/test/resources/xml/AUSMXARM.xml";
        final String RIF_FILE = "src/test/resources/xml/AUPricing_RIF_6.5.2.xml";

        JAXBContextImpl jc = (JAXBContextImpl) JAXBContext.newInstance( "mismo" );
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        JAXBElement element = ( JAXBElement ) unmarshaller.unmarshal( new File( XML_FILE ) );
        AUSLOANAPPLICATIONType application = ( AUSLOANAPPLICATIONType) element.getValue();

        JBossRulesDriver driver = new JBossRulesDriver( "mismo",
                                                        mismo.ObjectFactory.class,
                                                        Thread.currentThread().getContextClassLoader() );

        Reader reader = new BufferedReader( new FileReader( new File( RIF_FILE ) ) );

        Package pkg = driver.readFromRifXml( reader );
        RuleBase ruleBase = RuleBaseFactory.newRuleBase();
        ruleBase.addPackage( pkg );
        
        assertEquals( new Double( 5.0 ), application.getMORTGAGETERMS().getRequestedInterestRatePercent() );
        StatelessSession session = ruleBase.newStatelessSession();
        session.addEventListener( new DebugAgendaEventListener() );
        
        StatelessSessionResult results = session.executeWithResults( new Object[] { application }  );        

        AUSLOANAPPLICATIONType returnedApplication = ( AUSLOANAPPLICATIONType ) results.iterateObjects().next();
        
        assertEquals( new Double( 15.6 ), returnedApplication.getMORTGAGETERMS().getRequestedInterestRatePercent() );
        
        // This is how you "write" the results back to a stream using jaxb marshalling.
        Marshaller marshaller = jc.createMarshaller();        
        marshaller.marshal( new JAXBElement( new QName("mismo", "AUSLOANAPPLICATIONType"),returnedApplication.getClass(), returnedApplication ), System.out );
    }
    
    public void testAUSMXFHA() throws Exception {
        final String XML_FILE = "src/test/resources/xml/AUSMXFHA.xml";
        final String RIF_FILE = "src/test/resources/xml/AUPricing_RIF_6.5.2.xml";

        JAXBContextImpl jc = (JAXBContextImpl) JAXBContext.newInstance( "mismo" );
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        JAXBElement element = ( JAXBElement ) unmarshaller.unmarshal( new File( XML_FILE ) );
        AUSLOANAPPLICATIONType application = ( AUSLOANAPPLICATIONType) element.getValue();

        JBossRulesDriver driver = new JBossRulesDriver( "mismo",
                                                        mismo.ObjectFactory.class,
                                                        Thread.currentThread().getContextClassLoader() );

        Reader reader = new BufferedReader( new FileReader( new File( RIF_FILE ) ) );

        Package pkg = driver.readFromRifXml( reader );
        RuleBase ruleBase = RuleBaseFactory.newRuleBase();
        ruleBase.addPackage( pkg );
        
        assertEquals( new Double( 6.0 ), application.getMORTGAGETERMS().getRequestedInterestRatePercent() );
        StatelessSession session = ruleBase.newStatelessSession();
        session.addEventListener( new DebugAgendaEventListener() );
        
        StatelessSessionResult results = session.executeWithResults( new Object[] { application }  );        

        AUSLOANAPPLICATIONType returnedApplication = ( AUSLOANAPPLICATIONType ) results.iterateObjects().next();
        
        assertEquals( new Double( 12.9 ), returnedApplication.getMORTGAGETERMS().getRequestedInterestRatePercent() );
        
        // This is how you "write" the results back to a stream using jaxb marshalling.
        Marshaller marshaller = jc.createMarshaller();        
        marshaller.marshal( new JAXBElement( new QName("mismo", "AUSLOANAPPLICATIONType"),returnedApplication.getClass(), returnedApplication ), System.out );
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