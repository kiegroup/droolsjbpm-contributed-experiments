package apocrif.engine.jrules;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;

import com.sun.org.apache.xpath.internal.operations.Variable;

public class Rif2IrlTranslator
    implements
    PRNodeVisitor<Void> {

    PRHelper            prHelper = new PRHelper();

    PrintWriter         writer;

    Model               model;

    Map<String, Term>   varName2InitTerm;
    Map<String, Term>   varName2InTerm;
    Map<String, Term>   varName2FromTerm;

    Map<String, String> binaryOp2Irl;

    boolean             printGeneratedIrl;

    public Rif2IrlTranslator(IlrReflect xom,
                             boolean printGeneratedIrl) throws Exception {
        this.printGeneratedIrl = printGeneratedIrl;
        initModel( xom );
    }

    protected void initModel(IlrReflect xom) throws IlrXmlErrorException {
        IlrXmlDefaultDataDriver dataDriver = new IlrXmlDefaultDataDriver( xom );
        model = new Model( xom,
                           dataDriver.getXmlModel() );

        binaryOp2Irl = new HashMap<String, String>();
        binaryOp2Irl.put( PRHelper.greaterEqualOp.getName(),
                          ">=" );
        binaryOp2Irl.put( PRHelper.greaterOp.getName(),
                          ">" );
        binaryOp2Irl.put( PRHelper.lowerEqualOp.getName(),
                          "<=" );
        binaryOp2Irl.put( PRHelper.lowerOp.getName(),
                          "<" );
        binaryOp2Irl.put( PRHelper.notEqualBinaryOp.getName(),
                          "!=" );
        binaryOp2Irl.put( PRHelper.plusBinaryOp.getName(),
                          "+" );
        binaryOp2Irl.put( PRHelper.minusBinaryOp.getName(),
                          "-" );
    }

    public IlrRuleset translate(Ruleset rifRuleset) throws Exception {
        StringWriter sw = new StringWriter();
        writer = new PrintWriter( sw );

        List<Rule> rifRules = rifRuleset.getRules();

        for ( Rule rifRule : rifRules ) {
            rifRule.accept( this );
        }

        IlrRuleset irlRuleset = new IlrRuleset( model.getXom() );

        if ( printGeneratedIrl ) {
            System.out.println( "==================================" );
            System.out.println( sw.toString() );
            System.out.println( "==================================" );
        }

        if ( !irlRuleset.parseString( sw.toString() ) ) return null;
        else return irlRuleset;
    }

    public Void visit(AndCondition n) {
        for ( Condition c : n.getConditions() ) {
            c.accept( this );
            writer.print( ';' );
        }
        return null;
    }

    public Void visit(Const n) {
        if ( prHelper.isStringConst( n ) ) {
            writer.print( '"' );
            writer.print( n.getName() );
            writer.print( '"' );
        } else {
            writer.print( n.getName() );
        }
        return null;
    }

    public Void visit(Equal n) {
        n.getLeftTerm().accept( this );
        writer.print( ".equals(" );
        n.getRightTerm().accept( this );
        writer.print( ')' );
        return null;
    }

    protected void declareConditions(Rule rifRule) {
        for ( Variable variable : rifRule.getVariables() ) {
            if ( varName2InitTerm.get( variable.getName() ) == null ) {
                IlrType condType = model.getXomType( variable.getType() );
                if ( condType == null ) {
                    throw new UnsupportedOperationException();
                } else {
                    writer.print( variable.getName() );
                    writer.print( ':' );
                    writer.print( condType.getFullyQualifiedName() );
                    Term inGenerator = varName2InTerm.get( variable.getName() );
                    if ( inGenerator != null ) {
                        writer.print( "() in " );
                        inGenerator.accept( this );
                        writer.println( ";" );
                    } else {
                        Term fromGenerator = varName2FromTerm.get( variable.getName() );
                        if ( fromGenerator != null ) {
                            writer.print( "() from " );
                            fromGenerator.accept( this );
                            writer.println( ";" );
                        } else {
                            writer.println( "();" );
                        }
                    }
                }
            }
        }
    }

    protected void declareConstraintVariables(Rule rifRule) {
        for ( Variable variable : rifRule.getVariables() ) {
            Term initTerm = varName2InitTerm.get( variable.getName() );
            if ( initTerm != null ) {

                writer.print( variable.getName() );
                writer.print( ':' );
                initTerm.accept( this );
                writer.print( ';' );

            }
        }
    }

    public Void visit(LogicalRule rifRule) {
        throw new UnsupportedOperationException();
    }

    public Void visit(Implies n) {
        throw new UnsupportedOperationException();
    }

    public Void visit(ProductionRule rifRule) {
        writer.print( "rule " );
        writer.print( rifRule.getName() );
        writer.println( '{' );
        writer.println( "when {" );

        // declare variable
        varName2InitTerm = prHelper.getVariableConstraints( rifRule );
        varName2InTerm = prHelper.getVariableInTerm( rifRule );
        varName2FromTerm = prHelper.getVariableFromTerm( rifRule );
        declareConditions( rifRule );

        // declare constraint variable
        writer.print( "evaluate(" );
        declareConstraintVariables( rifRule );

        // declare tests
        rifRule.getIfPart().accept( this );
        writer.println( ");" );
        writer.println( '}' );

        writer.println( "then {" );

        // declare actions
        rifRule.getThenPart().accept( this );

        writer.println( '}' );
        writer.print( '}' );

        return null;
    }

    public Void visit(Uniterm n) {
        XmlFieldGetter fieldGetter = prHelper.getXmlFieldGetter( n );
        if ( fieldGetter != null ) {
            IlrAttribute xomField = model.getXomField( fieldGetter.xmlDeclaringCType,
                                                       fieldGetter.xmlField );
            fieldGetter.targetTerm.accept( this );
            writer.print( '.' );
            writer.print( xomField.getName() );
            return null;
        }

        XmlFieldSetter fieldSetter = prHelper.getXmlFieldSetter( n );
        if ( fieldSetter != null ) {
            IlrAttribute xomField = model.getXomField( fieldSetter.xmlDeclaringCType,
                                                       fieldSetter.xmlField );
            fieldSetter.targetTerm.accept( this );
            writer.print( '.' );
            writer.print( xomField.getName() );
            writer.print( '=' );
            fieldSetter.newTerm.accept( this );
            writer.println( ';' );
            return null;
        }

        BinaryTerm bPred = prHelper.getBinaryTerm( n );
        if ( bPred != null ) {
            bPred.getLeftTerm().accept( this );
            writer.print( " " );
            writer.print( binaryOp2Irl.get( bPred.getName().getName() ) );
            writer.print( " " );
            bPred.getRightTerm().accept( this );
            return null;
        }

        Update modify = prHelper.getUpdate( n );
        if ( modify != null ) {
            writer.print( "modify " );
            modify.getTarget().accept( this );
            writer.println( '{' );
            modify.getStatement().accept( this );
            writer.println( '}' );
            return null;
        }

        throw new UnsupportedOperationException();

    }

    public Void visit(Variable n) {

        writer.print( n.getName() );

        return null;
    }

}
