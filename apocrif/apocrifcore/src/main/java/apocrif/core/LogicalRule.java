package apocrif.core;

public class LogicalRule extends Rule {
    private Clause formula;

    public <T> T accept(NodeVisitor<T> v) {
        return v.visit( this );
    }

    public Clause getFormula() {
        return formula;
    }

    public void setFormula(Clause formula) {
        this.formula = formula;
    }
}
