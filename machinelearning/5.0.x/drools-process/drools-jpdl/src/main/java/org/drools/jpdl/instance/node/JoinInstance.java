package org.drools.jpdl.instance.node;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.drools.jpdl.core.node.Join;
import org.drools.workflow.core.Connection;
import org.drools.workflow.core.Node;
import org.drools.workflow.instance.NodeInstance;
import org.jbpm.graph.exe.Token;

public class JoinInstance extends JpdlNodeInstance {

    private static final long serialVersionUID = 1L;
    
    private Map<Long, Integer> triggers = new HashMap<Long, Integer>();
    
    public Join getJoin() {
        return (Join) getNode();
    }

    public void execute(NodeInstance from, String type) {
        if (getJoin().isDiscriminator()) {
            leave();
        } else {
            // register trigger
            Integer count = (Integer) this.triggers.get(type);
            if (count == null) {
                this.triggers.put(from.getNodeId(), 1);
            } else {
                this.triggers.put(from.getNodeId(), count.intValue() + 1);
            }
            // check whether we should leave node
            if (getJoin().getTokenNames() != null) {
                Collection<String> tokenNames = getJoin().getTokenNames();
                // TODO: implement this
//                if (checkTriggers(tokenNames)) {
//                    decreaseTriggers(tokenNames);
//                    leave();
//                }
            } else if (getJoin().getScript() != null) {
                Object result = null;
                try {
                    result = getJoin().getScript().eval((Token) null);
                } catch (Exception e) {
                    this.raiseException(e);
                }
                if (result instanceof Collection) {
                    // TODO: implement this
//                    Collection<String> runtimeTokenNames = (Collection<String>) result;
//                    if (checkTriggers(runtimeTokenNames)) {
//                        decreaseTriggers(runtimeTokenNames);
//                        leave();
//                    }
                } else if (result instanceof Boolean) {
                    if ((Boolean) result) {
                        leave();
                    }
                }
            } else if (getJoin().getNOutOfM() != -1) {
                if (triggers.size() >= getJoin().getNOutOfM()) {
                    resetAllTriggers();
                    leave();
                }
            } else {
                // TODO this only works if all incoming connection paths were
                // activated; fix this in cases where not all incoming connection
                // paths were activated by letting the corresponding split transfer
                // the exact number of triggered connections to this join
                if (checkAllTriggers()) {
                    decreaseAllTriggers();
                    leave();
                }
            }
        }
    }
    
    private boolean checkAllTriggers() {
        for (Connection connection: getJoin().getIncomingConnections(Node.CONNECTION_DEFAULT_TYPE)) {
            if (this.triggers.get(connection.getFrom().getId()) == null) {
                return false;
            }
        }
        return true;
    }
    
    private void decreaseAllTriggers() {
        for (Connection connection: getJoin().getIncomingConnections(Node.CONNECTION_DEFAULT_TYPE)) {
            final Integer count = (Integer) this.triggers.get( connection.getFrom().getId() );
            if ( count.intValue() == 1 ) {
                this.triggers.remove( connection.getFrom().getId() );
            } else {
                this.triggers.put( connection.getFrom().getId(),
                                   count.intValue() - 1 );
            }
        }
    }

    private void resetAllTriggers() {
        triggers.clear();
    }

}
