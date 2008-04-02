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
	
	public FactTargetDistribution(Domain<?> targetDomain) {

		this.targetDomain = targetDomain;
		List<?> targetValues = targetDomain.getValues();
		num_at_target =  new Hashtable<Object, Integer>(targetValues.size() + 1);		
		for (Object t : targetValues) {
			num_at_target.put(t, 0);			
		}
		num_at_target.put(attr_sum, 0);
		
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
	
	public void change(Object targetValue, int i) {
		int num_1 = num_at_target.get(targetValue).intValue();
		num_1 += i;
		num_at_target.put(targetValue, num_1);
	}

	public void setDistribution(FactTargetDistribution targetDist) {
		for (Object targetValue: targetDomain.getValues()) {
			num_at_target.put(targetValue, targetDist.getVoteFor(targetValue));
		}
		
	}
	

}
