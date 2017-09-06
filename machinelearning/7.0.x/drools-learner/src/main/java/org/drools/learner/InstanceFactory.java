package org.drools.learner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.drools.core.spi.ReadAccessor;
import org.drools.learner.builder.Learner.DataType;
import org.drools.learner.tools.ClassStructure;
import org.drools.learner.tools.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstanceFactory {

    protected static final transient Logger log = LoggerFactory.getLogger(InstanceFactory.class);

    private Schema schema;

    public InstanceFactory(Schema schema) {
        this.schema = schema;
    }

    public Instance createInstance(Object obj) {
        if (schema.getObjectClass().isAssignableFrom(obj.getClass())) {
            Instance i   = new Instance();
            boolean  bok = instantiateAttributes(i, obj, obj.getClass());
            return i; // perfectly instantiated
        } else {
            return null;// could not instantiate
        }
    }

    public boolean instantiateAttributes(Instance inst, Object obj, Class<?> klass) {

        if (log.isInfoEnabled()) {
            log.info("Klass : " + klass);
        }
        ClassStructure struct = schema.getClassStructure().get(klass);
        if (log.isInfoEnabled()) {
            log.info("\t ClassStructure : " + struct + "\n");
        }
        if (struct == null) {
            return false;
        }

        if (struct.hasLabel()) {// it has a method or label
            if (log.isDebugEnabled()) {
                log.debug(":instantiateAttributes:structure has a label ");
            }
            for (Method m : struct.getMethods()) {
                String mName = m.getName();
                //				if (m_name.startsWith("get")) {//TODO check
                //					m_name = m_name.substring(3).toLowerCase();
                //				}

                DataType mType = DataType.PRIMITIVE;// must be primitive
                if (log.isDebugEnabled()) {
                    log.debug("method= " + mName + " m_type=" + mType + "\n");
                }
                getAttributeValue(inst, obj, mName, mType);
            }
        }
        // get the fields declared in the class
        for (Field f : struct.getFields()) {
            String   fName = f.getName();
            DataType fType = struct.getFieldType(f);
            getAttributeValue(inst, obj, fName, fType);
        }
        if (!struct.getParent().equals(Object.class)) {
            return instantiateAttributes(inst, obj, struct.getParent());
        }

        return true;
    }

    public void getAttributeValue(Instance inst, Object obj, String name, DataType type)  {
        Class<?> objKlass       = obj.getClass();
        String   fieldReference = Util.getFReference(objKlass, name);
        switch (type) {
            case PRIMITIVE: // domain exist only for primitive types
                Domain fieldDomain = schema.getAttrDomain(fieldReference);
                ReadAccessor fieldExtractor = schema.getAttrExtractor(fieldReference);
                if (log.isInfoEnabled()) {
                    log.info("Field name : " + name + " of the object " + obj + " the extract:" + fieldExtractor + "\n");
                }

                //Object f_value = f_extractor.getValue( (InternalWorkingMemory) session, _obj);
                Object fieldValue = fieldExtractor.getValue(obj);
                if (fieldValue != null) {
                    try {
                        if (fieldDomain.isPossible(fieldValue)) {
                            fieldDomain.addCategory(fieldValue);
                            inst.setAttr(fieldReference, fieldValue);
                        }
                    } catch (Exception e) {
                        if (log.isDebugEnabled()) {
                            log.debug("Domain: " + fieldDomain + " could not add the value " + fieldValue + "\n");
                        }
                        e.printStackTrace();
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Field name : " + name + " of the object " + obj + " the extract:" + fieldExtractor + "\n");
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("The attribute value cannot be read, check the attribute\n");
                    }
                    System.exit(0);
                }
                break;
            case STRUCTURED: // the extractor exists for both types of data.
                ReadAccessor complexFExtractor = schema.getAttrExtractor(fieldReference); // this returns an object to me
                //Object obj_value = complex_f_extractor.getValue( (InternalWorkingMemory) session, _obj);
                Object objValue = complexFExtractor.getValue(obj);
                instantiateAttributes(inst, objValue, objValue.getClass());

                break;
            // case Collection
            default:
                throw new RuntimeException("What type of data is this");
        }

        return;
    }
}
