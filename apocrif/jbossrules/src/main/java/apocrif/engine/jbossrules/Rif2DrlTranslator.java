package apocrif.engine.jbossrules;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

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

public class Rif2DrlTranslator
    implements
    PRNodeVisitor<Void> {
    PRHelper                 prHelper    = new PRHelper();

    PrintWriter              writer;

    Map<String, String>      binaryOp2Irl;

    private Set<String>      imports;

    private Map              patterns;

    // this is a temporary buffer, used to build field constraints
    private StringBuilder    buffer;

    // this is the variable, and thus the patter, the current buffer will be applied to
    private String           var;

    private boolean          inConsequence;

    private ClassLoader      classLoader;

    private Map              mappings;
    
    private Class            lastLeftClass;

    private int              currentElement;

    private static final int PATTERN     = 0;
    private static final int FROM        = 1;
    private static final int CONSEQUENCE = 2;
    private static final int EVAL        = 3;

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

    public Package translateToPackage(Ruleset rifRuleset,
                                      String pkgName,
                                      Class objectFactoryClass) throws Exception {
        String drlString = translateToString( rifRuleset,
                                              pkgName,
                                              objectFactoryClass );

        PackageBuilder pkgBuilder = new PackageBuilder();
        pkgBuilder.addPackageFromDrl( new StringReader( drlString ) );
        Package pkg = pkgBuilder.getPackage();
        return pkg;
    }

    public String translateToString(Ruleset rifRuleset,
                                    String pkgName,
                                    Class objectFactoryClass) throws Exception {
        this.mappings = new HashMap();

        for ( Method method : objectFactoryClass.getMethods() ) {
            if ( method.getName().startsWith( "create" ) ) {
                //String name = method.getName().substring( 6 );

                Class clazz = method.getReturnType();

                if ( clazz == JAXBElement.class ) {
                    clazz = method.getParameterTypes()[0];
                    //String name = method.getName().substring( 6 );
                    //clazz = objectFactoryClass.getClassLoader().loadClass( objectFactoryClass.getPackage().getName() + "." + name );
                }

                //Class clazz = objectFactoryClass.getClassLoader().loadClass( objectFactoryClass.getPackage().getName() + "." + name );
                XmlRootElement xmlRootElement = (XmlRootElement) clazz.getAnnotation( XmlRootElement.class );
                ClassMapping classMapping = new ClassMapping( clazz );
                if ( xmlRootElement != null ) {
                    //System.out.println( clazz.getName()  + " = " + xmlRootElement.name() );
                    this.mappings.put( new QName( pkgName,
                                                  xmlRootElement.name() ),
                                       classMapping );
                } else {
                    XmlType xmlType = (XmlType) clazz.getAnnotation( XmlType.class );
                    if ( xmlType == null ) {
                        throw new RuntimeException( "unable to complete xml type to class type mappings" );
                    }
                    //System.out.println( clazz.getName()  + " = " + xmlType.name() );
                    this.mappings.put( new QName( pkgName,
                                                  xmlType.name() ),
                                       classMapping );
                }

                for ( Field field : clazz.getDeclaredFields() ) {
                    XmlAttribute xmlAttribute = field.getAnnotation( XmlAttribute.class );
                    if ( xmlAttribute != null ) {
                        String fieldName = xmlAttribute.name();
                        if ( fieldName == null ) {
                            fieldName = field.getName();
                        }
                        classMapping.addFieldMapping( fieldName,
                                                      getFieldName( clazz,
                                                                    field.getName() ) );
                    } else {
                        XmlElement xmlEement = (XmlElement) field.getAnnotation( XmlElement.class );
                        String fieldName = null;
                        if ( xmlEement != null ) {
                            fieldName = xmlEement.name();
                        }

                        if ( fieldName == null || fieldName.equals( "##default" ) ) {
                            fieldName = field.getName();
                        }
                        classMapping.addFieldMapping( fieldName,
                                                      getFieldName( clazz,
                                                                    field.getName() ) );
                    }
                }
            }
        }

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

        System.out.println( builder.toString() );

        return builder.toString();
    }

    private String getFieldName(Class clazz,
                                String property) {
        String upperProperty = "GET" + property.toUpperCase();
        String fieldName = null;
        for ( Method method : clazz.getMethods() ) {
            if ( method.getName().toUpperCase().equals( upperProperty ) ) {
                fieldName = method.getName().substring( 3 );
                break;
            }
        }

        if ( fieldName != null && Character.isLowerCase( fieldName.charAt( 1 ) ) ) {
            // second char is lower case, so lowercase first;
            fieldName = lcFirst( fieldName );
        }
        return fieldName;
    }

    public Void visit(ProductionRule rifRule) {
        inConsequence = false;
        writer.println( "rule " + "\"" + rifRule.getName() + "\"" );
        writer.println( "dialect \"mvel\"" );
        writer.println( "no-loop true" );
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
            List<String> evals = pattern.getEvals();
            if ( !evals.isEmpty() ) {
                writer.print( "eval( " );
                for ( int i = 0; i < evals.size(); i++ ) {
                    String str = evals.get( i );
                    int index = str.indexOf( '.' ) + 1;
                    writer.print( str.substring( index ) );
                    if ( i != evals.size() - 1 ) {
                        writer.println( " && " );
                    }
                }
                writer.println( " )" );
            }
        }

        writer.println( "then" );

        inConsequence = true;
        // declare actions
        buffer = new StringBuilder();
        //buffer.append( "System.out.println(\"hello\");" );
        this.currentElement = CONSEQUENCE;
        rifRule.getThenPart().accept( this );
        writer.print( buffer.toString() );

        writer.println( "end\n" );

        return null;
    }

    public Void visit(AndCondition n) {
        for ( Condition c : n.getConditions() ) {
            this.currentElement = PATTERN;
            buffer = new StringBuilder();
            c.accept( this );
            Pattern pattern = (Pattern) patterns.get( var );
            if ( this.currentElement == EVAL ) {
                pattern.getEvals().add( buffer.toString() );
            } else {
                pattern.getConstraints().add( buffer.toString() );
            }
        }
        return null;
    }

    public Void visit(Const n) {
        if ( prHelper.isStringConst( n ) ) {
            if ( lastLeftClass != null && lastLeftClass.isEnum() ) {
                //System.out.println( lastLeftType );
                for ( Enum e : ( Enum[] ) lastLeftClass.getEnumConstants() ) {
                    String value = null;
                    
                    try {
                        value = (String) e.getClass().getDeclaredMethod( "value", null ).invoke( e, null );
                    } catch ( Exception e1 ) {
                        e1.printStackTrace();
                    }
                    
                    if ( value != null && value.equals( n.getName() ) ) {
                        //this.imports.add( e.getClass().getName() );
                        //System.out.println( "---" + e.getClass().getSimpleName() + "." + e );
                        buffer.append( e.getClass().getName() + "." + e.name()  );
                        return null;
                    }
                }
            } 
            buffer.append( "\"" + n.getName() + "\"" );
        } else {
            buffer.append( n.getName() );
        }
        return null;
    }

    public Void visit(Equal n) {
        n.getLeftTerm().accept( this );
        buffer.append( " == " );
        n.getRightTerm().accept( this );

        return null;
    }

    public Void visit(Uniterm n) {
        XmlFieldGetter fieldGetter = prHelper.getXmlFieldGetter( n );
        if ( fieldGetter != null ) {
            if ( fieldGetter.targetTerm instanceof Uniterm ) {
                if ( this.currentElement == PATTERN ) {
                    this.currentElement = EVAL;
                    buffer.append( var + "." );
                }
                fieldGetter.targetTerm.accept( this );
            } else {
                var = ((Variable) fieldGetter.targetTerm).getName();
                fieldGetter.targetTerm.accept( this );
            }
            buffer.append( '.' );

            ClassMapping mapping = (ClassMapping) this.mappings.get( fieldGetter.xmlDeclaringCType );
            String fieldName = mapping.getFieldNameMapping( fieldGetter.xmlField.getLocalPart() );

            Field field = null;
            lastLeftClass = null;
            try {
                field = mapping.getMappedClass().getDeclaredField( fieldName );
                
                if ( field.getType().isEnum() ) {
                    lastLeftClass = field.getType();  
                    //System.out.println( lastLeftType );
//                    for ( Enum e : ( Enum[] ) field.getType().getEnumConstants() ) {
//                        String value = null;
//                        
//                        try {
//                            value = (String) e.getClass().getDeclaredMethod( "value", null ).invoke( e, null );
//                        } catch ( Exception e1 ) {
//                            e1.printStackTrace();
//                        }
//                        
//                        if ( value != null && value.equals( fieldName ) ) {
//                            //this.imports.add( e.getClass().getName() );
//                            //System.out.println( "---" + e.getClass().getSimpleName() + "." + e );
//                            buffer.append( e.getClass().getName() + "." + e.name() + ".value()" );
//                            return null;
//                        }
                    }                
            } catch ( Exception e ) {
                lastLeftClass = null;
//                e.printStackTrace();
//                throw new RuntimeException( e );
            }                        
            
            buffer.append( fieldName );

//            if ( lastLeftClass != null ) {
//                buffer.append( ".value" );
//            }
            return null;
        }

        XmlFieldSetter fieldSetter = prHelper.getXmlFieldSetter( n );
        if ( fieldSetter != null ) {
            ClassMapping mapping = (ClassMapping) this.mappings.get( fieldSetter.xmlDeclaringCType );
            String fieldName = mapping.getFieldNameMapping( fieldSetter.xmlField.getLocalPart() );

            if ( fieldSetter.targetTerm instanceof Uniterm ) {
                fieldSetter.targetTerm.accept( this );
            } else {
                buffer.append( ((Variable) fieldSetter.targetTerm).getName() );
            }                        
            
            buffer.append( '.' + fieldName + " = ");

            if ( fieldSetter.newTerm instanceof Uniterm ) {
                fieldSetter.newTerm.accept( this );
            } else if ( ((Const) fieldSetter.newTerm).getType().getLocalPart().equals( "integer" ) ) {
                fieldSetter.newTerm.accept( this );
            } else if ( ((Const) fieldSetter.newTerm).getType().getLocalPart().equals( "decimal" ) ) {
                fieldSetter.newTerm.accept( this );
            } else {
                fieldSetter.newTerm.accept( this );
            }
            //buffer = new StringBuilder();

            //buffer.append( buffer.toString() );
            buffer.append( ";\n" );
            //buffer.append( "modify(" + ((Variable) fieldSetter.targetTerm).getName() + ");\n" );
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

    private String lcFirst(String str) {
        return str.substring( 0,
                              1 ).toLowerCase() + str.substring( 1 );
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
                this.currentElement = FROM;
                Variable v0 = (Variable) uniterm.getArguments().get( 0 );
                Uniterm fromTerm = (Uniterm) uniterm.getArguments().get( 1 );
                buffer = new StringBuilder();

                XmlFieldGetter fieldGetter = prHelper.getXmlFieldGetter( fromTerm );

                if ( fieldGetter.targetTerm instanceof Uniterm ) {
                    fieldGetter.targetTerm.accept( this );
                } else {
                    buffer.append( ((Variable) fieldGetter.targetTerm).getName() );
                }

                buffer.append( '.' );

                ClassMapping mapping = (ClassMapping) this.mappings.get( fieldGetter.xmlDeclaringCType );
                String fieldName = mapping.getFieldNameMapping( fieldGetter.xmlField.getLocalPart() );

                buffer.append( fieldName );

                ((Pattern) patterns.get( v0.getName() )).setFrom( buffer.toString() );

                buffer = null;
            }

        }
    }

    public class ClassMapping {
        private Class mappedClass;
        private Map   fieldMapping;

        public ClassMapping(Class mappedClass) {
            this.mappedClass = mappedClass;
            this.fieldMapping = new HashMap();
        }

        public Class getMappedClass() {
            return this.mappedClass;
        }

        public void addFieldMapping(String attributeName,
                                    String fieldName) {
            this.fieldMapping.put( attributeName,
                                   fieldName );
        }

        public String getFieldNameMapping(String attributeName) {
            return (String) this.fieldMapping.get( attributeName );
        }
    }

    protected void createPatterns(ProductionRule rifRule) {
        for ( Variable variable : rifRule.getVariables() ) {
            Pattern pattern = new Pattern();
            pattern.setBinding( variable.getName() );
            patterns.put( variable.getName(),
                          pattern );

            ClassMapping mapping = (ClassMapping) this.mappings.get( variable.getType() );
            String typeName = mapping.getMappedClass().getSimpleName();

            imports.add( mapping.getMappedClass().getName() );

            pattern.setType( typeName );
        }
    }

    public class Pattern {
        private String       type;
        private String       binding;
        private List<String> constraints;
        private List<String> evals;
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

        public List<String> getEvals() {
            if ( this.evals == null ) {
                this.evals = new ArrayList();
            }
            return this.evals;
        }

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }
    }

}
