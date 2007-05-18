package apocrif.pr;

import javax.xml.namespace.QName;

import apocrif.core.Term;

public class XmlFieldSetter {
    public final QName xmlDeclaringCType;
    public final QName xmlField;
    public final Term  targetTerm;
    public final Term  newTerm;

    public XmlFieldSetter(QName xmlDeclaringCType,
                          QName xmlField,
                          Term targetTerm,
                          Term newTerm) {
        this.xmlDeclaringCType = xmlDeclaringCType;
        this.xmlField = xmlField;
        this.targetTerm = targetTerm;
        this.newTerm = newTerm;
    }
}
