package org.drools.learner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.drools.base.ClassFieldExtractor;
import org.drools.base.ClassFieldExtractorCache;
import org.drools.learner.builder.Learner.DataType;
import org.drools.learner.builder.Learner.DomainAlgo;
import org.drools.learner.tools.ClassAnnotation;
import org.drools.learner.tools.ClassStructure;
import org.drools.learner.tools.ClassVisitor;
import org.drools.learner.tools.FeatureNotSupported;
import org.drools.learner.tools.FieldAnnotation;
import org.drools.learner.tools.PseudoFieldExtractor;
import org.drools.learner.tools.Util;
import org.drools.spi.Extractor;

/*
 * A description of a data set's attributes and their properties.
 */
public class Schema {

	public static Schema createFromClass(Class<?> clazz, DomainAlgo domain_type, DataType data_type) throws FeatureNotSupported {
		Schema schema = new Schema(clazz);
		ClassFieldExtractorCache cache = ClassFieldExtractorCache.getInstance();
		/*
		ArrayList<Field> element_fields = new ArrayList<Field>();
		Util.getAllFields(clazz, element_fields); 
		*/
		
		ArrayList<Field> element_fields = new ArrayList<Field>();
		/* Apperantly the getMethod function recurse on the superclasses
		 * i dont need to recurse myself
		ArrayList<Class<?>> element_classes = new ArrayList<Class<?>>();
		Util.getAllFields(clazz, element_fields, element_classes); 
		 */
		Util.getSuperFields(clazz, element_fields); 	//clazz.getDeclaredFields(); //clazz.getFields();
		ArrayList<Field> decomposed_fields = new ArrayList<Field>(element_fields.size()+10);
		Util.decomposeStructuredFields(element_fields, decomposed_fields);
		for (Field f: element_fields) {
			String f_name = f.getName();
			FieldAnnotation spec = Util.getFieldAnnotations(f);
			
			if (spec != null) {
				if (!spec.ignore() && f.getType() == String.class && !spec.discrete()) {
					
					throw new FeatureNotSupported("String categorization not supported");
				}
				boolean skip = false;
				switch (domain_type) {
				case CATEGORICAL:	//ID3 can work only with categorical types
					if (spec.ignore() || !spec.discrete())
						skip = true;
					break;
				case QUANTITATIVE: // C45 can work with categorical || quantitative domain
					if (spec.ignore())
						skip = true;
					break;
				default:
					if (spec.ignore())
						skip = true;
				}
				// only if the annotations are given and the flag to ignore is set true 
				// then continue to next field	
				if (skip)	continue;	
			}
			Domain fieldDomain = new Domain(f_name, f.getType());
			if (spec != null) {	// if the annotations are given Read them to update domain's properties
				fieldDomain.setCategorical(spec.discrete());	
				if (spec.target())	{	schema.addTarget(f_name);	}
			}// else just set every thing categorical
			
			if (f.getType() == Boolean.TYPE || f.getType() == Boolean.class)	{	/* set discrete*/
//				fieldDomain.setCategorical(true);	// BY DEFAULT it is categorical
				fieldDomain.addCategory(Boolean.TRUE);
				fieldDomain.addCategory(Boolean.FALSE);
				fieldDomain.setFixed(true);
			}
			else if (f.getType() == String.class) {	
				/* BY DEFAULT it is categorical*/
			} else {
				// structured data
				if (data_type == DataType.STRUCTURED) {
					// u need to process then
					
				}
			}
			
			/* TODO insanity checks:
			 * 1- the annotations are not given
			 * 2- there is no target specified
			 * 3- more than one target is specified
			 */
			ClassFieldExtractor f_extractor = cache.getExtractor( clazz, f_name, clazz.getClassLoader() );
			schema.extractorMap.put(f_name, f_extractor);
			schema.domainMap.put(f_name, fieldDomain);			
		}
		/* Apperantly the getMethod function recurse on the superclasses
		 * i dont need to recurse myself
		for (Class<?> c: element_classes) {
			Annotation[] annotations = c.getAnnotations();
		*/
		
		ClassAnnotation class_label = Util.getClassAnnotations(clazz);	
		if (class_label != null && class_label.label_element() != "") {
			// the targetting label is set, put the function that gets that value somewhere

			try {
				/* Apperantly the getMethod function recurse on the superclasses
				 * i dont need to recurse myself
					Method m =c.getDeclaredMethod(lab.label_element(), null);
				 */
				Method m = clazz.getMethod(class_label.label_element(), null);

				Domain fieldDomain = new Domain(class_label.label_element(), m.getReturnType());
				fieldDomain.setArtificial(true);
				if (m.getReturnType() == Boolean.TYPE || m.getReturnType() == Boolean.class)	{	/* set discrete*/
//					fieldDomain.setCategorical(true);	// BY DEFAULT it is categorical
					fieldDomain.addCategory(Boolean.TRUE);
					fieldDomain.addCategory(Boolean.FALSE);
					fieldDomain.setFixed(true);					
				}
				//else if (m.getReturnType() == String.class)	{	/* BY DEFAULT it is categorical*/}
				schema.domainMap.put(class_label.label_element(), fieldDomain);

				Extractor m_extractor = new PseudoFieldExtractor(clazz, m);
				//cache.getExtractor( clazz, lab.label_element(), clazz.getClassLoader() );
				schema.extractorMap.put(class_label.label_element(), m_extractor);
				schema.clearTargets();
				schema.addTarget(class_label.label_element());
				//break; // if the ClassAnnotation is found then stop

			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//}
		
		return schema;
	}
	
	
	public static Schema createStructuredSchema(Class<?> clazz, DomainAlgo domain_type, DataType data_type) throws Exception {
		Schema schema = new Schema(clazz);
		ClassFieldExtractorCache cache = ClassFieldExtractorCache.getInstance();

		ClassVisitor visitor = new ClassVisitor(domain_type, data_type);
		visitor.visit(clazz);
		schema.klass_structure = visitor.getStructure();
		
		for (Class<?> _klass : schema.klass_structure.keySet()) {
			ClassStructure struct = schema.klass_structure.get(_klass);
			//Iterator<Field> f_ite = struct.getFieldIterator();
			//while (f_ite.hasNext()) {
			//Field f= f_ite.next();
			for (Field f : struct.getFields()) {
				String f_name = f.getName();
				
				DataType f_type = struct.getFieldType(f);
				switch (f_type) {
				case PRIMITIVE:		// domain will be created only for primitive types
					Domain fieldDomain = new Domain(f_name, f.getType());
					if (f.getType() == Boolean.TYPE || f.getType() == Boolean.class)	{	/* set discrete*/
//						fieldDomain.setCategorical(true);	// BY DEFAULT it is categorical
						fieldDomain.addCategory(Boolean.TRUE);
						fieldDomain.addCategory(Boolean.FALSE);
						fieldDomain.setFixed(true);
					} else if (f.isEnumConstant()) {
						Class<?> enum_f = f.getType();
						//for (E e:enum_f.getEnumConstants())
						//fieldDomain.setFixed(true);
					} else if (f.getType() == String.class) {	
						/* BY DEFAULT it is categorical*/
					} 
					schema.domainMap.put(f_name, fieldDomain);	
				case STRUCTURED:	// the extractor is necessary for both types of data.
					ClassFieldExtractor f_extractor = cache.getExtractor( _klass, f_name, _klass.getClassLoader() );
					schema.extractorMap.put(f_name, f_extractor);
					break;
				default:
					throw new Exception("What type of data is this");
				
				}
				
				/* TODO insanity checks:
				 * 1- the annotations are not given
				 * 2- there is no target specified
				 * 3- more than one target is specified
				 */	
			}
		}
		return schema;
	}
	
	
	public static Schema createSchemaStructure(Class<?> clazz, DomainAlgo domain_type, DataType data_type) throws Exception {
		Schema schema = new Schema(clazz);
		//ŒClassFieldExtractorCache cache = ClassFieldExtractorCache.getInstance();

		// schema is modified
		ClassVisitor visitor = new ClassVisitor(schema, domain_type, data_type);
		visitor.visit();
//		schema.klass_structure = visitor.getStructure();
//		schema.domainMap= visitor.getDomains();
//		schema.extractorMap = visitor.getExtractors();
//		schema.targets = visitor.getTargets();
		
//		for (Class<?> _klass : schema.klass_structure.keySet()) {
//			ClassStructure struct = schema.klass_structure.get(_klass);
//			//Iterator<Field> f_ite = struct.getFieldIterator();
//			//while (f_ite.hasNext()) {
//			//Field f= f_ite.next();
//			for (Field f : struct.getFields()) {
//				String f_name = f.getName();
//				
//				DataType f_type = struct.getFieldType(f);
//				switch (f_type) {
//				case PRIMITIVE:		// domain will be created only for primitive types
//					Domain fieldDomain = new Domain(f_name, f.getType());
//					if (f.getType() == Boolean.TYPE || f.getType() == Boolean.class)	{	/* set discrete*/
////						fieldDomain.setCategorical(true);	// BY DEFAULT it is categorical
//						fieldDomain.addCategory(Boolean.TRUE);
//						fieldDomain.addCategory(Boolean.FALSE);
//						fieldDomain.setFixed(true);
//					} else if (f.isEnumConstant()) {
//						Class<?> enum_f = f.getType();
//						//for (E e:enum_f.getEnumConstants())
//						//fieldDomain.setFixed(true);
//					} else if (f.getType() == String.class) {	
//						/* BY DEFAULT it is categorical*/
//					} 
//					schema.domainMap.put(f_name, fieldDomain);	
//				case STRUCTURED:	// the extractor is necessary for both types of data.
//					ClassFieldExtractor f_extractor = cache.getExtractor( _klass, f_name, _klass.getClassLoader() );
//					schema.extractorMap.put(f_name, f_extractor);
//					break;
//				default:
//					throw new Exception("What type of data is this");
//				
//				}
//				
//
//			}
//		}
		
		/* TODO insanity checks:
		 * 1- the annotations are not given
		 * 2- there is no target specified
		 * 3- more than one target is specified
		 */	
		return schema;
	}
	// the owner class of the schema
	private Class<?> klass;
	
	// the structure of the owner class
	private HashMap<Class<?>,ClassStructure> klass_structure;
	
	// key: field name
	private Hashtable<String, Extractor> extractorMap;

	// key: field name
	private Hashtable<String, Domain> domainMap;

	private HashSet<String> targets;
	
	public Schema(Class<?> _klass) {
		this.klass = _klass;
		
		this.klass_structure = new HashMap<Class<?>,ClassStructure>();
		this.extractorMap = new Hashtable<String, Extractor>();
		this.domainMap = new Hashtable<String, Domain>();
		this.targets = new HashSet<String>();
	}
	
	public Class<?> getObjectClass() {
		return klass;
	}
	
	public HashMap<Class<?>,ClassStructure> getClassStructure() {
		return klass_structure;
	}
	public void putStructure(Class<?> clazz, ClassStructure structure) {
		klass_structure.put(clazz, structure);;
	}
	
	
	public boolean addTarget(String _target) {
		return targets.add(_target);
	}
	
	public void clearTargets() {
		targets.clear();
	}
	
	public Set<String> getAttrNames() {
		return this.domainMap.keySet();
	}
	
	public void putDomain(String attr_name, Domain d) {
		domainMap.put(attr_name, d);
	}
	
	public Domain getAttrDomain(String attr_name) {
		return this.domainMap.get(attr_name);
	}
	
	public void putExtractor(String attr_name, Extractor extract) {
		extractorMap.put(attr_name, extract);
	}
	public Extractor getAttrExtractor(String attr_name) {
		return this.extractorMap.get(attr_name);
	}
	
	public Collection<String> getTargets() {
		return targets;
	}

	
	
}
