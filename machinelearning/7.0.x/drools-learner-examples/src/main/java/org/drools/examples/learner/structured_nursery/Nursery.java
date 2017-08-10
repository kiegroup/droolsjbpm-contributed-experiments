package org.drools.examples.learner.structured_nursery;

import org.drools.learner.tools.FieldAnnotation;

public class Nursery {

    @FieldAnnotation(readingSeq = 8, target = true)
    private String classnursery; //"not_recom", "recommend", "very_recom", "priority","spec_prior"

    private Employ      employ;
    private StructFinan structFinance;
    private SocHealth   soc_health;

    public Nursery() {

    }

    public String getClassnursery() {
        return classnursery;
    }

    public void setClassnursery(String classnursery) {
        this.classnursery = classnursery;
    }

    public Employ getEmploy() {
        return employ;
    }

    public void setEmploy(Employ employ) {
        this.employ = employ;
    }

    public StructFinan getStructFinance() {
        return structFinance;
    }

    public void setStructFinance(StructFinan _structFinance) {
        this.structFinance = _structFinance;
    }

    public SocHealth getSoc_health() {
        return soc_health;
    }

    public void setSoc_health(SocHealth soc_health) {
        this.soc_health = soc_health;
    }
}
