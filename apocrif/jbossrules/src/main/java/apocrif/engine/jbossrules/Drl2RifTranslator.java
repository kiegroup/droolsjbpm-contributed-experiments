package apocrif.engine.jbossrules;

import org.drools.lang.descr.PackageDescr;
import org.drools.lang.descr.PackageDescrDumper;
import org.drools.util.ReflectiveVisitor;

public class Drl2RifTranslator extends ReflectiveVisitor
    implements
    PackageDescrDumper {

    public String dump(PackageDescr arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    //    PRHelper factory = new PRHelper();
    //
    //    // transient
    //    ProductionRule rifRule;    
    //
    //    public synchronized Ruleset translate(final PackageDescr packageDescr) {
    //        //this.xmlDump = new StringBuffer();
    //        visitPackageDescr( packageDescr );
    //        //return this.xmlDump.toString();
    //        return null;
    //    }
    //
    //    public void visitAndDescr(final AndDescr descr) {
    //        //this.template = new String();
    //        if ( descr.getDescrs() != Collections.EMPTY_LIST ) {
    //            //this.template = "<and>" + processDescrList( descr.getDescrs() ) + "</and>";
    //        } else {
    //            //this.template = "<and> </and>";
    //        }
    //    }
    //
    //    public void visitAttributeDescr(final AttributeDescr attributeDescr) {
    //        //this.template = new String();
    //        //this.template = "<rule-attribute name=\"" + attributeDescr.getName() + "\" value=\"" + attributeDescr.getValue() + "\" />" + XmlDumper.eol;
    //    }
    //
    //    public void visitVariableRestrictionDescr(final VariableRestrictionDescr descr) {
    //        throw new UnsupportedOperationException();
    //    }
    //
    //    public void visitPatternDescr(final PatternDescr descr) {
    //        this.template = new String();
    //        if ( descr.getDescrs() != Collections.EMPTY_LIST ) {
    //            if ( descr.getIdentifier() != null ) {
    //                this.template = "<pattern identifier=\"" + descr.getIdentifier() + "\" object-type=\"" + descr.getObjectType() + "\" >" + XmlDumper.eol + processDescrList( descr.getDescrs() ) + XmlDumper.eol + "</pattern>" + XmlDumper.eol;
    //            } else {
    //                this.template = "<pattern object-type=\"" + descr.getObjectType() + "\" >" + XmlDumper.eol + processDescrList( descr.getDescrs() ) + XmlDumper.eol + "</pattern>" + XmlDumper.eol;
    //            }
    //        } else {
    //            if ( descr.getIdentifier() != null ) {
    //                this.template = "<pattern identifier=\"" + descr.getIdentifier() + "\" object-type=\"" + descr.getObjectType() + "\" > </pattern>" + XmlDumper.eol;
    //            } else {
    //                this.template = "<pattern object-type=\"" + descr.getObjectType() + "\" > </pattern>" + XmlDumper.eol;
    //            }
    //        }
    //
    //    }
    //
    //    public void visitFieldConstraintDescr(final FieldConstraintDescr descr) {
    //        if ( !descr.getRestrictions().isEmpty() ) {
    //            this.template = "<field-constraint field-name=\"" + descr.getFieldName() + "\"> " + XmlDumper.eol + processFieldConstraint( descr.getRestrictions() ) + XmlDumper.eol + "</field-constraint>";
    //        }
    //    }
    //
    //    public void visitEvalDescr(final EvalDescr descr) {
    //        throw new UnsupportedOperationException();
    //    }
    //
    //    public void visitExistsDescr(final ExistsDescr descr) {
    //        throw new UnsupportedOperationException();
    //    }
    //
    //    public void visitFieldBindingDescr(final FieldBindingDescr descr) {
    //        this.template = new String();
    //        this.template = "<field-binding field-name=\"" + descr.getFieldName() + "\" identifier=\"" + descr.getIdentifier() + "\" />" + XmlDumper.eol;
    //    }
    //
    //    public void visitFunctionDescr(final FunctionDescr functionDescr) {
    //        throw new UnsupportedOperationException();
    //    }
    //
    //    public void visitLiteralRestrictionDescr(final LiteralRestrictionDescr descr) {
    //        this.template = new String();
    //        this.template = "<literal-restriction evaluator=\"" + replaceIllegalChars( descr.getEvaluator() ) + "\" value=\"" + replaceIllegalChars( descr.getText() ) + "\" />" + XmlDumper.eol;
    //    }
    //
    //    public void visitNotDescr(final NotDescr descr) {
    //        throw new UnsupportedOperationException();
    //    }
    //
    //    public void visitOrDescr(final OrDescr descr) {
    //        throw new UnsupportedOperationException();
    //    }
    //
    //    public void visitPackageDescr(final PackageDescr packageDescr) {
    //        final String packageName = packageDescr.getName();
    //        final String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " + XmlDumper.eol + " <package name=\"" + packageName + "\"  " + XmlDumper.eol + "\txmlns=\"http://drools.org/drools-3.0\" " + XmlDumper.eol
    //                                 + "\txmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\" " + XmlDumper.eol + "\txs:schemaLocation=\"http://drools.org/drools-3.0 drools-3.0.xsd\"> " + XmlDumper.eol;
    //        appendXmlDump( xmlString );
    //        appendXmlDump( processImportsList( packageDescr.getImports() ) );
    //        //appendXmlDump( processGlobalsList( packageDescr.getGlobals() ) );
    //        //appendXmlDump( processFunctionsList( packageDescr.getFunctions() ) );
    //        appendXmlDump( processRules( packageDescr.getRules() ) );
    //        appendXmlDump( "</package>" );
    //    }
    //
    //    public void visitPredicateDescr(final PredicateDescr descr) {
    //        throw new UnsupportedOperationException();
    //    }
    //
    //    public void visitReturnValueRestrictionDescr(final ReturnValueRestrictionDescr descr) {
    //        throw new UnsupportedOperationException();
    //    }
    //
    //    public void visitQueryDescr(final QueryDescr descr) {
    //        throw new UnsupportedOperationException();
    //    }
    //
    //    private String template;
    //
    //    private String processRules(final List rules) {
    //        String ruleList = "";
    //        for ( final Iterator iterator = rules.iterator(); iterator.hasNext(); ) {
    //            final RuleDescr ruleDescr = (RuleDescr) iterator.next();
    //            String rule = "<rule name=\"" + ruleDescr.getName() + "\">" + XmlDumper.eol;
    //            final String attribute = processAttribute( ruleDescr.getAttributes() );
    //            String lhs = "";
    //            if ( ruleDescr.getLhs().getDescrs() != Collections.EMPTY_LIST ) {
    //                lhs = "<lhs>" + processDescrList( ruleDescr.getLhs().getDescrs() ) + "</lhs>";
    //            } else {
    //
    //                lhs = "<lhs> </lhs>";
    //            }
    //
    //            final String rhs = "<rhs>" + replaceIllegalChars( (String) ruleDescr.getConsequence() ) + "</rhs>" + XmlDumper.eol;
    //            rule += attribute;
    //            rule += lhs;
    //            rule += rhs;
    //            rule += "</rule>";
    //            ruleList += rule;
    //        }
    //
    //        return ruleList + XmlDumper.eol;
    //    }
    //
    //    private String processFieldConstraint(final List list) {
    //        String descrString = "";
    //        for ( final Iterator it = list.iterator(); it.hasNext(); ) {
    //            final Object temp = it.next();
    //            visit( temp );
    //            descrString += this.template;
    //        }
    //        return descrString;
    //    }
    //
    //    public void visitRestrictionConnectiveDescr(final RestrictionConnectiveDescr descr) {
    //        throw new UnsupportedOperationException();
    //    }
    //
    //    private String processDescrList(final List descr) {
    //        String descrString = "";
    //        for ( final Iterator iterator = descr.iterator(); iterator.hasNext(); ) {
    //            visit( iterator.next() );
    //            descrString += this.template;
    //            descrString += XmlDumper.eol;
    //        }
    //        return descrString + XmlDumper.eol;
    //    }
    //
    //    private String processFunctionsList(final List functions) {
    //        throw new UnsupportedOperationException();
    //    }
    //
    //    private String processAttribute(final List attributes) {
    //        throw new UnsupportedOperationException();
    //    }
    //
    //    private String processParameters(final List parameterNames,
    //                                     final List parameterTypes) {
    //        throw new UnsupportedOperationException();
    //    }
    //
    //    private String processGlobalsList(final List globals) {
    //        throw new UnsupportedOperationException();
    //    }
    //
    //    private String processImportsList(final List imports) {
    //        String importList = "";
    //
    //        for ( final Iterator iterator = imports.iterator(); iterator.hasNext(); ) {
    //            final String importString = ((ImportDescr) iterator.next()).getTarget();
    //            final String importTemplate = "<import name=\"" + importString + "\" /> " + XmlDumper.eol;
    //            importList += importTemplate;
    //        }
    //        return importList + XmlDumper.eol;
    //    }
    //

}
