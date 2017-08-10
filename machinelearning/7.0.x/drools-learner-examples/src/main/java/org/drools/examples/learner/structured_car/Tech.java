package org.drools.examples.learner.structured_car;

import org.drools.learner.tools.FieldAnnotation;

public class Tech {

    private Comfort comfort;
    @FieldAnnotation(readingSeq = 5)
    private String  safety;        //"low", "med", "high"

    public Tech() {

    }

    public Comfort getComfort() {
        return comfort;
    }

    public void setComfort(Comfort comfort) {
        this.comfort = comfort;
    }

    public String getSafety() {
        return safety;
    }

    public void setSafety(String safety) {
        this.safety = safety;
    }
}
