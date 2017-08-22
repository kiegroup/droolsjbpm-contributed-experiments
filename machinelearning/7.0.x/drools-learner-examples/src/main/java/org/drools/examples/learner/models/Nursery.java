package org.drools.examples.learner.models;

import org.drools.learner.tools.FieldAnnotation;

public class Nursery {

    @FieldAnnotation(readingSeq = 0)
    private String parents;    //"usual","pretentious", "great_pret"
    @FieldAnnotation(readingSeq = 1)
    private String has_nurs;    //"proper","less_proper", "improper", "critical", "very_crit"
    @FieldAnnotation(readingSeq = 2)
    private String children;    //"1", "2", "3","more"
    @FieldAnnotation(readingSeq = 3)
    private String form;        //"complete","completed", "incomplete", "foster"
    @FieldAnnotation(readingSeq = 4)
    private String housing;    //"convenient","less_conv", "critical"
    @FieldAnnotation(readingSeq = 5)
    private String finance;    //"convenient","inconv"
    @FieldAnnotation(readingSeq = 6)
    private String social;        //"nonprob","slightly_prob", "problematic"
    @FieldAnnotation(readingSeq = 7)
    private String health;        //"recommended","priority", "not_recom"
    @FieldAnnotation(readingSeq = 8, target = true)
    private String classnursery; //"not_recom", "recommend", "very_recom", "priority","spec_prior"

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

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }

    public String getChildren() {
        return children;
    }

    public void setChildren(String children) {
        this.children = children;
    }

    public String getHousing() {
        return housing;
    }

    public void setHousing(String housing) {
        this.housing = housing;
    }

    public String getFinance() {
        return finance;
    }

    public void setFinance(String finance) {
        this.finance = finance;
    }

    public String getSocial() {
        return social;
    }

    public void setSocial(String social) {
        this.social = social;
    }

    public String getHealth() {
        return health;
    }

    public void setHealth(String health) {
        this.health = health;
    }

    public String getClassnursery() {
        return classnursery;
    }

    public void setClassnursery(String classnursery) {
        this.classnursery = classnursery;
    }
}
