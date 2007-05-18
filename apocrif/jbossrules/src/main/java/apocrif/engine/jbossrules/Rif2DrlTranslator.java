package apocrif.engine.jbossrules;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.drools.compiler.PackageBuilder;
import org.drools.rule.Package;

import apocrif.core.AndCondition;
import apocrif.core.Condition;
import apocrif.core.Const;
import apocrif.core.Equal;
import apocrif.core.Implies;
import apocrif.core.LogicalRule;
import apocrif.core.Rule;
import apocrif.core.Ruleset;
import apocrif.core.Uniterm;
import apocrif.core.Variable;
import apocrif.pr.BinaryTerm;
import apocrif.pr.PRHelper;
import apocrif.pr.PRNodeVisitor;
import apocrif.pr.ProductionRule;
import apocrif.pr.Update;
import apocrif.pr.XmlFieldGetter;
import apocrif.pr.XmlFieldSetter;

import com.sun.xml.bind.v2.runtime.JAXBContextImpl;

public class Rif2DrlTranslator
    implements
    PRNodeVisitor<Void> {
    PRHelper              prHelper = new PRHelper();

    PrintWriter           writer;

    Map<String, String>   binaryOp2Irl;

    private Set<String>   imports;

    private Map           patterns;

    // this is a temporary buffer, used to build field constraints
    private StringBuilder buffer;

    // this is the variable, and thus the patter, the current buffer will be applied to
    private String        var;

    private boolean       inConsequence;
    
    private ClassLoader   classLoader;
    
    public Rif2DrlTranslator(ClassLoader classLoader) {
        this();
        this.classLoader = classLoader;
    }
    

    public Rif2DrlTranslator() {
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

    public Package translateToPackage(Ruleset rifRuleset, String pkgName) throws Exception {
        String drlString = translateToString( rifRuleset, 
                                              pkgName );

        PackageBuilder pkgBuilder = new PackageBuilder();
        pkgBuilder.addPackageFromDrl( new StringReader( drlString ) );
        Package pkg = pkgBuilder.getPackage();
        return pkg;
    }

    public String translateToString(Ruleset rifRuleset, String pkgName) throws Exception {
        StringWriter sw = new StringWriter();
        writer = new PrintWriter( sw );

        imports = new HashSet<String>();

        List<Rule> rifRules = rifRuleset.getRules();

        for ( Rule rifRule : rifRules ) {
            rifRule.accept( this );
        }

        StringBuilder builder = new StringBuilder();
        builder.append( "package " + pkgName + "\n" );
        for ( String entry : imports ) {
            builder.append( "import " + entry + "\n" );
        }
        builder.append( sw );

        return builder.toString();
    }

    public Void visit(ProductionRule rifRule) {
        inConsequence = false;
        writer.print( "rule " );
        writer.println( "\"" + rifRule.getName() + "\"" );
        writer.println( "when " );

        // declare variable
        patterns = new HashMap<String, Pattern>();
        createPatterns( rifRule );
        rifRule.getIfPart().accept( this );
        declareConditions( rifRule );

        for ( Variable var : rifRule.getVariables() ) {
            Pattern pattern = (Pattern) this.patterns.get( var.getName() );
            writer.print( pattern.getBinding() + " : " + pattern.getType() + "(" );
            List<String> constraints = pattern.getConstraints();
            for ( int i = 0; i < constraints.size(); i++ ) {
                String str = constraints.get( i );
                int index = str.indexOf( '.' ) + 1;
                writer.print( str.substring( index ) );
                if ( i != constraints.size() - 1 ) {
                    writer.println( "," );
                }
            }
            writer.println( ")" );
            if ( pattern.getFrom() != null ) {
                writer.println( " from " + pattern.getFrom() );
            }
        }

        writer.println( "then" );

        inConsequence = true;
        // declare actions
        buffer = new StringBuilder();
        rifRule.getThenPart().accept( this );
        writer.print( buffer.toString() );

        writer.print( "end" );

        return null;
    }

    public Void visit(AndCondition n) {
        for ( Condition c : n.getConditions() ) {
            buffer = new StringBuilder();
            c.accept( this );
            Pattern pattern = (Pattern) patterns.get( var );
            pattern.getConstraints().add( buffer.toString() );
        }
        return null;
    }

    public Void visit(Const n) {
        if ( prHelper.isStringConst( n ) ) {
            buffer.append( "\"" + n.getName() + "\"" );
        } else {
            buffer.append( n.getName() );
        }
        return null;
    }

    public Void visit(Equal n) {
        n.getLeftTerm().accept( this );
        buffer.append( "==" );
        n.getRightTerm().accept( this );

        return null;
    }

    public Void visit(Uniterm n) {
        XmlFieldGetter fieldGetter = prHelper.getXmlFieldGetter( n );
        if ( fieldGetter != null ) {
            var = ((Variable) fieldGetter.targetTerm).getName();
            fieldGetter.targetTerm.accept( this );
            buffer.append( '.' );
            if ( inConsequence ) {
                buffer.append( "get" + ucFirst( fieldGetter.xmlField.getLocalPart() ) + "()" );
            } else {
                buffer.append( fieldGetter.xmlField );
            }
            return null;
        }

        XmlFieldSetter fieldSetter = prHelper.getXmlFieldSetter( n );
        if ( fieldSetter != null ) {
            buffer.append( ((Variable) fieldSetter.targetTerm).getName() );
            buffer.append( '.' );
            buffer.append( "set" + ucFirst( fieldSetter.xmlField.getLocalPart() ) );
            buffer.append( '(' );
            //buffer = new StringBuilder();
            fieldSetter.newTerm.accept( this );
            //buffer.append( buffer.toString() );
            buffer.append( ");\n" );
            return null;
        }

        BinaryTerm bPred = prHelper.getBinaryTerm( n );

        if ( bPred != null ) {
            bPred.getLeftTerm().accept( this );
            buffer.append( " " );
            buffer.append( binaryOp2Irl.get( bPred.getName().getName() ) );
            buffer.append( " " );
            bPred.getRightTerm().accept( this );
            return null;
        }

        Update modify = prHelper.getUpdate( n );
        if ( modify != null ) {
            //writer.print("modify ");
            //modify.getTarget().accept(this);            
            //writer.println('{');
            modify.getStatement().accept( this );
            //writer.println('}');
            buffer.append( "modify(" + ((Variable) modify.getTarget()).getName() + ");\n" );
            return null;
        }

        throw new UnsupportedOperationException();
    }

    private String ucFirst(String str) {
        return str.substring( 0,
                              1 ).toUpperCase() + str.substring( 1 );
    }

    public Void visit(Variable n) {

        buffer.append( n.getName() );
        var = n.getName();
        return null;
    }

    public Void visit(LogicalRule n) {
        throw new UnsupportedOperationException();
    }

    public Void visit(Implies n) {
        throw new UnsupportedOperationException();
    }

    protected void declareConditions(ProductionRule rifRule) {
        for ( Condition c : rifRule.getConstraintPart().getConditions() ) {
            Uniterm uniterm = (Uniterm) c;

            if ( uniterm.getOperator().equals( PRHelper.fromGeneratorOp ) || uniterm.getOperator().equals( PRHelper.inGeneratorOp ) ) {
                Variable v0 = (Variable) uniterm.getArguments().get( 0 );
                Uniterm fromTerm = (Uniterm) uniterm.getArguments().get( 1 );
                buffer = new StringBuilder();
                XmlFieldGetter fieldGetter = prHelper.getXmlFieldGetter( fromTerm );
                var = ((Variable) fieldGetter.targetTerm).getName();
                buffer.append( var + '.' );
                buffer.append( fieldGetter.xmlField );

                ((Pattern) patterns.get( v0.getName() )).setFrom( buffer.toString() );
                buffer = null;
            }

        }
    }

    protected void createPatterns(ProductionRule rifRule) {
        for ( Variable variable : rifRule.getVariables() ) {
            Pattern pattern = new Pattern();
            pattern.setBinding( variable.getName() );
            patterns.put( variable.getName(),
                          pattern );

            String typeName = null;

            try {
                JAXBContextImpl contextImpl = (JAXBContextImpl) JAXBContext.newInstance( variable.getType().getNamespaceURI(),
                                                                                         classLoader );
                Class clazz = contextImpl.getGlobalType( variable.getType() ).jaxbType;
                imports.add( clazz.getName() );
                typeName = clazz.getSimpleName();

            } catch ( JAXBException e ) {
                e.printStackTrace();
            }

            pattern.setType( typeName );
        }
    }

    
    public class Pattern {
        private String       type;
        private String       binding;
        private List<String> constraints;
        private String       from;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getBinding() {
            return binding;
        }

        public void setBinding(String binding) {
            this.binding = binding;
        }

        public List<String> getConstraints() {
            if ( this.constraints == null ) {
                this.constraints = new ArrayList();
            }
            return this.constraints;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }
    }

}
