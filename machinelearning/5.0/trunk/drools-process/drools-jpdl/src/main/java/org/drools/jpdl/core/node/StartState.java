package org.drools.jpdl.core.node;

import org.drools.workflow.core.Connection;

public class StartState extends JpdlNode {

	private static final long serialVersionUID = 1L;

    public void validateAddIncomingConnection(final String type, final Connection connection) {
        throw new UnsupportedOperationException(
            "A start state does not have an incoming connection!");
    }

    public void validateRemoveIncomingConnection(final String type, final Connection connection) {
        throw new UnsupportedOperationException(
            "A start state does not have an incoming connection!");
    }

}
