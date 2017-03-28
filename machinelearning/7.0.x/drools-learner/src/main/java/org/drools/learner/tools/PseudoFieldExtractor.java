package org.drools.learner.tools;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.drools.core.base.ValueType;
import org.drools.core.spi.ReadAccessor;

public class PseudoFieldExtractor implements ReadAccessor {

    //private String methodName;
    private Method method;
    //private Class clazz;

    public PseudoFieldExtractor( Class<?> clazz, Method m ) {
        super();
        method = m;
        //clazz = _clazz;
    }

    public boolean getBooleanValue( Object object ) {
        // TODO Auto-generated method stub
        return false;
    }

    public byte getByteValue( Object object ) {
        // TODO Auto-generated method stub
        return 0;
    }

    public char getCharValue( Object object ) {
        // TODO Auto-generated method stub
        return 0;
    }

    public double getDoubleValue( Object object ) {
        // TODO Auto-generated method stub
        return 0;
    }

    public Class<?> getExtractToClass() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getExtractToClassName() {
        // TODO Auto-generated method stub
        return null;
    }

    public float getFloatValue( Object object ) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getHashCode( Object object ) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getIndex() {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getIntValue( Object object ) {
        // TODO Auto-generated method stub
        return 0;
    }

    public long getLongValue( Object object ) {
        // TODO Auto-generated method stub
        return 0;
    }

    public Method getNativeReadMethod() {
        // TODO Auto-generated method stub
        return null;
    }

    public short getShortValue( Object object ) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* DONT FORGET THAT */
    public Object getValue( Object object ) {
        try {
            return method.invoke( object, null );
        } catch ( IllegalArgumentException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( IllegalAccessException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( InvocationTargetException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public ValueType getValueType() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isNullValue( Object object ) {
        // TODO Auto-generated method stub
        return false;
    }

    public void readExternal( ObjectInput in ) throws IOException, ClassNotFoundException {
        // TODO Auto-generated method stub

    }

    public void writeExternal( ObjectOutput out ) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public BigDecimal getBigDecimalValue( Object object ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BigInteger getBigIntegerValue( Object object ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getNativeReadMethodName() {
        // TODO Auto-generated method stub
        return null;
    }
}
