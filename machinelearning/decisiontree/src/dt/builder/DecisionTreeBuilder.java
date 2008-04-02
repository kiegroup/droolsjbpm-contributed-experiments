package dt.builder;

import java.util.List;

import dt.DecisionTree;
import dt.memory.WorkingMemory;

public interface DecisionTreeBuilder {
	
	
	DecisionTree build(WorkingMemory wm, Class<?> klass, String targetField, List<String> workingAttributes);

	DecisionTree build(WorkingMemory simple, String klass_name, String target_attr,List<String> workingAttributes);

	int getNum_fact_processed();
	void setNum_fact_processed(int num);
}
