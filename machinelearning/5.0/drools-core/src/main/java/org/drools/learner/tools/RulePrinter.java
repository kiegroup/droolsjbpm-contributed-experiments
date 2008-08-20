package org.drools.learner.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
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

public class RulePrinter {
	
	private static SimpleLogger flog = LoggerFactory.getUniqueFileLogger(RulePrinter.class, SimpleLogger.WARN);
	private static SimpleLogger slog = LoggerFactory.getSysOutLogger(RulePrinter.class, SimpleLogger.DEBUG);
	
	public static Reader readRules(DecisionTree learned_dt) {
		
		RulePrinter my_printer = new RulePrinter();	//bocuk.getNum_fact_trained()
		my_printer.setBoundOnNumRules(Util.MAX_NUM_RULES);
		my_printer.print(learned_dt, Util.SORT_RULES_BY_RANK);
		
		String all_rules = my_printer.write2string();
		if (Util.PRINT_RULES) {
			//my_printer.write2file("examples", "src/rules/examples/" + file);
			my_printer.write2File(all_rules, false, Util.DRL_DIRECTORY+learned_dt.getSignature());
		}
		
		return new StringReader(all_rules);
	}

	private Class<?> rule_clazz;	
	
	private DecisionTreeVisitor visitor;
	private ArrayList<Rule> rules;
	
	private int bound_on_num_rules;
	private double num_instances;

	private HashMap<String, ArrayList<Field>> attrRelations;
	private HashMap<String, Class<?>> importList;	// TODO init
	
	//private NumberComparator nComparator;
	
	public RulePrinter() {
//		/* most important */
//		this.nodes = new Stack<NodeValue>();
		
		this.visitor = new DecisionTreeVisitor();
		
		this.rules = new ArrayList<Rule>();
		//ruleText = new ArrayList<String>();
		
		this.bound_on_num_rules = -1;
		this.num_instances = -1.0d;
	}
	
	public Class<?> getRuleClass() {
		return rule_clazz;
	}
	
	public int getBoundOnNumRules() {
		return bound_on_num_rules;
	}

	public void setBoundOnNumRules(int max_num_rules) {
		this.bound_on_num_rules = max_num_rules;
	}
	
	public double getNumInstances() {
		return this.num_instances;
	}

	public void setNumInstances(int num) {
		this.num_instances = num;
	}
	
	public void print(DecisionTree dt, boolean sort) {//, PrintStream object
		this.rule_clazz = dt.getObjClass();
		this.attrRelations = dt.getAttrRelationMap();
		this.importList = new HashMap<String, Class<?>>();
		this.num_instances = dt.getRoot().getNumMatch();
		
		visitor.visit(dt);
		rules.ensureCapacity(visitor.getNumPathsFound());
		int id = 1;
		for (Path p: visitor.getPathList()) {
//			if (id == 289)
//				System.out.println("Here the p");
			Rule newRule = createRule(p);
			newRule.setId(id);//rules.size());
			rules.add(newRule);
			id++;
		}
		
		if (sort)
			Collections.sort(rules, Rule.getRankComparator());
	}

	private Rule createRule(Path p) {
		//, Stack<NodeValue> leaves // if more than one leaf
		Rule newRule = new Rule(this.getRuleClass());// (nodes, leaves) //if more than one leaf
		
		Iterator<NodeValue> it = p.getConditionIterator();
		while (it.hasNext()) {
			NodeValue current = it.next();
			if (slog.error() != null)
				slog.error().log("NodeValue " +current + "\n");
			
			if (slog.error() != null)
				slog.error().log("attrRelations [" +attrRelations.size() + "]\n");
//			if (it.hasNext()) { 
			ArrayList<Field> nodeRelations = attrRelations.get(current.getFReference());

			if (nodeRelations == null || nodeRelations.isEmpty()) { 
				// this a direct child add 
				newRule.addConditionToMain(current);
			} else {
				for (Field f:nodeRelations) {	
					// i need the class that the field belongs to boooook
					String referenceOfCondition = Util.getDecReference(f);
					if (slog.error() != null)
						slog.error().log("[" +referenceOfCondition + "],");
				}
				if (slog.error() != null)
					slog.error().log("\n");
				// call with rule_decs.get(0)
				newRule.processNodeValue(current, nodeRelations, newRule.getMainDeclaration(), 0, 1);	//int condition_or_action = condition = 1
			}
//			} else {			}
		}
		NodeValue action_node = p.getAction();
		ArrayList<Field> nodeRelations = attrRelations.get(action_node.getFReference());
		if (nodeRelations == null || nodeRelations.isEmpty()) { 
			// this a direct child add to reference to the main guy 
			newRule.addActionToMain(action_node);
		} else {
			newRule.processNodeValue(action_node, nodeRelations, newRule.getMainDeclaration(), 0, 2);	//int condition_or_action = action = 2	
		}
		return newRule;	
	}
	
	public void write2File(String toWrite, boolean append, String fileSignature) {//DomainType domain_type, int tree_set  
	
		//String drlFileName = 
		if (!fileSignature.endsWith(".drl"))
			fileSignature += ".drl";
		System.out.println("file:"+ fileSignature);
		File file =new File(fileSignature);
		if (append)
		{
			if(!file.exists())
				//flog.warn("File doesnot exit, creating...");
			try {
				BufferedWriter out = new BufferedWriter(new FileWriter(fileSignature, true));
				out.write(toWrite);
				out.close();
				//System.out.println("I wrote "+ toWrite);
			} catch (IOException e) {
				//flog.error("No I couldnot append to the file e:"+ e);
				/* TODO */
			}

		} else {
			if(file.exists()&& (file.length()>0)) {
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
	
	public String write2string(){//String packageName) {
		StringBuffer introBuffer = new StringBuffer();
		StringBuffer bodyBuffer = new StringBuffer();
		
		String packageName = this.getRuleClass().getPackage().getName();
		
		//log.debug("Package name: "+ packageName);
		
		if (packageName != null)
			introBuffer.append("package " + packageName +";\n\n");	
		else {
			//TODO throw exception
			//flog.error("RulePrinter write2string packageName="+packageName);
		}
	
		int total_num_facts=0;
		int i = 0, active_i = 0;
		for( Rule rule: rules) {
			i++;
			//flog.debug("Rule: "+ i);
			if (Util.ONLY_ACTIVE_RULES) {
				if (rule.getRank() >= 0) {
					active_i++;
					bodyBuffer.append(rule.toString());
					bodyBuffer.append("\n");
					
					introBuffer.append(getImports(rule.getDeclarationIt()));
				}

			} else {
				if (rule.getRank() >= 0) {
					active_i++;
				}
				bodyBuffer.append(rule.toString());
				bodyBuffer.append("\n");
				introBuffer.append(getImports(rule.getDeclarationIt()));
			}
			total_num_facts += rule.getNumClassifiedInstances();	
			if (this.getBoundOnNumRules()>0 && i >= this.getBoundOnNumRules())
				break;
		}
		bodyBuffer.append("//THE END: Total number of facts correctly classified= "+ total_num_facts + " over "+ this.getNumInstances());
		bodyBuffer.append("\n//with " + active_i + " number of rules over "+i+" total number of rules ");
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
			Declaration dec = declarationIt.next();
			String name = dec.getDeclaringTypeName();
			if (!importList.containsKey(name)) {
				importList.put(name, dec.getDeclaringType());
				//String import_name = dec.getDeclaringType().getName().replaceAll("$", ".");
				String import_name = dec.getDeclaringType().getName().replace('$', '.');
				importBuffer.append("import "+ import_name);
				importBuffer.append("\n"); 
			}
		}
		return importBuffer;
	}
}

class Rule {
	
	private Class<?> main_obj;	// object class name
	private ArrayList<Declaration> rule_decs;
	private ArrayList<AttrReference>  actions;
	
	// key: the reference of the declaration, => id of the declaration
	private HashMap <String, Integer> declarationMap;
	
	private int num_declarations; 
	
	private double rank;				 // matching ratio
	private double num_classified_instances;// number of instances matching that rule
	
	private int id;						 // unique id, need a unique name in the drl file
//	private String referenceToMain = main_obj.getName()+"0";
	private int main_obj_id = 0;
	
	Rule(Class<?> obj) {
		num_declarations = 0;
		rule_decs = new ArrayList<Declaration>(1); //new ArrayList<Declaration>(1);
		declarationMap = new HashMap<String, Integer>(1);
		main_obj= obj;
		String obj_ref = getObjectClassName().toLowerCase();
		declarationMap.put(obj_ref, num_declarations);
		
		rule_decs.add(new Declaration(main_obj, obj_ref, num_declarations));
		actions = new ArrayList<AttrReference>(1);
	}
	
	public Iterator<Declaration> getDeclarationIt() {
		return rule_decs.iterator();
	}

	public void addConditionToMain(NodeValue current) {
		rule_decs.get(main_obj_id).addCondition(current);
	}
	
	public Declaration getMainDeclaration(){
		return rule_decs.get(main_obj_id);
	}
	public void addActionToMain(NodeValue current) {
		AttrReference aRef = new AttrReference(current);	//D
		rule_decs.get(main_obj_id).addActionReference(aRef);						//D
		addAction(aRef);									//D
		setRuleStats(current);								//D
	}
	
	public void addCondition(NodeValue current, ArrayList<Field> nodeRelations, int rel_id) {
		if (rel_id == nodeRelations.size()) {	// it must be primitive
			Field referenceField = nodeRelations.get(nodeRelations.size()-1);			
			String referenceOfCondition = Util.getDecReference(referenceField);
			
			//System.out.println("It is primitive, should add a condition to its father "+referenceOfCondition+ " rel_id "+ rel_id + " size "+ (nodeRelations.size()-1)+ " \n");	
			Declaration dec = rule_decs.get(declarationMap.get(referenceOfCondition));
			dec.addCondition(current);					//D
		} else {
			Field referenceField = nodeRelations.get(rel_id);			
			String referenceOfCondition = Util.getDecReference(referenceField);
			//System.out.println("referenceOfCondition " +referenceOfCondition + " rel_id "+ rel_id + " size "+ (nodeRelations.size()-1));
			Declaration the_place_declared = null;
			
			if (rel_id ==0 ) {
				
				the_place_declared = rule_decs.get(0);
				//System.out.println("The first guy "+referenceField.getName()+" to main declaration (ref?)"+ " rel_id "+ rel_id + " size "+ (nodeRelations.size()-1)+ " \n");	
				
			} else {
				the_place_declared = rule_decs.get(rel_id-1);//declarationMap.get(referenceOfCondition));
				//System.out.println("Continue"+referenceField.getName()+" in "+the_place_declared+ "??? rel_id "+ rel_id + " size "+ (nodeRelations.size()-1)+ " \n");	

			}
			if (!the_place_declared.hasReference(referenceField.getName())) {
				num_declarations++;
				Declaration new_dec = new Declaration(referenceField.getType(), referenceField.getName(), num_declarations);
				//System.out.println("Create new dec "+referenceOfCondition+" (Main declaration doesnot have a ref"+ referenceField.getName() + ") rel_id "+ rel_id + " size "+ (nodeRelations.size()-1)+ " \n");	
				
				the_place_declared.addReference(new_dec);					//D
				declarationMap.put(referenceOfCondition, num_declarations);
				rule_decs.add(new_dec);
			}
			
			addCondition(current, nodeRelations, rel_id+1);
			
			
			
		}
	}
	// first time: the_place_declared = rule_decs.get(0)
	public void processNodeValue(NodeValue current, ArrayList<Field> nodeRelations, Declaration the_place_declared, int rel_id, int condition_or_action) {
		if (rel_id == nodeRelations.size()) {	// it must be primitive
			Field referenceField = nodeRelations.get(nodeRelations.size()-1);			
			String referenceOfCondition = Util.getDecReference(referenceField);
			
			//System.out.println("It is primitive, should add a condition to its father "+referenceOfCondition+ " rel_id "+ rel_id + " size "+ (nodeRelations.size()-1)+ " \n");	
			Declaration dec = rule_decs.get(declarationMap.get(referenceOfCondition));
			
			switch (condition_or_action) {
			case 1: // condition
				dec.addCondition(current);					//D
				break;
			case 2: // action
				AttrReference aRef = new AttrReference(current);	//D
				dec.addActionReference(aRef);						//D
				addAction(aRef);									//D
				setRuleStats (current);								//D
				break;
			}
			return;

		} else {
			Field referenceField = nodeRelations.get(rel_id);			
			String referenceOfCondition = Util.getDecReference(referenceField);
			
			//Reference current_ref = the_place_declared.getReference(referenceField.getName());
			Declaration current_dec = null;
			if (!the_place_declared.hasReference(referenceField.getName())) {
			//if (current_ref == null) {
				num_declarations++;
				current_dec = new Declaration(referenceField.getType(), referenceField.getName(), num_declarations);
				//System.out.println("Create new dec "+referenceOfCondition+" (Main declaration doesnot have a ref"+ referenceField.getName() + ") rel_id "+ rel_id + " size "+ (nodeRelations.size()-1)+ " \n");	
				switch (condition_or_action) {
				case 1: // condition
					the_place_declared.addReference(current_dec);			//D
					break;
				case 2: // action
					AttrReference aRef = new AttrReference(current);	//D
					the_place_declared.addActionReference(aRef);		//D
					break;
				}
				declarationMap.put(referenceOfCondition, num_declarations);
				rule_decs.add(current_dec);
			} else {
				//current_dec = the_place_declared.getReference(referenceField.getName()).
				current_dec = the_place_declared.getReferenceDec(referenceField.getName());
			}
			
			processNodeValue(current, nodeRelations, current_dec, rel_id+1, condition_or_action);	
			
		}
	}
	
	public void addAction(AttrReference aRef) {
		actions.add(aRef);	
	}
	
	public void setRuleStats (NodeValue current) {
		this.setRank(((LeafNode)current.getNode()).getRank());
		this.setNumClassifiedInstances(((LeafNode)current.getNode()).getNumClassification());
	}
	
	public String getObjectClassName() {
		return main_obj.getSimpleName();
	}
	
	private int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id= id;
	}
	
	private void setNumClassifiedInstances(double dataSize) {
		this.num_classified_instances = dataSize;	
	}
	public double getNumClassifiedInstances() {
		return this.num_classified_instances;	
	}

	public void setRank(double r) {
		this.rank = r;
	}
	public double getRank() {
		return this.rank;
	}
	
	public String toString() {
		/*		 
		rule "Good Bye"
    		dialect "java"
			when
				$m:Message( status == Message.GOODBYE)
			then
				System.out.println( "[getLabel()] Expected value (" + $c.getLabel() + "), Classified as (False)"); 
		end
		
		// complex rule
		rule "Purchase notification"
    		salience 10

			when
				$c : Customer( name == "Bla")
				$p : Purchase( amount == 1, customer == $c)	    
			then
	    		System.out.println( "Customer " + $c.name + " just purchased " + $p.product.name );
		end
		 */
		//"rule \"#"+getId()+" "+decision+" rank:"+rank+"\" \n";
		StringBuffer sb_out = new StringBuffer("");
		
		sb_out.append("\t when");
		for (int dec_i =rule_decs.size()-1; dec_i>=0; dec_i--) {
			Declaration d = rule_decs.get(dec_i);
			String obj_ref = d.getSymbol(); //"$"+this.getObjectClassName().substring(0, 1).toLowerCase();
			sb_out.append("\n\t\t "+obj_ref+" : "+d.getDeclaringTypeName()+"("+ "");
			Iterator<NodeValue> dec_it = d.getConditionIt();
			while (dec_it.hasNext()) {
				NodeValue cond = dec_it.next();
				sb_out.append(cond.toString() + ", ");
			}
			
			Iterator<Reference> ref_it = d.getReferenceIt();
			while (ref_it.hasNext()) {
				Reference ref = ref_it.next();
				sb_out.append(ref.toString() + ", ");
			}			
			sb_out.delete(sb_out.length()-2, sb_out.length()-1);
			sb_out.append(")");
		}
		sb_out.append("\n");
		StringBuffer sb_action = new StringBuffer("");
		StringBuffer sb_field = new StringBuffer("");
		StringBuffer sb_expected_value = new StringBuffer("");
		for (AttrReference act: actions) {
			sb_action.append(act.getValue() + " , ");
			sb_expected_value.append(act.getVariableName()); 
			sb_field.append(act.getFName() + "");
			
		}
		sb_action.delete(sb_action.length()-3, sb_action.length()-1);
		
		sb_out.append("\t then ");
		sb_out.append("\n\t\t System.out.println(\"["+sb_field.toString()+ "] Expected value (\" + "+  sb_expected_value.toString()+ " + \"), Classified as ("+sb_action.toString()+")\");\n"); 
		if (getRank() <0)
			sb_out.append("\n\t\t System.out.println(\"But no matching fact found = DOES not fire on\");\n");
		
		sb_out.insert(0, "rule \"#"+getId()+" "+sb_field.toString()+ "= "+sb_action.toString()+" classifying "+this.getNumClassifiedInstances()+" num of facts with rank:"+getRank() +"\" \n");
		sb_out.append("end\n");

		return sb_out.toString();
	}

	public static Comparator<Rule> getRankComparator() {
		return new RuleComparator();
	}
	
	private static class RuleComparator implements Comparator<Rule>{
		// this will sort from best rank to least rank
		public int compare(Rule r1, Rule r2) {
			if (r1.getRank() < r2.getRank())
				return 1; // normally -1
			else if (r1.getRank() > r2.getRank())
				return -1; // normally 1
			else
				return 0;
		}	
	}

}

class Declaration{
	private int id; 
	private String dec_ref; 		
	private Class<?> declared_obj;	// object class name
//	private ArrayList<RuleNode> conditions;
	private ArrayList<NodeValue> conditions;
	private ArrayList<Reference> references;
	
	public Declaration(Class<?> obj_class, String name, int dec_id) {
		id = dec_id;
		declared_obj = obj_class;
		dec_ref = name;
		references = new ArrayList<Reference>();
		conditions = new ArrayList<NodeValue>();
	}
	
	public String getDeclaringFName() {
		return dec_ref;
	}
	
	public Class<?> getDeclaringType() {
		return declared_obj;
	}
	public String getDeclaringTypeName() {
		return declared_obj.getSimpleName();
	}

	public void addCondition(NodeValue current) {
		/* TODO check do u need to copy the node value 
			NodeValue nv = new NodeValue(current.getNode());
			nv.setNodeValue(current.getNodeValue());
			conditions.add(nv);
		/**/
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
			if (df.getFName().equalsIgnoreCase(fName))
				return df;
		}
		return null;
		
	}
	
	public Declaration getReferenceDec(String fName) {
		for (Reference df : references) {
			if (df.getFName().equalsIgnoreCase(fName) && df instanceof DecReference)
				return ((DecReference)df).getDec();
		}
		return null;
		
	}
	public boolean hasReference(String fName) {
		for (Reference df : references) {
			if (df.getFName().equalsIgnoreCase(fName))
				return true;
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
		return "$"+dec_ref + "_" + id;
	}
	
}

interface Reference {
	public String getFName();
}
class DecReference implements Reference {
	
	private String fName;
	Declaration toReference;
	
	public DecReference (Declaration d) {
		toReference = d;
		fName = d.getDeclaringFName();
	}
	
	public Declaration getDec() {
		return toReference;
	}
	
	public String getFName() {
		return fName;
	}
	public void setReference(Declaration _toReference) {
		toReference = _toReference;
	}
	
	
	public String toString() {
		return fName + " == "+ toReference.getSymbol();
	}
}

class AttrReference implements Reference {
	
	private String fName;
	private NodeValue real_value;
	
	public AttrReference (NodeValue v) {
		String _fName = v.getFName();
		if (v.getNode().getDomain().isArtificial()) {
			_fName = Util.getFieldName(_fName);
		}
		fName = _fName;
		/* TODO is the copying really necessary?
		real_value = new NodeValue(v.getNode());
		real_value.setNodeValue(v.getNodeValue());
		*/
		real_value = v;
	}
	public Object getVariableName() {
		return "$target_label";
	}
	public Object getValue() {
		return real_value.getValue();
	}
	public String getFName() {
		return fName;
	}
	
	public String toString() {
		return getVariableName()+" : "+ fName ;
	}
}

