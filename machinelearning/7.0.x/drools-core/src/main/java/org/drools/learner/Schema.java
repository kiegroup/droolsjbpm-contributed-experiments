package org.drools.learner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.drools.core.spi.ReadAccessor;
import org.drools.learner.builder.Learner.DataType;
import org.drools.learner.builder.Learner.DomainAlgo;
import org.drools.learner.tools.ClassStructure;
import org.drools.learner.tools.ClassVisitor;
import org.drools.learner.tools.FeatureNotSupported;

public class Schema {
	
	public static Schema createSchemaStructure(Class<?> clazz, DomainAlgo domain_type, DataType data_type) throws Exception {
		Schema schema = new Schema(clazz);
		//�ClassFieldExtractorCache cache = ClassFieldExtractorCache.getInstance();

		// schema is modified
		ClassVisitor visitor = new ClassVisitor(schema, domain_type, data_type);
		visitor.visit();
		
		
		/* TODO insanity checks:
		 * 1- the annotations are not given
		 * 2- there is no target specified
		 * 3- more than one target is specified
		 */	
		if (!visitor.isTargetFound()) {
			throw new Exception("Target Not Found, please annotate the target field");
		} else if (schema.getTargets().size()>1) {
			//System.out.println("More ");
			throw new FeatureNotSupported("More than one target specified, feature not supported yet ");
		}
		return schema;
	}
	// the owner class of the schema
	private Class<?> klass;
	
	// the structure of the owner class
	private HashMap<Class<?>,ClassStructure> klassStructure;
	
	// key: Object_class_name@field_name
	private HashMap<String, ReadAccessor> extractorMap;

	// key: Object_class_name@field_name
	private HashMap<String, Domain> domainMap;
	
	// key: Object_class_name@field_name
	private HashMap<String, ArrayList<Field>> attrRelationMap;
	

	private HashSet<String> targets;
	
	public Schema(Class<?> _klass) {
		this.klass = _klass;
		
		this.klassStructure = new HashMap<Class<?>,ClassStructure>();
		this.extractorMap = new HashMap<String, ReadAccessor>();
		this.domainMap = new HashMap<String, Domain>();
		this.attrRelationMap = new HashMap<String, ArrayList<Field>>();
		this.targets = new HashSet<String>();
	}
	
	public Class<?> getObjectClass() {
		return klass;
	}
	
	public HashMap<Class<?>,ClassStructure> getClassStructure() {
		return klassStructure;
	}
	public void putStructure(Class<?> clazz, ClassStructure structure) {
		klassStructure.put(clazz, structure);;
	}
	
	public void addParentField(String attr_name, Field klass) {
		if (!this.attrRelationMap.containsKey(attr_name)) {
			this.attrRelationMap.put(attr_name, new ArrayList<Field>());
		}
		this.attrRelationMap.get(attr_name).add(klass);
	}
	
	public HashMap<String, ArrayList<Field>> getAttrRelationMap() {
		return attrRelationMap;
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
	
	public void putExtractor(String attr_name, ReadAccessor extract) {
		extractorMap.put(attr_name, extract);
	}
	public ReadAccessor getAttrExtractor(String attr_name) {
		return this.extractorMap.get(attr_name);
	}
	
	public Collection<String> getTargets() {
		return targets;
	}



}
