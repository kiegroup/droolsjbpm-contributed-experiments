package dt.memory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import dt.tools.Util;

public class FactTargetDistribution {
	
	private String attr_sum = Util.sum();
	private Domain<?> targetDomain;
	private Hashtable<Object, Integer> num_at_target;
	private Hashtable<Object, List<Fact>> facts_at_target;
	
	private int num_supported_target_classes;
	private Object the_winner_target_class;
	
	public FactTargetDistribution(Domain<?> targetDomain) {
		
//		this.targetDomain = targetDomain.clone();
//		targetDomain.
		
		num_supported_target_classes = 0;
		this.targetDomain = targetDomain;
		List<?> targetValues = targetDomain.getValues();
		num_at_target =  new Hashtable<Object, Integer>(targetValues.size() + 1);
		facts_at_target = new Hashtable<Object, List<Fact>>(targetValues.size());
		for (Object t : targetValues) {
			num_at_target.put(t, 0);
			facts_at_target.put(t, new ArrayList<Fact>());
		}
		num_at_target.put(attr_sum, 0);
		
	}
	
	public void calculateDistribution(List<Fact> facts){
		int total_num_facts = 0;
		String target = targetDomain.getName();
		for (Fact f : facts) {
			total_num_facts++;
			Object key = f.getFieldValue(target);
			// System.out.println("My key: "+ key.toString());
			num_at_target.put(key, num_at_target.get(key).intValue() + 1); // bocuk
			facts_at_target.get(key).add(f);

		}
		num_at_target.put(attr_sum, num_at_target.get(attr_sum).intValue() + total_num_facts);
		
	}
	public Collection<Object> getTargetClasses() {
		return facts_at_target.keySet();
	}
	public int getSum() {
		return num_at_target.get(attr_sum).intValue();
	}
	
	public int getVoteFor(Object value) {
		return num_at_target.get(value).intValue();
	}
	
	public List<Fact> getSupportersFor(Object value) {
		return facts_at_target.get(value);
	}
	public void evaluateMajority() {
		
		List<?> targetValues = targetDomain.getValues();
		int winner_vote = 0;
		int num_supporters = 0;
		
		Object winner = null;
		for (Object key : targetValues) {

			int num_in_class = num_at_target.get(key).intValue();
			if (num_in_class > 0)
				num_supporters++;
			if (num_in_class > winner_vote) {
				winner_vote = num_in_class;
				winner = key;
			}
		}
		setNum_supperted_target_classes(num_supporters);
		setThe_winner_target_class(winner);
		
	}

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
	
	
	

}
