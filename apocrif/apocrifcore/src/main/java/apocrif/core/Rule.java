package apocrif.core;

import java.util.ArrayList;
import java.util.List;

public abstract class Rule
    implements
    Node {

    public List<Variable> getVariables() {
        return variables;
    }

    public void addVariable(Variable v) {
        variables.add( v );
    }

    private List<Variable> variables = new ArrayList<Variable>();

}
