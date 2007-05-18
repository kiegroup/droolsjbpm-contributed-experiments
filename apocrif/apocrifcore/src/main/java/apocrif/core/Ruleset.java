package apocrif.core;

import java.util.ArrayList;
import java.util.List;

public class Ruleset {

    public Ruleset(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void addRule(Rule rule) {
        rules.add( rule );
    }

    private String     name;

    private List<Rule> rules = new ArrayList();

}
