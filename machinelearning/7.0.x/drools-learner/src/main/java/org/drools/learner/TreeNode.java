package org.drools.learner;

import java.util.Collection;
import java.util.Hashtable;

import org.drools.learner.tools.Util;

public class TreeNode {

    private Domain                      domain;
    private TreeNode                    father;
    private Hashtable<Object, TreeNode> children;
    /*
     * TODO explain rank: gain: gainRatio
     */
    private double                      rank, infoMea;

    // Number of all instances matching at that node
    private double numMatchingInstances;
    private Object label;
    private int    labelSize;
    private int    leaves;

    private int depth;

    public TreeNode(Domain domain, Object category, TreeNode father, DecisionTree tree) {
        this.father = father;
        this.domain = domain;
        this.children = new Hashtable<Object, TreeNode>();
        if (father != null) {
            father.addChild(category, this);
        } else {
            tree.setRoot(this);
        }
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int d) {
        depth = d;
    }

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    public double getNumMatch() {
        return this.numMatchingInstances;
    }

    public void setNumMatch(double size) {
        this.numMatchingInstances = size;
    }

    public Domain getDomain() {
        return domain;
    }

    public void addChild(Object key, TreeNode child) {
        child.father = this;
        children.put(key, child);
    }

    public Collection<Object> getChildrenKeys() {
        return children.keySet();
    }

    public TreeNode getChild(Object attrKey) {
        return children.get(attrKey);
    }

    public double getInfoMea() {
        return infoMea;
    }

    public void setInfoMea(double mea) {
        this.infoMea = mea;
    }

    public Object getLabel() {
        return label;
    }

    public void setLabel(Object getWinnerClass) {
        label = getWinnerClass;
    }

    public int getNumLabeled() {
        return labelSize;
    }

    public void setNumLabeled(int supportersFor) {
        labelSize = supportersFor;
    }

    public int getNumLeaves() {
        return leaves;
    }

    public void setNumLeaves(int leaves2) {
        leaves = leaves2;
    }

    public TreeNode getFather() {
        return father;
    }

    public Object voteFor(Instance i) {
        final Object attrValue = i.getAttrValue(this.domain.getFReferenceName());
        final Object category  = domain.getCategoryOf(attrValue);

        final TreeNode myNode = this.getChild(category);

        //		flog.debug(new Object() {
        //			public String toString() {
        //				
        //				StringBuffer sb = new StringBuffer(Util.ntimes("$", 5)+"\nDomain:"+domain.getFName()+"->");
        //				for (int idx = 0; idx < domain.getCategoryCount(); idx++) {
        //					sb.append(domain.getCategory(idx)+"-");
        //				}
        //				sb.append(" SEARCHING for = "+ attr_value + " in "+ domain.getFName());
        //				sb.append("\n KEYS:");
        //				for (Object key: getChildrenKeys()) {
        //					sb.append(" "+key +"% "+ getChild(key).getDomain() + " :");
        //				}
        //				return sb.toString();
        //			}
        //		});
        return myNode.voteFor(i);
    }

    public int getMissClassified() {
        int numMissclassified = 0;
        for (Object key : this.getChildrenKeys()) {
            TreeNode child = this.getChild(key);
            numMissclassified += child.getMissClassified();
        }
        return numMissclassified;
    }

    public int hashCode() {
        return domain.hashCode(); // ^ id;//should i add the children
    }

    public String toString() {
        return toString(1, 5, new StringBuffer());
    }

    public String toString(int tab, int depth, StringBuffer buf) {
        //if (depth > 0 && domain != null) {
        if (domain != null) {
            buf.append(Util.ntimes("\t", tab));
            buf.append(Util.ntimes("***", 1));
            buf.append(domain.getFName() + " n.hash:" + this.hashCode() + " match:" + numMatchingInstances + " correct:" + labelSize + " \n");
            for (Object attributeValue : children.keySet()) {
                buf.append(Util.ntimes("\t", tab + 1));
                buf.append("+" + attributeValue);
                buf.append("\n");
                TreeNode child = children.get(attributeValue);
                buf.append(child.toString(tab + 1, --depth, new StringBuffer()));
            }
        }
        return buf.toString();
    }
}
