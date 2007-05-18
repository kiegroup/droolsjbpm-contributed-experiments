package apocrif.io;

import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import apocrif.core.AndCondition;
import apocrif.core.Condition;
import apocrif.core.Const;
import apocrif.core.Equal;
import apocrif.core.Implies;
import apocrif.core.LogicalRule;
import apocrif.core.Rule;
import apocrif.core.Ruleset;
import apocrif.core.Term;
import apocrif.core.Uniterm;
import apocrif.core.Variable;
import apocrif.pr.PRHelper;
import apocrif.pr.PRNodeVisitor;
import apocrif.pr.ProductionRule;

public class DOMSerializer
    implements
    PRNodeVisitor<Element>,
    XmlConstants {
    // transient
    Document                document;

    HashMap<String, String> namespace2Alias;
    int                     aliasCounter;

    public Document serialize(Ruleset ruleset) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware( false );
        factory.setValidating( true );

        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        document = docBuilder.newDocument();

        namespace2Alias = new HashMap<String, String>();
        namespace2Alias.put( XSD_NS,
                             "xsd" );
        namespace2Alias.put( PRHelper.APOCRIF_NAMESPACE,
                             "aprif" );
        namespace2Alias.put( PRHelper.XPATH_NAMESPACE,
                             "xpath" );
        aliasCounter = 0;

        // top level ruleset tag
        Element rulesetElem = document.createElement( RULESET_ELEM );
        rulesetElem.appendChild( createElement( NAME_ELEM,
                                                ruleset.getName() ) );
        document.appendChild( rulesetElem );

        for ( Rule rule : ruleset.getRules() ) {
            addSubElement( rulesetElem,
                           rule.accept( this ) );
        }

        addNamespaceAttributes( rulesetElem );

        return document;
    }

    public void serialize(Ruleset ruleset,
                          Writer writer) throws Exception {
        Document doc = serialize( ruleset );
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty( OutputKeys.INDENT,
                                       "yes" );
        Source xmlSource = new DOMSource( doc );
        Result xmlDest = new StreamResult( writer );
        transformer.transform( xmlSource,
                               xmlDest );

    }

    protected void addSubElement(Element parentElem,
                                 Element subElem) {
        if ( subElem != null ) {
            parentElem.appendChild( subElem );
        }
    }

    protected void addNamespaceAttributes(Element rulesetElem) {
        Iterator<String> nsIte = namespace2Alias.keySet().iterator();

        while ( nsIte.hasNext() ) {
            String ns = nsIte.next();
            String alias = namespace2Alias.get( ns );
            String attrName = XMLNS_PREFIX + alias;
            rulesetElem.setAttribute( attrName,
                                      ns );
        }
    }

    public Element visit(ProductionRule n) {
        Element ruleElem = document.createElement( PRODUCTION_RULE_ELEM );
        ruleElem.appendChild( createElement( NAME_ELEM,
                                             n.getName() ) );
        declareVariables( n.getVariables(),
                          ruleElem );
        Element ifElem = document.createElement( IF_ELEM );
        ruleElem.appendChild( ifElem );
        addSubElement( ifElem,
                       n.getIfPart().accept( this ) );
        Element constraintsElem = document.createElement( CONSTRAINTS_ELEM );
        ruleElem.appendChild( constraintsElem );
        addSubElement( constraintsElem,
                       n.getConstraintPart().accept( this ) );
        Element thenElem = document.createElement( THEN_ELEM );
        ruleElem.appendChild( thenElem );
        addSubElement( thenElem,
                       n.getThenPart().accept( this ) );

        return ruleElem;
    }

    protected Element createElement(String name,
                                    String content) {
        Element elem = document.createElement( name );
        elem.appendChild( document.createTextNode( content ) );
        return elem;
    }

    public Element visit(AndCondition n) {
        Element condElem = document.createElement( AND_ELEM );
        for ( Condition c : n.getConditions() ) {
            addSubElement( condElem,
                           c.accept( this ) );
        }
        return condElem;
    }

    public Element visit(Const c) {
        String value = c.getName();
        if ( PRHelper.qNameType.equals( c.getType() ) ) {
            value = formatQNameValue( c.getName() );
        }
        Element constElem = createElement( CONST_ELEM,
                                           value );
        if ( c.getType() != null ) {
            constElem.setAttribute( "type",
                                    formatQNameValue( c.getType() ) );
        }

        return constElem;
    }

    protected String formatQNameValue(String qNameStr) {
        return formatQNameValue( QName.valueOf( qNameStr ) );
    }

    protected String formatQNameValue(QName qName) {
        String alias = namespace2Alias.get( qName.getNamespaceURI() );
        if ( alias == null ) {
            alias = ALIAS_PREFIX + aliasCounter++;
            namespace2Alias.put( qName.getNamespaceURI(),
                                 alias );
        }
        return alias + ':' + qName.getLocalPart();
    }

    public Element visit(Equal eq) {
        Element equalElem = document.createElement( EQUAL_ELEM );

        addSubElement( equalElem,
                       eq.getLeftTerm().accept( this ) );
        addSubElement( equalElem,
                       eq.getRightTerm().accept( this ) );

        return equalElem;
    }

    public Element visit(Implies impl) {
        Element equalElem = document.createElement( IMPLIES_ELEM );

        addSubElement( equalElem,
                       impl.getIfPart().accept( this ) );
        addSubElement( equalElem,
                       impl.getThenPart().accept( this ) );

        return equalElem;
    }

    protected void declareVariables(List<Variable> variables,
                                    Element fatherElem) {
        for ( Variable v : variables ) {
            Element declElem = document.createElement( DECLARE_ELEM );
            addSubElement( declElem,
                           v.accept( this ) );
            addSubElement( fatherElem,
                           declElem );
        }
    }

    public Element visit(LogicalRule r) {
        Element ruleElem = document.createElement( LOGICAL_RULE_ELEM );
        declareVariables( r.getVariables(),
                          ruleElem );
        addSubElement( ruleElem,
                       r.getFormula().accept( this ) );
        return ruleElem;
    }

    public Element visit(Uniterm uniterm) {
        Element unitermElem = document.createElement( UNITERM_ELEM );
        addSubElement( unitermElem,
                       uniterm.getOperator().accept( this ) );
        for ( Term term : uniterm.getArguments() ) {
            addSubElement( unitermElem,
                           term.accept( this ) );
        }
        return unitermElem;
    }

    public Element visit(Variable v) {
        Element varElem = createElement( VAR_ELEM,
                                         v.getName() );
        if ( v.getType() != null ) {
            varElem.setAttribute( TYPE_ATTR,
                                  formatQNameValue( v.getType() ) );
        }
        return varElem;
    }

}
