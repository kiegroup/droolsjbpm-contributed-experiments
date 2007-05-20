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
                    creditscore.ObjectFactory.class,
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
                    creditscore.ObjectFactory.class,
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
                    creditscore.ObjectFactory.class,
                    Thread.currentThread().getContextClassLoader() );
    }

    public void testMismo() throws Exception {
        final String XSD_FILE = "src/test/resources/xsd/AUS_v2_4.xsd";
        //		final String XSD_FILE = "data/xsd/CREDIT_RESPONSE_v2_3_1(MXCompliance).xsd";

        final String XML_FILE = "src/test/resources/xml/AUSMXARM.xml";
        //final String IRL_FILE = "src/test/resources/drl/test.irl";
        final String RIF_FILE = "src/test/resources/xml/test.xml";

        Ruleset rifRuleset = deserializer.deserialize( new BufferedReader( new FileReader( new File( RIF_FILE ) ) ) );
        String actualDrlText = new Rif2DrlTranslator( Thread.currentThread().getContextClassLoader() ).translateToString( rifRuleset,
                                                                                                                          "mismo",
                                                                                                                          mismo.ObjectFactory.class );

        System.out.println( actualDrlText );

        JAXBContextImpl jc = (JAXBContextImpl) JAXBContext.newInstance( "mismo" );
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        LOANAPPLICATION application = (LOANAPPLICATION) unmarshaller.unmarshal( new File( XML_FILE ) );

        //jc.
        //System.out.println( jc.createJAXBIntrospector().( new QName("mismo", "LOAN_APPLICATION") ) );

        //                for ( Method method : ObjectFactory.class.getMethods() ) {
        //                    if ( method.getName().startsWith( "create" ) ) {
        //                        String name = method.getName().substring( 6 );
        //                        Class clazz = ObjectFactory.class.getClassLoader().loadClass( ObjectFactory.class.getPackage().getName() + "." + name );
        //                        XmlRootElement xmlRootElement = ( XmlRootElement ) clazz.getAnnotation( XmlRootElement.class );
        //                        System.out.println( clazz.getName()  + " = " + xmlRootElement.name() );
        //                    }
        //                }
        //                
//        System.out.println( jc.getGlobalType( new QName( "mismo",
//                                                         "LOAN_APPLICATION" ) ) );

        JBossRulesDriver driver = new JBossRulesDriver( "mismo",
                                                        mismo.ObjectFactory.class,
                                                        Thread.currentThread().getContextClassLoader() );

        Reader reader = new BufferedReader( new FileReader( new File( RIF_FILE ) ) );

        Package pkg = driver.readFromRifXml( reader );
        RuleBase ruleBase = RuleBaseFactory.newRuleBase();
        ruleBase.addPackage( pkg );
        
        StatelessSession session = ruleBase.newStatelessSession();
        StatelessSessionResult results = session.executeWithResults( new Object[] { application }  );

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