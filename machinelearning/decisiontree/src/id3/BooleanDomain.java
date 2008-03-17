package id3;

import java.util.ArrayList;
import java.util.List;

public class BooleanDomain implements Domain<Boolean> {

	private String fName;
	private ArrayList<Boolean> fValues;
	private boolean constant;


	public BooleanDomain(String _name) {
		fName = _name.trim();
		fValues = new ArrayList<Boolean>();
		fValues.add(Boolean.TRUE);
		fValues.add(Boolean.FALSE);
	}
	
	public boolean isDiscrete() {
		return true;
	}

	public String getName() {
		return fName;
	}

	public boolean contains(Boolean value) {
		return true;
	}

	public void addValue(Boolean value) {
		// TODO Auto-generated method stub
		
	}

	public List<Boolean> getValues() {
		return fValues;
	}
	
	public int hashCode() {
		return fName.hashCode();
	}

	public boolean isConstant() {
		return this.constant;
	}

	public void setConstant() {
		this.constant = true;	
	}
	
	public Object readString(String data) {
		if (isValid(data))
			return Boolean.parseBoolean(data);
		else 
			return null;
	}
	
	public boolean isValid(String string) {
		try{
			Boolean.parseBoolean(string);
			return true;
		}
		catch (Exception e){
			return false;
		}
	}
	
	public boolean isPossible(Object value) {
		//if (isDiscrete() && constant)
		if (value instanceof Boolean && fValues.contains(value))
			return true;
		return false;
	}
	
	public String toString() {
		String out = fName;
		return out;
	}

}
