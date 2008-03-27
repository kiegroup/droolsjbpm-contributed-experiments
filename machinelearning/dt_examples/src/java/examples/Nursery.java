package examples;

import id3.ReadingSeq;


public class Nursery {

	private String parents; 	//"usual","pretentious", "great_pret"
	private String has_nurs; 	//"proper","less_proper", "improper", "critical", "very_crit"
	private String form; 		//"complete","completed", "incomplete", "foster"
	private String children; 	//"1", "2", "3","more"
	private String housing; 	//"convenient","less_conv", "critical"
	private String finance; 	//"convenient","inconv"
	private String social; 		//"nonprob","slightly_prob", "problematic"
	private String health; 		//"recommended","priority", "not_recom"
	private String classnursery; //"not_recom", "recommend", "very_recom", "priority","spec_prior"
	
	
	public String getParents() {
		return parents;
	}
	@ReadingSeq(0)
	public void setParents(String parents) {
		this.parents = parents;
	}
	public String getHas_nurs() {
		return has_nurs;
	}
	@ReadingSeq(1)
	public void setHas_nurs(String has_nurs) {
		this.has_nurs = has_nurs;
	}
	public String getForm() {
		return form;
	}
	@ReadingSeq(2)
	public void setForm(String form) {
		this.form = form;
	}
	public String getChildren() {
		return children;
	}
	
	@ReadingSeq(3)
	public void setChildren(String children) {
		this.children = children;
	}
	public String getHousing() {
		return housing;
	}
	
	@ReadingSeq(4)
	public void setHousing(String housing) {
		this.housing = housing;
	}
	
	
	public String getFinance() {
		return finance;
	}
	
	@ReadingSeq(5)
	public void setFinance(String finance) {
		this.finance = finance;
	}
	public String getSocial() {
		return social;
	}
	
	@ReadingSeq(6)
	public void setSocial(String social) {
		this.social = social;
	}
	public String getHealth() {
		return health;
	}
	@ReadingSeq(7)
	public void setHealth(String health) {
		this.health = health;
	}
	public String getClassnursery() {
		return classnursery;
	}
	
	@ReadingSeq(8)
	public void setClassnursery(String classnursery) {
		this.classnursery = classnursery;
	}
	
	
	

}
