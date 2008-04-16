package dt.memory;

public class DomainFactory {
	public static BooleanDomain createBooleanDomain(String name) {
		return new BooleanDomain(name);
		
	}
	
	public static NumericDomain createNumericDomain(String name, boolean discrete) {
		return new NumericDomain(name, discrete);
	}

	public static LiteralDomain createLiteralDomain(String name) {
		return new LiteralDomain(name);
	}
	
	public static Domain<?> createDomainFromClass(Class<?> c, String domainName) {
		if (c.isPrimitive())
			if (c.getName().equalsIgnoreCase("boolean")) {
				System.out.println("Yuuuupiii boolean");
				return createBooleanDomain(domainName);
			} else if (c.getName().equalsIgnoreCase("int")) {
				return createNumericDomain(domainName, true);
			}else if (c.getName().equalsIgnoreCase("double") || c.getName().equalsIgnoreCase("float")) {
				System.out.println("Yuuuupiii number");
				return createNumericDomain(domainName, false);
			} else
				return createComplexDomain(c,"kicimi ye simple: "+domainName);
		else if (c.isAssignableFrom(String.class)) {
			System.out.println("Yuuuupiii string");
			return createLiteralDomain(domainName);
		} else if (c.isAssignableFrom(Integer.class)) {
			return createNumericDomain(domainName, true);
		}
		else if (c.isAssignableFrom(Double.class)  || c.isAssignableFrom(Float.class)) {
			return createNumericDomain(domainName, false);
		} else if (c.isAssignableFrom(Boolean.class))
			return createBooleanDomain(domainName);
		else
			return createComplexDomain(c,domainName);
	}

	private static Domain<?> createComplexDomain(Class<?> c, String domainName) {
		System.out.println("Bok ye this is complex type: "+ c);
		return null;
	}
	
//	public static Domain<?> createDomainFromString(String data, String domainName) {
//		if (c.isNumeric()) {
//			System.out.println("Yuuuupiii string");
//			return createNumericDomain(domainName);
//		} else if (c.true/false || 
//			c.isAssignableFrom(Double.class)  ||
//			c.isAssignableFrom(Float.class)) {
//			return createNumericDomain(domainName);
//		} else if (c.is literal )
//			return createLiteral(domainName);
//		else
//			return createComplexDomain(c,domainName);
//	}

}