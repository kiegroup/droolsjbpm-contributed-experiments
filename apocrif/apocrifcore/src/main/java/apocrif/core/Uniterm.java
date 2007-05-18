package apocrif.core;

import java.util.ArrayList;
import java.util.List;

public class Uniterm
    implements
    Atomic,
    Term {

    public Uniterm(Const operator) {
        this.operator = operator;
    }

    public Const getOperator() {
        return operator;
    }

    public List<Term> getArguments() {
        return arguments;
    }

    public void addArgument(Term arg) {
        arguments.add( arg );
    }

    public <T> T accept(NodeVisitor<T> v) {
        return v.visit( this );
    }

    private final Const operator;
    private List<Term>  arguments = new ArrayList<Term>();
}
