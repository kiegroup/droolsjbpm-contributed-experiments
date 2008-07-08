package org.drools.jpdl.instance;

import java.io.Externalizable;
import java.io.ObjectOutput;
import java.io.IOException;
import java.io.ObjectInput;

import org.drools.process.instance.ProcessInstance;
import org.drools.process.instance.ProcessInstanceFactory;

public class JpdlProcessInstanceFactory implements ProcessInstanceFactory, Externalizable {

    private static final long serialVersionUID = 1L;

    public ProcessInstance createProcessInstance() {
        return new JpdlProcessInstance();
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    }

    public void writeExternal(ObjectOutput out) throws IOException {
    }


}
