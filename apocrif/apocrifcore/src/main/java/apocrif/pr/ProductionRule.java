package apocrif.pr;

import apocrif.core.AndCondition;
import apocrif.core.Atomic;
import apocrif.core.Condition;
import apocrif.core.NodeVisitor;
import apocrif.core.Rule;

public class ProductionRule extends Rule {

    public ProductionRule(String name) {
        this.name = name;
    }

    public Condition getIfPart() {
        return ifPart;
    }

    public Atomic getThenPart() {
        return thenPart;
    }

    public void setIfPart(Condition c) {
        ifPart = c;
    }

    public void setThenPart(Atomic a) {
        thenPart = a;
    }

    public String getName() {
        return name;
    }

    public AndCondition getConstraintPart() {
        return constraintPart;
    }

    public void setConstraintPart(AndCondition constraintPart) {
        this.constraintPart = constraintPart;
    }

    public void addConstraint(Atomic constraint) {
        constraintPart.addCondition( constraint );
    }

    public <T> T accept(NodeVisitor<T> v) {
        if ( v instanceof PRNodeVisitor ) {
            return ((PRNodeVisitor<T>) v).visit( this );
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private String       name;

    private Condition    ifPart;

    private Atomic       thenPart;

    private AndCondition constraintPart = new AndCondition();

}
