package apocrif.core;

import java.util.List;

import javax.xml.namespace.QName;

public class CoreHelper {
    public Ruleset createRuleset(String name) {
        return new Ruleset( name );
    }

    public LogicalRule createLogicalRule() {
        return new LogicalRule();
    }

    public AndCondition createAndCondition() {
        return new AndCondition();
    }

    public Variable createVariable(String name) {
        return new Variable( name );
    }

    public Variable createVariable(String name,
                                   QName type) {
        return new Variable( name,
                             type );
    }

    public Equal createEqual(Term leftTerm,
                             Term rightTerm) {
        return new Equal( leftTerm,
                          rightTerm );
    }

    public Uniterm createUniterm(Const operator) {
        return new Uniterm( operator );
    }

    public Uniterm createUniterm(Const operator,
                                 List<Term> arguments) {
        Uniterm term = new Uniterm( operator );
        for ( Term arg : arguments ) {
            term.addArgument( arg );
        }
        return term;
    }

    public Const createConst(String name) {
        return new Const( name );
    }

    public Const createConst(String name,
                             QName type) {
        return new Const( name,
                          type );
    }

}
