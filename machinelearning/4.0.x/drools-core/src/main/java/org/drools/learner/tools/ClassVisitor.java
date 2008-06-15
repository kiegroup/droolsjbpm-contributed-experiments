package org.drools.learner.tools;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.drools.base.ClassFieldExtractorCache;
import org.drools.learner.Domain;
import org.drools.learner.Schema;
import org.drools.learner.builder.Learner;
import org.drools.learner.builder.Learner.DataType;
import org.drools.learner.builder.Learner.DomainAlgo;
import org.drools.spi.Extractor;

public class ClassVisitor {
	private static ClassFieldExtractorCache cache = ClassFieldExtractorCache.getInstance();
	
	private DomainAlgo domain_type = Learner.DEFAULT_DOMAIN;
	private DataType data_type = Learner.DEFAULT_DATA;
	
//	private HashMap<Class<?>, ClassStructure> all_klasses;
	
//	// key: field name
//	private Hashtable<String, Extractor> extractorMap;
//
//	// key: field name
//	private Hashtable<String, Domain> domainMap;
//
//	private HashSet<String> targets;
	private Schema class_schema;

	private boolean TARGET_FOUND;
	private boolean MULTIPLE_TARGETS = false;
	
	
	public ClassVisitor (DomainAlgo _domain_type, DataType _data_type) {
		domain_type = _domain_type;
		data_type = _data_type;
		
	}
	
	public ClassVisitor (Schema _class_schema, DomainAlgo _domain_type, DataType _data_type) {
		domain_type = _domain_type;
		data_type = _data_type;

		this.class_schema = _class_schema;
	}
	
	// new one
	public void visit() throws FeatureNotSupported {
//		this.all_klasses = new HashMap<Class<?>,ClassStructure>();
		TARGET_FOUND = false;
		getStructuredSuperFields(class_schema.getObjectClass());		
		return;
	}
	
	public void visit(Class<?> clazz) throws FeatureNotSupported {
		class_schema = new Schema(clazz);
//		this.all_klasses = new HashMap<Class<?>,ClassStructure>();
		getStructuredSuperFields(clazz);		
		return;
	}
	
	public HashMap<Class<?>,ClassStructure> getStructure() {
		return class_schema.getClassStructure();
	}
	
	
	public void getStructuredSuperFields(Class<?> clazz) throws FeatureNotSupported {
		System.out.println("On the class "+ clazz);
		System.out.println(" the structure exists "+ class_schema.getClassStructure().containsKey(clazz));
		if (clazz.equals(Object.class) || 
		   (class_schema.getClassStructure().containsKey(clazz) && class_schema.getClassStructure().get(clazz).isDone()))
			return;

		ClassStructure structure = new ClassStructure(clazz);
		if (MULTIPLE_TARGETS || !TARGET_FOUND)
			processClassLabel(structure);
		class_schema.putStructure(clazz, structure);
		// get the fields declared in the class
		Field [] element_fields = clazz.getDeclaredFields(); //clazz.getFields();
		for (Field f: element_fields) {
			decomposeField(structure, f);
		}
		
		structure.setDone();
		
		Class<?> parent_klass = clazz.getSuperclass();
		structure.setParent(parent_klass);
		getStructuredSuperFields(parent_klass);
		
		return;
	}
	
	public void decomposeField(ClassStructure structure, Field f) throws FeatureNotSupported {
		// can get the field annotation
		// if it is ignored do not do anything
		FieldAnnotation f_spec = Util.getFieldAnnotations(f);
		boolean skip_by_annotation = false;
		if (f_spec != null) {
			// the type of the fields that cannot be processed
			if (!f_spec.ignore() && f.getType() == String.class && !f_spec.discrete()) {
				throw new FeatureNotSupported("String categorization not supported");
			}
			switch (domain_type) {
			case CATEGORICAL:	//ID3 can work only with categorical types
				if (f_spec.ignore() || !f_spec.discrete())
					skip_by_annotation = true;
				break;
			case QUANTITATIVE: // C45 can work with categorical || quantitative domain
				if (f_spec.ignore())
					skip_by_annotation = true;
				break;
			default:
				if (f_spec.ignore())
					skip_by_annotation = true;
			}
			// only if the annotations are given and the flag to ignore is set true 
			// then continue to next field	
			
		}
		if (!skip_by_annotation) {
			String f_name= f.getName();
			Class<?> _obj_klass = structure.getOwnerClass();
			DataType d_type = Util.getDataType(f.getType());	
			
			///////////
			if (d_type != DataType.COLLECTION) { // TODO 
				Extractor f_extractor = cache.getExtractor( _obj_klass, f_name , _obj_klass.getClassLoader() );
				class_schema.putExtractor(f_name, f_extractor);
				structure.addField(f, d_type);	
			}
			
			///////////
		
			switch (d_type) {    
			case PRIMITIVE:		// domain will be created only for primitive types
				Domain fieldDomain = new Domain(f_name, f.getType());
				if (f_spec != null) {
					fieldDomain.setCategorical(f_spec.discrete());	
					if ((MULTIPLE_TARGETS || !TARGET_FOUND) && f_spec.target()) {	
						class_schema.addTarget(f.getName());	
						TARGET_FOUND = true;
					}
				}
				Util.processDomain(fieldDomain, f.getType());
				
				class_schema.putDomain(f_name, fieldDomain);
//				Extractor f_extractor = cache.getExtractor( _obj_klass, f_name , _obj_klass.getClassLoader() );
//				class_schema.putExtractor(f_name, f_extractor);		
//				structure.addField(f, d_type);	
				break;
			case STRUCTURED:	// the extractor is necessary for both types of data.
//				ClassFieldExtractor complex_f_extractor = cache.getExtractor( _obj_klass, f_name , _obj_klass.getClassLoader() );
//				class_schema.putExtractor(f_name, complex_f_extractor);			
//				structure.addField(f, d_type);		
				getStructuredSuperFields(f.getType());//recurse on the structured 
				break;
			case COLLECTION:
				throw new FeatureNotSupported("Can not deal with collections as an attribute");
			default:
				//throw new Exception("What type of data is this");	
			}
		}
		return;
	}

	public void processClassLabel(ClassStructure clazz_structure) {

		Class<?> clazz = clazz_structure.getOwnerClass();
		/* Apperantly the getAnnotation function recurse on the superclasses
		 * so use the getDeclaredAnnotations() not to recurse
		*/
		ClassAnnotation class_label = Util.getDecClassAnnotations(clazz);	
		if (class_label != null && class_label.label_element() != "") {
			// the targetting label is set, put the function that gets that value somewhere

			try {
				/* Apperantly the getMethod function recurse on the superclasses
				 * i dont need to recurse myself
					Method m =c.getDeclaredMethod(lab.label_element(), null);
				 */
				Method m = clazz.getMethod(class_label.label_element(), null);
	
				if (Util.isSimpleType(m.getReturnType())) {
					Domain fieldDomain = new Domain(class_label.label_element(), m.getReturnType());
					fieldDomain.setArtificial(true);
					Class<?> method_class = m.getReturnType();
					Util.processDomain(fieldDomain, method_class);
					clazz_structure.addMethod(m);
					class_schema.putDomain(class_label.label_element(), fieldDomain);

					Extractor m_extractor = new PseudoFieldExtractor(clazz, m);
					//cache.getExtractor( clazz, lab.label_element(), clazz.getClassLoader() );
					class_schema.putExtractor(class_label.label_element(), m_extractor);
					if (!MULTIPLE_TARGETS)
						class_schema.clearTargets();
					class_schema.addTarget(class_label.label_element());
					TARGET_FOUND = true;
					//break; // if the ClassAnnotation is found then stop
				}

			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}	

}
