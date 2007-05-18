package apocrif.io;

import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import apocrif.core.AndCondition;
import apocrif.core.Atomic;
import apocrif.core.Condition;
import apocrif.core.Const;
import apocrif.core.Equal;
import apocrif.core.LogicalRule;
import apocrif.core.Rule;
import apocrif.core.Ruleset;
import apocrif.core.Term;
import apocrif.core.Uniterm;
import apocrif.core.Variable;
import apocrif.pr.PRHelper;
import apocrif.pr.ProductionRule;

public class DOMDeserializer
    implements
    XmlConstants {
    PRHelper            prHelper = new PRHelper();

    // transient
    Map<String, String> alias2Namespace;

    public Ruleset deserialize(Document document) throws Exception {
        Element rootElement = document.getDocumentElement();

        readNamespaceAttributes( rootElement );
        check( rootElement,
               RULESET_ELEM );
        ElementChildIterator ruleElems = new ElementChildIterator( rootElement );

        String name = readContent( ruleElems.next() );
        Ruleset ruleset = new Ruleset( name );

        while ( ruleElems.hasNext() ) {
            Element ruleElem = ruleElems.next();
            ruleset.addRule( readRule( ruleElem ) );
        }

        alias2Namespace = null;

        return ruleset;
    }

    public Ruleset deserialize(Reader reader) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware( true );
        factory.setValidating( false );

        DocumentBuilder docBuilder = factory.newDocumentBuilder();
        Document document = docBuilder.parse( new InputSource( reader ) );
        return deserialize( document );
    }

    protected void check(Element element,
                         String expectedTag) throws Exception {
        if ( !element.getTagName().equals( expectedTag ) ) {
            throw new Exception( "Expected tag: " + expectedTag + " === found tag: " + element.getTagName() );
        }
    }

    protected void readNamespaceAttributes(Element rulesetElem) {
        alias2Namespace = new HashMap<String, String>();

        NamedNodeMap attrMap = rulesetElem.getAttributes();
        for ( int i = 0; i < attrMap.getLength(); i++ ) {
            Attr attr = (Attr) attrMap.item( i );
            String name = attr.getName();
            int ix = name.indexOf( XMLNS_PREFIX );
            if ( ix == 0 ) {
                String alias = name.substring( ix + XMLNS_PREFIX.length() );
                String namespace = attr.getValue();
                alias2Namespace.put( alias,
                                     namespace );
            }
        }
    }

    protected Rule readRule(Element ruleElem) throws Exception {
        if ( ruleElem.getTagName().equals( PRODUCTION_RULE_ELEM ) ) {
            return readProductionRule( ruleElem );
        } else if ( ruleElem.getTagName().equals( LOGICAL_RULE_ELEM ) ) {
            return readLogicalRule( ruleElem );
        } else {
            throw new Exception( "Expected tag: ProductionRule | LogicalRule" + " === found tag: " + ruleElem.getTagName() );
        }
    }

    protected ProductionRule readProductionRule(Element ruleElem) throws Exception {
        ElementChildIterator elems = new ElementChildIterator( ruleElem );
        String name = readContent( elems.next() );

        ProductionRule productionRule = prHelper.createProductionRule( name );
        readVariableDeclarations( productionRule,
                                  elems );
        Element ifElem = elems.next();
        productionRule.setIfPart( readCondition( getFirstElement( ifElem ) ) );
        Element constraintsElem = elems.next();
        productionRule.setConstraintPart( readAndCondition( getFirstElement( constraintsElem ) ) );
        Element thenElem = elems.next();
        productionRule.setThenPart( readAtomic( getFirstElement( thenElem ) ) );

        return productionRule;
    }

    protected Element getFirstElement(Element father) {
        NodeList children = father.getChildNodes();
        for ( int i = 0; i < children.getLength(); i++ ) {
            Node n = children.item( i );
            if ( n instanceof Element ) {
                return (Element) n;
            }
        }
        return null;
    }

    protected void readVariableDeclarations(Rule rule,
                                            ElementChildIterator children) throws Exception {
        while ( children.hasNext( DECLARE_ELEM ) ) {
            Element declareElem = children.next( DECLARE_ELEM );
            Element varElem = (Element) declareElem.getElementsByTagName( VAR_ELEM ).item( 0 );
            rule.addVariable( readVar( varElem ) );
        }
    }

    protected Condition readCondition(Element condElem) throws Exception {
        if ( condElem.getTagName().equals( AND_ELEM ) ) {
            return readAndCondition( condElem );
        } else {
            return readAtomic( condElem );
        }
    }

    protected Atomic readAtomic(Element atomElem) throws Exception {
        if ( atomElem.getTagName().equals( EQUAL_ELEM ) ) {
            return readEqual( atomElem );
        } else if ( atomElem.getTagName().equals( UNITERM_ELEM ) ) {
            return readUniterm( atomElem );
        } else {
            throw new UnsupportedOperationException();
        }
    }

    protected Equal readEqual(Element atomElem) throws Exception {
        check( atomElem,
               EQUAL_ELEM );
        ElementChildIterator elems = new ElementChildIterator( atomElem );

        Term leftTerm = readTerm( elems.next() );
        Term rightTerm = readTerm( elems.next() );
        return prHelper.createEqual( leftTerm,
                                     rightTerm );
    }

    protected Uniterm readUniterm(Element atomElem) throws Exception {
        check( atomElem,
               UNITERM_ELEM );
        ElementChildIterator elems = new ElementChildIterator( atomElem );
        Const operator = readConst( elems.next() );
        Uniterm uniterm = prHelper.createUniterm( operator );
        while ( elems.hasNext() ) {
            Term arg = readTerm( elems.next() );
            uniterm.addArgument( arg );
        }
        return uniterm;
    }

    protected Term readTerm(Element termElem) throws Exception {
        if ( termElem.getTagName().equals( VAR_ELEM ) ) {
            return readVar( termElem );
        } else if ( termElem.getTagName().equals( CONST_ELEM ) ) {
            return readConst( termElem );
        } else if ( termElem.getTagName().equals( UNITERM_ELEM ) ) {
            return readUniterm( termElem );
        } else {
            throw new UnsupportedOperationException();
        }
    }

    protected Variable readVar(Element varElem) throws Exception {
        check( varElem,
               VAR_ELEM );
        String qTypeStr = varElem.getAttribute( TYPE_ATTR );
        QName type = readQName( qTypeStr );
        String name = readContent( varElem );
        return prHelper.createVariable( name,
                                        type );
    }

    protected Const readConst(Element constElem) throws Exception {
        check( constElem,
               CONST_ELEM );
        String qTypeStr = constElem.getAttribute( TYPE_ATTR );
        QName type = readQName( qTypeStr );
        String name = readContent( constElem ).trim();
        if ( prHelper.qNameType.equals( type ) ) {
            name = readQName( name ).toString();
        }

        return prHelper.createConst( name,
                                     type );
    }

    protected QName readQName(String qNameStr) {
        QName qName = null;
        if ( qNameStr != null ) {
            int semiColumnIx = qNameStr.indexOf( ':' );
            if ( semiColumnIx != -1 ) {
                String alias = qNameStr.substring( 0,
                                                   semiColumnIx );
                String localPart = qNameStr.substring( semiColumnIx + 1 );
                qName = new QName( alias2Namespace.get( alias ),
                                   localPart );
            }
        }
        return qName;
    }

    protected String readContent(Element elem) {
        Text textNode = (Text) elem.getFirstChild();
        return (textNode == null ? null : textNode.getNodeValue());
    }

    protected AndCondition readAndCondition(Element condElem) throws Exception {
        check( condElem,
               AND_ELEM );
        AndCondition cond = prHelper.createAndCondition();
        ElementChildIterator elems = new ElementChildIterator( condElem );
        while ( elems.hasNext() ) {
            Element subCondElem = elems.next();
            cond.addCondition( readCondition( subCondElem ) );
        }
        return cond;
    }

    protected LogicalRule readLogicalRule(Element ruleElem) {
        throw new UnsupportedOperationException();
    }

    private static class ElementChildIterator
        implements
        Iterator<Element> {
        private final NodeList children;

        int                    nextChild;

        public ElementChildIterator(Element father) {
            children = father.getChildNodes();
            nextChild = -1;
            computeNext();
        }

        public boolean hasNext() {

            return (nextChild < children.getLength());
        }

        public boolean hasNext(String tag) {
            for ( int i = nextChild; i < children.getLength(); i++ ) {
                org.w3c.dom.Node n = children.item( i );
                if ( n instanceof Element ) {
                    Element e = (Element) n;
                    if ( e.getTagName().equals( tag ) ) {
                        return true;
                    }
                }
            }
            return false;
        }

        public Element next(String tag) {
            for ( ; nextChild < children.getLength(); nextChild++ ) {
                org.w3c.dom.Node n = children.item( nextChild );
                if ( n instanceof Element ) {
                    Element e = (Element) n;
                    if ( e.getTagName().equals( tag ) ) {
                        computeNext();
                        return e;
                    }
                }
            }
            return null;
        }

        public Element next() {

            Element elem = (Element) children.item( nextChild );
            computeNext();
            return elem;
        }

        protected void computeNext() {
            for ( nextChild++; nextChild < children.getLength(); nextChild++ ) {
                if ( children.item( nextChild ) instanceof Element ) {
                    return;
                }
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

    }
}
