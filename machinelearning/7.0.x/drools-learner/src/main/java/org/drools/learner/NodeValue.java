package org.drools.learner;

public class NodeValue { //implements RuleNode {
	
	//private static final Logger flog = LoggerFactory.getFileLogger(NodeValue.class, LogLevel.ERROR, Util.log_file); 
	
	private TreeNode node;
	private Object nodeValue;	// should it be Attribute???
	
	public NodeValue(TreeNode n) {
		this.node = n;
	}
	public String getFReference() {
		return node.getDomain().getFReferenceName();
	}
	
	public String getFName() {
//		String full_name = node.getDomain().getFName();
//		String fname = full_name.substring(full_name.lastIndexOf('@')+1, full_name.length());
		return node.getDomain().getFName() ;
	}
	
	public Object getValue() {
		return nodeValue;
	}
	public void setValue(Object category) {
		this.nodeValue = category;
		
	}
	
	public TreeNode getNode() {
		return node;
	}
	
	@Override
	public int hashCode() {
		String hash = stringCode();
		return hash.hashCode();
		
	}
	
	public String stringCode() {
		return node.getDomain().getObjKlass().getName()+"." + node.getDomain().getFName()+"."+nodeValue;
	}
	
	public String toString() {
		
		String fName = this.getFName();//object class name
		Class<?> node_obj = node.getDomain().getObjKlass();
		
		String value;
		if (node.getDomain().getFType() == String.class)
			value = "\""+nodeValue+ "\""; 
		else
			value = nodeValue + "";
		
		if (node.getDomain().isCategorical())
			return fName + " == "+ value; 
		else {
			
			int size = node.getDomain().getCategoryCount()-1;
			//System.out.println("How many guys there of "+node.getDomain().getName() +" and the value "+nodeValue+" : "+size);
			
			int idx = size;
			if (nodeValue instanceof Number) {
				for (; idx>=0; idx--) {
					Object categoryValue = node.getDomain().getCategory(idx);
					if (nodeValue instanceof Comparable && categoryValue instanceof Comparable) {
						// TODO ask this to daniel???
//						if (Util.DEBUG_RULE_PRINTER) {
//							System.out.println("NodeValue:"+ nodeValue+ " c-"+nodeValue.getClass() +" & category:"+ categoryValue+ " c-"+categoryValue.getClass());
//						}
						if ( AttributeValueComparator.instance.compare(nodeValue, categoryValue) == 0 ) {
							break;
						}
					} else {
						System.out.println("Fuck not comparable NodeValue:"+ nodeValue+ " c-"+nodeValue.getClass() +" & category:"+ categoryValue+ " c-"+categoryValue.getClass());
						System.exit(0);
					}

				}
			} else {
				/* TODO implement the String setting */
				System.out.println("Fuck not number:"+ nodeValue+ " c-"+nodeValue.getClass());
				System.exit(0);

			}
			
			if (idx == 0)
				return fName + " <= "+ value;
			else if (idx == size)
				// if the category is the last one that the rule is domain.name > category(last-1)
				return fName+ " > "+ node.getDomain().getCategory(size-1);
			else {
				//return node.getDomain().getCategory(idx) + " < " + fName+ " <= "+ node.getDomain().getCategory(idx+1);
				// Why drools does not support category(idx) < domain.name <= category(idx+1)
				//flog.debug("value "+ value + "=====?????"+   node.getDomain().getCategory(idx+1));
				
				return fName+ " <= "+ value; // node.getDomain().getCategory(idx+1);
			}
		}
	}
	
}