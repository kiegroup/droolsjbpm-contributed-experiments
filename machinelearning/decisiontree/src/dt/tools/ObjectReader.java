package dt.tools;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import dt.memory.Domain;
import dt.memory.BooleanDomain;
import dt.memory.NumericDomain;
import dt.memory.LiteralDomain;
import dt.memory.DomainSpec;

public class ObjectReader {
	
	private static final boolean DEBUG = false;

	public static Object read(Class<?> element_class,
			Collection<Domain<?>> domains, String data, String separator) {

		// System.out.println("BOK BOK domains: "+ domains.size());
		Object element = null;
		try {
			// element = Class.forName(element_class.getName());

			element = element_class.newInstance();

			Method[] element_methods = element_class.getDeclaredMethods();

			if (data.endsWith("."))
				data = data.substring(0, data.length() - 1);
			List<String> attributeValues = Arrays.asList(data.split(separator));

			for (Method m : element_methods) {
				String m_name = m.getName();
				Class<?>[] param_type_name = m.getParameterTypes();
				if (Util.isSetter(m_name) & Util.isSimpleType(param_type_name)) {
					// if (!Util.isSimpleType(return_type_name))
					// continue; // in the future we should support classes
					/*
					 * Annotation[] annotations = m.getAnnotations();
					 *  // iterate over the annotations to locate the MaxLength
					 * constraint if it exists DomainSpec spec = null; for
					 * (Annotation a : annotations) { if (a instanceof
					 * DomainSpec) { spec = (DomainSpec)a; // here it is !!!
					 * break; } } if (DEBUG) System.out.println("What annotation
					 * i found: "+ spec + " for method "+ m); String fieldString =
					 * attributeValues.get(spec.readingSeq());
					 * 
					 */

					String field = Util.getAttributeName(m_name);

					Iterator<Domain<?>> domain_it = domains.iterator();
					// Iterator<String> value_it = attributeValues.iterator();
					while (domain_it.hasNext()) {
						Domain<?> attr_domain = domain_it.next();
						// String name = attr_domain.getName();
						if (field.equalsIgnoreCase(attr_domain.getName())) {

							String fieldString = attributeValues
									.get(attr_domain.getReadingSeq());
							Object fieldValue = attr_domain
									.readString(fieldString);

							if (attr_domain instanceof NumericDomain) {
								if (param_type_name[0].getName()
										.equalsIgnoreCase("int")) {
									fieldValue = ((Number) fieldValue)
											.intValue();

								} else if (param_type_name[0].getName()
										.equalsIgnoreCase("float")) {
									fieldValue = ((Number) fieldValue)
											.floatValue();

								} else if (!param_type_name[0].getName()
										.equalsIgnoreCase("double")) {
									System.out
											.println("What the hack, which type of number is this??");
									fieldValue = ((Number) fieldValue)
											.doubleValue();
									System.exit(0);
								}
							} else if (attr_domain instanceof LiteralDomain) {
								if (param_type_name[0].getName()
										.equalsIgnoreCase("java.lang.String")) {
								} else {
									System.out
											.println("What the hack, which type of string is this?? "
													+ fieldValue);
									System.exit(0);
								}
							} else if (attr_domain instanceof BooleanDomain) {
								if (param_type_name[0].getName()
										.equalsIgnoreCase("boolean")) {
								} else {
									System.out
											.println("What the hack, which type of boolean is this?? "
													+ fieldValue);
									System.exit(0);
								}
							} else {
								System.out
										.println("What the hack, which type of object is this?? "
												+ fieldValue);
								System.exit(0);
							}

							// String fieldValue = fieldString;

							try {

								if (DEBUG)
									System.out.println("ObjectReader.read obj "
											+ element.getClass()
											+ " fielddomain name "
											+ attr_domain.getName()
											+ " value: " + fieldValue);
								if (DEBUG)
									System.out
											.println("ObjectReader.read method "
													+ m
													+ " the parameter type:"
													+ fieldValue.getClass());
								m.invoke(element, fieldValue);

							} catch (IllegalArgumentException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (InvocationTargetException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							}
							break;
						}

					}
				}
			}

		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return element;

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

	//read(Class<?> element_class, Collection<Domain<?>> collection, String data, String separator)
	public static Object read_(Class<?> element_class, Collection<Domain<?>> domains, String data, String separator) {

		Object element= null;
		try {
			//element = Class.forName(element_class.getName());
			
			element = element_class.newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Method [] element_methods = element_class.getDeclaredMethods();
		
		if (data.endsWith("."))
			data = data.substring(0, data.length()-1);
		List<String> attributeValues = Arrays.asList(data.split(separator));

		
		for (Method m: element_methods) {
			String m_name = m.getName();
			Class<?>[] param_type_name = m.getParameterTypes();
			if (Util.isSetter(m_name) & Util.isSimpleType(param_type_name) ) {
//				if (!Util.isSimpleType(return_type_name))
//					continue; // in the future we should support classes
				Annotation[] annotations = m.getAnnotations();
				
				// iterate over the annotations to locate the MaxLength constraint if it exists
				DomainSpec spec = null;
				for (Annotation a : annotations) {
				    if (a instanceof DomainSpec) {
				        spec = (DomainSpec)a; // here it is !!!
				        break;
				    }
				}
				if (DEBUG) System.out.println("What annotation i found: "+ spec + " for method "+ m);
				String fieldString = attributeValues.get(spec.readingSeq());
				String field = Util.getAttributeName(m_name);
				
				Iterator<Domain<?>> domain_it = domains.iterator();
				//Iterator<String> value_it = attributeValues.iterator();
				while(domain_it.hasNext()){
					Domain<?> attr_domain = domain_it.next();
					//String name = attr_domain.getName();
					if (field.equalsIgnoreCase(attr_domain.getName())) {
						
						Object fieldValue =  attr_domain.readString(fieldString);
						
						if (attr_domain instanceof NumericDomain) {
							if (param_type_name[0].getName().equalsIgnoreCase("int")) {
								fieldValue = ((Number)fieldValue).intValue();

							} else if (param_type_name[0].getName().equalsIgnoreCase("float")) {
								fieldValue = ((Number)fieldValue).floatValue();

							} else if (!param_type_name[0].getName().equalsIgnoreCase("double")) {
								System.out.println("What the hack, which type of number is this??");
								fieldValue = ((Number)fieldValue).doubleValue();
								System.exit(0);
							} 
						} else if (attr_domain instanceof LiteralDomain) {
							if (param_type_name[0].getName().equalsIgnoreCase("java.lang.String")) {
							} else {
								System.out.println("What the hack, which type of string is this?? " + fieldValue);
								System.exit(0);
							}
						} else if (attr_domain instanceof BooleanDomain) {
							if (param_type_name[0].getName().equalsIgnoreCase("boolean")) {
							} else {
								System.out.println("What the hack, which type of boolean is this?? " + fieldValue);
								System.exit(0);
							}
						} else {
							System.out.println("What the hack, which type of object is this?? " + fieldValue);
							System.exit(0);
						}
							
						
						// String fieldValue = fieldString;
						
						try {
							
							if (DEBUG) System.out.println("ObjectReader.read obj "+ element.getClass() + " fielddomain name "+attr_domain.getName()+" value: "+fieldValue);
							if (DEBUG) System.out.println("ObjectReader.read method "+ m + " the parameter type:"+ fieldValue.getClass());
							m.invoke(element, fieldValue);
							
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvocationTargetException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
						break;
					}
					
					
				}					
			}
		}

		return element;
		
		
	}
	
	
	// ------------------------------------------------------------------
	void read_fields(Object obj, Object classobj) throws Exception {
		Class<?> cl;
		Field fields[];

		//level++;

		// Get a handle to the class of the object.
		cl = (classobj instanceof Class) ? (Class<?>) classobj : classobj
				.getClass();

		// detect when we've reached out limits. This is particularly
		// the case when we're traversing the superclass chain.
		if ((cl == null) || (cl.isInstance(new Object()))) {
			//level--;
			return;
		}

		// comment is silently skipped so nothing to do here.

		// process each field in turn.
		fields = cl.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Class<?> ctype = fields[i].getType();
			int mod;
			String typeName = null, varName = null;

			mod = fields[i].getModifiers();
			if (Modifier.isStatic(mod))
				continue;

			Object varValue = null;
			// primitive types are handled directly.
			if (ctype.isPrimitive()) {
				varName = fields[i].getName();

				if (ctype.equals(Boolean.TYPE) || ctype.equals(Character.TYPE)
						|| ctype.equals(Byte.TYPE) || ctype.equals(Short.TYPE)
						|| ctype.equals(Integer.TYPE)
						|| ctype.equals(Long.TYPE) || ctype.equals(Float.TYPE)
						|| ctype.equals(Double.TYPE))
					handle_instance_field(ctype.getName(), varName, obj, fields[i],
							varValue );
				else if (ctype.equals(Void.TYPE))
					throw new IOException("read_fields: VOID type field found!");
				else
					throw new IOException("read_fields: unknown primitive type");

				// for arrays we need to extract the underlying type 1st
			}
			// else if (ctype.isArray()) {
			// StringTokenizer st = null;
			// int dim;
			// Object nval = null;
			//
			// typeName = readToken();
			// if (typeName.equals("class"))
			// typeName = readToken();
			// varName = readToken();
			// readToken(); // skip "="
			// readToken(); // skip curly brace
			//
			// st = new StringTokenizer(varName, "[]");
			// st.nextToken(); // skip var name
			//
			// dim = Integer.parseInt(st.nextToken());
			//
			// // no support for multi-dim arrays yet
			// if (st.hasMoreTokens())
			// throw new IOException(
			// "multi-dimensional array found - only one dimensional arrays
			// supported");
			//
			// // if the constructor didn't make one for us...
			// if (fields[i].get(obj) == null) {
			// fields[i].set(obj, Array.newInstance(ctype
			// .getComponentType(), dim));
			// }
			//
			// // pull in each element of the array
			// for (int j = 0; j < dim; j++) {
			// handle_array_field(typeName, varName, obj, fields[i], j);
			// }
			// readToken(); // skip curly brace
			// }
			else {

				// Strings need special care
				if (ctype.isInstance(new String())) {
					typeName = ctype.getName();
					varName = fields[i].getName();
					handle_instance_field(typeName, varName, obj, fields[i],
							varValue);

					// recurse as everything else is another class
				}
				// else {
				// Object nval = readAsciiObject();
				//
				// handle_instance_field(typeName, varName, obj, fields[i],
				// nval);
				// }
				else {
					continue;
				}
			}
		}

		//level--;
	}

	// ------------------------------------------------------------------
	// pull in a single field which isn't an array or a non-string class
	public static void handle_instance_field(String tname, String fname,
			Object obj, Field fl, Object value) throws IOException {

		String svalue = null;

		// Some sanity and 'do nothing' tests
		if (value == null)
			return;
		if (value == null)
			return;

		// Convenience to save lots of casts later.
		if (value instanceof String) {
			svalue = (String) value;

			if (svalue.equals("null"))
				return;
		}

		// now try to run the assignments
		try {
			if (fl.getType().equals(Boolean.TYPE))
				fl.set(obj, new Boolean(svalue));
			else if (fl.getType().equals(Character.TYPE)) {
				char[] onechar = new char[1];
				svalue.getChars(0, 1, onechar, 0);
				fl.set(obj, new Character(onechar[0]));
			} else if (fl.getType().equals(Byte.TYPE))
				fl.set(obj, new Byte(svalue));
			else if (fl.getType().equals(Short.TYPE))
				fl.set(obj, new Short(svalue));
			else if (fl.getType().equals(Integer.TYPE))
				fl.set(obj, new Integer(svalue));
			else if (fl.getType().equals(Long.TYPE))
				fl.set(obj, new Long(svalue));
			else if (fl.getType().equals(Float.TYPE))
				fl.set(obj, new Float(svalue));
			else if (fl.getType().equals(Double.TYPE))
				fl.set(obj, new Double(svalue));
			else if (fl.getType().equals((new String("").getClass())))
				fl.set(obj, value);
			else
				fl.set(obj, value);
		} catch (Exception e) {
			System.err.println("ERROR: assigning to " + fl + "\n" + "\tread: "
					+ tname + " " + fname + " = " + value);
			throw new IOException("field assignment failure:" + e);
		}
	}

	

}
