/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */ 
package com.sample;

import java.io.Serializable;


public class Customer
     implements Serializable
{

    private static final long serialVersionUID = 1L;

    long    id;

	String 	name;
    Integer region;
    Integer age;
    Long    income;

    public Customer() {
    }
    
    public Customer(int id, String name, Integer region, Integer age, Long income) {
        this.id = id;
    	this.name = name;
    	this.region = region;
    	this.age = age;
    	this.income = income;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Integer getRegion() {
        return region;
    }
    public void setRegion(Integer region) {
        this.region = region;
    }
 
    public Integer getAge() {
        return age;
    }
    public void setAge(Integer age) {
        this.age = age;
    }

    public Long getIncome() {
        return income;
    }
    public void setIncome(Long income) {
        this.income = income;
    }

    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + (int) (id ^ (id >>> 32));
        return result;
    }

    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        final Customer other = (Customer) obj;
        if ( id != other.id ) return false;
        return true;
    }

}
