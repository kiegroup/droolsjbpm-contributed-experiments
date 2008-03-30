package dt.builder;

import java.util.Collection;

import dt.DecisionTree;
import dt.memory.WorkingMemory;

public interface DecisionTreeBuilder {
	
	
	DecisionTree build(WorkingMemory wm, Class<?> klass, String targetField, Collection<String> workingAttributes);

	DecisionTree build(WorkingMemory simple, String klass_name, String target_attr,Collection<String> workingAttributes);

}
