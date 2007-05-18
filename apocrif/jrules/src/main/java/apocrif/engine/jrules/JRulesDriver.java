package apocrif.engine.jrules;

import java.io.Reader;
import java.io.Writer;

import ilog.rules.engine.*;
import ilog.rules.factory.*;
import apocrif.engine.*;
import apocrif.io.*;
import apocrif.core.*;
import apocrif.pr.*;

public class JRulesDriver
    implements
    Driver<IlrRuleset> {
    Irl2RifTranslator irlTranslator = new Irl2RifTranslator();
    Rif2IrlTranslator rifTranslator;
    DOMSerializer     serializer    = new DOMSerializer();
    DOMDeserializer   deserializer  = new DOMDeserializer();

    public JRulesDriver(IlrReflect xom,
                        boolean printIntermediateIrl) throws Exception {
        rifTranslator = new Rif2IrlTranslator( xom,
                                               printIntermediateIrl );
    }

    public IlrRuleset readFromRif(Ruleset rifRuleset) throws Exception {
        return rifTranslator.translate( rifRuleset );
    }

    public Ruleset writeToRif(IlrRuleset er) throws Exception {
        return irlTranslator.translate( er );
    }

    public IlrRuleset readFromRifXml(Reader reader) throws Exception {
        Ruleset rifRuleset = deserializer.deserialize( reader );
        return readFromRif( rifRuleset );
    }

    public void writeToRifXml(IlrRuleset er,
                              Writer writer) throws Exception {
        Ruleset rifRuleset = writeToRif( er );
        serializer.serialize( rifRuleset,
                              writer );
    }

}
