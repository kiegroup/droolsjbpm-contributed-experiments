package apocrif.engine.jrules;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

public class Model {
    private static final String XSD_NAMESPACE   = "http://www.w3.org/2001/XMLSchema";

    private static final QName  qNameType       = new QName( XSD_NAMESPACE,
                                                             "QName" );

    private static final QName  stringType      = new QName( XSD_NAMESPACE,
                                                             "string" );

    private static final QName  decimalType     = new QName( XSD_NAMESPACE,
                                                             "decimal" );

    private static final QName  intType         = new QName( XSD_NAMESPACE,
                                                             "int" );

    IlrReflect                  xom;

    IlrXmlModel                 xmlModel;

    Map<IlrType, QName>         xomType2xmlType = new HashMap<IlrType, QName>();

    Map<QName, IlrType>         xmlType2xomType = new HashMap<QName, IlrType>();

    public Model(IlrReflect xom,
                 IlrXmlModel xmlModel) {
        this.xom = xom;
        this.xmlModel = xmlModel;
        xomType2xmlType.put( xom.getStringClass(),
                             stringType );
        xomType2xmlType.put( xom.getDoubleType(),
                             decimalType );
        xomType2xmlType.put( xom.getIntType(),
                             intType );
        xmlType2xomType.put( stringType,
                             xom.getStringClass() );
        xmlType2xomType.put( decimalType,
                             xom.getDoubleType() );
        xmlType2xomType.put( intType,
                             xom.getIntType() );
    }

    public IlrReflect getXom() {
        return xom;
    }

    public QName getXmlType(IlrType xomType) {
        QName qName = null;
        String xomTypeFqn = xomType.getFullyQualifiedName();
        IlrXmlClass xmlClass = xmlModel.getClass( xomTypeFqn );
        if ( xmlClass == null ) {
            IlrXmlSimpleType xmlSType = xmlModel.getSimpleType( xomTypeFqn );
            if ( xmlSType == null ) {
                qName = xomType2xmlType.get( xomType );
            } else {
                qName = new QName( xmlSType.getXmlNamespace(),
                                   xmlSType.getXmlName() );
            }

        } else {
            qName = new QName( xmlClass.getXmlNamespace(),
                               xmlClass.getXmlName() );
        }

        return qName;
    }

    public QName getXmlField(IlrAttribute xomAttribute) {
        QName qName = null;

        IlrXmlFieldInfo fieldInfo = IlrXmlXomFactory.getFieldInfo( xomAttribute );
        if ( fieldInfo != null ) {
            qName = new QName( fieldInfo.getXmlNamespace(),
                               fieldInfo.getXmlLocalName() );
        }

        return qName;
    }

    public IlrType getXomType(QName xmlType) {
        IlrType xomType = null;

        IlrXmlClass xmlClass = xmlModel.getClass( xmlType.getNamespaceURI(),
                                                  xmlType.getLocalPart() );
        if ( xmlClass == null ) {
            IlrXmlSimpleType xmlSType = xmlModel.getSimpleType( xmlType.getNamespaceURI(),
                                                                xmlType.getLocalPart() );
            if ( xmlSType != null ) {
                xomType = xom.getType( xmlClass.getName() );
            } else {
                xomType = xmlType2xomType.get( xmlType );
            }
        } else {
            xomType = xom.getType( xmlClass.getName() );
        }
        return xomType;
    }

    public IlrAttribute getXomField(QName xmlComplexType,
                                    QName xmlField) {
        IlrAttribute xomAttribute = null;
        IlrXmlClass xmlClass = xmlModel.getClass( xmlComplexType.getNamespaceURI(),
                                                  xmlComplexType.getLocalPart() );
        if ( xmlClass != null ) {

            IlrClass xomClass = xom.getClass( xmlClass.getName() );
            if ( xomClass != null ) {
                Iterator attributeIte = xomClass.allAttributes();
                while ( attributeIte.hasNext() ) {
                    IlrAttribute xomAttr = (IlrAttribute) attributeIte.next();
                    IlrXmlFieldInfo fieldInfo = IlrXmlXomFactory.getFieldInfo( xomAttr );
                    if ( fieldInfo != null ) {
                        if ( fieldInfo.getXmlName().isSame( xmlField.getNamespaceURI(),
                                                            xmlField.getLocalPart() ) ) {
                            xomAttribute = xomAttr;
                            break;
                        }
                    }
                }
            }
        }
        return xomAttribute;
    }
}
