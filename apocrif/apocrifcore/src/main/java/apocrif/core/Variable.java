package apocrif.core;

import javax.xml.namespace.QName;

public class Variable
    implements
    Term {
    public Variable(String name) {
        this.name = name;
    }

    public Variable(String name,
                    QName type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public QName getType() {
        return type;
    }

    public <T> T accept(NodeVisitor<T> v) {
        return v.visit( this );
    }

    private String name;
    private QName  type;
}
