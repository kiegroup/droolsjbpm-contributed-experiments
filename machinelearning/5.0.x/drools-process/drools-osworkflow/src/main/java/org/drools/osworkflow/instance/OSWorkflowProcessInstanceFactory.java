package org.drools.osworkflow.instance;

import org.drools.process.instance.ProcessInstance;
import org.drools.process.instance.ProcessInstanceFactory;

public class OSWorkflowProcessInstanceFactory implements ProcessInstanceFactory {

    public ProcessInstance createProcessInstance() {
        return new OSWorkflowProcessInstance();
    }

}
