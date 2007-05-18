package apocrif.engine;

import java.io.Reader;
import java.io.Writer;

import apocrif.core.Ruleset;

public interface Driver<EngineRuleset> {
    Ruleset writeToRif(EngineRuleset er) throws Exception;

    EngineRuleset readFromRif(Ruleset rifRuleset) throws Exception;

    void writeToRifXml(EngineRuleset er,
                       Writer writer) throws Exception;

    EngineRuleset readFromRifXml(Reader reader) throws Exception;
}
