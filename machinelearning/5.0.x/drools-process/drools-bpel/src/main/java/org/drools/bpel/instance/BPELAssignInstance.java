package org.drools.bpel.instance;

import java.io.ByteArrayInputStream;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;

import org.apache.ode.utils.DOMUtils;
import org.drools.bpel.core.BPELAssign;
import org.drools.bpel.core.BPELAssign.Copy;
import org.drools.bpel.core.BPELAssign.Expression;
import org.drools.bpel.core.BPELAssign.From;
import org.drools.bpel.core.BPELAssign.Literal;
import org.drools.bpel.core.BPELAssign.VariableRef;
import org.drools.bpel.xpath.XMLDataType;
import org.drools.bpel.xpath.XPathReturnValueEvaluator;
import org.drools.process.core.context.variable.VariableScope;
import org.drools.process.core.datatype.DataType;
import org.drools.process.instance.ProcessInstance;
import org.drools.process.instance.context.variable.VariableScopeInstance;
import org.drools.runtime.process.NodeInstance;
import org.drools.spi.ProcessContext;
import org.drools.workflow.core.Node;
import org.drools.workflow.instance.impl.NodeInstanceImpl;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class BPELAssignInstance extends NodeInstanceImpl {
    
    private static final long serialVersionUID = 400L;

    public BPELAssign getBPELAssign() {
    	return (BPELAssign) getNode();
    }
    
    public void internalTrigger(NodeInstance from, String type) {
        if (BPELLinkManager.checkActivityEnabled(this)) {
        	BPELAssign assign = getBPELAssign();
        	for (Copy copy: assign.getCopies()) {
        		From fromPart = copy.getFrom();
        		VariableRef toPart = (VariableRef) copy.getTo();
        		Object fromValue = getValue(fromPart);
        		if (toPart.getPart() == null) {
        			if (fromValue instanceof String) {
        				setVariableValue(toPart.getVariable(), (String) fromValue);
        			} else if (fromValue instanceof Element) {
        				String value = DOMUtils.domToString(((Element) fromValue).getFirstChild());
        				setVariableValue(toPart.getVariable(), value);
        			} else {
        				throw new IllegalArgumentException(
    						"Cannot set variable of this type " + fromValue);
        			}
        		} else {
            		String toValue = getVariableValue(toPart.getVariable());
	        		if (toValue == null) {
	        			toValue = initializeVariable(toPart.getVariable());
	        		}
	        		toValue = copy(fromValue, toValue, toPart.getPart());
	        		setVariableValue(toPart.getVariable(), toValue);
        		}
        	}
            triggerCompleted(Node.CONNECTION_DEFAULT_TYPE, true);
        }
    }
    
    private Object getValue(From from) {
    	if (from instanceof VariableRef) {
    		VariableRef fromPart = (VariableRef) from;
    		String fromValue = getVariableValue(fromPart.getVariable());
    		if (fromPart.getPart() == null) {
    			return fromValue;
    		}
            try {
	        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    		Document fromDocument = factory.newDocumentBuilder().parse(new ByteArrayInputStream(fromValue.getBytes()));
	        	return DOMUtils.findChildByName((Element) fromDocument.getDocumentElement(), new QName(fromPart.getPart()));
            } catch (Throwable t) {
            	throw new IllegalArgumentException("Could not get value", t);
            }
    	} else if (from instanceof Literal) {
    		String literal = ((Literal) from).getValue();
    		if (literal.startsWith("<?xml")) {
    			int index = literal.indexOf("?>");
    			literal = "<message>" + literal.substring(index + 3) + "</message>";
                try {
    	        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	    		Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(literal.getBytes()));
    	        	return document.getDocumentElement();
                } catch (Throwable t) {
                	throw new IllegalArgumentException("Could not get value", t);
                }
    		}
    		return literal;
    	} else if (from instanceof Expression) {
    		String expression = ((Expression) from).getExpression();
    		try {
	    		XPathReturnValueEvaluator evaluator = new XPathReturnValueEvaluator();
	    		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(expression.getBytes()));
				org.w3c.dom.Node node = document.getFirstChild().getFirstChild();
		        if (node == null) {
		            throw new IllegalStateException();
		        }
		        if (node.getNodeType() != org.w3c.dom.Node.TEXT_NODE) {
		            throw new IllegalArgumentException("Unexpected node type for XPath");
		        }
		        String xpathString = node.getNodeValue();
	    		evaluator.setExpression(xpathString);
	    		ProcessContext processContext = new ProcessContext();
	    		processContext.setNodeInstance(this);
    			return (String) evaluator.evaluate(
					((ProcessInstance) getProcessInstance()).getWorkingMemory(), processContext, XPathConstants.STRING);
    		} catch (Throwable t) {
    			throw new IllegalArgumentException("Could not evaluate expression " + expression, t);
    		}
    	} else {
    		throw new UnsupportedOperationException();
    	}
    }
    
    private String copy(Object fromValue, String toValue, String toPart) {
        try {
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    		Document toDocument = factory.newDocumentBuilder().parse(new ByteArrayInputStream(toValue.getBytes()));
        	Element to = DOMUtils.findChildByName((Element) toDocument.getDocumentElement(), new QName(toPart));
        	if (fromValue instanceof Element) {
        		Element from = (Element) fromValue;
	        	Element replacement = toDocument.createElementNS(null, toPart);
	            NodeList nl = from.getChildNodes();
	            for (int i = 0; i < nl.getLength(); ++i)
	                replacement.appendChild(toDocument.importNode(nl.item(i), true));
	            NamedNodeMap attrs = from.getAttributes();
	            for (int i = 0; i < attrs.getLength(); ++i) {
	                if (!((Attr)attrs.item(i)).getName().startsWith("xmlns")) {
	                    replacement.setAttributeNodeNS((Attr) toDocument.importNode(attrs.item(i), true));
	                }
	            }
	            if (to == null) {
	            	toDocument.getDocumentElement().appendChild(replacement);
	            } else {
	            	to.getParentNode().replaceChild(replacement, to);
	            }
        	} else {
        		Element replacement = toDocument.createElementNS(null, toPart);
        		replacement.setTextContent((String) fromValue);
        		if (to == null) {
        			toDocument.getDocumentElement().appendChild(replacement);
	            } else {
	            	to.getParentNode().replaceChild(replacement, to);
	            }
        	}
        	return DOMUtils.domToString(toDocument.getDocumentElement());
        } catch (Throwable t) {
        	throw new IllegalArgumentException("Could not copy value", t);
        }
    }
    
    private String initializeVariable(String variable) {
    	VariableScopeInstance variableScopeInstance = (VariableScopeInstance)
			resolveContextInstance(VariableScope.VARIABLE_SCOPE, variable);
		if (variableScopeInstance != null) {
			DataType dataType = ((VariableScope) variableScopeInstance.getContext()).findVariable(variable).getType();
			if (dataType instanceof XMLDataType) {
				String type = ((XMLDataType) dataType).getTypeDefinition();
				type = type.substring(type.lastIndexOf("}") + 1);
				return "<" + type + "></" + type + ">";
			}
		}
        return "";
    }
    
    private String getVariableValue(String variable) {
    	VariableScopeInstance variableScopeInstance = (VariableScopeInstance)
    		resolveContextInstance(VariableScope.VARIABLE_SCOPE, variable);
    	if (variableScopeInstance != null) {
            return (String) variableScopeInstance.getVariable(variable);
        } else {
            System.err.println("Could not find variable scope for variable " + variable);
            System.err.println("when trying assign");
            System.err.println("Continuing without setting variable.");
        }
    	return null;
    }
    
    private void setVariableValue(String variable, String value) {
    	VariableScopeInstance variableScopeInstance = (VariableScopeInstance)
    		resolveContextInstance(VariableScope.VARIABLE_SCOPE, variable);
    	if (variableScopeInstance != null) {
            variableScopeInstance.setVariable(variable, value);
        } else {
            System.err.println("Could not find variable scope for variable " + variable);
            System.err.println("when trying assign");
            System.err.println("Continuing without setting variable.");
        }
    }
    
    public void triggerCompleted(String type, boolean remove) {
        super.triggerCompleted(type, remove);
        BPELLinkManager.activateTargetLinks(this);
    }
    
}
