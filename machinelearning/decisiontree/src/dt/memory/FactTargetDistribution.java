package dt.memory;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import dt.tools.Util;

/* simple histogram keeps the number of facts in each class of target */
public class FactTargetDistribution {
	
	private String attr_sum = Util.sum();
	protected Domain<?> targetDomain;
	private Hashtable<Object, Integer> num_at_target;
	
	//
	private int num_supported_target_classes;
	private Object the_winner_target_class;
	//
	
	public FactTargetDistribution(Domain<?> targetDomain) {

		this.targetDomain = targetDomain;
		List<?> targetValues = targetDomain.getValues();
		num_at_target =  new Hashtable<Object, Integer>(targetValues.size() + 1);		
		for (Object t : targetValues) {
			num_at_target.put(t, 0);			
		}
		num_at_target.put(attr_sum, 0);
		num_supported_target_classes = 0;
		
	}
	
	public FactTargetDistribution(FactTargetDistribution copy_dist) {

		this.targetDomain = copy_dist.getTargetDomain();
		List<?> targetValues = targetDomain.getValues();
		this.num_at_target =  new Hashtable<Object, Integer>(targetValues.size() + 1);		
		this.setDistribution(copy_dist);
		this.num_supported_target_classes = copy_dist.getNum_supported_target_classes();
		
	}
	
	public Collection<?> getTargetClasses() {
		return targetDomain.getValues();
	}
	
	public int getSum() {
		return num_at_target.get(attr_sum).intValue();
	}
	public void setSum(int sum) {
		num_at_target.put(attr_sum, sum);
	}
	
	public int getVoteFor(Object value) {
		return num_at_target.get(value).intValue();
	}
	
	public Domain<?> getTargetDomain() {
		return targetDomain;
	}

	public void setTargetDomain(Domain<?> targetDomain) {
		this.targetDomain = targetDomain.clone();
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
	
	public void change(Object targetValue, int i) {
		int num_1 = num_at_target.get(targetValue).intValue();
		num_1 += i;
		num_at_target.put(targetValue, num_1);
	}
	
	public void evaluateMajority() {
		
		List<?> targetValues = targetDomain.getValues();
		int winner_vote = 0;
		int num_supporters = 0;
		
		Object winner = null;
		for (Object key : targetValues) {

			int num_in_class = getVoteFor(key);
			if (num_in_class > 0)
				num_supporters++;
			if (num_in_class > winner_vote) {
				winner_vote = num_in_class;
				winner = key;
			}
		}
		this.setNum_supperted_target_classes(num_supporters);
		this.setThe_winner_target_class(winner);
		
	}

	public void setDistribution(FactTargetDistribution targetDist) {
		for (Object targetValue: targetDomain.getValues()) {
			num_at_target.put(targetValue, targetDist.getVoteFor(targetValue));
		}
		num_at_target.put(attr_sum, targetDist.getSum());
	}
	
	public String toString() {
		String out = "FTD: target:"+ this.targetDomain.getName()+ " total: "+ this.getSum() + " dist:";
		for (Object value: this.getTargetClasses())
			out += this.getVoteFor(value) +" @"+value+ ", ";
		
		out +="\n";
		return out;
	}
	

}
