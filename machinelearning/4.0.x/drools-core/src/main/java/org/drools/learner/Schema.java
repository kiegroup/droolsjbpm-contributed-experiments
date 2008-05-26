package org.drools.learner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.drools.base.ClassFieldExtractor;
import org.drools.base.ClassFieldExtractorCache;
import org.drools.learner.tools.FeatureNotSupported;
import org.drools.learner.tools.FieldAnnotation;
import org.drools.learner.tools.Util;

/*
 * A description of a data set's attributes and their properties.
 */
public class Schema {

	public static Schema createFromClass(Class<?> clazz, int domain_type) throws FeatureNotSupported {
		Schema schema = new Schema(clazz);
		ClassFieldExtractorCache cache = ClassFieldExtractorCache.getInstance();
		
		ArrayList<Field> element_fields = new ArrayList<Field>();
		Util.getAllFields(clazz, element_fields); 
			//clazz.getDeclaredFields(); //clazz.getFields();
		for (Field f: element_fields) {
			String f_name = f.getName();
			ClassFieldExtractor f_extractor = cache.getExtractor( clazz, f_name, clazz.getClassLoader() );
			schema.extractorMap.put(f_name, f_extractor);
			//f_extractor.
			
			int f_type = 0; 
			if (f.getType().isPrimitive() || f.getType() == String.class) {
				
				f_type = 1;
			} else {
				f_type = 2;
			}

			Annotation[] annotations = f.getAnnotations();
			FieldAnnotation spec = null;
			for (Annotation a : annotations) {
				if (a instanceof FieldAnnotation) {
					spec = (FieldAnnotation)a; // here it is !!!
					break;
				}
			}
			
			if (spec != null) {
				if (!spec.ignore() && f.getType() == String.class && !spec.discrete()) {
					
					throw new FeatureNotSupported("String categorization not supported");
				}
				boolean skip = false;
				switch (domain_type) {
				case Util.ID3:	//ID3
					if (spec.ignore() || !spec.discrete())
						skip = true;
					break;
				case Util.C45:
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
			else if (f.getType() == String.class)	{	/* BY DEFAULT it is categorical*/}
			
			/* TODO insanity checks:
			 * 1- the annotations are not given
			 * 2- there is no target specified
			 * 3- more than one target is specified
			 */
			schema.domainMap.put(f_name, fieldDomain);			
		}
		
		return schema;
	}
	
	// the owner class of the schema
	private Class<?> klass;
	
	// key: field name
	private Hashtable<String, ClassFieldExtractor> extractorMap;

	// key: field name
	private Hashtable<String, Domain> domainMap;

	private HashSet<String> targets;
	
	public Schema(Class<?> _klass) {
		this.klass = _klass;
		this.extractorMap = new Hashtable<String, ClassFieldExtractor>();
		this.domainMap = new Hashtable<String, Domain>();
		this.targets = new HashSet<String>();
	}
	
	public Class<?> getObjectClass() {
		return klass;
	}

	public boolean addTarget(String _target) {
		return targets.add(_target);
	}
	
	public Set<String> getAttrNames() {
		return this.domainMap.keySet();
	}
	
	public Domain getAttrDomain(String attr_name) {
		return this.domainMap.get(attr_name);
	}
	
	public ClassFieldExtractor getAttrExtractor(String attr_name) {
		return this.extractorMap.get(attr_name);
	}
	
	public Collection<String> getTargets() {
		return targets;
	}

	
	
}
