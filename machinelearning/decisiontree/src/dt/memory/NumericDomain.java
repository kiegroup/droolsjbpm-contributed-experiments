package dt.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NumericDomain implements Domain<Number> {

	private String fName;
	private ArrayList<Number> fValues;
	private ArrayList<Integer> indices;
	private boolean constant;
	private boolean discrete;
	private int readingSeq;

	private Comparator<Fact> fComparator;
	private Comparator<Number> nComparator;

	public NumericDomain(String _name) {
		fName = _name.trim();
		fValues = new ArrayList<Number>();
		discrete = true;
		fComparator = new FactNumericAttributeComparator(_name);
		nComparator = new NumberComparator();
		readingSeq = -1;
	}

	public Domain<Number> clone() {
		NumericDomain dom = new NumericDomain(fName);
		dom.constant = constant;
		dom.setDiscrete(discrete);
		dom.readingSeq = readingSeq;
		return dom;
	}
	
	public void setDiscrete(boolean d) {
		this.discrete = d;
		if (!this.discrete) {
			indices = new ArrayList<Integer>();
		}
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
			return;
		}
		
	}

	public void addPseudoValue(Number value) {
		if (discrete) {
			return;
		} else {
			
			if (!fValues.contains(value))
				fValues.add(value);
			Collections.sort(fValues, nComparator);
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
	
	public void setReadingSeq(int readingSeq) {
		this.readingSeq = readingSeq;
		
	}
	
	public int getReadingSeq() {
		return this.readingSeq;
		
	}
	
	public String toString() {
		String out = fName;
		return out;
	}

	public Comparator<Fact> factComparator() {
		return fComparator;
	}

	public void setIndices(List<Integer> split_indices) {
		indices.clear();
		indices.addAll(split_indices);
	}

	public List<Integer> getIndices() {
		return indices;
	}

}
