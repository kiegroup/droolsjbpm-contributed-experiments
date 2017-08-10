package org.drools.examples.learner.structured_nursery;

import org.drools.learner.tools.FieldAnnotation;

public class SocHealth {
	
	@FieldAnnotation(readingSeq =6)
	private String social; 		//"nonprob","slightly_prob", "problematic"
	@FieldAnnotation(readingSeq =7)
	private String health; 		//"recommended","priority", "not_recom"
	
	public SocHealth() {

	}
	
	public String getSocial() {
		return social;
	}
	public void setSocial(String social) {
		this.social = social;
	}
	public String getHealth() {
		return health;
	}
	public void setHealth(String health) {
		this.health = health;
	}
	
}
