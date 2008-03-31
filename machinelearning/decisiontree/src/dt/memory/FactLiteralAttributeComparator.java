package dt.memory;

import java.util.Comparator;

import dt.tools.Util;

public class FactLiteralAttributeComparator  implements Comparator<Fact> {
	private String attr_name;

	public FactLiteralAttributeComparator(String _attr_name) {
		attr_name = _attr_name;
	}

	public int compare(Fact f0, Fact f1) {
		String n0 = (String) f0.getFieldValue(attr_name);
		String n1 = (String) f1.getFieldValue(attr_name);
		
		//System.out.println(Util.ntimes("!*", 10)+ " "+ n0 +" and " + n1 +" "+ n0.compareToIgnoreCase(n1));
		return n0.compareToIgnoreCase(n1);
	}
}