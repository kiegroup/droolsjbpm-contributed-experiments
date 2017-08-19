package org.drools.learner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.drools.learner.eval.TreeStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DecisionTree {
    protected static final transient Logger       log        = LoggerFactory.getLogger(DecisionTree.class);
    public                           long         FACTS_READ = 0;
    //private Class<?> obj_clazz;
    private Schema objSchema;
    /* the target attribute */
    private Domain target;
    ///* all attributes that will be used during classification */
    private ArrayList<Domain> attrsToClassify;
    private TreeNode root;
    // The id of the tree in the forest
    private int id;
    private String executionSignature;

    //	private double validation_error, training_error;
    private TreeStats errorStats;
    private int       numNonterminalNodes;
    private int       trainingDataSize, testDataSize;
    //private InstanceList train, test;

    public DecisionTree(Schema instSchema, String targetAttributeName) {
        this.objSchema = instSchema; //inst_schema.getObjectClass();
        this.numNonterminalNodes = 0;

        if (log.isDebugEnabled()) {
            log.debug("The target attribute: " + targetAttributeName + "\n");
        }

        this.target = instSchema.getAttrDomain(targetAttributeName);

        if (log.isDebugEnabled()) {
            log.debug("The target domain: " + target + "\n");
        }
        this.attrsToClassify = new ArrayList<Domain>(instSchema.getAttrNames().size() - 1);
        for (String attrName : instSchema.getAttrNames()) {
            if (!attrName.equals(targetAttributeName)) {
                //flog.debug("Adding the attribute: "+ attr_name);
                if (log.isDebugEnabled()) {
                    log.debug("Adding the attribute: " + attrName + "\n");
                }
                this.attrsToClassify.add(instSchema.getAttrDomain(attrName));
            }
        }
        Collections.sort(this.attrsToClassify, new DomainComparator()); // compare the domains by the name
        errorStats = new TreeStats(0.0d, 0.0d);
    }

    private Schema getSchema() {
        return objSchema;
    }

    public Class<?> getObjClass() {
        return objSchema.getObjectClass();
    }

    public HashMap<String, ArrayList<Field>> getAttrRelationMap() {
        return objSchema.getAttrRelationMap();
    }

    public int getId() {
        return id;
    }

    public void setID(int i) {
        this.id = i;
    }

    public Domain getTargetDomain() {
        return target;
    }

    public ArrayList<Domain> getAttrDomains() {
        return attrsToClassify;
    }

    public TreeNode getRoot() {
        return (root);
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    public Object vote(Instance i) {
        return this.getRoot().voteFor(i);
    }

    public TreeStats getStats() {
        return errorStats;
    }

    public void changeTestError(double error) {
        errorStats.setErrorEstimation(errorStats.getErrorEstimation() + error);
    }

    public void changeTrainError(double error) {
        errorStats.setTrainError(errorStats.getTrainError() + error);
    }

    public double getTestError() {
        return errorStats.getErrorEstimation();
    }

    public double getTrainError() {
        return errorStats.getTrainError();
    }

    //	public void setValidationError(double error) {
    //		validation_error = error;
    //	}	
    //	public double getValidationError() {
    //		return validation_error;
    //	}
    //	
    //	public void setTrainingError(double error) {
    //		training_error = error;
    //	}
    //	public double getTrainingError() {
    //		return training_error;
    //	}

    public int calcNumNodeLeaves(TreeNode myNode) {
        if (myNode instanceof LeafNode) {

            return 1;
        } else {
            numNonterminalNodes++; // TODO does this really work?
        }

        int leaves = 0;
        for (Object childKey : myNode.getChildrenKeys()) {
            /* split the last two class at the same time */

            TreeNode child = myNode.getChild(childKey);
            leaves += calcNumNodeLeaves(child);
        }
        myNode.setNumLeaves(leaves);
        return leaves;
    }

    //private ArrayList<LeafNode> leaf_nodes;
    public ArrayList<LeafNode> getLeaves(TreeNode startNode) {
        ArrayList<LeafNode> terminalNodes = new ArrayList<LeafNode>();

        findLeaves(terminalNodes, startNode);

        return terminalNodes;
    }

    private int findLeaves(ArrayList<LeafNode> terminals, TreeNode myNode) {
        if (myNode instanceof LeafNode) {
            terminals.add((LeafNode) myNode);
            return 1;
        } else {
            numNonterminalNodes++; // TODO does this really work?
        }

        int leaves = 0;
        for (Object childKey : myNode.getChildrenKeys()) {
            /* split the last two class at the same time */

            TreeNode child = myNode.getChild(childKey);
            leaves += findLeaves(terminals, child);
        }
        //my_node.setNumLeaves(leaves);
        return leaves;
    }

    public ArrayList<TreeNode> getAnchestorOfLeaves(TreeNode startNode) {
        ArrayList<LeafNode> terminalNodes = new ArrayList<LeafNode>();

        ArrayList<TreeNode> ancTerminalNodes = new ArrayList<TreeNode>();

        findLeaves(terminalNodes, ancTerminalNodes, startNode);

        return ancTerminalNodes;
    }

    private int findLeaves(ArrayList<LeafNode> terminals, ArrayList<TreeNode> anchestors, TreeNode myNode) {

        int     leaves         = 0;
        boolean anchestorAdded = false;
        for (Object childKey : myNode.getChildrenKeys()) {
            /* split the last two class at the same time */

            TreeNode child = myNode.getChild(childKey);
            if (child instanceof LeafNode) {
                terminals.add((LeafNode) child);
                if (!anchestorAdded) {
                    numNonterminalNodes++; // TODO does this really work?
                    anchestors.add(myNode);
                    anchestorAdded = true;
                }
                return 1;
            } else {
                leaves += findLeaves(terminals, anchestors, child);
            }
        }
        //my_node.setNumLeaves(leaves);
        return leaves;
    }

    public int getNumNonTerminalNodes() {
        return numNonterminalNodes;
    }

    public String getSignature() {
        return executionSignature;
    }

    public void setSignature(String executionSignature) {
        this.executionSignature = executionSignature;
    }

    @Override
    public String toString() {
        String out = "Facts scanned " + FACTS_READ + "\n";
        return out + root.toString();
    }

    public int getTrainingDataSize() {
        return trainingDataSize;
    }

    public void setTrainingDataSize(int size) {
        trainingDataSize = size;
    }

    public int getTestingDataSize() {
        return testDataSize;
    }

    public void setTestingDataSize(int size) {
        testDataSize = size;
    }

    //	public void setTrain(InstanceList x) {
    //		train = x;
    //	}
    //	
    //	public void setTest(InstanceList x) {
    //		 test = x;
    //	}
    //
    //	public InstanceList getTrain() {
    //		return train;
    //	}
    //	
    //	public InstanceList getTest() {
    //		return test;
    //	}
}
