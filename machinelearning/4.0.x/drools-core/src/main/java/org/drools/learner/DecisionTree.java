package org.drools.learner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.drools.learner.tools.LoggerFactory;
import org.drools.learner.tools.SimpleLogger;


public class DecisionTree {
	
	protected static SimpleLogger slog = LoggerFactory.getSysOutLogger(DecisionTree.class, SimpleLogger.DEFAULT_LEVEL);
	
	//private Class<?> obj_clazz;
	private Schema obj_schema;
	
	/* the target attribute */
	private Domain target;
	
	///* all attributes that will be used during classification */
	private ArrayList<Domain> attrsToClassify;

	private TreeNode root;
	
	// The id of the tree in the forest
	private int id;
	
	private String execution_signature;
	public long FACTS_READ = 0;

	private int validation_error;

	public DecisionTree(Schema inst_schema, String _target) {
		this.obj_schema = inst_schema; //inst_schema.getObjectClass();

		if (slog.debug() != null)
			slog.debug().log("The target attribute: "+ _target+ "\n");
		
		this.target = inst_schema.getAttrDomain(_target);
		
		if (slog.debug() != null)
			slog.debug().log("The target domain: "+ target+ "\n");
		this.attrsToClassify = new ArrayList<Domain>(inst_schema.getAttrNames().size()-1);
		for (String attr_name : inst_schema.getAttrNames()) {
			if (!attr_name.equals(_target)) {
				//flog.debug("Adding the attribute: "+ attr_name);
				if (slog.debug() != null)
					slog.debug().log("Adding the attribute: "+ attr_name+ "\n");
				this.attrsToClassify.add(inst_schema.getAttrDomain(attr_name));
			}
		}
		Collections.sort(this.attrsToClassify, new DomainComparator()); // compare the domains by the name
		
	}
	
	public DecisionTree(DecisionTree parentTree, Domain exceptDomain) {
		//this.domainSet = new Hashtable<String, Domain<?>>();
		this.obj_schema = parentTree.getSchema();
		this.target = parentTree.getTargetDomain();
		this.attrsToClassify = new ArrayList<Domain>(parentTree.getAttrDomains().size()-1);
		for (Domain attr_domain : parentTree.getAttrDomains()) {
			if (attr_domain.isNotJustSelected(exceptDomain))
				this.attrsToClassify.add(attr_domain);
		}
//		System.out.print("New tree ");
//		for (Domain d:attrsToClassify)
//			System.out.print("d: "+d);
//		System.out.println("");
		//Collections.sort(this.attrsToClassify, new Comparator<Domain>()); // compare the domains by the name
		
	}
	
	private Schema getSchema() {
		return obj_schema;
	}

	public Class<?> getObjClass() {
		return obj_schema.getObjectClass();
	}
	
	public HashMap<String, ArrayList<Field>> getAttrRelationMap() {
		return obj_schema.getAttrRelationMap();
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
	

	public void setValidationError(int error) {
		validation_error = error;
	}	
	public int getValidationError() {
		return validation_error;
	}
	public void setSignature(String executionSignature) {
		execution_signature = executionSignature;
	}
	
	public String getSignature() {
		return execution_signature;
	}
	
	@Override
	public String toString() {
		String out = "Facts scanned " + FACTS_READ + "\n";
		return out + root.toString();
	}
	
}
