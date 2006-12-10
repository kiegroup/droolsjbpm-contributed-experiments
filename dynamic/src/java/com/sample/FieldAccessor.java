package com.sample;

/**
 * An interface for dynamically generated FieldAccessor classes </p>
 *
 * @author etirelli
 */
public interface FieldAccessor {
    
    /**
     * Sets the attribute value in the given instance
     * 
     * @param instance
     * @param value
     */
    public void setValue(Object instance, Object value);
    
    /**
     * Gets the attribute value in the given instance
     * 
     * @param instance
     * @return
     */
    public Object getValue(Object instance);

}
