package org.drools.learner.tools;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Stack;

import org.drools.RuntimeDroolsException;
import org.drools.base.ClassFieldAccessorCache;
import org.drools.learner.Domain;
import org.drools.learner.Schema;
import org.drools.learner.builder.Learner;
import org.drools.learner.builder.Learner.DataType;
import org.drools.learner.builder.Learner.DomainAlgo;
//import org.drools.spi.Extractor;ReadAccessor
import org.drools.spi.ReadAccessor;

public class ClassVisitor {
	
	private static SimpleLogger flog = LoggerFactory.getUniqueFileLogger(ClassVisitor.class, SimpleLogger.WARN);
	private static SimpleLogger slog = LoggerFactory.getSysOutLogger(ClassVisitor.class, SimpleLogger.WARN);
	
	
	//private static ClassFieldExtractorCache cache = ClassFieldExtractorCache.getInstance();
	private static ClassFieldAccessorCache cache = ClassFieldAccessorCache.getInstance();
	
	private DomainAlgo domain_type = Learner.DEFAULT_DOMAIN;
	private DataType data_type = Learner.DEFAULT_DATA;

	private Schema class_schema;
	private Stack<Field> class_relation;

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
	
	public void visit() throws FeatureNotSupported {
//		this.all_klasses = new HashMap<Class<?>,ClassStructure>();
		TARGET_FOUND = false;
		class_relation =  new Stack<Field>();
		getStructuredSuperFields(class_schema.getObjectClass()/*, null*/);		 
			
		return;
	}
	
	public HashMap<Class<?>,ClassStructure> getStructure() {
		return class_schema.getClassStructure();
	}
	
	public void getStructuredSuperFields(Class<?> clazz/*, Class<?> owner_clazz*/){
		if (slog.debug() != null)
			slog.debug().log("On the class "+ clazz+  " the structure exists "+ class_schema.getClassStructure().containsKey(clazz));
		if (class_schema.getClassStructure().containsKey(clazz) && class_schema.getClassStructure().get(clazz).isDone())
			return;
		// process if the parent_klass.equals(Object.class) ?????
		ClassStructure structure = new ClassStructure(clazz);
		if (MULTIPLE_TARGETS || !TARGET_FOUND)
			processClassLabel(structure);
		class_schema.putStructure(clazz, structure);
		
		// get the fields declared in the class
		Field [] element_fields = clazz.getDeclaredFields(); //clazz.getFields();
		for (Field f: element_fields) {
			try {
				decomposeField(structure, f /*, owner_clazz*/);
			} catch (FeatureNotSupported e) {
				if (slog.error() != null)
					slog.error().log("FeatureNotSupported "+e+")\n");
				
			}
		}	
		structure.setDone();
		Class<?> parent_klass = clazz.getSuperclass();
		structure.setParent(parent_klass);
		if (parent_klass.equals(Object.class))
			return;
		getStructuredSuperFields(parent_klass);
		
		return;
	}
	
	public void decomposeField(ClassStructure structure, Field f/*, Class<?> owner_clazz*/) throws FeatureNotSupported {
		
//		if (slog.warn() != null)
//			slog.warn().log("!!!!!!!!!The field "+f+"  " +f.getName()+ "\n");
		// can get the field annotation
		// if it is ignored do not do anything
		FieldAnnotation f_spec = Util.getFieldAnnotations(f);
		boolean skip = false, ignore_field = false;
		if (f_spec != null) {
			// the type of the fields that cannot be processed
			if (!f_spec.ignore() && f.getType() == String.class && !f_spec.discrete()) {
				throw new FeatureNotSupported("String categorization not supported");
			}
			switch (domain_type) {
			case CATEGORICAL:	//ID3 can work only with categorical types
				if (f_spec.skip() || !f_spec.discrete())
					skip = true;
				break;
			case QUANTITATIVE: // C45 can work with categorical || quantitative domain
				if (f_spec.skip())
					skip = true;
				break;
			default:
				if (f_spec.skip())
					skip = true;
			}
			ignore_field = f_spec.ignore();
			if (ignore_field)
				skip = ignore_field;
			// only if the annotations are given and the flag to ignore is set true 
			// then continue to next field	
			
		}
		// if there is a getter?
		//if (Util.isGetter(m_name) & Util.isSimpleType(returns)) {
		
		if (!skip) {
			String f_name= f.getName();		
			Class<?> _obj_klass = structure.getOwnerClass();
			String f_refName = Util.getFReference(_obj_klass, f_name);
			DataType d_type = Util.getDataType(f.getType());	
			
			///////////
			if (d_type != DataType.COLLECTION) { // TODO 
				if (slog.warn() != null)
					slog.warn().log("The field "+f_name+" of the obj klass:"+_obj_klass + " and the loader:" + _obj_klass.getClassLoader()+ "\n");
				try {
					ReadAccessor f_extractor;
//					if (f_name.startsWith("get")) {//TODO check
//						System.out.println(f_name.substring(3));
//						f_extractor = cache.getAccessor( _obj_klass, f_name.substring(3) , _obj_klass.getClassLoader() );
//					} else
						f_extractor = cache.getAccessor( _obj_klass, f_name , _obj_klass.getClassLoader() );
					
					class_schema.putExtractor(f_refName, f_extractor);
					
					
					structure.addField(f, d_type);	
					switch (d_type) {    
					case PRIMITIVE:		// domain will be created only for primitive types
						Domain fieldDomain = new Domain(_obj_klass, f_name, f.getType());
						//fieldDomain.setOwner(owner_clazz);
						if (f_spec != null) {
							fieldDomain.setCategorical(f_spec.discrete());	
							if ((MULTIPLE_TARGETS || !TARGET_FOUND) && f_spec.target()) {	
								class_schema.addTarget(f_refName);	
								TARGET_FOUND = true;
							}
						}
						Util.processDomain(fieldDomain, f.getType());
						fieldDomain.ignore(ignore_field);
						class_schema.putDomain(f_refName, fieldDomain);
						for (Field parent_klass: class_relation)
							class_schema.addParentField(f_refName, parent_klass);
						break;
					case STRUCTURED:	// the extractor is necessary for both types of data.	
						class_relation.push(f);
						getStructuredSuperFields(f.getType());//recurse on the structured 
						class_relation.pop();
						break;
					default:
						//throw new Exception("What type of data is this");	
					}
				} catch (RuntimeDroolsException  e) {
					if (slog.warn() != null)
						slog.warn().log("Exception e:"+ e +" skip\n");
					if (flog.warn() != null)
						flog.warn().log("Exception e:"+ e +" skip");
				}
			} else {	//case COLLECTION:
				if (slog.warn() != null)
					slog.warn().log("Case Collection(f_name "+f_name+" What is the obj klass:"+_obj_klass + ")\n");
				throw new FeatureNotSupported("Can not deal with collections as an attribute");
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
				/* Apperantly the java function "getMethod()" recurses on the superclasses
				 * i dont need to recurse myself
					Method m =c.getDeclaredMethod(lab.label_element(), null);
				 */
				Method m = clazz.getMethod(class_label.label_element(), null);	
				if (Util.isSimpleType(m.getReturnType())) {
					String mName = class_label.label_element();
//					if (mName.startsWith("get")) {//TODO check??
//						mName = mName..toLowerCase();
//					}
					System.out.println(mName);
					String f_refName = Util.getFReference(clazz, mName); // class.name + "@" + label
					Domain fieldDomain = new Domain(clazz, mName, m.getReturnType());
					fieldDomain.setArtificial(true);
					Class<?> method_class = m.getReturnType();	
					
					Util.processDomain(fieldDomain, method_class);
					clazz_structure.addMethod(m);
					class_schema.putDomain(f_refName, fieldDomain);

					ReadAccessor m_extractor = new PseudoFieldExtractor(clazz, m);
					//cache.getExtractor( clazz, lab.label_element(), clazz.getClassLoader() );
					class_schema.putExtractor(f_refName, m_extractor);
					if (!MULTIPLE_TARGETS)
						class_schema.clearTargets();
					class_schema.addTarget(f_refName);
					TARGET_FOUND = true;
					if (slog.warn() != null)
						slog.warn().log("!!!!!:processClassLabel: TARGET FOUND "+f_refName+" "+class_label.label_element() + ")\n");
					
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

	public boolean isTargetFound() {
		// TODO Auto-generated method stub
		return TARGET_FOUND;
	}	

}
