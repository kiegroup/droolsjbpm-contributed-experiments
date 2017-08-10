package org.drools.examples.learner.structured_car;

import org.drools.learner.tools.FieldAnnotation;

public class Price {

    @FieldAnnotation(readingSeq = 0)
    private String buying;        //"vhigh", "high", "med", "low"
    @FieldAnnotation(readingSeq = 1)
    private String maint;        //"vhigh", "high", "med", "low"

    public Price() {

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
}
