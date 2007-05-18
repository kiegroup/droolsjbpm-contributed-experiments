package apocrif.pr;

import javax.xml.namespace.QName;

import apocrif.core.Term;

public class XmlFieldGetter {
    public final QName xmlDeclaringCType;
    public final QName xmlField;
    public final Term  targetTerm;

    public XmlFieldGetter(QName xmlDeclaringCType,
                          QName xmlField,
                          Term targetTerm) {
        this.xmlDeclaringCType = xmlDeclaringCType;
        this.xmlField = xmlField;
        this.targetTerm = targetTerm;
    }
}
