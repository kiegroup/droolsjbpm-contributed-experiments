package org.drools.examples.learner.structured_car;

import org.drools.learner.tools.FieldAnnotation;

public class Comfort {

    @FieldAnnotation(readingSeq = 2)
    private String doors;        //"2", "3", "4", "5more"
    @FieldAnnotation(readingSeq = 3)
    private String persons;        //"2", "4", "more"
    @FieldAnnotation(readingSeq = 4)
    private String lug_boot;    //"small", "med", "big"

    public Comfort() {

    }

    public String getDoors() {
        return doors;
    }

    public void setDoors(String doors) {
        this.doors = doors;
    }

    public String getPersons() {
        return persons;
    }

    public void setPersons(String persons) {
        this.persons = persons;
    }

    public String getLug_boot() {
        return lug_boot;
    }

    public void setLug_boot(String lug_boot) {
        this.lug_boot = lug_boot;
    }
}
