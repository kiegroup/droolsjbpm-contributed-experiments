package org.drools.learner;

import java.util.ArrayList;
import java.util.Collections;


public class DecisionTree {
	
	private Class<?> obj_clazz;
	
	/* the target attribute */
	private Domain target;
	
	///* all attributes that will be used during classification */
	private ArrayList<Domain> attrsToClassify;

	private TreeNode root;
	
	public long FACTS_READ = 0;

	public DecisionTree(Schema inst_schema, String _target) {
		this.obj_clazz = inst_schema.getObjectClass();
		
		System.out.println("The target attribute: "+ _target);
		this.target = inst_schema.getAttrDomain(_target);
		this.attrsToClassify = new ArrayList<Domain>(inst_schema.getAttrNames().size()-1);
		for (String attr_name : inst_schema.getAttrNames()) {
			if (!attr_name.equals(_target)) {
				System.out.println("Adding the attribute: "+ attr_name);
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

//	public void setTarget(String _target) {
//		this.target = _target;
//	}
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
	
	public Integer test(Instance i) {
		return this.getRoot().evaluate(i);
	}
	
	@Override
	public String toString() {
		String out = "Facts scanned " + FACTS_READ + "\n";
		return out + root.toString();
	}
	
	
	
}
