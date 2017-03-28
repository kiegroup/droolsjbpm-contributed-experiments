package org.drools.learner;

import java.util.Comparator;

public class InstanceComparator implements Comparator<Instance> {

    private String attrName;

    public InstanceComparator( String attrName ) {
        this.attrName = attrName;
    }

    public int compare( Instance i0, Instance i1 ) {
        return i0.getAttr( this.attrName ).compareValue( i1.getAttr( this.attrName ) );
    }

}
