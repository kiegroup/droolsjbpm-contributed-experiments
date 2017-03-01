package org.drools.learner;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.drools.core.WorkingMemory;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.learner.builder.Learner.DataType;
import org.drools.learner.tools.ClassStructure;
import org.drools.learner.tools.LoggerFactory;
import org.drools.learner.tools.SimpleLogger;
import org.drools.learner.tools.Util;
import org.drools.core.spi.ReadAccessor;

public class InstanceFactory {

	private static SimpleLogger flog = LoggerFactory.getUniqueFileLogger(InstanceFactory.class, SimpleLogger.DEFAULT_LEVEL);
	private static SimpleLogger slog = LoggerFactory.getSysOutLogger(InstanceFactory.class, SimpleLogger.DEFAULT_LEVEL);
	
	private WorkingMemory session;
	private Schema schema;
	public InstanceFactory(WorkingMemory _session, Schema _schema) {
		session = _session;
		schema = _schema;
	}
	
	public Instance createInstance(Object _obj) {
		try {
			//if (schema.getClassStructure().containsKey(_obj.getClass())) {
			if (schema.getObjectClass().isAssignableFrom(_obj.getClass())) {
				Instance i= new Instance();
				boolean bok = instantiateAttributes(i, _obj, _obj.getClass());
				if (!bok) {
					if (slog.error() != null)
						slog.error().log("What is going on, how come it is wrong : ");
					System.exit(0);
				}
				return i; // perfectly instantiated
			} else 
				return null;// could not instantiate
			
//			if (instantiateAttributes(i, _obj, _obj.getClass()))
//				return i;	// perfectly instantiated
//			else
//				return null;// could not instantiate
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public boolean instantiateAttributes(Instance inst, Object _obj, Class<?> klass) throws Exception {

		if (slog.info() != null)
			slog.info().log("Klass : "+ klass);
		ClassStructure struct = schema.getClassStructure().get(klass);
		if (slog.info() != null)
			slog.info().log("\t ClassStructure : "+ struct + "\n");
		if (struct == null)
			return false;
		
		if (struct.hasLabel()) {// it has a method or label	
			if (slog.debug() != null)
				slog.debug().log(":instantiateAttributes:structure has a label ");
			for (Method m : struct.getMethods()) {
				String m_name = m.getName();
//				if (m_name.startsWith("get")) {//TODO check
//					m_name = m_name.substring(3).toLowerCase();
//				}
				
				DataType m_type = DataType.PRIMITIVE;// must be primitive
				if (slog.debug() != null)
					slog.debug().log("method= "+m_name + " m_type="+m_type+ "\n");
				getAttributeValue(inst, _obj, m_name, m_type);
			}
		}
		// get the fields declared in the class
		for (Field f : struct.getFields()) {
			String f_name = f.getName();
			DataType f_type = struct.getFieldType(f);
			getAttributeValue(inst, _obj, f_name, f_type);
		}
		if (!struct.getParent().equals(Object.class))
			return instantiateAttributes(inst, _obj, struct.getParent());
		
		return true;
	}
	
	public void getAttributeValue(Instance inst, Object _obj, String name, DataType type) throws Exception {
		Class<?> _obj_klass = _obj.getClass();
		String fReference = Util.getFReference(_obj_klass, name);
		switch (type) {
		case PRIMITIVE:		// domain exist only for primitive types
			Domain fieldDomain = schema.getAttrDomain(fReference);		
			ReadAccessor f_extractor = schema.getAttrExtractor(fReference);
			if (slog.info() != null)
				slog.info().log("Field name : "+ name+ " of the object "+ _obj + " the extract:"+f_extractor+"\n");
			
			//Object f_value = f_extractor.getValue( (InternalWorkingMemory) session, _obj);
			Object f_value = f_extractor.getValue( _obj);
			if (f_value != null) {
				try {
					if (fieldDomain.isPossible(f_value)) {
						fieldDomain.addCategory(f_value);
						inst.setAttr(fReference, f_value);
					}
				} catch (Exception e) {
					if (slog.debug() != null)
						slog.debug().log("Domain: "+fieldDomain+ " could not add the value "+ f_value+ "\n");
					e.printStackTrace();
				}
			} else {
				if (slog.debug() != null)
					slog.debug().log("Field name : "+ name+ " of the object "+ _obj + " the extract:"+f_extractor+"\n");
				if (slog.debug() != null)
					slog.debug().log("The attribute value cannot be read, check the attribute\n");
				System.exit(0);
			}
			break;
		case STRUCTURED:	// the extractor exists for both types of data.
			ReadAccessor complex_f_extractor = schema.getAttrExtractor(fReference);	// this returns an object to me
			//Object obj_value = complex_f_extractor.getValue( (InternalWorkingMemory) session, _obj);
			Object obj_value = complex_f_extractor.getValue(_obj);
			instantiateAttributes(inst, obj_value, obj_value.getClass());
			
			break;
		// case Collection
		default:
			throw new Exception("What type of data is this");
		
		}

		return;
	}



}
