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
	
	public NumericDomain(String _name, boolean discrete) {
		fName = _name.trim();
		fValues = new ArrayList<Number>();
		this.discrete = discrete;
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
			/* you can add to the correct position = using binary search */
			Collections.sort(fValues, nComparator);
		}
		
	}
	
	public Number getClass(Object value) {
		if (discrete) {
			return (Number)value;
		} else {
			Number num_value = (Number)value;
			
			int insertion_point = Collections.binarySearch(fValues, num_value, nComparator);
			/*
			 * index of the search key, if it is contained in the list; otherwise, (-(insertion point) - 1). 
			 * The insertion point is defined as the point at which the key would be inserted into the list: 
			 * the index of the first element greater than the key, or list.size(), if all elements in the 
			 * list are less than the specified key. Note that this guarantees that the return value will be >= 0 
			 * if and only if the key is found.
			 */
			if (insertion_point >= 0) {
				return fValues.get(insertion_point);
			} else {
				return fValues.get(-(insertion_point) -1);
			}
		}
		
	}

	public boolean contains(Number value) throws Exception {
		if (discrete) {
			for(Number n: fValues) {
				if (nComparator.compare(n, value) == 0)
					return true;
			}
		} else {
			if (fValues.isEmpty() || fValues.size()==1)
				throw new Exception("Numerical domain "+fName+" is constant and not discrete but bounds are not set: possible values size: "+ fValues.size());
			
			// they must be sorted 
			return (nComparator.compare((Number)value, fValues.get(0)) >= 0 && nComparator.compare((Number)value, fValues.get(fValues.size()-1)) <= 0);
			
			/* should i check if the value is in one of the intervals
			 * this is necessary only if the intervals are unbroken 
			 */
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
			return this.contains((Number)value);
		} else {
			return true;
		}
		
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
	
	public void addIndex(int index) {
		indices.add(index);
	}

	public List<Integer> getIndices() {
		return indices;
	}

}
