package org.drools.learner;

import org.drools.learner.tools.Util;

public class LeafNode extends TreeNode {

    private Object targetCategory;
    private double numIntancesClassified;

    public LeafNode(Domain targetDomain, Object value, Object incomingCategory, TreeNode father, DecisionTree tree) {
        super(targetDomain, incomingCategory, father, tree);
        this.targetCategory = value;
        this.numIntancesClassified = 0;
    }

    public void putNode(Object attributeCategory, TreeNode node) {
        throw new RuntimeException("cannot add Node to a leaf node");
    }

    public Object getCategory() {
        return targetCategory;
    }

    public double getNumClassification() {
        return this.numIntancesClassified;
    }

    public void setNumClassification(double size) {
        this.numIntancesClassified = size;
    }

    public int getMissClassified() {
        return (int) (getNumMatch() - numIntancesClassified);
    }

    public int getNumLeaves() {
        return 1;
    }
    //	public Integer evaluate(Instance i) {
    //		String targetFName = super.getDomain().getFName();
    //		
    //		Object attr_value = i.getAttrValue(targetFName);
    //		Object i_category = super.getDomain().getCategoryOf(attr_value);
    //		if (AttributeValueComparator.instance.compare(i_category, this.targetCategory) == 0) {
    //			return Integer.valueOf(1); 	//correct
    //		} else {
    //			return Integer.valueOf(0);	// mistake
    //		}			
    //	}

    public Object voteFor(Instance i) {

        return this.targetCategory;
    }

    public int hashCode() {
        return super.hashCode() ^ targetCategory.hashCode(); // ^ id;//should i add the children
    }

    public String toString() {
        return "DECISION -> " + targetCategory.toString();
    }

    public String toString(int tab, int depth, StringBuffer buf) {
        buf.append(Util.ntimes("\t", tab + 1));

        buf.append(super.getDomain().getFName() + ":DECISION->" + targetCategory.toString() + " match: " + getNumMatch() + " correct:" + numIntancesClassified + "\n");
        return buf.toString();
    }
}
