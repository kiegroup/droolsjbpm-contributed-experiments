package apocrif.core;

import javax.xml.namespace.QName;

public class Const
    implements
    Term {
    public Const(String name) {
        this( name,
              null );
    }

    public Const(String name,
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

    public boolean equals(Object o) {
        if ( this == o ) return true;
        else if ( o instanceof Const ) {
            Const c2 = (Const) o;
            boolean sameName = (name == null ? c2.name == null : name.equals( c2.name ));
            if ( sameName ) {
                return (type == null ? c2.type == null : type.equals( c2.type ));
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private final String name;
    private final QName  type;
}
