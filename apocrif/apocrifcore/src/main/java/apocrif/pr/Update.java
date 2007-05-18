package apocrif.pr;

import apocrif.core.Term;

public class Update {
    public Update(Term target,
                  Term statement) {
        this.target = target;
        this.statement = statement;
    }

    public Term getStatement() {
        return statement;
    }

    public Term getTarget() {
        return target;
    }

    private final Term target;
    private final Term statement;

}
