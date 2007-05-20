package apocrif.engine.jbossrules;

import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import org.drools.compiler.PackageBuilder;
import org.drools.rule.Package;

import apocrif.core.Ruleset;
import apocrif.engine.Driver;
import apocrif.io.DOMDeserializer;
import apocrif.io.DOMSerializer;

public class JBossRulesDriver
    implements
    Driver {
    private DOMSerializer   serializer   = new DOMSerializer();
    private DOMDeserializer deserializer = new DOMDeserializer();
    private String pkgName;
    private Class objectFactoryClass;
    private ClassLoader classLoader;
    
    
    public JBossRulesDriver(String pkgName,
                            Class objectFactoryClass,
                            ClassLoader classLoader) {
        this.pkgName = pkgName;
        this.objectFactoryClass = objectFactoryClass;
        this.classLoader = classLoader;
    }       

    public Package readFromRif(Ruleset rifRuleset) throws Exception {
        return new Rif2DrlTranslator(this.classLoader).translateToPackage( rifRuleset, pkgName, objectFactoryClass );
    }

    public Package readFromRifXml(Reader reader) throws Exception {
        Ruleset rifRuleset = deserializer.deserialize( reader );
        return readFromRif( rifRuleset );
    }

    public Ruleset writeToRif(Object er) throws Exception {
        throw new UnsupportedOperationException();
    }

    public void writeToRifXml(Object er,
                              Writer writer) throws Exception {
        throw new UnsupportedOperationException();
    }

}
