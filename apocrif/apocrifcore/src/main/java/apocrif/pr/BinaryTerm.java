package apocrif.pr;

import apocrif.core.Const;
import apocrif.core.Term;

public class BinaryTerm {

    private final Const name;
    private final Term  leftTerm;
    private final Term  rightTerm;

    public BinaryTerm(Const name,
                      Term leftTerm,
                      Term rightTerm) {
        this.name = name;
        this.leftTerm = leftTerm;
        this.rightTerm = rightTerm;
    }

    public Const getName() {
        return name;
    }

    public Term getLeftTerm() {
        return leftTerm;
    }

    public Term getRightTerm() {
        return rightTerm;
    }

}
