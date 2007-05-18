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
    DOMSerializer   serializer   = new DOMSerializer();
    DOMDeserializer deserializer = new DOMDeserializer();

    public Package readFromRif(Ruleset rifRuleset) throws Exception {
        return new Rif2DrlTranslator().translateToPackage( rifRuleset );
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
