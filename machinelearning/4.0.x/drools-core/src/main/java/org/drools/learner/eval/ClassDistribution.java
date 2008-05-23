package org.drools.learner.eval;

import java.util.Hashtable;

import org.drools.learner.Domain;
import org.drools.learner.tools.Util;


/* simple histogram keeps the number of instances in each class 
 * class: categories of the target attribute*/
public class ClassDistribution {
	
	protected Domain target_attr;
	private Hashtable<Object, Integer> quantity_by_class;
	
	private String sum_key = Util.sum();
	private int num_category_ideas;
	private Object winner_category;
	
	public ClassDistribution(Domain targetDomain) {
		this.target_attr = targetDomain;
		
		this.quantity_by_class =  new Hashtable<Object, Integer>(this.target_attr.getCategoryCount() + 1);		
		for (int c=0; c<this.target_attr.getCategoryCount(); c++) {
			Object category = this.target_attr.getCategory(c);
			quantity_by_class.put(category, 0);			
		}
		
		quantity_by_class.put(sum_key, 0);
		
		num_category_ideas = 0;
	}
	
	public ClassDistribution(ClassDistribution copy_dist) {
		this.target_attr = copy_dist.getClassDomain();
		this.quantity_by_class =  new Hashtable<Object, Integer>(this.target_attr.getCategoryCount() + 1);		
		this.setDistribution(copy_dist);
		
		this.num_category_ideas = copy_dist.get_num_ideas();
		this.winner_category = copy_dist.get_winner_class();
		
	}
	
	public void setSum(int sum) {
		quantity_by_class.put(sum_key, sum);
	}
	
	public int getSum() {
		return quantity_by_class.get(sum_key).intValue();
	}
	
	public Domain getClassDomain() {
		return target_attr;
	}
	
	public void change(Object target_category, int i) {
		/* TODO ????
		 * if (target_category == sum_key) return;
		 */
		int num_1 = quantity_by_class.get(target_category).intValue();
		num_1 += i;
		quantity_by_class.put(target_category, num_1);
		
		//quantity_by_class.put(target_category, quantity_by_class.get(target_category)+i);
	}

	public int getVoteFor(Object targetCategory) {
		return quantity_by_class.get(targetCategory).intValue();
	}
	
	public void evaluateMajority() {
		int winner_vote = 0;
		int num_ideas = 0;	// the number of target categories that the instances belong to
		
		Object winner = null;
		
		for (int c=0; c<this.target_attr.getCategoryCount(); c++) {
			Object category = this.target_attr.getCategory(c);
			
			int num_in_class = this.getVoteFor(category);
			if (num_in_class > 0) {
				num_ideas++;
				if (num_in_class > winner_vote) {
					winner_vote = num_in_class;
					winner = category;
				}
			}
		}

		this.set_num_ideas(num_ideas);
		this.set_winner_class(winner);		
	}
	
	public void set_num_ideas(int num_supperted_target_classes) {
		this.num_category_ideas = num_supperted_target_classes;
	}
	
	public void set_winner_class(Object the_winner_target_class) {
		this.winner_category = the_winner_target_class;
	}
	
	public int get_num_ideas() {
		return this.num_category_ideas;
	}
	
	public Object get_winner_class() {
		return this.winner_category;
	}
	
	public String toString() {
		String out = "ClassDist: target:"+ this.target_attr.getFName()+ " total: "+ this.getSum() + " & categories:";
		for (int c=0; c<this.target_attr.getCategoryCount(); c++) {
			Object category = this.target_attr.getCategory(c);
			out += this.getVoteFor(category) +" @"+category+ ", ";
		}
//		out +="\n";
		return out;
	}

	public void setDistribution(ClassDistribution targetDist) {
		for (int c=0; c<this.target_attr.getCategoryCount(); c++) {
			Object category = this.target_attr.getCategory(c);
			this.quantity_by_class.put(category, targetDist.getVoteFor(category));			
		}
		this.quantity_by_class.put(sum_key, targetDist.getSum());	
	}
	

}
