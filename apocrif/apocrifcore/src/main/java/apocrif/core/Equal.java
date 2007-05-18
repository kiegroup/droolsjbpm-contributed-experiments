package apocrif.core;

public class Equal
    implements
    Atomic {

    public Equal(Term leftTerm,
                 Term rightTerm) {
        this.leftTerm = leftTerm;
        this.rightTerm = rightTerm;
    }

    public Term getLeftTerm() {
        return leftTerm;
    }

    public Term getRightTerm() {
        return rightTerm;
    }

    public <T> T accept(NodeVisitor<T> v) {
        return v.visit( this );
    }

    private final Term leftTerm;
    private final Term rightTerm;
}
