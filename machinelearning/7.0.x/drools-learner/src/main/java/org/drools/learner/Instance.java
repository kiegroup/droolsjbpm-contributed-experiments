package org.drools.learner;

import java.util.HashMap;

public class Instance {
    private HashMap<String, Attribute> attributes;
    private double weight = 1.0;

    public Instance() {
        this.attributes = new HashMap<String, Attribute>(); // TODO should i set a size, HOW?
    }

    public void setWeight( double w ) {
        weight = w;
    }

    public double getWeight() {
        return weight;
    }

    // name : _obj_klass+"@"+attr_name
    // value : its value
    public void setAttr( String name, Object value ) {
        Attribute fAttr = new Attribute();
        fAttr.setName( name );
        fAttr.setValue( value );
        this.attributes.put( name, fAttr );
    }

    public Attribute getAttr( String fieldName ) {
        return attributes.get( fieldName );
    }

    public Object getAttrValue( String fieldName ) {
        return attributes.get( fieldName ).getValue();
    }

    public int hashCode() {
        return attributes.hashCode();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer( this.hashCode() + " " );
        for ( String key : attributes.keySet() ) {
            sb.append( key + "=" + attributes.get( key ) + ", " );
        }
        return sb.toString();
    }

}
