package org.drools.examples.learner;

import org.drools.learner.tools.FieldAnnotation;

//@ClassAnnotation(label_element = "getLabel2")
public class Car {

    @FieldAnnotation(readingSeq = 0)
    private String buying;        //"vhigh", "high", "med", "low"
    @FieldAnnotation(readingSeq = 1)
    private String maint;        //"vhigh", "high", "med", "low"
    @FieldAnnotation(readingSeq = 2)
    private String doors;        //"2", "3", "4", "5more"
    @FieldAnnotation(readingSeq = 3)
    private String persons;        //"2", "4", "more"
    @FieldAnnotation(readingSeq = 4)
    private String lug_boot;    //"small", "med", "big"
    @FieldAnnotation(readingSeq = 5)
    private String safety;        //"low", "med", "high"
    @FieldAnnotation(readingSeq = 6, target = true)
    private String target;        //"unacc", "acc", "good", "vgood"

    public Car() {

    }

    public boolean getLabel2() {
        return (doors.equalsIgnoreCase("5more") && safety.equalsIgnoreCase("med") && buying.equalsIgnoreCase("low"));
    }

    public String getBuying() {
        return buying;
    }

    public void setBuying(String buying) {
        this.buying = buying;
    }

    public String getMaint() {
        return maint;
    }

    public void setMaint(String maint) {
        this.maint = maint;
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

    public String getSafety() {
        return safety;
    }

    public void setSafety(String safety) {
        this.safety = safety;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String carClass) {
        this.target = carClass;
    }

    public boolean getLabel() {
        return (doors.equalsIgnoreCase("5more"));
    }

    public String toString() {
        String out = "Car(buy:" + getBuying() +
                " doors:" + getDoors() +
                " lug_boot:" + getLug_boot() +
                " maint:" + getMaint() +
                " persons:" + getPersons() +
                " safety:" + getSafety() +
                " target:" + getTarget();
        return out;
    }
}