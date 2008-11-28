package org.drools.osworkflow.xml;

import org.drools.xml.XmlWorkflowProcessDumper;

public class XmlOSWorkflowProcessDumper extends XmlWorkflowProcessDumper {

    public static final XmlOSWorkflowProcessDumper INSTANCE = new XmlOSWorkflowProcessDumper();
    
    private XmlOSWorkflowProcessDumper() {
        super(
            "OSWorkflow",
            "http://drools.org/drools-4.0/osworkflow",
            "drools-osworkflow-4.0.xsd",
            new OSWorkflowSemanticModule()
        );
    }
    
}
