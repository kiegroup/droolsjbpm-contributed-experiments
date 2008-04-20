package dt.tools;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class Util {
	public static boolean RUN = true;
	public static boolean DEBUG = true;
	public static boolean DEBUG_RETRAIN = true;
	public static boolean DEBUG_TEST = false;
	
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


	public static String sum() {
		return "sum";
	}

	public static void insert(List<Object> list, Object key, Comparator<Object> c) {
		int insertion_point_1 = Collections.binarySearch(list, key, c);
		if (insertion_point_1 <0)
			list.add(-(insertion_point_1+1), key);
		else {
			System.out.println("Util.insert() It exits ???");
		}
		
	}
	public static void insert(List list, Object key) {
//		if (list.isEmpty())
//			list.add(key);
		int insertion_point_1 = Collections.binarySearch(list, key);
//		if (list.isEmpty())
//			System.out.println(Util.ntimes("*", 10)+Util.ntimes("BOK", 5) +" \n What the fuck index is when it is empty "+ insertion_point_1);
		if (insertion_point_1 <0)
			list.add(-(insertion_point_1+1), key);
		else {
			System.out.println("Util.insert() It exits ???");
		}
		
	}



}