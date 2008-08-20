package org.drools.examples.learner.structured_car;

import org.drools.learner.tools.FieldAnnotation;

public class Car {
	
	private Price overall_price;
	private Tech tech_char;
	
	@FieldAnnotation(readingSeq = 6, target = true)
	private String target; 		//"unacc", "acc", "good", "vgood"

	public Car() {

	}
	public Price getOverall_price() {
		return overall_price;
	}

	public void setOverall_price(Price overall_price) {
		this.overall_price = overall_price;
	}

	public Tech getTech_char() {
		return tech_char;
	}

	public void setTech_char(Tech tech_char) {
		this.tech_char = tech_char;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}
	
	

}


