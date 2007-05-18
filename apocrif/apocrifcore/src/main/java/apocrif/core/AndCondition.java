package apocrif.core;

import java.util.ArrayList;
import java.util.List;

public class AndCondition
    implements
    Condition {

    public void addCondition(Condition c) {
        conditions.add( c );
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public <T> T accept(NodeVisitor<T> v) {
        return v.visit( this );
    }

    private List<Condition> conditions = new ArrayList<Condition>();
}
