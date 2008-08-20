package org.drools.examples.learner.structured_nursery;

import org.drools.learner.tools.FieldAnnotation;

public class StructFinan {
	
	private Structure struct;
	
	@FieldAnnotation(readingSeq =4)
	private String housing; 	//"convenient","less_conv", "critical"
	@FieldAnnotation(readingSeq =5)
	private String finance; 	//"convenient","inconv"
	
	public StructFinan(){
	
	}
	public Structure getStruct() {
		return struct;
	}
	public void setStruct(Structure structure) {
		this.struct = structure;
	}
	public String getHousing() {
		return housing;
	}
	public void setHousing(String housing) {
		this.housing = housing;
	}
	public String getFinance() {
		return finance;
	}
	public void setFinance(String finance) {
		this.finance = finance;
	}
	
	
}
