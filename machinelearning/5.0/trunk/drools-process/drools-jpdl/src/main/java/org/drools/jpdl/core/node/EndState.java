package org.drools.jpdl.core.node;

import org.drools.workflow.core.Connection;

public class EndState extends JpdlNode {

	private static final long serialVersionUID = 1L;

    public void validateAddOutgoingConnection(final String type, final Connection connection) {
        throw new UnsupportedOperationException(
            "An end state does not have an outgoing connection!");
    }

    public void validateRemoveOutgoingConnection(final String type, final Connection connection) {
        throw new UnsupportedOperationException(
            "An end state does not have an outgoing connection!");
    }
    
}
