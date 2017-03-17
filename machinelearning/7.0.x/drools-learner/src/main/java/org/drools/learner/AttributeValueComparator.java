package org.drools.learner;

import java.util.Comparator;

public class AttributeValueComparator implements Comparator<Object> {

	/* we assume that "another" points to the same attribute type as this */
	public int compare(Object value1, Object value2) {
		if (value1 instanceof Comparable)
			return ((Comparable)value1).compareTo(value2); /* Boolean, Double, Integer, String...*/
		return value1.hashCode() - value2.hashCode();
	}

	// singleton wannabe
	public static AttributeValueComparator instance = new AttributeValueComparator();
}
