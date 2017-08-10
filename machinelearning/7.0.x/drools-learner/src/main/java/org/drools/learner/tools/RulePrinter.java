package org.drools.learner.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import org.drools.learner.DecisionTree;
import org.drools.learner.DecisionTreeVisitor;
import org.drools.learner.LeafNode;
import org.drools.learner.NodeValue;
import org.drools.learner.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

interface Reference {

    public String getFName();
}

public class RulePrinter {

    protected static final transient Logger log = LoggerFactory.getLogger(RulePrinter.class);
    private Class<?> ruleClazz;
    private DecisionTreeVisitor visitor;
    private ArrayList<Rule>     rules;
    private int    boundOnNumRules;
    private double numInstances;
    private HashMap<String, ArrayList<Field>> attrRelations;
    private HashMap<String, Class<?>>         importList; // TODO init
    public RulePrinter() {
        //		/* most important */
        //		this.nodes = new Stack<NodeValue>();

        this.visitor = new DecisionTreeVisitor();

        this.rules = new ArrayList<Rule>();
        //ruleText = new ArrayList<String>();

        this.boundOnNumRules = -1;
        this.numInstances = -1.0d;
    }

    //private NumberComparator nComparator;

    public static String readRules(DecisionTree learnedDt) {

        RulePrinter myPrinter = new RulePrinter(); //bocuk.getNum_fact_trained()
        myPrinter.setBoundOnNumRules(Util.MAX_NUM_RULES);
        myPrinter.print(learnedDt, Util.SORT_RULES_BY_RANK);

        String allRules = myPrinter.write2string();
        if (Util.PRINT_RULES) {
            //my_printer.write2file("examples", "src/rules/examples/" + file);
            myPrinter.write2File(allRules, false, Util.DRL_DIRECTORY + learnedDt.getSignature());
        }

        return allRules;
    }

    public Class<?> getRuleClass() {
        return ruleClazz;
    }

    public int getBoundOnNumRules() {
        return boundOnNumRules;
    }

    public void setBoundOnNumRules(int maxNumRules) {
        this.boundOnNumRules = maxNumRules;
    }

    public double getNumInstances() {
        return this.numInstances;
    }

    public void setNumInstances(int num) {
        this.numInstances = num;
    }

    public void print(DecisionTree dt, boolean sort) {//, PrintStream object
        this.ruleClazz = dt.getObjClass();
        this.attrRelations = dt.getAttrRelationMap();
        this.importList = new HashMap<String, Class<?>>();
        this.numInstances = dt.getRoot().getNumMatch();

        visitor.visit(dt);
        rules.ensureCapacity(visitor.getNumPathsFound());
        int id = 1;
        for (Path p : visitor.getPathList()) {
            //			if (id == 289)
            //				System.out.println("Here the p");
            Rule newRule = createRule(p);
            newRule.setId(id);//rules.size());
            rules.add(newRule);
            id++;
        }

        if (sort) {
            Collections.sort(rules, Rule.getRankComparator());
        }
    }

    private Rule createRule(Path p) {
        //, Stack<NodeValue> leaves // if more than one leaf
        Rule newRule = new Rule(this.getRuleClass());// (nodes, leaves) //if more than one leaf

        Iterator<NodeValue> it = p.getConditionIterator();
        while (it.hasNext()) {
            NodeValue current = it.next();
            if (log.isErrorEnabled()) {
                log.error("NodeValue " + current + "\n");
            }

            if (log.isErrorEnabled()) {
                log.error("attrRelations [" + attrRelations.size() + "]\n");
            }
            //			if (it.hasNext()) {
            ArrayList<Field> nodeRelations = attrRelations.get(current.getFReference());

            if (nodeRelations == null || nodeRelations.isEmpty()) {
                // this a direct child add
                newRule.addConditionToMain(current);
            } else {
                for (Field f : nodeRelations) {
                    // i need the class that the field belongs to boooook
                    String referenceOfCondition = Util.getDecReference(f);
                    if (log.isErrorEnabled()) {
                        log.error("[" + referenceOfCondition + "],");
                    }
                }
                if (log.isErrorEnabled()) {
                    log.error("\n");
                }
                // call with rule_decs.get(0)
                newRule.processNodeValue(current, nodeRelations, newRule.getMainDeclaration(), 0, 1); //int condition_or_action = condition = 1
            }
            //			} else {			}
        }
        NodeValue        actionNode    = p.getAction();
        ArrayList<Field> nodeRelations = attrRelations.get(actionNode.getFReference());
        if (nodeRelations == null || nodeRelations.isEmpty()) {
            // this a direct child add to reference to the main guy
            newRule.addActionToMain(actionNode);
        } else {
            newRule.processNodeValue(actionNode, nodeRelations, newRule.getMainDeclaration(), 0, 2); //int condition_or_action = action = 2
        }
        return newRule;
    }

    public void write2File(String toWrite, boolean append,
                           String fileSignature) {//DomainType domain_type, int tree_set

        //String drlFileName =
        if (!fileSignature.endsWith(".drl")) {
            fileSignature += ".drl";
        }

        File file = new File(fileSignature);
        if (append) {
            if (!file.exists())
            //flog.warn("File doesnot exit, creating...");
            {
                try {
                    BufferedWriter out = new BufferedWriter(new FileWriter(fileSignature, true));
                    out.write(toWrite);
                    out.close();
                } catch (IOException e) {
                    //flog.error("No I couldnot append to the file e:"+ e);
                    /* TODO */
                }
            }
        } else {
            if (file.exists() && (file.length() > 0)) {
                file.delete();
                //flog.warn("File exits, deleting...");
            }
            try {
                BufferedWriter out = new BufferedWriter(new FileWriter(fileSignature));
                out.write(toWrite);
                out.close();
            } catch (IOException e) {
                //flog.error("No I couldnot create the file e:"+ e);
                /* TODO */
            }
        }
    }

    public String write2string() {//String packageName) {
        StringBuffer introBuffer = new StringBuffer();
        StringBuffer bodyBuffer  = new StringBuffer();

        String packageName = this.getRuleClass().getPackage().getName();

        //log.debug("Package name: "+ packageName);

        if (packageName != null) {
            introBuffer.append("package " + packageName + ";\n\n");
        } else {
            //TODO throw exception
            //flog.error("RulePrinter write2string packageName="+packageName);
        }

        int totalNumFacts = 0;
        int i             = 0, activeI = 0;
        for (Rule rule : rules) {
            i++;
            //flog.debug("Rule: "+ i);
            if (Util.ONLY_ACTIVE_RULES) {
                if (rule.getRank() >= 0) {
                    activeI++;
                    bodyBuffer.append(rule.toString());
                    bodyBuffer.append("\n");

                    introBuffer.append(getImports(rule.getDeclarationIt()));
                }
            } else {
                if (rule.getRank() >= 0) {
                    activeI++;
                }
                bodyBuffer.append(rule.toString());
                bodyBuffer.append("\n");
                introBuffer.append(getImports(rule.getDeclarationIt()));
            }
            totalNumFacts += rule.getNumClassifiedInstances();
            if (this.getBoundOnNumRules() > 0 && i >= this.getBoundOnNumRules()) {
                break;
            }
        }
        bodyBuffer.append("//THE END: Total number of facts correctly classified= " + totalNumFacts + " over " + this.getNumInstances());
        bodyBuffer.append("\n//with " + activeI + " number of rules over " + i + " total number of rules ");
        bodyBuffer.append("\n"); // EOF

        StringBuffer outputBuffer = new StringBuffer();
        outputBuffer.append(introBuffer);
        outputBuffer.append("\n");
        outputBuffer.append(bodyBuffer);
        return outputBuffer.toString();
    }

    private StringBuffer getImports(Iterator<Declaration> declarationIt) {
        StringBuffer importBuffer = new StringBuffer();
        while (declarationIt.hasNext()) {
            Declaration dec  = declarationIt.next();
            String      name = dec.getDeclaringTypeName();
            if (!importList.containsKey(name)) {
                importList.put(name, dec.getDeclaringType());
                //String import_name = dec.getDeclaringType().getName().replaceAll("$", ".");
                String importName = dec.getDeclaringType().getName().replace('$', '.');
                importBuffer.append("import " + importName);
                importBuffer.append("\n");
            }
        }
        return importBuffer;
    }
}

class Rule {

    private Class<?>                 mainObj; // object class name
    private ArrayList<Declaration>   ruleDecs;
    private ArrayList<AttrReference> actions;

    // key: the reference of the declaration, => id of the declaration
    private HashMap<String, Integer> declarationMap;

    private int numDeclarations;

    private double rank; // matching ratio
    private double numClassifiedInstances;// number of instances matching that rule

    private int id; // unique id, need a unique name in the drl file
    //	private String referenceToMain = main_obj.getName()+"0";
    private int mainObjId = 0;

    Rule(Class<?> obj) {
        numDeclarations = 0;
        ruleDecs = new ArrayList<Declaration>(1); //new ArrayList<Declaration>(1);
        declarationMap = new HashMap<String, Integer>(1);
        mainObj = obj;
        String objRef = getObjectClassName().toLowerCase();
        declarationMap.put(objRef, numDeclarations);

        ruleDecs.add(new Declaration(mainObj, objRef, numDeclarations));
        actions = new ArrayList<AttrReference>(1);
    }

    public static Comparator<Rule> getRankComparator() {
        return new RuleComparator();
    }

    public Iterator<Declaration> getDeclarationIt() {
        return ruleDecs.iterator();
    }

    public void addConditionToMain(NodeValue current) {
        ruleDecs.get(mainObjId).addCondition(current);
    }

    public Declaration getMainDeclaration() {
        return ruleDecs.get(mainObjId);
    }

    public void addActionToMain(NodeValue current) {
        AttrReference aRef = new AttrReference(current); //D
        ruleDecs.get(mainObjId).addActionReference(aRef); //D
        addAction(aRef); //D
        setRuleStats(current); //D
    }

    public void addCondition(NodeValue current, ArrayList<Field> nodeRelations, int relId) {
        if (relId == nodeRelations.size()) { // it must be primitive
            Field  referenceField       = nodeRelations.get(nodeRelations.size() - 1);
            String referenceOfCondition = Util.getDecReference(referenceField);

            //System.out.println("It is primitive, should add a condition to its father "+referenceOfCondition+ " rel_id "+ rel_id + " size "+ (nodeRelations.size()-1)+ " \n");
            Declaration dec = ruleDecs.get(declarationMap.get(referenceOfCondition));
            dec.addCondition(current); //D
        } else {
            Field  referenceField       = nodeRelations.get(relId);
            String referenceOfCondition = Util.getDecReference(referenceField);
            //System.out.println("referenceOfCondition " +referenceOfCondition + " rel_id "+ rel_id + " size "+ (nodeRelations.size()-1));
            Declaration thePlaceDeclared = null;

            if (relId == 0) {

                thePlaceDeclared = ruleDecs.get(0);
                //System.out.println("The first guy "+referenceField.getName()+" to main declaration (ref?)"+ " rel_id "+ rel_id + " size "+ (nodeRelations.size()-1)+ " \n");

            } else {
                thePlaceDeclared = ruleDecs.get(relId - 1);//declarationMap.get(referenceOfCondition));
                //System.out.println("Continue"+referenceField.getName()+" in "+the_place_declared+ "??? rel_id "+ rel_id + " size "+ (nodeRelations.size()-1)+ " \n");

            }
            if (!thePlaceDeclared.hasReference(referenceField.getName())) {
                numDeclarations++;
                Declaration newDec = new Declaration(referenceField.getType(), referenceField.getName(), numDeclarations);
                //System.out.println("Create new dec "+referenceOfCondition+" (Main declaration doesnot have a ref"+ referenceField.getName() + ") rel_id "+ rel_id + " size "+ (nodeRelations.size()-1)+ " \n");

                thePlaceDeclared.addReference(newDec); //D
                declarationMap.put(referenceOfCondition, numDeclarations);
                ruleDecs.add(newDec);
            }

            addCondition(current, nodeRelations, relId + 1);
        }
    }

    // first time: the_place_declared = rule_decs.get(0)
    public void processNodeValue(NodeValue current, ArrayList<Field> nodeRelations, Declaration thePlaceDeclared,
                                 int relId, int conditionOrAction) {
        if (relId == nodeRelations.size()) { // it must be primitive
            Field  referenceField       = nodeRelations.get(nodeRelations.size() - 1);
            String referenceOfCondition = Util.getDecReference(referenceField);

            //System.out.println("It is primitive, should add a condition to its father "+referenceOfCondition+ " rel_id "+ rel_id + " size "+ (nodeRelations.size()-1)+ " \n");
            Declaration dec = ruleDecs.get(declarationMap.get(referenceOfCondition));

            switch (conditionOrAction) {
                case 1: // condition
                    dec.addCondition(current); //D
                    break;
                case 2: // action
                    AttrReference aRef = new AttrReference(current); //D
                    dec.addActionReference(aRef); //D
                    addAction(aRef); //D
                    setRuleStats(current); //D
                    break;
            }
            return;
        } else {
            Field  referenceField       = nodeRelations.get(relId);
            String referenceOfCondition = Util.getDecReference(referenceField);

            //Reference current_ref = the_place_declared.getReference(referenceField.getName());
            Declaration currentDec = null;
            if (!thePlaceDeclared.hasReference(referenceField.getName())) {
                //if (current_ref == null) {
                numDeclarations++;
                currentDec = new Declaration(referenceField.getType(), referenceField.getName(), numDeclarations);
                //System.out.println("Create new dec "+referenceOfCondition+" (Main declaration doesnot have a ref"+ referenceField.getName() + ") rel_id "+ rel_id + " size "+ (nodeRelations.size()-1)+ " \n");
                switch (conditionOrAction) {
                    case 1: // condition
                        thePlaceDeclared.addReference(currentDec); //D
                        break;
                    case 2: // action
                        AttrReference aRef = new AttrReference(current); //D
                        thePlaceDeclared.addActionReference(aRef); //D
                        break;
                }
                declarationMap.put(referenceOfCondition, numDeclarations);
                ruleDecs.add(currentDec);
            } else {
                //current_dec = the_place_declared.getReference(referenceField.getName()).
                currentDec = thePlaceDeclared.getReferenceDec(referenceField.getName());
            }

            processNodeValue(current, nodeRelations, currentDec, relId + 1, conditionOrAction);
        }
    }

    public void addAction(AttrReference aRef) {
        actions.add(aRef);
    }

    public void setRuleStats(NodeValue current) {
        this.setRank(((LeafNode) current.getNode()).getRank());
        this.setNumClassifiedInstances(((LeafNode) current.getNode()).getNumClassification());
    }

    public String getObjectClassName() {
        return mainObj.getSimpleName();
    }

    private int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getNumClassifiedInstances() {
        return this.numClassifiedInstances;
    }

    private void setNumClassifiedInstances(double dataSize) {
        this.numClassifiedInstances = dataSize;
    }

    public double getRank() {
        return this.rank;
    }

    public void setRank(double r) {
        this.rank = r;
    }

    public String toString() {
        /*
         * rule "Good Bye" dialect "java" when $m:Message( status ==
         * Message.GOODBYE) then System.out.println(
         * "[getLabel()] Expected value (" + $c.getLabel() +
         * "), Classified as (False)"); end
         *
         * // complex rule rule "Purchase notification" salience 10
         *
         * when $c : Customer( name == "Bla") $p : Purchase( amount == 1,
         * customer == $c) then System.out.println( "Customer " + $c.name +
         * " just purchased " + $p.product.name ); end
         */
        //"rule \"#"+getId()+" "+decision+" rank:"+rank+"\" \n";
        StringBuffer sbOut = new StringBuffer("");

        sbOut.append("\t when");
        for (int decI = ruleDecs.size() - 1; decI >= 0; decI--) {
            Declaration d      = ruleDecs.get(decI);
            String      objRef = d.getSymbol(); //"$"+this.getObjectClassName().substring(0, 1).toLowerCase();
            sbOut.append("\n\t\t " + objRef + " : " + d.getDeclaringTypeName() + "(" + "");
            Iterator<NodeValue> decIt = d.getConditionIt();
            while (decIt.hasNext()) {
                NodeValue cond = decIt.next();
                sbOut.append(cond.toString() + ", ");
            }

            Iterator<Reference> refIt = d.getReferenceIt();
            while (refIt.hasNext()) {
                Reference ref = refIt.next();
                sbOut.append(ref.toString() + ", ");
            }
            sbOut.delete(sbOut.length() - 2, sbOut.length() - 1);
            sbOut.append(")");
        }
        sbOut.append("\n");
        StringBuffer sbAction        = new StringBuffer("");
        StringBuffer sbField         = new StringBuffer("");
        StringBuffer sbExpectedValue = new StringBuffer("");
        for (AttrReference act : actions) {
            sbAction.append(act.getValue() + " , ");
            sbExpectedValue.append(act.getVariableName());
            sbField.append(act.getFName() + "");
        }
        sbAction.delete(sbAction.length() - 3, sbAction.length() - 1);

        sbOut.append("\t then ");
        sbOut.append("\n\t\t System.out.println(\"[" + sbField.toString() + "] Expected value (\" + " + sbExpectedValue.toString() + " + \"), Classified as (" + sbAction.toString() + ")\");\n");
        if (getRank() < 0) {
            sbOut.append("\n\t\t System.out.println(\"But no matching fact found = DOES not fire on\");\n");
        }

        sbOut.insert(0,
                     "rule \"#" + getId() + " " + sbField.toString() + "= " + sbAction.toString() + " classifying " + this.getNumClassifiedInstances() + " num of facts with rank:" + getRank() + "\" \n");
        sbOut.append("end\n");

        return sbOut.toString();
    }

    private static class RuleComparator implements Comparator<Rule> {

        // this will sort from best rank to least rank
        public int compare(Rule r1, Rule r2) {
            if (r1.getRank() < r2.getRank()) {
                return 1; // normally -1
            } else if (r1.getRank() > r2.getRank()) {
                return -1; // normally 1
            } else {
                return 0;
            }
        }
    }
}

class Declaration {

    private int                  id;
    private String               decRef;
    private Class<?>             declaredObj; // object class name
    //	private ArrayList<RuleNode> conditions;
    private ArrayList<NodeValue> conditions;
    private ArrayList<Reference> references;

    public Declaration(Class<?> objClass, String name, int decId) {
        id = decId;
        declaredObj = objClass;
        decRef = name;
        references = new ArrayList<Reference>();
        conditions = new ArrayList<NodeValue>();
    }

    public String getDeclaringFName() {
        return decRef;
    }

    public Class<?> getDeclaringType() {
        return declaredObj;
    }

    public String getDeclaringTypeName() {
        return declaredObj.getSimpleName();
    }

    public void addCondition(NodeValue current) {
        /*
         * TODO check do u need to copy the node value NodeValue nv = new
         * NodeValue(current.getNode());
         * nv.setNodeValue(current.getNodeValue()); conditions.add(nv); /
         **/
        conditions.add(current);
    }

    public void addActionReference(AttrReference aRef) {//NodeValue current) {
        references.add(aRef);
    }

    public void addReference(Declaration d) {
        DecReference df = new DecReference(d);
        references.add(df);
    }

    public Reference getReference(String fName) {
        for (Reference df : references) {
            if (df.getFName().equalsIgnoreCase(fName)) {
                return df;
            }
        }
        return null;
    }

    public Declaration getReferenceDec(String fName) {
        for (Reference df : references) {
            if (df.getFName().equalsIgnoreCase(fName) && df instanceof DecReference) {
                return ((DecReference) df).getDec();
            }
        }
        return null;
    }

    public boolean hasReference(String fName) {
        for (Reference df : references) {
            if (df.getFName().equalsIgnoreCase(fName)) {
                return true;
            }
        }
        return false;
    }

    public Iterator<NodeValue> getConditionIt() {
        return conditions.iterator();
    }

    public Iterator<Reference> getReferenceIt() {
        return references.iterator();
    }

    public String getSymbol() {
        return "$" + decRef + "_" + id;
    }
}

class DecReference implements Reference {

    Declaration toReference;
    private String fName;

    public DecReference(Declaration d) {
        toReference = d;
        fName = d.getDeclaringFName();
    }

    public Declaration getDec() {
        return toReference;
    }

    public String getFName() {
        return fName;
    }

    public void setReference(Declaration toReference) {
        this.toReference = toReference;
    }

    public String toString() {
        return fName + " == " + toReference.getSymbol();
    }
}

class AttrReference implements Reference {

    private String    fName;
    private NodeValue realValue;

    public AttrReference(NodeValue v) {
        String fName = v.getFName();
        if (v.getNode().getDomain().isArtificial()) {
            fName = Util.getFieldName(fName);
        }
        this.fName = fName;
        /*
         * TODO is the copying really necessary? real_value = new
         * NodeValue(v.getNode()); real_value.setNodeValue(v.getNodeValue());
         */
        realValue = v;
    }

    public Object getVariableName() {
        return "$target_label";
    }

    public Object getValue() {
        return realValue.getValue();
    }

    public String getFName() {
        return fName;
    }

    public String toString() {
        return getVariableName() + " : " + fName;
    }
}
