package dt.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import dt.tools.Util;

/* adding to the FactTargetDistribution 
 * it keeps the facts themselves which have that target value 
 */
public class FactDistribution extends FactTargetDistribution{
	
	private String attr_sum = Util.sum();
	private Hashtable<Object, List<Fact>> facts_at_target;
	
	/*
	private int num_supported_target_classes;
	private Object the_winner_target_class;
	*/
	public FactDistribution(Domain<?> targetDomain) {
		
		super(targetDomain);
	
		//num_supported_target_classes = 0;
		List<?> targetValues = targetDomain.getValues();

		facts_at_target = new Hashtable<Object, List<Fact>>(targetValues.size());
		for (Object t : targetValues) {

			facts_at_target.put(t, new ArrayList<Fact>());
		}
		
	}
	
	public void calculateDistribution(List<Fact> facts){
		int total_num_facts = 0;
		String target = super.getTargetDomain().getName();
		for (Fact f : facts) {
			total_num_facts++;
			Object target_key = f.getFieldValue(target);
			// System.out.println("My key: "+ key.toString());
			super.change(target_key, 1);	// add one for vote for the target value : target_key
			facts_at_target.get(target_key).add(f);

		}
		super.change(attr_sum, total_num_facts);

	}
	
	public Collection<Object> getTargetClasses() {
		return facts_at_target.keySet();
	}
	
	public List<Fact> getSupportersFor(Object value) {
		return facts_at_target.get(value);
	}
	
/*
	public int getNum_supported_target_classes() {
		return num_supported_target_classes;
	}

	public void setNum_supperted_target_classes(int num_supperted_target_classes) {
		this.num_supported_target_classes = num_supperted_target_classes;
	}

	public Object getThe_winner_target_class() {
		return the_winner_target_class;
	}

	public void setThe_winner_target_class(Object the_winner_target_class) {
		this.the_winner_target_class = the_winner_target_class;
	}	
	*/

}
