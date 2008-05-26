package org.drools.learner;

import java.util.ArrayList;
import java.util.Collections;

import org.drools.learner.tools.Util;


public class DecisionTree {
	
	private Class<?> obj_clazz;
	
	/* the target attribute */
	private Domain target;
	
	///* all attributes that will be used during classification */
	private ArrayList<Domain> attrsToClassify;

	private TreeNode root;
	
	// The id of the tree in the forest
	private int id;
	
	public long FACTS_READ = 0;

	public DecisionTree(Schema inst_schema, String _target) {
		this.obj_clazz = inst_schema.getObjectClass();
		
		if (Util.DEBUG_DECISION_TREE) {
			System.out.println("The target attribute: "+ _target);
		}
		this.target = inst_schema.getAttrDomain(_target);
		this.attrsToClassify = new ArrayList<Domain>(inst_schema.getAttrNames().size()-1);
		for (String attr_name : inst_schema.getAttrNames()) {
			if (!attr_name.equals(_target)) {
				if (Util.DEBUG_DECISION_TREE) {
					System.out.println("Adding the attribute: "+ attr_name);
				}
				this.attrsToClassify.add(inst_schema.getAttrDomain(attr_name));
			}
		}
		Collections.sort(this.attrsToClassify, new DomainComparator()); // compare the domains by the name
		
	}
	
	public DecisionTree(DecisionTree parentTree, Domain exceptDomain) {
		//this.domainSet = new Hashtable<String, Domain<?>>();

		this.target = parentTree.getTargetDomain();
		this.attrsToClassify = new ArrayList<Domain>(parentTree.getAttrDomains().size()-1);
		for (Domain attr_domain : parentTree.getAttrDomains()) {
			if (!attr_domain.getFName().equals(exceptDomain.getFName()))
				this.attrsToClassify.add(attr_domain);
		}
		//Collections.sort(this.attrsToClassify, new Comparator<Domain>()); // compare the domains by the name
		
	}
	
	public Class<?> getObjClass() {
		return obj_clazz;
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
	
	@Override
	public String toString() {
		String out = "Facts scanned " + FACTS_READ + "\n";
		return out + root.toString();
	}
	
	
	
}
