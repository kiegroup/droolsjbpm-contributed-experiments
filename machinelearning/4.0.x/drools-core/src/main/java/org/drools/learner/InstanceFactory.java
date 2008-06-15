package org.drools.learner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.drools.WorkingMemory;
import org.drools.common.InternalWorkingMemory;
import org.drools.learner.builder.Learner.DataType;
import org.drools.learner.tools.ClassStructure;
import org.drools.spi.Extractor;

public class InstanceFactory {

	private WorkingMemory session;
	private Schema schema;
	public InstanceFactory(WorkingMemory _session, Schema _schema) {
		session = _session;
		schema = _schema;
	}
	
	public Instance createInstance(Object _obj) {
		try {
			Instance i= new Instance();
			instantiateAttributes(i, _obj, _obj.getClass());
			return i;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void instantiateAttributes(Instance inst, Object _obj, Class<?> klass) throws Exception {
		if (klass.equals(Object.class))
			return;
		
		ClassStructure struct = schema.getClassStructure().get(klass);
		if (struct.hasLabel()) {// it has a method or label		
			for (Method m : struct.getMethods()) {
				String m_name = m.getName();
				DataType m_type = DataType.PRIMITIVE;// must be primitive
				getAttributeValue(inst, _obj, m_name, m_type);
			}
		}
		// get the fields declared in the class
		for (Field f : struct.getFields()) {
			String f_name = f.getName();
			DataType f_type = struct.getFieldType(f);
			getAttributeValue(inst, _obj, f_name, f_type);
		}

		instantiateAttributes(inst, _obj, struct.getParent());
		
		return;
	}
	
	public void getAttributeValue(Instance inst, Object _obj, String name, DataType type) throws Exception {
		switch (type) {
		case PRIMITIVE:		// domain exist only for primitive types
			Domain fieldDomain = schema.getAttrDomain(name);
			Extractor f_extractor = schema.getAttrExtractor(name);
			Object f_value = f_extractor.getValue( (InternalWorkingMemory) session, _obj);
			try {
				if (fieldDomain.isPossible(f_value)) {
					fieldDomain.addCategory(f_value);
					inst.setAttr(name, f_value);
				}
			} catch (Exception e) {
				System.out.println("Domain: "+fieldDomain+ " could not add the value "+ f_value);
				e.printStackTrace();
			}
			break;
		case STRUCTURED:	// the extractor exists for both types of data.
			Extractor complex_f_extractor = schema.getAttrExtractor(name);	// this returns an object to me
			Object obj_value = complex_f_extractor.getValue( (InternalWorkingMemory) session, _obj);
			instantiateAttributes(inst, obj_value, obj_value.getClass());
			
			break;
		// case Collection
		default:
			throw new Exception("What type of data is this");
		
		}

		return;
	}



}
