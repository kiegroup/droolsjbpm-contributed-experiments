package dt;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import dt.memory.Domain;

public class DecisionTree {

	public long FACTS_READ = 0;

	/* set of the attributes, their types */
	private Hashtable<String, Domain<?>> domainSet;

	/* the class of the objects */
	private String className;

	/* the target attribute */
	private String target;

	private TreeNode root;

	/* all attributes that can be used during classification */
	private ArrayList<String> attrsToClassify;

	public DecisionTree(String klass) {
		this.className = klass;
		this.domainSet = new Hashtable<String, Domain<?>>();
		this.attrsToClassify = new ArrayList<String>();
	}


	public void setTarget(String targetField) {
		target = targetField;
		attrsToClassify.remove(target);
	}

	public void addDomain(Domain<?> domain) {
		domainSet.put(domain.getName(), domain);
		if (!domain.getName().equals(this.target))
			attrsToClassify.add(domain.getName());

	}

	public List<?> getPossibleValues(String fieldName) {
		return domainSet.get(fieldName).getValues();
	}


	public List<String> getAttributes() {
		return attrsToClassify;
	}

	public String getTarget() {
		return target;
	}

	public String getName() {
		return className;
	}

	public Domain<?> getDomain(String key) {
		return domainSet.get(key);
	}

	public TreeNode getRoot() {
		return (root);

	}

	public void setRoot(TreeNode root) {
		this.root = root;

	}

	public long getNumRead() {
		return FACTS_READ;
	}

	@Override
	public String toString() {
		return "Facts scanned " + FACTS_READ + "\n" + root.toString();
	}

	/*
	 * **OPT int getTotalSize(List<FactSet> facts) {
	 * 
	 * int num = 0; for(FactSet fs : facts) { num += fs.getSize(); }
	 * 
	 * return num; }
	 */

}
