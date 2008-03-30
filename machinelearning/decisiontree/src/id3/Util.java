package id3;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.List;

public class Util {
	
	public static String ntimes(String s,int n){
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < n; i++) {
			buf.append(s);
		}
		return buf.toString();
	}
	
	//private static HashSet<String> simpletype = new HashSet<String>(0);
	public static boolean isSimpleType(Class<?>[] type_name) {
//		simpletype.contains(type_name)
		if (type_name.length==1 && (type_name[0].getName().equalsIgnoreCase("boolean") ||
				type_name[0].getName().equalsIgnoreCase("int") ||
				type_name[0].getName().equalsIgnoreCase("double") ||
				type_name[0].getName().equalsIgnoreCase("float") ||
				type_name[0].getName().equalsIgnoreCase("java.lang.String")))
				return true;
		else
			return false;
	}
	
//	public static boolean castType(Object value, Class<?>[] type_name) {
////		simpletype.contains(type_name)
//		if (type_name.length!=1)
//			return false;
//		
//		if (type_name[0].getName().equalsIgnoreCase("boolean")) {
//			
//		} else if (type_name[0].getName().equalsIgnoreCase("int")) {
//		
//		} else if (type_name[0].getName().equalsIgnoreCase("double")) {
//		} else if (type_name[0].getName().equalsIgnoreCase("float")){
//				
//		} else if (type_name[0].getName().equalsIgnoreCase("java.lang.String")){
//				return true;
//		}else
//			return false;
//	}

	public static boolean isGetter(String method_name) {
		if (method_name.startsWith("get") || method_name.startsWith("is") )
			return true;
		return false;
	}
	
	public static boolean isSetter(String m_name) {
		if (m_name.startsWith("set") )
			return true;
		return false;
	}

	public static String getAttributeName(String method_name) {
		if (method_name.startsWith("get") || method_name.startsWith("set"))
			return method_name.substring(3, method_name.length()).toLowerCase();
		else if (method_name.startsWith("is"))
			return method_name.substring(2, method_name.length()).toLowerCase();
		return null;
	}

	public static double log2(double prob) {
		return Math.log(prob) / Math.log(2);
	}

	public static int getDividingSize() {
		return 2;
	}

	public static String getTargetAnnotation(Class<? extends Object> classObj) {
		
		Field [] element_fields = classObj.getDeclaredFields();
		for( Field f: element_fields) {
			String f_name = f.getName();
			Class<?>[] f_class = {f.getType()};
			if (Util.isSimpleType(f_class)) {
				Annotation[] annotations = f.getAnnotations();
				
				// iterate over the annotations to locate the MaxLength constraint if it exists
				DomainSpec spec = null;
				for (Annotation a : annotations) {
				    if (a instanceof DomainSpec) {
				        spec = (DomainSpec)a; // here it is !!!
				        if (spec.target())
				        	return f_name;
				    }
				}
			}
		}
		return null;
	}


	public static String getSum() {
		return "sum";
	}



}