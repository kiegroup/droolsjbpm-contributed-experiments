package id3;

import java.util.ArrayList;
import java.util.List;

public class NumericDomain implements Domain<Number> {

	private String fName;
	private ArrayList<Number> fValues;
	private boolean constant;
	private boolean discrete;


	public NumericDomain(String _name) {
		fName = _name.trim();
		fValues = new ArrayList<Number>();
		discrete = true;
	}
	public void setContinuous() {
		discrete = false;
	}
	
	public boolean isDiscrete() {
		return discrete;
	}

	public String getName() {
		return fName;
	}

	public void addValue(Number value) {
		if (constant)
			return;
		if (discrete) {
			if (!fValues.contains(value))
				fValues.add(value);
		} else {
			if (fValues.isEmpty()) {
				fValues.add(value);
				return;
			} else if (fValues.size()==1) {
				if (value.doubleValue() < fValues.get(0).doubleValue()) {
					Number first = fValues.remove(0);
					fValues.add(value);
					fValues.add(first);
				} else if (value.doubleValue() > fValues.get(0).doubleValue()) {
					fValues.add(value);
				}	
				return;
			} else {
				if (value.doubleValue() > fValues.get(1).doubleValue()) {
					fValues.remove(1);
					fValues.add(1, value);
					return;
				}
				if (value.doubleValue() < fValues.get(0).doubleValue()) {
					fValues.remove(0);
					fValues.add(0, value);	
					return;
				}
			}
		}
		
	}

	public boolean contains(Number value) {
		for(Number n: fValues) {
			if (value.intValue() == n.intValue() ||
				value.doubleValue() == n.doubleValue() ||
				value.floatValue() == n.floatValue())
				return true;
		}
		return false;
	}

	public List<Number> getValues() {
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
			return Double.parseDouble(data);
		else 
			return null;
	}
	
	public boolean isValid(String string) {
		if (string == null)
			return true;
		try{
			Double.parseDouble(string);
			return true;
		}
		catch (Exception e){
			return false;
		}
	}

	public boolean isPossible(Object value) throws Exception {
		//System.out.println("NumericDomain.isPossible() start "+ value+ " ?");
		
		if (!(value instanceof Number))
			return false;
		//System.exit(0);
		if (constant) {
			//System.out.println("NumericDomain.isPossible() constant "+ value+ " ?");
			//System.exit(0);
			
			if (discrete) {
				if (fValues.contains(value))
					return true;
				
				//System.out.println("NumericDomain.isPossible() constant && discrete "+ value+ " ?");
				//System.exit(0);
			} else {
				if (fValues.isEmpty() || fValues.size()==1)
					throw new Exception("Numerical domain "+fName+" is constant and not discrete but bounds are not set: possible values size: "+ fValues.size());
				if (((Number)value).doubleValue() >= fValues.get(0).doubleValue() && 
					((Number)value).doubleValue() <= fValues.get(1).doubleValue()) {
					return true;	
				}
				//System.out.println("NumericDomain.isPossible() "+ value+ " ?");
			}
		} else {
			return true;
		}
		
		//System.out.println("NumericDomain.isPossible() end "+ value+ " ?");
		//System.exit(0);
		
		return false;
	}
	
	public String toString() {
		String out = fName;
		return out;
	}
	


}
