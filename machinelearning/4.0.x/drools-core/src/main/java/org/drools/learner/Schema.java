package org.drools.learner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.drools.base.ClassFieldExtractor;
import org.drools.base.ClassFieldExtractorCache;
import org.drools.learner.builder.Learner.DomainAlgo;
import org.drools.learner.tools.ClassAnnotation;
import org.drools.learner.tools.FeatureNotSupported;
import org.drools.learner.tools.FieldAnnotation;
import org.drools.learner.tools.PseudoFieldExtractor;
import org.drools.learner.tools.Util;
import org.drools.spi.Extractor;

/*
 * A description of a data set's attributes and their properties.
 */
public class Schema {

	public static Schema createFromClass(Class<?> clazz, DomainAlgo domain_type) throws FeatureNotSupported {
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
		Util.getAllFields(clazz, element_fields); 	//clazz.getDeclaredFields(); //clazz.getFields();
		for (Field f: element_fields) {
			String f_name = f.getName();
			ClassFieldExtractor f_extractor = cache.getExtractor( clazz, f_name, clazz.getClassLoader() );
			schema.extractorMap.put(f_name, f_extractor);

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
				case ID3:	//ID3
					if (spec.ignore() || !spec.discrete())
						skip = true;
					break;
				case C45:
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
		/* Apperantly the getMethod function recurse on the superclasses
		 * i dont need to recurse myself
		for (Class<?> c: element_classes) {
			Annotation[] annotations = c.getAnnotations();
		*/
			Annotation[] annotations = clazz.getAnnotations();	// it should get the inherited annotations 
			ClassAnnotation lab = null;
			for (Annotation a : annotations) {
				if (a instanceof ClassAnnotation) {
					lab= (ClassAnnotation)a; // here it is !!!
					break;
				}
			}
			
			if (lab != null && lab.label_element() != "") {
				// the targetting label is set, put the function that gets that value somewhere
				
				try {
					/* Apperantly the getMethod function recurse on the superclasses
					 * i dont need to recurse myself
					Method m =c.getDeclaredMethod(lab.label_element(), null);
					*/
					Method m = clazz.getMethod(lab.label_element(), null);
					
					Domain fieldDomain = new Domain(lab.label_element(), m.getReturnType());
					fieldDomain.setArtificial(true);
					if (m.getReturnType() == Boolean.TYPE || m.getReturnType() == Boolean.class)	{	/* set discrete*/
//						fieldDomain.setCategorical(true);	// BY DEFAULT it is categorical
						fieldDomain.addCategory(Boolean.TRUE);
						fieldDomain.addCategory(Boolean.FALSE);
						fieldDomain.setFixed(true);					
					}
					//else if (m.getReturnType() == String.class)	{	/* BY DEFAULT it is categorical*/}
					schema.domainMap.put(lab.label_element(), fieldDomain);
					
					Extractor m_extractor = new PseudoFieldExtractor(clazz, m);
						//cache.getExtractor( clazz, lab.label_element(), clazz.getClassLoader() );
					schema.extractorMap.put(lab.label_element(), m_extractor);
					schema.clearTargets();
					schema.addTarget(lab.label_element());
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
	
	// the owner class of the schema
	private Class<?> klass;
	
	// key: field name
	private Hashtable<String, Extractor> extractorMap;

	// key: field name
	private Hashtable<String, Domain> domainMap;

	private HashSet<String> targets;
	
	public Schema(Class<?> _klass) {
		this.klass = _klass;
		this.extractorMap = new Hashtable<String, Extractor>();
		this.domainMap = new Hashtable<String, Domain>();
		this.targets = new HashSet<String>();
	}
	
	public Class<?> getObjectClass() {
		return klass;
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
	
	public Domain getAttrDomain(String attr_name) {
		return this.domainMap.get(attr_name);
	}
	
	public Extractor getAttrExtractor(String attr_name) {
		return this.extractorMap.get(attr_name);
	}
	
	public Collection<String> getTargets() {
		return targets;
	}

	
	
}
