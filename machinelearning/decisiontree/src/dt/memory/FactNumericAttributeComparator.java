package dt.memory;

import java.io.Serializable;
import java.util.Comparator;

public class FactNumericAttributeComparator implements Comparator<Fact>, Serializable {
	private String attr_name;

	public FactNumericAttributeComparator(String _attr_name) {
		attr_name = _attr_name;
	}

	public int compare(Fact f0, Fact f1) {
		Number n0 = (Number) f0.getFieldValue(attr_name);
		Number n1 = (Number) f1.getFieldValue(attr_name);
		if (n0.doubleValue() < n1.doubleValue())
			return -1;
		else if (n0.doubleValue() > n1.doubleValue())
			return 1;
		else
			return 0;
	}
}