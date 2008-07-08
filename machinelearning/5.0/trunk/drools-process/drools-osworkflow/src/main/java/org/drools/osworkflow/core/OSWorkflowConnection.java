package org.drools.osworkflow.core;

import java.util.List;

import org.drools.workflow.core.Node;
import org.drools.workflow.core.impl.ConnectionImpl;

import com.opensymphony.workflow.loader.FunctionDescriptor;

public class OSWorkflowConnection extends ConnectionImpl {

    private static final long serialVersionUID = 1L;
    
    private List<FunctionDescriptor> preFunctions;
    private List<FunctionDescriptor> postFunctions;

    public OSWorkflowConnection(Node from, String fromType, Node to, String toType) {
        super(from, fromType, to, toType);
    }

    public List<FunctionDescriptor> getPreFunctions() {
        return preFunctions;
    }

    public void setPreFunctions(List<FunctionDescriptor> preFunctions) {
        this.preFunctions = preFunctions;
    }

    public List<FunctionDescriptor> getPostFunctions() {
        return postFunctions;
    }

    public void setPostFunctions(List<FunctionDescriptor> postFunctions) {
        this.postFunctions = postFunctions;
    }

}
