package org.drools.examples.learner.structured_nursery;

import org.drools.learner.tools.FieldAnnotation;

public class Structure {

    @FieldAnnotation(readingSeq = 2)
    private String children;    //"1", "2", "3","more"
    @FieldAnnotation(readingSeq = 3)
    private String form;        //"complete","completed", "incomplete", "foster"

    public Structure() {

    }

    public String getChildren() {
        return children;
    }

    public void setChildren(String children) {
        this.children = children;
    }

    public String getForm() {
        return form;
    }

    public void setForm(String form) {
        this.form = form;
    }
}
