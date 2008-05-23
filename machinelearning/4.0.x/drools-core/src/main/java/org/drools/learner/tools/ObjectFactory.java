package org.drools.learner.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


public class ObjectFactory {
	
//	public static List<Object> getObjectsFromFile(Class<?> clazz, String filename, String separator) {
//		
//		ObjectFactory class_factory = new ObjectFactory(clazz);
//		List<Object> new_objects = fromFileAsObject(filename, separator);
//		
//	}
	public static List<Object> getObjects(Class<?> obj_class, String filename) {
		ObjectFactory class_factory = new ObjectFactory(obj_class);
		try {
			//return class_factory.fromFileAsObject("src/main/java/org/drools"+filename, ",");
			return class_factory.fromFileAsObject(filename, ",");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private Class<?> obj_clazz;
	private ArrayList<Field> clazz_fields;
	private HashMap<String,Integer> field_sequences;
	
	private ObjectFactory (Class<?> clazz) {
		this.obj_clazz = clazz;
		this.clazz_fields = new ArrayList<Field>();
		Util.getAllFields(clazz, clazz_fields); 
		
		this.field_sequences = getFieldReadingSeqs(clazz, clazz_fields);
	}
	
	public List<Object> fromFileAsObject(String filename, String separator) 
				throws Exception
	{
		
		List<Object> obj_read = new ArrayList<Object>();

		/**/
		File file =new File(filename);
		if(!file.exists()){
			System.out.println("where is the file ? "+ filename);
			//System.exit(0);
			
			file =new File("src/main/java/org/drools/examples/learner/"+filename);
			if(!file.exists()){
				System.out.println("where is still the file ? "+ file);
				System.exit(0);
			}
		}
		
		BufferedReader reader = new BufferedReader(new FileReader(file));	
		/**/
		
//		BufferedReader reader = new BufferedReader(new InputStreamReader(
//				this.obj_clazz.getResourceAsStream(filename)));// "../data/"
		String line;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.length() == 0)
				break;
			Object element = this.read(line, separator);
			//System.out.println("New object "+ element);
			obj_read.add(element);

		}
		return obj_read;
	}
	
	private Object read(String data, String separator) throws Exception {
		Object element = null;
		
		try {
			element = this.obj_clazz.newInstance();
			
			
			if (data.endsWith("."))
				data = data.substring(0, data.length() - 1);
			List<String> fieldValues = Arrays.asList(data.split(separator));
			
			
			Method[] element_methods = this.obj_clazz.getDeclaredMethods();
			
			for (Method m : element_methods) {
				String m_name = m.getName();
				Class<?>[] param_type_name = m.getParameterTypes();
				if (ObjectFactory.isSetter(m_name) & ObjectFactory.isSimpleType(param_type_name)) {
					String basicFieldName = ObjectFactory.getFieldName(m_name);
					
					/* TODO dont use the basic field name as the real field name, there can be a capital/miniscul 
					 * letter problem
					 */
					Field f = null;
					for (Field _f: this.clazz_fields) {
						if (_f.getName().equalsIgnoreCase(basicFieldName)) {
							f = _f;
							break;
						}
							
					}
					
					if (f == null) {
						throw new Exception("Field("+basicFieldName+") could not be found for the class("+this.obj_clazz+")");
					}
					else {
						String fieldString = fieldValues.get(this.field_sequences.get(f.getName()));
						Object fieldValue = readString(f, fieldString.trim());
						
						try {
							m.invoke(element, fieldValue);
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							System.out.println("invoking the method of the element e:"+element + " v:"+ fieldValue);
							
							e.printStackTrace();
						}
						
						
					}
				}
			}
			
			/*
			for (Field f: element_fields) {
				String f_name = f.getName();
				
				String fieldString = fieldValues.get(fieldSequences.get(f_name));
				Object fieldValue = readString(f, fieldString);
				
				ClassFieldExtractor f_extractor = cache.getExtractor( clazz, f_name, clazz.getClassLoader() );
//				f_extractor.
				
			}*/
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return element;
	}
	
	
	private static HashMap<String,Integer> getFieldReadingSeqs(Class<? extends Object> classObj, ArrayList<Field> element_fields) {

		HashMap<String,Integer> readingSeqs = new HashMap<String,Integer>(element_fields.size()) ;
		for( Field f: element_fields) {
			String f_name = f.getName();
			
			Annotation[] annotations = f.getAnnotations();
			FieldAnnotation spec = null;
			for (Annotation a : annotations) {
				if (a instanceof FieldAnnotation) {
					spec = (FieldAnnotation)a; // here it is !!!
					readingSeqs.put(f_name, spec.readingSeq());
					break;
				}
			}
		}
		return readingSeqs;
	}
	
	private static Object readString(Field f, String text) {
		
		Class<?> type_name = f.getType();
		if (type_name.isPrimitive()) { //boolean, byte, char, short, int, long, float, and double.
			if (type_name == Boolean.TYPE) {
				return readBooleanFromString(text);
			} else if (type_name == Integer.TYPE) {
				return readIntegerFromString(text);
			} else if (type_name == Short.TYPE) {
				return readShortFromString(text);
			} else if (type_name == Long.TYPE) {
				return readLongFromString(text);
			} else if (type_name == Double.TYPE) {
				return readDoubleFromString(text);
			} else if (type_name == Float.TYPE) {
				return readFloatFromString(text);
			} else {
				System.out.print("ObjectFactory.readString(field, text) primitive type "+type_name);
				return null;
			}
		} else if (type_name.isAssignableFrom(String.class)) {
			return text.trim();
		} else if (type_name.isAssignableFrom(Boolean.class)) {
			return readBooleanFromString(text);
		} else if (type_name.isAssignableFrom(Integer.class)) {
			return readIntegerFromString(text);	
		} else if (type_name.isAssignableFrom(Short.class)) {
			return readShortFromString(text);	
		} else if (type_name.isAssignableFrom(Long.class)) {
			return readLongFromString(text);	
		} else if (type_name.isAssignableFrom(Double.class)) {
			return readDoubleFromString(text);		
		} else if (type_name.isAssignableFrom(Float.class)) {
			return readFloatFromString(text);
		} else {
			System.out.print("ObjectFactory.readString(field, text) object type "+type_name);
			return null;
		}

	}
	
	
	private static Object readBooleanFromString(String data) {
		//System.out.print("What is the data : "+ data);
		if (data.trim().equalsIgnoreCase("true"))
			return Boolean.TRUE;
		else if ((data.trim().equalsIgnoreCase("false")))
			return Boolean.FALSE;
		else if (data.trim().equalsIgnoreCase("1"))
			return Boolean.TRUE;
		else if (data.trim().equalsIgnoreCase("0"))
			return Boolean.FALSE;
		else
			return Boolean.parseBoolean(data);
	}
	
	private static Object readIntegerFromString(String data) {
		return Integer.parseInt(data);
	}
	private static Object readShortFromString(String data) {
		return Short.parseShort(data);
	}
	private static Object readLongFromString(String data) {
		return Long.parseLong(data);
	}
	private static Object readFloatFromString(String data) {
		return Float.parseFloat(data);
	}
	
	private static Object readDoubleFromString(String data) {
		return Double.parseDouble(data);
	}
	
	private static boolean isSetter(String m_name) {
		if (m_name.startsWith("set") || m_name.startsWith("is") )
			return true;
		else 
			return false;
	}

	private static String getFieldName(String method_name) {
		if (method_name.startsWith("get") || method_name.startsWith("set"))
			return method_name.substring(3, method_name.length()).toLowerCase();
		else if (method_name.startsWith("is"))
			return method_name.substring(2, method_name.length()).toLowerCase();
		else 
			return null;
	}
	
	private static boolean isSimpleType(Class<?>[] type_name) {
		if (type_name.length==1) {
			if (type_name[0].isPrimitive() || type_name[0] == String.class)
				return true;
			if (type_name[0] == Boolean.class || type_name[0] == Boolean.TYPE ||
				type_name[0] == Integer.class || type_name[0] == Integer.TYPE ||
				type_name[0] == Long.class || type_name[0] == Long.TYPE ||
				type_name[0] == Double.class || type_name[0] == Double.TYPE ||
				type_name[0] == Float.class || type_name[0] == Float.TYPE ||
				type_name[0] == String.class)
				return true;
			else 
				return false;
		}
		else
			return false;
	}
	
	
	

}
