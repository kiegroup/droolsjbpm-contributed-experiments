package org.drools.learner.tools;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Stack;

import org.drools.core.base.ClassFieldAccessorCache;
import org.drools.core.base.ClassFieldAccessorStore;
import org.drools.core.spi.ReadAccessor;
import org.drools.learner.Domain;
import org.drools.learner.Schema;
import org.drools.learner.builder.Learner;
import org.drools.learner.builder.Learner.DataType;
import org.drools.learner.eval.CrossValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassVisitor {

    protected static final transient Logger                  log        = LoggerFactory.getLogger(CrossValidation.class);
    private static               ClassFieldAccessorCache cache      = new ClassFieldAccessorCache(Thread.currentThread().getContextClassLoader());
    //private static ClassFieldExtractorCache cache = ClassFieldExtractorCache.getInstance();
    private                      ClassFieldAccessorStore store      = new ClassFieldAccessorStore();
    private                      DataType                dataType   = Learner.DEFAULT_DATA;

    private Schema       classSchema;
    private Stack<Field> classRelation; // for traversing to child objects

    //@mireynol - need to change these from contstant style to camel case
    private boolean targetFound;
    private boolean multpleTargets;

    public ClassVisitor() {
        store.setClassFieldAccessorCache(cache);
    }

    public ClassVisitor(DataType dataType) {
        this(null, dataType);
    }

    public ClassVisitor(Schema classSchema, DataType dataType) {
        this();
        this.classSchema = classSchema;
        this.dataType = dataType;
    }

    public void visit() {
        //		this.all_klasses = new HashMap<Class<?>,ClassStructure>();
        classRelation = new Stack<Field>();
        getStructuredSuperFields(classSchema.getObjectClass()/* , null */);

        return;
    }

    public HashMap<Class<?>, ClassStructure> getStructure() {
        return classSchema.getClassStructure();
    }

    public void getStructuredSuperFields(
        Class<?> clazz/* , Class<?> owner_clazz */) {
        if (log.isDebugEnabled()) {
            log.debug("On the class " + clazz + " the structure exists " + classSchema.getClassStructure().containsKey(clazz));
        }
        if (classSchema.getClassStructure().containsKey(clazz) && classSchema.getClassStructure().get(clazz).isDone()) {
            return;
        }
        // process if the parent_klass.equals(Object.class) ?????
        ClassStructure structure = new ClassStructure(clazz);
        if (multpleTargets || !targetFound) {
            processClassLabel(structure);
        }
        classSchema.putStructure(clazz, structure);

        // get the fields declared in the class
        Field[] elementFields = clazz.getDeclaredFields(); //clazz.getFields();
        for (Field f : elementFields) {
            try {
                decomposeField(structure, f /* , owner_clazz */);
            } catch (FeatureNotSupported e) {
                if (log.isErrorEnabled()) {
                    log.error("FeatureNotSupported " + e + ")\n");
                }
            }
        }
        structure.setDone();
        Class<?> parentKlass = clazz.getSuperclass();
        structure.setParent(parentKlass);
        if (parentKlass.equals(Object.class)) {
            return;
        }
        getStructuredSuperFields(parentKlass);

        return;
    }

    public void decomposeField(ClassStructure structure, Field field) throws FeatureNotSupported {

        //		if (log.warn() != null)
        //			log.warn().log("!!!!!!!!!The field "+f+"  " +f.getName()+ "\n");
        // can get the field annotation
        // if it is ignored do not do anything
        FieldAnnotation fieldSpec = Util.getFieldAnnotations(field);

        boolean skip        = false;
        boolean ignoreField = false;
        // FIXME, surely this should error if ID3 gets quantitive domain mdp
        if (fieldSpec != null) {
            // the type of the fields that cannot be processed
            if (!fieldSpec.ignore() && field.getType() == String.class && !fieldSpec.discrete()) {
                throw new FeatureNotSupported("String categorization not supported");
            }
            if (fieldSpec.skip()) {
                skip = true;
            }
            ignoreField = fieldSpec.ignore();
            if (ignoreField) {
                skip = ignoreField;
            }
            // only if the annotations are given and the flag to ignore is set true 
            // then continue to next field

        }
        // if there is a getter?
        //if (Util.isGetter(m_name) & Util.isSimpleType(returns)) {

        if (!skip) {
            String   fieldName          = field.getName();
            Class<?> objClass           = structure.getOwnerClass();
            String   fieldReferenceName = Util.getFReference(objClass, fieldName);
            DataType dataType           = Util.getDataType(field.getType());

            ///////////
            if (dataType != DataType.COLLECTION) { // TODO
                if (log.isWarnEnabled()) {
                    log.warn("The field " + fieldName + " of the obj klass:" + objClass + " and the loader:" + objClass.getClassLoader() + "\n");
                }
                try {
                    //					if (f_name.startsWith("get")) {//TODO check
                    //						System.out.println(f_name.substring(3));
                    //						f_extractor = cache.getAccessor( _obj_klass, f_name.substring(3) , _obj_klass.getClassLoader() );
                    //					} else
                    //                    f_extractor = cache.getAccessor( _obj_klass, f_name, _obj_klass.getClassLoader() );

                    // @mireynol - original code
                    //                    ReadAccessor f_extractor;
                    //                    f_extractor = cache.getAccessor( _obj_klass, f_name , _obj_klass.getClassLoader() );
                    //                    class_schema.putExtractor(f_refName, f_extractor);

                    structure.addField(field, dataType);

                    ReadAccessor fieldExtractor = store.getReader(objClass, fieldName);
                    classSchema.putExtractor(fieldReferenceName, fieldExtractor);

                    switch (dataType) {
                        case PRIMITIVE: // domain will be created only for primitive types
                            Domain fieldDomain = new Domain(objClass, fieldName, field.getType());
                            //fieldDomain.setOwner(owner_clazz);
                            if (fieldSpec != null) {
                                fieldDomain.setCategorical(fieldSpec.discrete());
                                if ((multpleTargets || !targetFound) && fieldSpec.target()) {
                                    classSchema.addTarget(fieldReferenceName);
                                    targetFound = true;
                                }
                            }
                            Util.processDomain(fieldDomain, field.getType());
                            fieldDomain.ignore(ignoreField);
                            classSchema.putDomain(fieldReferenceName, fieldDomain);
                            for (Field parentKlass : classRelation) {
                                classSchema.addParentField(fieldReferenceName, parentKlass);
                            }
                            break;
                        case STRUCTURED: // the extractor is necessary for both types of data.	
                            classRelation.push(field);
                            getStructuredSuperFields(field.getType());//recurse on the structured
                            classRelation.pop();
                            break;
                        default:
                            //throw new Exception("What type of data is this");	
                    }
                } catch (RuntimeException e) {
                    if (log.isWarnEnabled()) {
                        log.warn("Exception e:" + e + " skip\n");
                    }
                    if (log.isWarnEnabled()) {
                        log.warn("Exception e:" + e + " skip");
                    }
                }
            } else { //case COLLECTION:
                if (log.isWarnEnabled()) {
                    log.warn("Case Collection(f_name " + fieldName + " What is the obj klass:" + objClass + ")\n");
                }
                throw new FeatureNotSupported("Can not deal with collections as an attribute");
            }
        }
        return;
    }

    /**
     * The ClassAnnotation has a label field, which refers to a method on the Class.
     * @param clazzStructure
     */
    public void processClassLabel(ClassStructure clazzStructure) {

        Class<?> clazz = clazzStructure.getOwnerClass();
         // use getDeclaredAnnotations() to not recurse super
        ClassAnnotation classLabel = Util.getDecClassAnnotations(clazz);
        if (classLabel != null && classLabel.labelElement() != "") {
            // the targetting label is set, put the function that gets that value somewhere

            try {
                /*
                 * Apperantly the java function "getMethod()" recurses on the
                 * superclasses i dont need to recurse myself Method m
                 * =c.getDeclaredMethod(lab.label_element(), null);
                 */
                Method m = clazz.getMethod(classLabel.labelElement(), null);
                if (Util.isSimpleType(m.getReturnType())) {
                    String mName = classLabel.labelElement();
                    //					if (mName.startsWith("get")) {//TODO check??
                    //						mName = mName..toLowerCase();
                    //					}
                    System.out.println(mName);
                    String fRefName    = Util.getFReference(clazz, mName); // class.name + "@" + label
                    Domain fieldDomain = new Domain(clazz, mName, m.getReturnType());
                    fieldDomain.setArtificial(true);
                    Class<?> methodClass = m.getReturnType();

                    Util.processDomain(fieldDomain, methodClass);
                    clazzStructure.addMethod(m);
                    classSchema.putDomain(fRefName, fieldDomain);

                    ReadAccessor mExtractor = new PseudoFieldExtractor(clazz, m);
                    //cache.getExtractor( clazz, lab.label_element(), clazz.getClassLoader() );
                    classSchema.putExtractor(fRefName, mExtractor);
                    if (!multpleTargets) {
                        classSchema.clearTargets();
                    }
                    classSchema.addTarget(fRefName);
                    targetFound = true;
                    if (log.isWarnEnabled()) {
                        log.warn("!!!!!:processClassLabel: TARGET FOUND " + fRefName + " " + classLabel.labelElement() + ")\n");
                    }

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
        return targetFound;
    }
}
