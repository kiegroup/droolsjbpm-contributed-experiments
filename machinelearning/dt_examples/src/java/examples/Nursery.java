package examples;

import dt.memory.DomainSpec;


public class Nursery {
	@DomainSpec(readingSeq =0)
	private String parents; 	//"usual","pretentious", "great_pret"
	@DomainSpec(readingSeq =1)
	private String has_nurs; 	//"proper","less_proper", "improper", "critical", "very_crit"
	@DomainSpec(readingSeq =2)
	private String children; 	//"1", "2", "3","more"
	@DomainSpec(readingSeq =3)
	private String form; 		//"complete","completed", "incomplete", "foster"
	@DomainSpec(readingSeq =4)
	private String housing; 	//"convenient","less_conv", "critical"
	@DomainSpec(readingSeq =5)
	private String finance; 	//"convenient","inconv"
	@DomainSpec(readingSeq =6)
	private String social; 		//"nonprob","slightly_prob", "problematic"
	@DomainSpec(readingSeq =7)
	private String health; 		//"recommended","priority", "not_recom"
	@DomainSpec(readingSeq =8, target = true)
	private String classnursery; //"not_recom", "recommend", "very_recom", "priority","spec_prior"
	
	
	public String getParents() {
		return parents;
	}
	
	public void setParents(String parents) {
		this.parents = parents;
	}
	public String getHas_nurs() {
		return has_nurs;
	}
	//@DomainSpec(readingSeq =1)
	public void setHas_nurs(String has_nurs) {
		this.has_nurs = has_nurs;
	}
	public String getForm() {
		return form;
	}
	//@DomainSpec(readingSeq =2)
	public void setForm(String form) {
		this.form = form;
	}
	public String getChildren() {
		return children;
	}
	
	//@DomainSpec(readingSeq =3)
	public void setChildren(String children) {
		this.children = children;
	}
	public String getHousing() {
		return housing;
	}
	
	//@DomainSpec(readingSeq =4)
	public void setHousing(String housing) {
		this.housing = housing;
	}
	
	
	public String getFinance() {
		return finance;
	}
	
	//@DomainSpec(readingSeq =5)
	public void setFinance(String finance) {
		this.finance = finance;
	}
	public String getSocial() {
		return social;
	}
	
	//@DomainSpec(readingSeq =6)
	public void setSocial(String social) {
		this.social = social;
	}
	public String getHealth() {
		return health;
	}
	//@DomainSpec(readingSeq =7)
	public void setHealth(String health) {
		this.health = health;
	}
	public String getClassnursery() {
		return classnursery;
	}
	
	//@DomainSpec(readingSeq =8)
	public void setClassnursery(String classnursery) {
		this.classnursery = classnursery;
	}
	
	
	

}
