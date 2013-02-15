package com.drools.memory.banchmark.model;

/**
 * @author Victor
 */
public class ConfigData {
    private String id;
    private int rate;

    public void setId(String id) {
        this.id = id;

    }

    public void setRate(int rate) {
       this.rate = rate;
    }

    public int getRate() {
        return rate;
    }

    public String getId() {
        return id;
    }
}
