package org.drools.learner.tools;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.drools.learner.Domain;
import org.drools.learner.builder.Learner;
import org.drools.learner.builder.Learner.DataType;
import org.drools.learner.builder.Learner.DomainAlgo;

public class Util {
	
	public static final boolean PRINT_STATS = true;
	public static final String DRL_DIRECTORY = "src/main/rules/";
	public static final int NUM_TREES = 10;
	/*
	public static final boolean DEBUG = false;
	public static final boolean DEBUG_TEST = false;
	public static final boolean DEBUG_RULE_PRINTER = false;
	public static final boolean DEBUG_LEARNER = false;
	public static final boolean DEBUG_CATEGORIZER = false;
	public static final boolean DEBUG_ENTROPY = false;
	public static final boolean DEBUG_DIST = false;
	public static final boolean DEBUG_DECISION_TREE = false;
	*/
	public static int MAX_NUM_RULES = 1000000;
	public static boolean ONLY_ACTIVE_RULES = true;	/* TODO into global settings */
	
	public static boolean SORT_RULES_BY_RANK = true;
	public static boolean PRINT_RULES = true;
	
	private static Random BAGGING = new Random(System.currentTimeMillis());
	//public static String log_file = "testing.log";
	public static double TRAINING_RATIO = 1.0, TESTING_RATIO = 0.0;;
	public static double DEFAULT_TRAINING_RATIO = 0.80, DEFAULT_TESTING_RATIO= 0.20;
	
	
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
	
	public static double ln(double prob) {
		return Math.log(prob);
	}
	
	public static double exp(double prob) {
		return Math.exp(prob);
	}
	
	public static double division(int x, int y) {
		return (double)x/(double)y;
	}
	public static double division(int x, long y) {
		return (double)x/(double)y;
	}
	
	public static boolean epsilon(double d, double epsilon) {
		return Math.abs(d) <= epsilon; //0.000001;
	}
	
	/* TODO make this all_fields arraylist as hashmap */
	public static void getSuperFields(Class<?> clazz, ArrayList<Field> all_fields) {
		if (clazz == Object.class)
			return;
		
		//Field [] element_fields_ = clazz.getFields();
		Field [] element_fields = clazz.getDeclaredFields(); //clazz.getFields();
		for (Field f: element_fields) {
			all_fields.add(f);
		}
		getSuperFields(clazz.getSuperclass(), all_fields);
		
		return;
	}
	
	public static void decomposeStructuredFields(ArrayList<Field> element_fields, ArrayList<Field> decomposed_fields) {
		
		for (Field f: element_fields) {
			if (isSimpleType(f.getType()))
				decomposed_fields.add(f);
			else {
				// it is an structured attribute
				// u need to get its attributes
				//if the field is a collection of sth => BOK
				ArrayList<Field> field_structure = new ArrayList<Field>();
				Util.getSuperFields(f.getType(), field_structure);
				decomposeStructuredFields(field_structure, decomposed_fields);
			}
		}
		return;
	}
	
	public static boolean getterExits(String m_name) {
		if (m_name.startsWith("set"))
			return true;
		else 
			return false;
	}
	
	public static boolean isSetter(String m_name) {
		if (m_name.startsWith("set"))
			return true;
		else 
			return false;
	}
	
	public static String getFReference(Class<?> _obj_klass, String fName) {
		return _obj_klass.getName()+"@"+fName;
	}
	
	public static String getDecReference(Field f) {
		Class<?> owner_class = f.getDeclaringClass(); // i need the class that the field belongs to boooook
		return owner_class.getName() + "_"+f.getType().getName() +"_"+f.getName();
	}

	public static String getFieldName(String method_name) {
		if (method_name.startsWith("get") || method_name.startsWith("set"))
			return method_name.substring(3, method_name.length()).toLowerCase();
		else if (method_name.startsWith("is"))
			return method_name.substring(2, method_name.length()).toLowerCase();
		else 
			return null;
	}
	
	public static boolean isSimpleMethod(Class<?>[] type_name) {
		if (type_name.length==1) {
			return isSimpleType(type_name[0]);
		}
		else
			return false;
	}
	
	public static DataType getDataType(Class<?> type_name) {
		if (isSimpleType(type_name))
			return DataType.PRIMITIVE;
		else {
			if (isCollectionType(type_name)) {
				return DataType.COLLECTION;
			}
			else 
				return DataType.STRUCTURED;
		}
	}
	
	public static boolean isSimpleType(Class<?> type_name) {
		if (type_name.isPrimitive() || type_name == String.class)
			return true;
		else if (type_name == Boolean.class || type_name == Boolean.TYPE ||
			type_name == Integer.class || type_name == Integer.TYPE ||
			type_name == Long.class || type_name == Long.TYPE ||
			type_name == Double.class || type_name == Double.TYPE ||
			type_name == Float.class || type_name == Float.TYPE ||
			type_name == String.class)
			return true;
		else if (type_name.isEnum()) {
			return true;
		} else 
			return false;
	}
	
	public static boolean isCollectionType(Class<?> type_name) {
		if (type_name.isArray())
			return true;
		else if (type_name.isAssignableFrom(Iterable.class))
			return true;
		else 
			return false;
	}
	
	public static FieldAnnotation getFieldAnnotations(Field f) {
		Annotation[] annotations = f.getAnnotations();
		for (Annotation a : annotations) {
			if (a instanceof FieldAnnotation) {
				return (FieldAnnotation)a; // here it is !!!
			}
		}
		return null;
	}
	
	public static ClassAnnotation getDecClassAnnotations(Class<?> clazz) {
		Annotation[] annotations = clazz.getDeclaredAnnotations();	// it should get the inherited annotations 
		for (Annotation a : annotations) {
			if (a instanceof ClassAnnotation) {
				return (ClassAnnotation)a; // here it is !!!
			}
		}
		return null;
	}
	public static ClassAnnotation getClassAnnotations(Class<?> clazz) {
		Annotation[] annotations = clazz.getAnnotations();	// it should get the inherited annotations 
		for (Annotation a : annotations) {
			if (a instanceof ClassAnnotation) {
				return (ClassAnnotation)a; // here it is !!!
			}
		}
		return null;
	}
	// to process the type => it must be simple
	public static void processDomain(Domain fieldDomain, Class<?> clazz_type) {
		if (clazz_type == Boolean.TYPE || clazz_type == Boolean.class)	{	/* set discrete*/
//			fieldDomain.setCategorical(true);	// BY DEFAULT it is categorical
			fieldDomain.addCategory(Boolean.TRUE);
			fieldDomain.addCategory(Boolean.FALSE);
			fieldDomain.setFixed(true);
		} else if (clazz_type.isEnum()) {//f.isEnumConstant()) {
			Class<?> enum_f = clazz_type;
			//for (E e:enum_f.getEnumConstants())
			//fieldDomain.setFixed(true);
		} else if (clazz_type == String.class) {	
			/* BY DEFAULT it is categorical*/
		} 
		
	}
	
	/* TODO make this all_fields arraylist as hashmap */
	private static void getAllFields(Class<?> clazz, ArrayList<Field> all_fields, ArrayList<Class<?>> all_classes) {
		if (clazz == Object.class)
			return;
		all_classes.add(clazz);
		//Field [] element_fields_ = clazz.getFields();
		Field [] element_fields = clazz.getDeclaredFields(); //clazz.getFields();
		for (Field f: element_fields) {
			all_fields.add(f);
		}
		getAllFields(clazz.getSuperclass(), all_fields, all_classes);
		
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
	
	private static int getType(Class<?>[] type_name) {
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
	
	private static Object convertPrimitiveType (Object primitiveValue) {
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
}

