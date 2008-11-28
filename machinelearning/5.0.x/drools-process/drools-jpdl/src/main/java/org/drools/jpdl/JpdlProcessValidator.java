package org.drools.jpdl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.drools.jpdl.core.JpdlProcess;
import org.drools.jpdl.core.node.EndState;
import org.drools.jpdl.core.node.StartState;
import org.drools.process.core.Process;
import org.drools.process.core.Work;
import org.drools.process.core.context.variable.Variable;
import org.drools.process.core.validation.ProcessValidationError;
import org.drools.process.core.validation.ProcessValidator;
import org.drools.process.core.validation.impl.ProcessValidationErrorImpl;
import org.drools.workflow.core.Connection;
import org.drools.workflow.core.Node;
import org.drools.workflow.core.impl.DroolsConsequenceAction;
import org.drools.workflow.core.node.ActionNode;
import org.drools.workflow.core.node.CompositeNode;
import org.drools.workflow.core.node.Join;
import org.drools.workflow.core.node.MilestoneNode;
import org.drools.workflow.core.node.RuleSetNode;
import org.drools.workflow.core.node.Split;
import org.drools.workflow.core.node.SubProcessNode;
import org.drools.workflow.core.node.WorkItemNode;
import org.mvel.ErrorDetail;
import org.mvel.ParserContext;
import org.mvel.compiler.ExpressionCompiler;

public class JpdlProcessValidator implements ProcessValidator {
    
    private static JpdlProcessValidator instance;

    private JpdlProcessValidator() {
    }

    public static JpdlProcessValidator getInstance() {
        if ( instance == null ) {
            instance = new JpdlProcessValidator();
        }
        return instance;
    }

    public ProcessValidationError[] validateProcess(final JpdlProcess process) {
        final List<ProcessValidationError> errors = new ArrayList<ProcessValidationError>();

        if (process.getName() == null) {
            errors.add(new ProcessValidationErrorImpl(process,
                "Process has no name."));
        }

        if (process.getId() == null || "".equals(process.getId())) {
            errors.add(new ProcessValidationErrorImpl(process,
                "Process has no id."));
        }

        if ( process.getPackageName() == null || "".equals( process.getPackageName() ) ) {
            errors.add(new ProcessValidationErrorImpl(process,
                "Process has no package name."));
        }

        final Node[] nodes = process.getNodes();
        validateNodes(nodes, errors, process);
        Node startNode = process.getStart();
        if (startNode == null) {
            errors.add(new ProcessValidationErrorImpl(process,
                "Process has no start node."));
        }
        for (final Iterator<Variable> it = process.getVariableScope().getVariables().iterator(); it.hasNext(); ) {
            final Variable variable = it.next();
            if (variable.getType() == null) {
                errors.add(new ProcessValidationErrorImpl(process,
                    "Variable '" + variable.getName() + "' has no type."));
            }
        }

        checkAllNodesConnectedToStart(process, errors);

        return errors.toArray(new ProcessValidationError[errors.size()]);
    }
    
    private void validateNodes(Node[] nodes, List<ProcessValidationError> errors, JpdlProcess process) {
        for ( int i = 0; i < nodes.length; i++ ) {
            final Node node = nodes[i];
            if (node instanceof StartState) {
                final StartState startState = (StartState) node;
                if (startState.getOutgoingConnections().size() != 1) {
                    errors.add(new ProcessValidationErrorImpl(process,
                        "Start state '" + node.getName() + "' [" + node.getId() + "] has more than one type of outgoing connections."));
                }
                List<Connection> connections = startState.getOutgoingConnections(Node.CONNECTION_DEFAULT_TYPE);  
                if (connections == null || connections.size() == 0) {
                    errors.add(new ProcessValidationErrorImpl(process,
                        "Start state '" + node.getName() + "' [" + node.getId() + "] has no default outgoing connections."));
                }
                if (connections.size() != 1) {
                	errors.add(new ProcessValidationErrorImpl(process,
                        "Start state '" + node.getName() + "' [" + node.getId() + "] has more than one default outgoing connection."));
                }
            } else if (node instanceof EndState) {
                final EndState endState = (EndState) node;
                if (endState.getIncomingConnections().size() != 1) {
                    errors.add(new ProcessValidationErrorImpl(process,
                        "End state '" + node.getName() + "' [" + node.getId() + "] has more than one type of incoming connections."));
                }
                List<Connection> connections = endState.getIncomingConnections(Node.CONNECTION_DEFAULT_TYPE);  
                if (connections == null || connections.size() == 0) {
                    errors.add(new ProcessValidationErrorImpl(process,
                        "End state '" + node.getName() + "' [" + node.getId() + "] has no default incoming connections."));
                }
                if (connections.size() != 1) {
                    errors.add(new ProcessValidationErrorImpl(process,
                        "End state '" + node.getName() + "' [" + node.getId() + "] has more than one default incoming connection."));
                }
            } else if (node instanceof RuleSetNode) {
                final RuleSetNode ruleSetNode = (RuleSetNode) node;
                if (ruleSetNode.getFrom() == null) {
                    errors.add(new ProcessValidationErrorImpl(process,
                        "RuleSet node '" + node.getName() + "' [" + node.getId() + "] has no incoming connection."));
                }
                if (ruleSetNode.getTo() == null) {
                    errors.add(new ProcessValidationErrorImpl(process,
                        "RuleSet node '" + node.getName() + "' [" + node.getId() + "] has no outgoing connection."));
                }
                final String ruleFlowGroup = ruleSetNode.getRuleFlowGroup();
                if (ruleFlowGroup == null || "".equals(ruleFlowGroup)) {
                    errors.add( new ProcessValidationErrorImpl(process,
                        "RuleSet node '" + node.getName() + "' [" + node.getId() + "] has no ruleflow-group."));
                }
            } else if (node instanceof Split) {
                final Split split = (Split) node;
                if (split.getType() == Split.TYPE_UNDEFINED) {
                    errors.add(new ProcessValidationErrorImpl(process,
                        "Split node '" + node.getName() + "' [" + node.getId() + "] has no type."));
                }
                if (split.getFrom() == null) {
                    errors.add(new ProcessValidationErrorImpl(process,
                        "Split node '" + node.getName() + "' [" + node.getId() + "] has no incoming connection."));
                }
                if (split.getDefaultOutgoingConnections().size() < 2) {
                    errors.add(new ProcessValidationErrorImpl(process,
                        "Split node '" + node.getName() + "' [" + node.getId() + "] does not have more than one outgoing connection: " + split.getOutgoingConnections().size() + "."));
                }
                if (split.getType() == Split.TYPE_XOR || split.getType() == Split.TYPE_OR ) {
                    for ( final Iterator<Connection> it = split.getDefaultOutgoingConnections().iterator(); it.hasNext(); ) {
                        final Connection connection = it.next();
                        if (split.getConstraint(connection) == null) {
                            errors.add(new ProcessValidationErrorImpl(process,
                                "Split node '" + node.getName() + "' [" + node.getId() + "] does not have a constraint for " + connection.toString() + "."));
                        }
                    }
                }
            } else if ( node instanceof Join ) {
                final Join join = (Join) node;
                if (join.getType() == Join.TYPE_UNDEFINED) {
                    errors.add(new ProcessValidationErrorImpl(process,
                        "Join node '" + node.getName() + "' [" + node.getId() + "] has no type."));
                }
                if (join.getDefaultIncomingConnections().size() < 2) {
                    errors.add(new ProcessValidationErrorImpl(process,
                        "Join node '" + node.getName() + "' [" + node.getId() + "] does not have more than one incoming connection: " + join.getIncomingConnections().size() + "."));
                }
                if (join.getTo() == null) {
                    errors.add(new ProcessValidationErrorImpl(process,
                        "Join node '" + node.getName() + "' [" + node.getId() + "] has no outgoing connection."));
                }
            } else if (node instanceof MilestoneNode) {
                final MilestoneNode milestone = (MilestoneNode) node;
                if (milestone.getFrom() == null) {
                    errors.add(new ProcessValidationErrorImpl(process,
                        "Milestone node '" + node.getName() + "' [" + node.getId() + "] has no incoming connection."));
                }

                if (milestone.getTo() == null) {
                    errors.add( new ProcessValidationErrorImpl(process,
                        "Milestone node '" + node.getName() + "' [" + node.getId() + "] has no outgoing connection."));
                }
                if (milestone.getConstraint() == null) {
                    errors.add( new ProcessValidationErrorImpl(process,
                        "Milestone node '" + node.getName() + "' [" + node.getId() + "] has no constraint."));
                }
            } else if (node instanceof SubProcessNode) {
                final SubProcessNode subProcess = (SubProcessNode) node;
                if (subProcess.getFrom() == null) {
                    errors.add(new ProcessValidationErrorImpl(process,
                        "SubProcess node '" + node.getName() + "' [" + node.getId() + "] has no incoming connection."));
                }
                if (subProcess.getTo() == null) {
                    errors.add(new ProcessValidationErrorImpl(process,
                        "SubProcess node '" + node.getName() + "' [" + node.getId() + "] has no outgoing connection."));
                }
                if (subProcess.getProcessId() == null) {
                    errors.add(new ProcessValidationErrorImpl(process,
                        "SubProcess node '" + node.getName() + "' [" + node.getId() + "] has no process id."));
                }
            } else if (node instanceof ActionNode) {
                final ActionNode actionNode = (ActionNode) node;
                if (actionNode.getFrom() == null) {
                    errors.add(new ProcessValidationErrorImpl(process,
                        "Action node '" + node.getName() + "' [" + node.getId() + "] has no incoming connection."));
                }
                if (actionNode.getTo() == null) {
                    errors.add(new ProcessValidationErrorImpl(process,
                        "Action node '" + node.getName() + "' [" + node.getId() + "] has no outgoing connection."));
                }
                if (actionNode.getAction() == null) {
                    errors.add(new ProcessValidationErrorImpl(process,
                        "Action node '" + node.getName() + "' [" + node.getId() + "] has no action."));
                } else {
                    if (actionNode.getAction() instanceof DroolsConsequenceAction) {
                        DroolsConsequenceAction droolsAction = (DroolsConsequenceAction) actionNode.getAction();
                        String actionString = droolsAction.getConsequence();
                        if (actionString == null) {
                            errors.add(new ProcessValidationErrorImpl(process,
                                "Action node '" + node.getName() + "' [" + node.getId() + "] has empty action."));
                        } else {
                            try {
                                ExpressionCompiler compiler = new ExpressionCompiler(actionString);
                                compiler.setVerifying(true);
                                ParserContext parserContext = new ParserContext();
                                //parserContext.setStrictTypeEnforcement(true);
                                compiler.compile(parserContext);
                                List<ErrorDetail> mvelErrors = parserContext.getErrorList();
                                if (mvelErrors != null) {
                                    for (Iterator<ErrorDetail> iterator = mvelErrors.iterator(); iterator.hasNext(); ) {
                                        ErrorDetail error = iterator.next();
                                        errors.add(new ProcessValidationErrorImpl(process,
                                            "Action node '" + node.getName() + "' [" + node.getId() + "] has invalid action: " + error.getMessage() + "."));
                                    }
                                }
                            } catch (Throwable t) {
                                errors.add(new ProcessValidationErrorImpl(process,
                                    "Action node '" + node.getName() + "' [" + node.getId() + "] has invalid action: " + t.getMessage() + "."));
                            }
                        }
                    }
                }
            } else if (node instanceof WorkItemNode) {
                final WorkItemNode workItemNode = (WorkItemNode) node;
                if (workItemNode.getFrom() == null) {
                    errors.add(new ProcessValidationErrorImpl(process,
                        "WorkItem node '" + node.getName() + "' [" + node.getId() + "] has no incoming connection."));
                }
                if (workItemNode.getTo() == null) {
                    errors.add(new ProcessValidationErrorImpl(process,
                        "WorkItem node '" + node.getName() + "' [" + node.getId() + "] has no outgoing connection."));
                }
                if (workItemNode.getWork() == null) {
                    errors.add(new ProcessValidationErrorImpl(process,
                        "WorkItem node '" + node.getName() + "' [" + node.getId() + "] has no work specified."));
                } else {
                    Work work = workItemNode.getWork();
                    if (work.getName() == null) {
                        errors.add(new ProcessValidationErrorImpl(process,
                            "WorkItem node '" + node.getName() + "' [" + node.getId() + "] has no work name."));
                    }
                }
            } else if (node instanceof CompositeNode) {
                final CompositeNode compositeNode = (CompositeNode) node;
                for (String inType: compositeNode.getLinkedIncomingNodes().keySet()) {
                    if (compositeNode.getIncomingConnections(inType).size() == 0) {
                        errors.add(new ProcessValidationErrorImpl(process,
                            "Composite node '" + node.getName() + "' [" + node.getId() + "] has no incoming connection for type " + inType));
                    }
                }
                for (String outType: compositeNode.getLinkedOutgoingNodes().keySet()) {
                    if (compositeNode.getOutgoingConnections(outType).size() == 0) {
                        errors.add(new ProcessValidationErrorImpl(process,
                            "Composite node '" + node.getName() + "' [" + node.getId() + "] has no outgoing connection for type " + outType));
                    }
                }
                validateNodes(compositeNode.getNodes(), errors, process);
            } 
        }

    }

    private void checkAllNodesConnectedToStart(final JpdlProcess process,
                                               final List<ProcessValidationError> errors) {
        final Map<Node, Boolean> processNodes = new HashMap<Node, Boolean>();
        final Node[] nodes = process.getNodes();
        for (int i = 0; i < nodes.length; i++) {
            final Node node = nodes[i];
            processNodes.put(node, Boolean.FALSE);
        }
        Node startNode = process.getStart();
        if (startNode != null) {
            processNode(startNode, processNodes);
        }
        for ( final Iterator<Node> it = processNodes.keySet().iterator(); it.hasNext(); ) {
            final Node node = it.next();
            if (Boolean.FALSE.equals(processNodes.get(node))) {
                errors.add(new ProcessValidationErrorImpl(process,
                    "Node '" + node.getName() + "' [" + node.getId() + "] has no connection to the start node."));
            }
        }
    }

    private void processNode(final Node node, final Map<Node, Boolean> nodes) {
        if (!nodes.containsKey(node) ) {
            throw new IllegalStateException("A process node is connected with a node that does not belong to the process.");
        }
        final Boolean prevValue = (Boolean) nodes.put(node, Boolean.TRUE);
        if (prevValue == Boolean.FALSE) {
            for (final Iterator<List<Connection>> it = node.getOutgoingConnections().values().iterator(); it.hasNext(); ) {
                final List<Connection> list = it.next();
                for (final Iterator<Connection> it2 = list.iterator(); it2.hasNext(); ) {
                    processNode(it2.next().getTo(), nodes);
                }
            }
        }
    }

    public ProcessValidationError[] validateProcess(Process process) {
        if (!(process instanceof JpdlProcess)) {
            throw new IllegalArgumentException(
                "This validator can only validate ruleflow processes!");
        }
        return validateProcess((JpdlProcess) process);
    }

}
