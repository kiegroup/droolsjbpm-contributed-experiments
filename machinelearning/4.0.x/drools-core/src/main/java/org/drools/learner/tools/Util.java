package org.drools.learner.tools;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Util {
	
	public static final int ID3 = 1, C45 = 2;
	public static final int SINGLE = 1, BAG = 2, BOOST = 3;

	//public static final boolean DEBUG = false;
	public static final boolean DEBUG_TEST = false;

	public static final boolean PRINT_STATS = true;

	public static final boolean DEBUG_RULE_PRINTER = false;

	public static final boolean DEBUG_LEARNER = false;

	public static final boolean DEBUG_CATEGORIZER = false;

	public static final boolean DEBUG_ENTROPY = false;

	public static final boolean DEBUG_DIST = false;

	public static final boolean DEBUG_DECISION_TREE = false;


	
	public static int MAX_NUM_RULES = 1000;
	public static boolean ONLY_ACTIVE_RULES = true;	/* TODO into global settings */
	
	public static boolean SORT_RULES_BY_RANK = true;
	public static boolean PRINT_RULES = true;
	
	private static boolean WITH_REP = true;
	private static Random BAGGING = new Random(System.currentTimeMillis());
	
	
	public static String ntimes(String s,int n){
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < n; i++) {
			buf.append(s);
		}
		return buf.toString();
	}
	
	public static String sum() {
		return "sum";
	}
	
	public static double log2(double prob) {
		return Math.log(prob) / Math.log(2);
	}
	
	/* TODO make this all_fields arraylist as hashmap */
	public static void getAllFields(Class<?> clazz, ArrayList<Field> all_fields) {
		if (clazz == Object.class)
			return;
		
		//Field [] element_fields_ = clazz.getFields();
		Field [] element_fields = clazz.getDeclaredFields(); //clazz.getFields();
		for (Field f: element_fields) {
			all_fields.add(f);
		}
		getAllFields(clazz.getSuperclass(), all_fields);
		
		return;
	}

	public static Object calculateMidPoint(Class<?> fClass, Object cp_i, Object cp_i_next) {
		if (fClass.isAssignableFrom(Integer.class) || fClass == Integer.TYPE) {
			return ((Integer)cp_i + (Integer) cp_i_next) / 2;
		} else if (fClass.isAssignableFrom(Short.class) || fClass == Short.TYPE) {
			return ((Short)cp_i + (Short) cp_i_next) / 2;
		} else if (fClass.isAssignableFrom(Long.class) || fClass == Long.TYPE) {
			return ((Long)cp_i + (Long) cp_i_next) / 2;	
		} else if (fClass.isAssignableFrom(Double.class) || fClass == Double.TYPE) {
			return ((Double)cp_i + (Double) cp_i_next) / 2;	
		} else if (fClass.isAssignableFrom(Float.class) || fClass == Float.TYPE) {
			return ((Float)cp_i + (Float) cp_i_next) / 2;
		} else {
			System.out.print("What should i do then Categorizer.find_a_split() for "+fClass);
			System.exit(0);
		}
		return 0.0;
	}
	
	public static int getType(Class<?>[] type_name) {
		/*	
		 * simpletype.contains(type_name)
		 * how do you get the simple type
		 */
		if (type_name.length==1) {
			if (type_name[0].getName().equalsIgnoreCase("boolean") ||
				type_name[0].getName().equalsIgnoreCase("int") ||
				type_name[0].getName().equalsIgnoreCase("double") ||
				type_name[0].getName().equalsIgnoreCase("float")) 
				return 1;
			else if (type_name[0].getName().equalsIgnoreCase("java.lang.String"))
				return 2;
			else
				return -1;
		} else
			return 0;
	}
	
	public static int[] bag_w_rep(int k, int N) {
		int[] bag = new int[k];
		int i = 0;
		while (i<k) {
			bag[i++] = BAGGING.nextInt(N);
		}
		return bag;
	}
	
	public static int[] bag_wo_rep(int k, int N) {
		int[] bag = new int[k];
		boolean[] selected = new boolean[N];
		int i = 0;
		while (i<k) {
			int new_i = BAGGING.nextInt(N--);
			//if (Util.DEBUG)	System.out.print("new_i "+ new_i +"\t");
			int selected_j=0, j=0;
			while (j<new_i || selected[j]) {
				if (selected[j]) {
					new_i++;
					selected_j = j;
				}
				j++;
			}
			selected[j] = true;
			bag[i] = j;
			i++;
		}
		return bag;
	}
	
	public static Object convertPrimitiveType (Object primitiveValue) {
		Class klass = primitiveValue.getClass();
		if  (klass.equals(Boolean.TYPE)) {
			return (Boolean)primitiveValue;
			
		}
		return null; 
		
	}
	
	public boolean bok = false;
	
	public Boolean bokkafa = Boolean.FALSE;
	public String sbok = "x";
	
	public static final void main(final String[] args) throws Exception {
		System.out.println(Util.convertPrimitiveType(false));
		
		Util u = new Util();
		Field f = u.getClass().getField("bok");
		System.out.println(Util.convertPrimitiveType(f.get(u)));
		if (f.getType().isPrimitive())
			System.out.println("Primitive" + f.get(u));
		else
			System.out.println("Not Primitive" + f.get(u));
		Field fkafa = u.getClass().getField("bokkafa");
		if (fkafa.getType().isPrimitive())
			System.out.println("Primitive" + fkafa.get(u));
		else 
			System.out.println("Not Primitive kafa" + fkafa.get(u));
		Field f_s = u.getClass().getField("sbok");
		if (f_s.getType() == String.class)
			System.out.println(f_s.get(u));
		
		String abd = "abd";
		String abcm = "abcm";
		ArrayList<String> x  = new ArrayList<String>(2);
		x.add(abd);
		x.add(abcm);
		x.add("q");
		x.add("abdm");
		x.add("abc");
		System.out.println("the result: "+abd.compareTo(abcm));
		
		Collections.sort(x);
		
		for (String s:x){
			System.out.println(s);
			}
		
		
	}

	public static String getFileSuffix(int domain_type, int tree_set) {
		String suffix = "";
		switch (domain_type) {
		case Util.ID3:
			suffix += "id3" ;
			break;
		case Util.C45:
			suffix += "c45";
			break;
		default:
			suffix += "?" ;
		
		}
		
		switch (tree_set) {
		case Util.SINGLE:
			suffix += "_one";
			break;
		case Util.BAG:
			suffix += "_bag";
			break;
		case Util.BOOST:
			suffix += "_boost";
			break;
		default:
			suffix += "_?" ;
		
		}
		return suffix;
	}



}

