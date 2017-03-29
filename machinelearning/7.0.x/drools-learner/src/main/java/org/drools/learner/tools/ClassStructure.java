package org.drools.learner.tools;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.drools.learner.builder.Learner.DataType;

public class ClassStructure {
    private Class<?> klass, parentKlass;

    private ArrayList<Method> methods;

    //	private ArrayList<Field> fields;
    //	private ArrayList<DataType> field_types;
    private HashMap<Field, DataType> fieldStructure;

    private boolean done;

    public ClassStructure( Class<?> klass ) {
        this.klass = klass;
        done = false;
        fieldStructure = new HashMap<Field, DataType>();
        methods = new ArrayList<Method>();
    }

    public void setDone() {
        done = true;
    }

    public boolean isDone() {
        return done;
    }

    public void addField( Field f, DataType type ) {
        //		fields.add(f);
        //		field_types.add(type);
        fieldStructure.put( f, type );
    }

    public Collection<Field> getFields() {
        //return fields.iterator();
        return fieldStructure.keySet();
    }

    public DataType getFieldType( Field f ) {
        return fieldStructure.get( f );
    }

    public boolean hasLabel() {
        return methods.size() > 0;
    }

    public Collection<Method> getMethods() {
        return methods;
    }

    public void addMethod( Method m ) {
        methods.add( m );
    }

    public Class<?> getOwnerClass() {
        return klass;
    }

    public Class<?> getParent() {
        return parentKlass;
    }

    public void setParent( Class<?> parent ) {
        parentKlass = parent;
    }

}
