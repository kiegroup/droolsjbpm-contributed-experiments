package org.drools.examples.learner.structured_nursery;

import org.drools.learner.tools.FieldAnnotation;

public class Employ {

    @FieldAnnotation(readingSeq = 0)
    private String parents;    //"usual","pretentious", "great_pret"
    @FieldAnnotation(readingSeq = 1)
    private String has_nurs;    //"proper","less_proper", "improper", "critical", "very_crit"

    public Employ() {

    }

    public String getParents() {
        return parents;
    }

    public void setParents(String parents) {
        this.parents = parents;
    }

    public String getHas_nurs() {
        return has_nurs;
    }

    public void setHas_nurs(String has_nurs) {
        this.has_nurs = has_nurs;
    }
}
