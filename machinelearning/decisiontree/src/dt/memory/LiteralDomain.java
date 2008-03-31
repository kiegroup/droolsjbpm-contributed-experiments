package dt.memory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class LiteralDomain implements Domain<String> {

	private String fName;
	private List<String> fValues;
	private boolean constant;
	private boolean discrete;
	private int readingSeq;


	public LiteralDomain(String _name) {
		fName = _name.trim();
		fValues = new ArrayList<String>();
		discrete = true;
		readingSeq = -1;
	}
	
	public Domain<String> clone() {
		LiteralDomain dom = new LiteralDomain(fName);
		dom.constant = constant;
		dom.discrete = discrete;
		dom.readingSeq = readingSeq;
		return dom;
	}
	
	public LiteralDomain(String _name, String[] possibleValues) {
		fName = _name;
		fValues = Arrays.asList(possibleValues);
		discrete = true;
	}
	
	public void setDiscrete(boolean d) {
		this.discrete = d;
	}

	
	public boolean isDiscrete() {
		return this.discrete;
	}

	public String getName() {
		return fName;
	}
	
	public void addValue(String value) {
		if (constant)
			return;
		//if (discrete) {
		if (!fValues.contains(value))
			fValues.add(value);
//		} else {
//			fValues.add(value);
//		}
		
	}

	public boolean contains(String value) {
		for(String n: fValues) {
			if (value.equalsIgnoreCase(n))
				return true;
		}
		return false;
	}

	public List<String> getValues() {
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
		return data.trim();
	}
	
	public boolean isPossible(Object value) {
		if (!(value instanceof String))
			return false;
		if (constant && !fValues.contains(value))
			return false;
		return true;
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
		// TODO wee need groupings to be able to discretize the LiteralDomain
		System.out.println("LiteralDomain.factComparator() is not ready ");
		return null;
	}

}
