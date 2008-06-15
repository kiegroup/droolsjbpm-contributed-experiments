package org.drools.learner.tools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.drools.base.ValueType;
import org.drools.common.InternalWorkingMemory;
import org.drools.spi.Extractor;

public class PseudoFieldExtractor implements Extractor{
	
	//private String methodName;
	private Method method;
    //private Class clazz;

	public PseudoFieldExtractor(Class<?> _clazz, Method m) {
		method = m;
		//clazz = _clazz;
	}

	public boolean getBooleanValue(InternalWorkingMemory workingMemory,
			Object object) {
		// TODO Auto-generated method stub
		return false;
	}

	public byte getByteValue(InternalWorkingMemory workingMemory, Object object) {
		// TODO Auto-generated method stub
		return 0;
	}

	public char getCharValue(InternalWorkingMemory workingMemory, Object object) {
		// TODO Auto-generated method stub
		return 0;
	}

	public double getDoubleValue(InternalWorkingMemory workingMemory,
			Object object) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Class getExtractToClass() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getExtractToClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	public float getFloatValue(InternalWorkingMemory workingMemory,
			Object object) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getHashCode(InternalWorkingMemory workingMemory, Object object) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getIntValue(InternalWorkingMemory workingMemory, Object object) {
		// TODO Auto-generated method stub
		return 0;
	}

	public long getLongValue(InternalWorkingMemory workingMemory, Object object) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Method getNativeReadMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	public short getShortValue(InternalWorkingMemory workingMemory,
			Object object) {
		// TODO Auto-generated method stub
		return 0;
	}

	public Object getValue(InternalWorkingMemory workingMemory, Object object) {
		try {
			return method.invoke(object, null);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public ValueType getValueType() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isGlobal() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isNullValue(InternalWorkingMemory workingMemory,
			Object object) {
		// TODO Auto-generated method stub
		return false;
	}

}
