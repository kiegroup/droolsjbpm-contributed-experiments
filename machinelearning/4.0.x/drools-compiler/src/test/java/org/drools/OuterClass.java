package org.drools;

import java.io.Serializable;

public class OuterClass implements Serializable {

    private static final long serialVersionUID = 4398109820791230134L;
    private String attr1;

    public String getAttr1() {
        return attr1;
    }

    public void setAttr1(String attr1) {
        this.attr1 = attr1;
    }

    public static class InnerClass implements Serializable {

        private static final long serialVersionUID = 5025859088631741192L;
        private int intAttr;

        public InnerClass(int intAttr) {
            super();
            this.intAttr = intAttr;
        }

        public int getIntAttr() {
            return intAttr;
        }

        public void setIntAttr(int intAttr) {
            this.intAttr = intAttr;
        }
    }
}
