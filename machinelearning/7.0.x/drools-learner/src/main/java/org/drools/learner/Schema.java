package org.drools.learner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.drools.core.spi.ReadAccessor;
import org.drools.learner.builder.Learner.DataType;
import org.drools.learner.tools.ClassStructure;
import org.drools.learner.tools.ClassVisitor;
import org.drools.learner.tools.FeatureNotSupported;

public class Schema {

    // the owner class of the schema
    private Class<?> klass;
    // the structure of the owner class
    private HashMap<Class<?>, ClassStructure> klassStructure;
    // key: Object_class_name@field_name
    private HashMap<String, ReadAccessor> extractorMap;
    // key: Object_class_name@field_name
    private HashMap<String, Domain> domainMap;
    // key: Object_class_name@field_name
    private HashMap<String, ArrayList<Field>> attrRelationMap;
    private HashSet<String> targets;

    public Schema(Class<?> klass) {
        this.klass = klass;

        this.klassStructure = new HashMap<Class<?>, ClassStructure>();
        this.extractorMap = new HashMap<String, ReadAccessor>();
        this.domainMap = new HashMap<String, Domain>();
        this.attrRelationMap = new HashMap<String, ArrayList<Field>>();
        this.targets = new HashSet<String>();
    }

    public static Schema createSchemaStructure(Class<?> clazz,
                                               DataType dataType) {
        Schema schema = new Schema(clazz);
        //�ClassFieldExtractorCache cache = ClassFieldExtractorCache.getInstance();

        // schema is modified
        ClassVisitor visitor = new ClassVisitor(schema, dataType);
        visitor.visit();

        /*
         * TODO insanity checks: 1- the annotations are not given 2- there is no
         * target specified 3- more than one target is specified
         */
        if (!visitor.isTargetFound()) {
            throw new RuntimeException("Target Not Found, please annotate the target field");
        } else if (schema.getTargets().size() > 1) {
            //System.out.println("More ");
            throw new RuntimeException("More than one target specified, feature not supported yet ");
        }
        return schema;
    }

    public Class<?> getObjectClass() {
        return klass;
    }

    public HashMap<Class<?>, ClassStructure> getClassStructure() {
        return klassStructure;
    }

    public void putStructure(Class<?> clazz, ClassStructure structure) {
        klassStructure.put(clazz, structure);
        ;
    }

    public void addParentField(String attrName, Field klass) {
        if (!this.attrRelationMap.containsKey(attrName)) {
            this.attrRelationMap.put(attrName, new ArrayList<Field>());
        }
        this.attrRelationMap.get(attrName).add(klass);
    }

    public HashMap<String, ArrayList<Field>> getAttrRelationMap() {
        return attrRelationMap;
    }

    public boolean addTarget(String target) {
        return targets.add(target);
    }

    public void clearTargets() {
        targets.clear();
    }

    public Set<String> getAttrNames() {
        return this.domainMap.keySet();
    }

    public void putDomain(String attrName, Domain d) {
        domainMap.put(attrName, d);
    }

    public Domain getAttrDomain(String attrName) {
        return this.domainMap.get(attrName);
    }

    public void putExtractor(String attrName, ReadAccessor extract) {
        extractorMap.put(attrName, extract);
    }

    public ReadAccessor getAttrExtractor(String attrName) {
        return this.extractorMap.get(attrName);
    }

    public Collection<String> getTargets() {
        return targets;
    }
}
