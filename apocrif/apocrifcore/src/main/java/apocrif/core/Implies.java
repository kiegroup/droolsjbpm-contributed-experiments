package apocrif.core;

public class Implies
    implements
    Clause {

    private Condition ifPart;
    private Atomic    thenPart;

    public <T> T accept(NodeVisitor<T> explorer) {
        return explorer.visit( this );
    }

    public Condition getIfPart() {
        return ifPart;
    }

    public Atomic getThenPart() {
        return thenPart;
    }

    public void setIfPart(Condition ifPart) {
        this.ifPart = ifPart;
    }

    public void setThenPart(Atomic thenPart) {
        this.thenPart = thenPart;
    }
}
