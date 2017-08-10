package org.drools.examples.learner;

import org.drools.learner.tools.FieldAnnotation;

//import dt.memory.DomainSpec;

public class Restaurant {
	

	private boolean alternate; 	//yesno
	private boolean bar; 		//yesno
	private boolean fri_sat ;	//yesno
	private boolean hungry; 		//yesno
	private String patrons; 	//String[]{"None","Some","Full"});
	private int price; 		//",new String[]{"$","$$","$$$"});
	private boolean raining; 	//yesno
	private boolean reservation; //yesno
	private String type; 		//",new String[]{"French","Italian","Thai","Burger"});
	private String wait_estimate;	//",new String[]{"0-10","10-30","30-60",">60"});
	@FieldAnnotation(target =true, readingSeq = 0)
	private boolean will_wait; 	//yesno
	
	
	public Restaurant (boolean alt, boolean b, boolean f_s, boolean hung, String pat, int pri, 
				boolean rain, boolean reserv, String t, String wait, boolean will) {
		alternate = alt; 		//yesno
		bar = b; 				//yesno
		fri_sat = f_s;			//yesno
		hungry = hung; 			//yesno
		patrons = pat; 			//",new String[]{"None","Some","Full"});
		price = pri; 			//",new String[]{"$","$$","$$$"});
		raining = rain; 		//yesno
		reservation = reserv; 	//yesno
		type = t; 				//",new String[]{"French","Italian","Thai","Burger"});
		wait_estimate = wait; 	//",new String[]{"0-10","10-30","30-60",">60"});
		will_wait = will; 		//yesno
	}


	public boolean getAlternate() {
		return alternate;
	}


	public void setAlternate(boolean alternate) {
		this.alternate = alternate;
	}


	public boolean getBar() {
		return bar;
	}


	public void setBar(boolean bar) {
		this.bar = bar;
	}


	public boolean getFri_sat() {
		return fri_sat;
	}


	public void setFri_sat(boolean fri_sat) {
		this.fri_sat = fri_sat;
	}


	public boolean getHungry() {
		return hungry;
	}


	public void setHungry(boolean hungry) {
		this.hungry = hungry;
	}


	public String getPatrons() {
		return patrons;
	}


	public void setPatrons(String patrons) {
		this.patrons = patrons;
	}


	public int getPrice() {
		return price;
	}


	public void setPrice(int price) {
		this.price = price;
	}


	public boolean getRaining() {
		return raining;
	}


	public void setRaining(boolean raining) {
		this.raining = raining;
	}


	public boolean getReservation() {
		return reservation;
	}


	public void setReservation(boolean reservation) {
		this.reservation = reservation;
	}


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getWait_estimate() {
		return wait_estimate;
	}


	public void setWait_estimate(String wait_estimate) {
		this.wait_estimate = wait_estimate;
	}


	public boolean getWill_wait() {
		return will_wait;
	}


	public void setWill_wait(boolean will_wait) {
		this.will_wait = will_wait;
	}
	
	

}
