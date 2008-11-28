package org.drools.bpel.instance;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;

import org.apache.ode.utils.DOMUtils;
import org.drools.bpel.core.BPELAssign;
import org.drools.bpel.core.BPELAssign.Copy;
import org.drools.bpel.core.BPELAssign.Expression;
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
import org.w3c.dom.Text;

/**
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class CopyOfBPELAssignInstance extends NodeInstanceImpl {
    
    private static final long serialVersionUID = 400L;

    public BPELAssign getBPELAssign() {
    	return (BPELAssign) getNode();
    }
    
    public void internalTrigger(NodeInstance from, String type) {
        if (BPELLinkManager.checkActivityEnabled(this)) {
        	BPELAssign assign = getBPELAssign();
        	for (Copy copy: assign.getCopies()) {
        		copy(copy);
        	}
            triggerCompleted(Node.CONNECTION_DEFAULT_TYPE, true);
        }
    }
    
    private void copy(Copy copy) {
        org.w3c.dom.Node rvalue = evalRValue(copy.getFrom());
        org.w3c.dom.Node lvalue = evalLValue(copy.getTo());

        org.w3c.dom.Node lvaluePtr = lvalue;
        boolean headerAssign = false;
        if (copy.getTo() instanceof BPELAssign.DirectRef) {
        	throw new UnsupportedOperationException();
//            DirectRef dref = ((DirectRef) copy.getTo());
//            Element el = DOMUtils.findChildByName((Element) lvalue, dref.elName);
//            if (el == null) {
//                el = (Element) ((Element) lvalue).appendChild(lvalue.getOwnerDocument()
//                        .createElementNS(dref.elName.getNamespaceURI(), dref.elName.getLocalPart()));
//            }
//            lvaluePtr = el;
        } else if (copy.getTo() instanceof BPELAssign.VariableRef) {
            VariableRef varRef = ((VariableRef) copy.getTo());
            if (varRef.getHeaderPart() != null) {
            	headerAssign = true;
            }
            lvaluePtr = evalQuery(
        		lvalue, varRef.getPart() != null ? varRef.getPart() : varRef.getHeaderPart(), 
				varRef.getLocation());
        } else if (copy.getTo() instanceof BPELAssign.PropertyRef) {
        	throw new UnsupportedOperationException();
//            PropertyRef propRef = ((PropertyRef) copy.getTo());
//            lvaluePtr = evalQuery(lvalue, propRef.propertyAlias.part,
//                    propRef.propertyAlias.location,
//                    new EvaluationContextProxy(propRef.getVariable(),
//                            lvalue));
        } else if (copy.getTo() instanceof BPELAssign.LValueExpression) {
        	throw new UnsupportedOperationException();
//            LValueExpression lexpr = (LValueExpression) copy.getTo();
//            lvaluePtr = evalQuery(lvalue, null, lexpr.expression,
//                    new EvaluationContextProxy(lexpr.getVariable(), lvalue));
        }

        // For partner link assignmenent, the whole content is assigned.
        if (copy.getTo() instanceof BPELAssign.PartnerLinkRef) {
        	throw new UnsupportedOperationException();
//            PartnerLinkRef pLinkRef = ((PartnerLinkRef) copy.getTo());
//            PartnerLinkInstance plval = _scopeFrame.resolve(pLinkRef.partnerLink);
//            replaceEndpointRefence(plval, rvalue);
        } else {
            // Sneakily converting the EPR if it's not the format expected by the lvalue
            if (copy.getFrom() instanceof BPELAssign.PartnerLinkRef) {
            	throw new UnsupportedOperationException();
//                rvalue = getBpelRuntimeContext().convertEndpointReference((Element)rvalue, lvaluePtr);
//                if (rvalue.getNodeType() == org.w3c.dom.Node.DOCUMENT_NODE)
//                    rvalue = ((Document) rvalue).getDocumentElement();
            }
            if (headerAssign && lvaluePtr.getParentNode().getNodeName().equals("message")) {
                lvalue = copyInto((Element)lvalue, (Element) lvaluePtr, (Element) rvalue);
            } else if (rvalue.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE && lvaluePtr.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                lvalue = replaceElement((Element) lvalue, (Element) lvaluePtr, (Element) rvalue,
                        copy.isKeepSrcElementName());
            } else {
                lvalue = replaceContent(lvalue, lvaluePtr, rvalue.getTextContent());
            }
            setVariableValue(copy.getTo().getVariable(), lvalue);
        }
    }
    
    private org.w3c.dom.Node evalLValue(BPELAssign.To to) {
        org.w3c.dom.Node lval = null;
        if (!(to instanceof BPELAssign.PartnerLinkRef)) {
            String lvar = getVariableValue(to.getVariable());
            if (lvar == null) {
                Document doc = DOMUtils.newDocument();
                String type = getVariableType(to.getVariable());
                String ns = null;
                if (type != null) {
	                int index = type.lastIndexOf("}");
	                if (index != -1) {
	                	ns = type.substring(1, index);
	                	type = type.substring(index + 1);
	                }
                }
                org.w3c.dom.Node val = doc.createElementNS(ns, type);
                if (val.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                    Element tempwrapper = doc.createElementNS(null, "temporary-simple-type-wrapper");
                    doc.appendChild(tempwrapper);
                    tempwrapper.appendChild(val);
                    val = tempwrapper;
                } else {
                	doc.appendChild(val);
                }
                // Only external variables need to be initialized, others are new and going to be overwtitten
//                if (lvar.declaration.extVar != null) {
//                	lval = initializeVariable(lvar, val);
//                }
//                else
            	lval = val;
            } else {
                lval = getAsElement(getVariableValue(lvar));
            }
        }
        return lval;
    }

    private org.w3c.dom.Node evalRValue(BPELAssign.From from) {
        org.w3c.dom.Node retVal;
        if (from instanceof BPELAssign.DirectRef) {
        	throw new UnsupportedOperationException();
//            DirectRef dref = (DirectRef) from;
//            Node data = fetchVariableData(
//                    _scopeFrame.resolve(dref.variable), false);
//            retVal = DOMUtils.findChildByName((Element)data, dref.elName);
        } else if (from instanceof BPELAssign.VariableRef) {
            VariableRef varRef = (VariableRef) from;
            org.w3c.dom.Node data = getAsElement(getVariableValue(varRef.getVariable()));
            retVal = evalQuery(data, varRef.getPart() != null ? varRef.getPart() : varRef.getHeaderPart(), varRef.getLocation());
        } else if (from instanceof BPELAssign.PropertyRef) {
        	throw new UnsupportedOperationException();
//            PropertyRef propRef = (PropertyRef) from;
//            org.w3c.dom.Node data = fetchVariableData(_scopeFrame.resolve(propRef.variable), false);
//            retVal = evalQuery(data, propRef.propertyAlias.part,
//                    propRef.propertyAlias.location, getEvaluationContext());
        } else if (from instanceof BPELAssign.PartnerLinkRef) {
        	throw new UnsupportedOperationException();
//            PartnerLinkRef pLinkRef = (PartnerLinkRef) from;
//            PartnerLinkInstance pLink = _scopeFrame.resolve(pLinkRef.partnerLink);
//            Node tempVal = pLinkRef.isMyEndpointReference ?
//                    getBpelRuntimeContext().fetchMyRoleEndpointReferenceData(pLink)
//                    : getBpelRuntimeContext().fetchPartnerRoleEndpointReferenceData(pLink);
//            retVal = tempVal;
        } else if (from instanceof BPELAssign.Expression) {
            List<org.w3c.dom.Node> l;
            String expr = ((Expression) from).getExpression();
            try {
                l = new ArrayList<org.w3c.dom.Node>();
                l.add(getAsElement(evaluateExpression(expr)));
            } catch (Throwable t) {
                throw new IllegalArgumentException("Could not evaluate expression", t);
            }
            if (l.size() == 0) {
                throw new IllegalArgumentException(
            		"RValue no nodes selected: " + expr);
            } else if (l.size() > 1) {
                throw new IllegalArgumentException(
            		"RValue multiple nodes selected: " + expr);
            }
            retVal = (org.w3c.dom.Node) l.get(0);
        } else if (from instanceof BPELAssign.Literal) {
            Element literalRoot = getAsElement(((Literal) from).getValue());
            assert literalRoot.getLocalName().equals("literal");
            // We'd like a single text node...

            literalRoot.normalize();
            retVal = literalRoot.getFirstChild();

            // Adjust for whitespace before an element.
            if (retVal != null && retVal.getNodeType() == org.w3c.dom.Node.TEXT_NODE
                    && retVal.getTextContent().trim().length() == 0
                    && retVal.getNextSibling() != null) {
                retVal = retVal.getNextSibling();
            }

            if (retVal == null) {
                // Special case, no children --> empty TII
                retVal = literalRoot.getOwnerDocument().createTextNode("");
            } else if (retVal.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                // Make sure there is no more elements.
            	org.w3c.dom.Node x = retVal.getNextSibling();
                while (x != null) {
                    if (x.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                        throw new IllegalArgumentException(
                            "Literal contains multiple EIIs");

                    }
                    x = x.getNextSibling();
                }
            } else if (retVal.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
                // Make sure there are no elements following this text node.
            	org.w3c.dom.Node x = retVal.getNextSibling();
                while (x != null) {
                    if (x.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                        throw new IllegalArgumentException(
                            "Literal contains mixed content");
                    }
                    x = x.getNextSibling();
                }

            }

            if (retVal == null) {
                throw new IllegalArgumentException(
                    "Literal must contain TII or EII");
            }
        } else {
            throw new IllegalArgumentException(
        		"Unknown RVALUE type: " + from);
        }

        // Now verify we got something.
        if (retVal == null) {
            throw new IllegalArgumentException("Empty RValue");
        }

        // Now check that we got the right thing.
        switch (retVal.getNodeType()) {
            case org.w3c.dom.Node.TEXT_NODE:
            case org.w3c.dom.Node.ATTRIBUTE_NODE:
            case org.w3c.dom.Node.ELEMENT_NODE:
            case org.w3c.dom.Node.CDATA_SECTION_NODE:
                break;
            default:
                throw new IllegalArgumentException("Invalid RValue");

        }
        return retVal;
    }
    
//    private void replaceEndpointRefence(PartnerLinkInstance plval, org.w3c.dom.Node rvalue) {
//        // Eventually wrapping with service-ref element if we've been directly assigned some
//        // value that isn't wrapped.
//        if (rvalue.getNodeType() == org.w3c.dom.Node.TEXT_NODE ||
//                (rvalue.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE && !rvalue.getLocalName().equals("service-ref"))) {
//            Document doc = DOMUtils.newDocument();
//            Element serviceRef = doc.createElementNS(Namespaces.WSBPEL2_0_FINAL_SERVREF, "service-ref");
//            doc.appendChild(serviceRef);
//            NodeList children = rvalue.getChildNodes();
//            for (int m = 0; m < children.getLength(); m++) {
//            	org.w3c.dom.Node child = children.item(m);
//                serviceRef.appendChild(doc.importNode(child, true));
//            }
//            rvalue = serviceRef;
//        }
//
//        getBpelRuntimeContext().writeEndpointReference(plval, (Element)rvalue);
//    }

    private Element replaceElement(Element lval, Element ptr, Element src,
                                boolean keepSrcElement) {
        Document doc = ptr.getOwnerDocument();
        org.w3c.dom.Node parent = ptr.getParentNode();
        if (keepSrcElement) {
            Element replacement = (Element)doc.importNode(src, true);
            parent.replaceChild(replacement, ptr);
            return (lval == ptr) ? replacement :  lval;
        }

        Element replacement = doc.createElementNS(ptr.getNamespaceURI(), ptr.getLocalName());
        NodeList nl = src.getChildNodes();
        for (int i = 0; i < nl.getLength(); ++i)
            replacement.appendChild(doc.importNode(nl.item(i), true));
        NamedNodeMap attrs = src.getAttributes();
        for (int i = 0; i < attrs.getLength(); ++i)
            if (!((Attr)attrs.item(i)).getName().startsWith("xmlns"))
                replacement.setAttributeNodeNS((Attr)doc.importNode(attrs.item(i), true));
        parent.replaceChild(replacement, ptr);
        DOMUtils.copyNSContext(ptr, replacement);
        
        return (lval == ptr) ? replacement :  lval;
    }

    private Element copyInto(Element lval, Element ptr, Element src) {
        ptr.appendChild(ptr.getOwnerDocument().importNode(src, true));
        return lval;
    }

    /**
     * isInsert flag desginates this as an 'element' type insertion, which
     * requires insert the actual element value, rather than it's children
     *
     * @return
     * @throws FaultException
     */
    private org.w3c.dom.Node replaceContent(org.w3c.dom.Node lvalue, org.w3c.dom.Node lvaluePtr, String rvalue) {
        Document d = lvaluePtr.getOwnerDocument();

        switch (lvaluePtr.getNodeType()) {
            case org.w3c.dom.Node.ELEMENT_NODE:

                // Remove all the children.
                while (lvaluePtr.hasChildNodes())
                    lvaluePtr.removeChild(lvaluePtr.getFirstChild());

                // Append a new text node.
                lvaluePtr.appendChild(d.createTextNode(rvalue));

                // If lvalue is a text, removing all lvaluePtr children had just removed it
                // so we need to rebuild it as a child of lvaluePtr
                if (lvalue instanceof Text)
                    lvalue = lvaluePtr.getFirstChild();
                break;

            case org.w3c.dom.Node.TEXT_NODE:

            	org.w3c.dom.Node newval = d.createTextNode(rvalue);
                // Replace ourselves .
                lvaluePtr.getParentNode().replaceChild(newval, lvaluePtr);

                // A little kludge, let our caller know that the root element has changed.
                // (used for assignment to a simple typed variable)
                if (lvalue.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                    // No children, adding an empty text children to point to
                    if (lvalue.getFirstChild() == null) {
                        Text txt = lvalue.getOwnerDocument().createTextNode("");
                        lvalue.appendChild(txt);
                    }
                    if (lvalue.getFirstChild().getNodeType() == org.w3c.dom.Node.TEXT_NODE)
                        lvalue = lvalue.getFirstChild();
                }
                if (lvalue.getNodeType() == org.w3c.dom.Node.TEXT_NODE && ((Text) lvalue).getWholeText().equals(
                        ((Text) lvaluePtr).getWholeText()))
                    lvalue = lvaluePtr = newval;
                break;

            case org.w3c.dom.Node.ATTRIBUTE_NODE:

                ((Attr) lvaluePtr).setValue(rvalue);
                break;

            default:
                // This could occur if the expression language selects something
                // like
                // a PI or a CDATA.
                throw new IllegalArgumentException(
                    "Could not replace content, illegal node type: " + lvaluePtr.getNodeType());
        }

        return lvalue;
    }

    private org.w3c.dom.Node evalQuery(org.w3c.dom.Node data, String part, String expression) {
        assert data != null;

        if (part != null) {
            QName partName = new QName(null, part);
            org.w3c.dom.Node qualLVal = DOMUtils.findChildByName((Element) data, partName);
//            if (part.type instanceof OElementVarType) {
//                QName elName = ((OElementVarType) part.type).elementType;
//                qualLVal = DOMUtils.findChildByName((Element) qualLVal, elName);
//            } else if (part.type == null) {
//                // Special case of header parts never referenced in the WSDL def
//                if (qualLVal != null && qualLVal.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE
//                        && ((Element)qualLVal).getAttribute("headerPart") != null)
//                    qualLVal = DOMUtils.getFirstChildElement((Element) qualLVal);
//                // The needed part isn't there, dynamically creating it
//                if (qualLVal == null) {
//                    qualLVal = data.getOwnerDocument().createElementNS(null, part);
//                    ((Element)qualLVal).setAttribute("headerPart", "true");
//                    data.appendChild(qualLVal);
//                }
//            }
            data = qualLVal;
        }

        if (expression != null) {
            // Neat little trick....
            data = getAsElement(evaluateExpression(expression));
        }

        return data;
    }
    
    private String evaluateExpression(String expression) {
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
    		throw new IllegalArgumentException(
				"Could not evaluate expression " + expression, t);
    	}
    }

//    private Object getValue(From from) {
//    	if (from instanceof VariableRef) {
//    		VariableRef fromPart = (VariableRef) from;
//    		String fromValue = getVariableValue(fromPart.getVariable());
//    		if (fromPart.getPart() == null) {
//    			return fromValue;
//    		}
//            try {
//	        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//	    		Document fromDocument = factory.newDocumentBuilder().parse(new ByteArrayInputStream(fromValue.getBytes()));
//	        	return DOMUtils.findChildByName((Element) fromDocument.getDocumentElement(), new QName(fromPart.getPart()));
//            } catch (Throwable t) {
//            	throw new IllegalArgumentException("Could not get value", t);
//            }
//    	} else if (from instanceof Literal) {
//    		return ((Literal) from).getValue();
//    	} else if (from instanceof Expression) {
//    		String expression = ((Expression) from).getExpression();
//    		try {
//	    		XPathReturnValueEvaluator evaluator = new XPathReturnValueEvaluator();
//	    		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//				Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(expression.getBytes()));
//				org.w3c.dom.Node node = document.getFirstChild().getFirstChild();
//		        if (node == null) {
//		            throw new IllegalStateException();
//		        }
//		        if (node.getNodeType() != org.w3c.dom.Node.TEXT_NODE) {
//		            throw new IllegalArgumentException("Unexpected node type for XPath");
//		        }
//		        String xpathString = node.getNodeValue();
//	    		evaluator.setExpression(xpathString);
//	    		ProcessContext processContext = new ProcessContext();
//	    		processContext.setNodeInstance(this);
//    			return (String) evaluator.evaluate(getProcessInstance().getWorkingMemory(), processContext, XPathConstants.STRING);
//    		} catch (Throwable t) {
//    			throw new IllegalArgumentException("Could not evaluate expression " + expression, t);
//    		}
//    	} else {
//    		throw new UnsupportedOperationException();
//    	}
//    }
//    
//    private String copy(Object fromValue, String toValue, String toPart) {
//        try {
//        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//    		Document toDocument = factory.newDocumentBuilder().parse(new ByteArrayInputStream(toValue.getBytes()));
//        	Element to = DOMUtils.findChildByName((Element) toDocument.getDocumentElement(), new QName(toPart));
//        	if (fromValue instanceof Element) {
//        		Element from = (Element) fromValue;
//	        	Element replacement = toDocument.createElementNS(from.getNamespaceURI(), from.getNodeName());
//	            NodeList nl = from.getChildNodes();
//	            for (int i = 0; i < nl.getLength(); ++i)
//	                replacement.appendChild(toDocument.importNode(nl.item(i), true));
//	            NamedNodeMap attrs = from.getAttributes();
//	            for (int i = 0; i < attrs.getLength(); ++i) {
//	                if (!((Attr)attrs.item(i)).getName().startsWith("xmlns")) {
//	                    replacement.setAttributeNodeNS((Attr) toDocument.importNode(attrs.item(i), true));
//	                }
//	            }
//	            if (to == null) {
//	            	toDocument.getDocumentElement().appendChild(replacement);
//	            } else {
//	            	to.getParentNode().replaceChild(replacement, to);
//	            }
//        	} else {
//        		Element replacement = toDocument.createElementNS(null, toPart);
//        		replacement.setTextContent((String) fromValue);
//        		if (to == null) {
//        			toDocument.getDocumentElement().appendChild(replacement);
//	            } else {
//	            	to.getParentNode().replaceChild(replacement, to);
//	            }
//        	}
//        	return DOMUtils.domToString(toDocument.getDocumentElement());
//        } catch (Throwable t) {
//        	throw new IllegalArgumentException("Could not copy value", t);
//        }
//    }
//    
//    private String initializeVariable(String variable) {
//    	VariableScopeInstance variableScopeInstance = (VariableScopeInstance)
//			resolveContextInstance(VariableScope.VARIABLE_SCOPE, variable);
//		if (variableScopeInstance != null) {
//			DataType dataType = ((VariableScope) variableScopeInstance.getContext()).findVariable(variable).getType();
//			if (dataType instanceof XMLDataType) {
//				String type = ((XMLDataType) dataType).getTypeDefinition();
//				type = type.substring(type.lastIndexOf("}") + 1);
//				return "<" + type + "></" + type + ">";
//			}
//		}
//        return "";
//    }
    
    private String getVariableType(String variable) {
    	VariableScopeInstance variableScopeInstance = (VariableScopeInstance)
			resolveContextInstance(VariableScope.VARIABLE_SCOPE, variable);
		if (variableScopeInstance != null) {
			DataType dataType = ((VariableScope) variableScopeInstance.getContext()).findVariable(variable).getType();
			if (dataType instanceof XMLDataType) {
				String type = ((XMLDataType) dataType).getTypeDefinition();
				int index = type.lastIndexOf("}");
				if (index != -1) {
					type = type.substring(index + 1);
				}
				return type;
			}
		}
        return null;
    }
    
    private Element getAsElement(String value) {
        try {
        	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        	Document toDocument = factory.newDocumentBuilder().parse(new ByteArrayInputStream(value.getBytes()));
        	return toDocument.getDocumentElement();
        } catch (Throwable t) {
        	throw new IllegalArgumentException(
    			"Could not parse value " + value, t);
        }
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
    
    private void setVariableValue(String variable, Object value) {
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
