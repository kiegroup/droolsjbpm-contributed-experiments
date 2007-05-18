package apocrif.pr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import apocrif.core.Condition;
import apocrif.core.Const;
import apocrif.core.CoreHelper;
import apocrif.core.Rule;
import apocrif.core.Term;
import apocrif.core.Uniterm;
import apocrif.core.Variable;

public class PRHelper extends CoreHelper {
    public static final String XSD_NAMESPACE     = "http://www.w3.org/2001/XMLSchema";

    public static final String XPATH_NAMESPACE   = "http://www.w3.org/2005/xpath-functions";

    public static final String APOCRIF_NAMESPACE = "http://apocrif/pr";

    public static final QName  qNameType         = new QName( XSD_NAMESPACE,
                                                              "QName" );

    public static final QName  stringType        = new QName( XSD_NAMESPACE,
                                                              "string" );

    public static final Const  xmlGetterOp       = createUnitermName( APOCRIF_NAMESPACE,
                                                                      "xmlGetter" );

    public static final Const  xmlSetterOp       = createUnitermName( APOCRIF_NAMESPACE,
                                                                      "xmlSetter" );

    public static final Const  varConstraintOp   = createUnitermName( APOCRIF_NAMESPACE,
                                                                      "variableConstraintOp" );

    public static final Const  inGeneratorOp     = createUnitermName( APOCRIF_NAMESPACE,
                                                                      "inGeneratorOp" );

    public static final Const  fromGeneratorOp   = createUnitermName( APOCRIF_NAMESPACE,
                                                                      "fromGeneratorOp" );

    public static final Const  plusBinaryOp      = createUnitermName( XPATH_NAMESPACE,
                                                                      "numeric-add" );

    public static final Const  minusBinaryOp     = createUnitermName( XPATH_NAMESPACE,
                                                                      "numeric-subtract" );

    public static final Const  notEqualBinaryOp  = createUnitermName( APOCRIF_NAMESPACE,
                                                                      "ne" );

    public static final Const  greaterOp         = createUnitermName( XPATH_NAMESPACE,
                                                                      "numeric-greater-than" );

    public static final Const  greaterEqualOp    = createUnitermName( APOCRIF_NAMESPACE,
                                                                      "ge" );

    public static final Const  lowerOp           = createUnitermName( XPATH_NAMESPACE,
                                                                      "numeric-less-than" );

    public static final Const  lowerEqualOp      = createUnitermName( APOCRIF_NAMESPACE,
                                                                      "le" );

    public static final Const  updateOp          = createUnitermName( APOCRIF_NAMESPACE,
                                                                      "updateOp" );

    public PRHelper() {
    }

    protected static Const createUnitermName(String namespace,
                                             String name) {
        return new Const( new QName( namespace,
                                     name ).toString(),
                          qNameType );
    }

    public ProductionRule createProductionRule(String name) {
        return new ProductionRule( name );
    }

    public Uniterm createPlusTerm(Term leftTerm,
                                  Term rightTerm) {
        Uniterm uniterm = createUniterm( plusBinaryOp );
        uniterm.addArgument( leftTerm );
        uniterm.addArgument( rightTerm );
        return uniterm;
    }

    public Uniterm createMinusTerm(Term leftTerm,
                                   Term rightTerm) {
        Uniterm uniterm = createUniterm( minusBinaryOp );
        uniterm.addArgument( leftTerm );
        uniterm.addArgument( rightTerm );
        return uniterm;
    }

    public Uniterm createNotEqualTerm(Term leftTerm,
                                      Term rightTerm) {
        Uniterm uniterm = createUniterm( notEqualBinaryOp );
        uniterm.addArgument( leftTerm );
        uniterm.addArgument( rightTerm );
        return uniterm;
    }

    public Uniterm createGreaterTerm(Term leftTerm,
                                     Term rightTerm) {
        Uniterm uniterm = createUniterm( greaterOp );
        uniterm.addArgument( leftTerm );
        uniterm.addArgument( rightTerm );
        return uniterm;
    }

    public Uniterm createGreaterEqualTerm(Term leftTerm,
                                          Term rightTerm) {
        Uniterm uniterm = createUniterm( greaterEqualOp );
        uniterm.addArgument( leftTerm );
        uniterm.addArgument( rightTerm );
        return uniterm;
    }

    public Uniterm createLowerTerm(Term leftTerm,
                                   Term rightTerm) {
        Uniterm uniterm = createUniterm( lowerOp );
        uniterm.addArgument( leftTerm );
        uniterm.addArgument( rightTerm );
        return uniterm;
    }

    public Uniterm createLowerEqualTerm(Term leftTerm,
                                        Term rightTerm) {
        Uniterm uniterm = createUniterm( lowerEqualOp );
        uniterm.addArgument( leftTerm );
        uniterm.addArgument( rightTerm );
        return uniterm;
    }

    public Uniterm createUpdateTerm(Update modify) {
        Uniterm uniterm = createUniterm( updateOp );
        uniterm.addArgument( modify.getTarget() );
        uniterm.addArgument( modify.getStatement() );
        return uniterm;
    }

    public boolean isStringConst(Const c) {
        return (stringType.equals( c.getType() ));
    }

    public Term createXmlFieldSetter(XmlFieldSetter xmlFieldSetter) {
        Uniterm uniterm = createUniterm( xmlSetterOp );
        uniterm.addArgument( createConst( xmlFieldSetter.xmlDeclaringCType.toString(),
                                          qNameType ) );
        uniterm.addArgument( createConst( xmlFieldSetter.xmlField.toString() ) );
        uniterm.addArgument( xmlFieldSetter.targetTerm );
        uniterm.addArgument( xmlFieldSetter.newTerm );
        return uniterm;
    }

    public Term createXmlFieldGetter(XmlFieldGetter xmlFieldGetter) {
        Uniterm uniterm = createUniterm( xmlGetterOp );
        uniterm.addArgument( createConst( xmlFieldGetter.xmlDeclaringCType.toString(),
                                          qNameType ) );
        uniterm.addArgument( createConst( xmlFieldGetter.xmlField.toString() ) );
        uniterm.addArgument( xmlFieldGetter.targetTerm );
        return uniterm;
    }

    public Variable declareVariable(Rule rule,
                                    String name,
                                    QName xmlType) {
        Variable v = createVariable( name,
                                     xmlType );
        rule.addVariable( v );
        return v;
    }

    public Variable declareInVariable(ProductionRule rule,
                                      String name,
                                      QName xmlType,
                                      Term inTerm) {
        Variable v = createVariable( name,
                                     xmlType );
        rule.addVariable( v );

        Uniterm uniterm = createUniterm( inGeneratorOp );
        uniterm.addArgument( v );
        uniterm.addArgument( inTerm );
        rule.addConstraint( uniterm );
        return v;
    }

    public Variable declareFromVariable(ProductionRule rule,
                                        String name,
                                        QName xmlType,
                                        Term fromTerm) {
        Variable v = createVariable( name,
                                     xmlType );
        rule.addVariable( v );

        Uniterm uniterm = createUniterm( fromGeneratorOp );
        uniterm.addArgument( v );
        uniterm.addArgument( fromTerm );
        rule.addConstraint( uniterm );
        return v;
    }

    public Variable declareVariable(ProductionRule rule,
                                    String name,
                                    Term initTerm) {
        Variable v = createVariable( name );
        if ( initTerm != null ) {
            Uniterm uniterm = createUniterm( varConstraintOp );
            uniterm.addArgument( v );
            uniterm.addArgument( initTerm );
            rule.addConstraint( uniterm );
        }
        rule.addVariable( v );
        return v;
    }

    public Map<String, Term> getVariableInTerm(ProductionRule rule) {
        Map<String, Term> varName2InTerm = new HashMap<String, Term>();
        for ( Condition c : rule.getConstraintPart().getConditions() ) {
            Uniterm uniterm = (Uniterm) c;
            if ( uniterm.getOperator().equals( inGeneratorOp ) ) {
                Variable v0 = (Variable) uniterm.getArguments().get( 0 );
                Term inTerm = (Term) uniterm.getArguments().get( 1 );
                varName2InTerm.put( v0.getName(),
                                    inTerm );
            }
        }
        return varName2InTerm;
    }

    public Map<String, Term> getVariableFromTerm(ProductionRule rule) {
        Map<String, Term> varName2FromTerm = new HashMap<String, Term>();
        for ( Condition c : rule.getConstraintPart().getConditions() ) {
            Uniterm uniterm = (Uniterm) c;
            if ( uniterm.getOperator().equals( fromGeneratorOp ) ) {
                Variable v0 = (Variable) uniterm.getArguments().get( 0 );
                Term fromTerm = (Term) uniterm.getArguments().get( 1 );
                varName2FromTerm.put( v0.getName(),
                                      fromTerm );
            }
        }
        return varName2FromTerm;
    }

    public Map<String, Term> getVariableConstraints(ProductionRule rule) {
        Map<String, Term> varName2InitTerm = new HashMap<String, Term>();
        for ( Condition c : rule.getConstraintPart().getConditions() ) {
            Uniterm uniterm = (Uniterm) c;
            if ( uniterm.getOperator().equals( varConstraintOp ) ) {
                Variable v0 = (Variable) uniterm.getArguments().get( 0 );
                Term initTerm = (Term) uniterm.getArguments().get( 1 );
                varName2InitTerm.put( v0.getName(),
                                      initTerm );
            }
        }
        return varName2InitTerm;
    }

    public XmlFieldSetter getXmlFieldSetter(Uniterm uniterm) {
        if ( uniterm.getOperator().equals( xmlSetterOp ) ) {
            List<Term> args = uniterm.getArguments();
            Const c0 = (Const) args.get( 0 );
            Const c1 = (Const) args.get( 1 );
            return new XmlFieldSetter( QName.valueOf( c0.getName() ),
                                       QName.valueOf( c1.getName() ),
                                       args.get( 2 ),
                                       args.get( 3 ) );
        }
        return null;
    }

    public BinaryTerm getPlusBinaryTerm(Uniterm uniterm) {
        if ( uniterm.getOperator().equals( plusBinaryOp ) ) {
            List<Term> args = uniterm.getArguments();
            Term leftTerm = (Term) args.get( 0 );
            Term rightTerm = (Term) args.get( 1 );

            return new BinaryTerm( uniterm.getOperator(),
                                   leftTerm,
                                   rightTerm );
        }
        return null;
    }

    public BinaryTerm getMinusBinaryTerm(Uniterm uniterm) {
        if ( uniterm.getOperator().equals( minusBinaryOp ) ) {
            List<Term> args = uniterm.getArguments();
            Term leftTerm = (Term) args.get( 0 );
            Term rightTerm = (Term) args.get( 1 );

            return new BinaryTerm( uniterm.getOperator(),
                                   leftTerm,
                                   rightTerm );
        }
        return null;
    }

    public BinaryTerm getNotEqualTerm(Uniterm uniterm) {
        if ( uniterm.getOperator().equals( notEqualBinaryOp ) ) {
            List<Term> args = uniterm.getArguments();
            Term leftTerm = (Term) args.get( 0 );
            Term rightTerm = (Term) args.get( 1 );

            return new BinaryTerm( uniterm.getOperator(),
                                   leftTerm,
                                   rightTerm );
        }
        return null;
    }

    public BinaryTerm getGreaterTerm(Uniterm uniterm) {
        if ( uniterm.getOperator().equals( greaterOp ) ) {
            List<Term> args = uniterm.getArguments();
            Term leftTerm = (Term) args.get( 0 );
            Term rightTerm = (Term) args.get( 1 );

            return new BinaryTerm( uniterm.getOperator(),
                                   leftTerm,
                                   rightTerm );
        }
        return null;
    }

    public BinaryTerm getGreaterEqualTerm(Uniterm uniterm) {
        if ( uniterm.getOperator().equals( greaterEqualOp ) ) {
            List<Term> args = uniterm.getArguments();
            Term leftTerm = (Term) args.get( 0 );
            Term rightTerm = (Term) args.get( 1 );

            return new BinaryTerm( uniterm.getOperator(),
                                   leftTerm,
                                   rightTerm );
        }
        return null;
    }

    public BinaryTerm getLowerTerm(Uniterm uniterm) {
        if ( uniterm.getOperator().equals( lowerOp ) ) {
            List<Term> args = uniterm.getArguments();
            Term leftTerm = (Term) args.get( 0 );
            Term rightTerm = (Term) args.get( 1 );

            return new BinaryTerm( uniterm.getOperator(),
                                   leftTerm,
                                   rightTerm );
        }
        return null;
    }

    public BinaryTerm getLowerEqualTerm(Uniterm uniterm) {
        if ( uniterm.getOperator().equals( lowerEqualOp ) ) {
            List<Term> args = uniterm.getArguments();
            Term leftTerm = (Term) args.get( 0 );
            Term rightTerm = (Term) args.get( 1 );

            return new BinaryTerm( uniterm.getOperator(),
                                   leftTerm,
                                   rightTerm );
        }
        return null;
    }

    public BinaryTerm getBinaryTerm(Uniterm uniterm) {
        BinaryTerm bt = getPlusBinaryTerm( uniterm );
        if ( bt != null ) return bt;

        bt = getMinusBinaryTerm( uniterm );
        if ( bt != null ) return bt;

        bt = getNotEqualTerm( uniterm );
        if ( bt != null ) return bt;

        bt = getGreaterTerm( uniterm );
        if ( bt != null ) return bt;

        bt = getGreaterEqualTerm( uniterm );
        if ( bt != null ) return bt;

        bt = getLowerTerm( uniterm );
        if ( bt != null ) return bt;

        bt = getLowerEqualTerm( uniterm );
        if ( bt != null ) return bt;

        return null;
    }

    public Update getUpdate(Uniterm uniterm) {
        if ( uniterm.getOperator().equals( updateOp ) ) {
            List<Term> args = uniterm.getArguments();
            Term target = (Term) args.get( 0 );
            Term statement = (Term) args.get( 1 );

            return new Update( target,
                               statement );
        }
        return null;
    }

    public XmlFieldGetter getXmlFieldGetter(Uniterm uniterm) {
        if ( uniterm.getOperator().equals( xmlGetterOp ) ) {
            List<Term> args = uniterm.getArguments();
            Const c0 = (Const) args.get( 0 );
            Const c1 = (Const) args.get( 1 );
            return new XmlFieldGetter( QName.valueOf( c0.getName() ),
                                       QName.valueOf( c1.getName() ),
                                       args.get( 2 ) );
        }
        return null;
    }

}
