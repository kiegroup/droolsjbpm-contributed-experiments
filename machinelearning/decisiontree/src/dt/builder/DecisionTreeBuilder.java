package dt.builder;

import java.io.Serializable;
import java.util.List;

import dt.DecisionTree;
import dt.TreeNode;
import dt.memory.Fact;
import dt.memory.WorkingMemory;

public interface DecisionTreeBuilder extends Serializable{
	
	
	DecisionTree build(WorkingMemory wm, Class<?> klass, String targetField, List<String> workingAttributes);
	
	public TreeNode train(DecisionTree dt, List<Fact> facts, List<String> attributeNames);
	public List<Integer> test(DecisionTree dt, List<Fact> facts);
	//DecisionTree build(WorkingMemory simple, String klass_name, String target_attr,List<String> workingAttributes);
	int getNum_fact_trained();
	void setNum_fact_trained(int num);
}
