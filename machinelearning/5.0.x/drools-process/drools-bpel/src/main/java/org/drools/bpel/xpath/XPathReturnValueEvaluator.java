package org.drools.bpel.xpath;

import java.io.ByteArrayInputStream;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;

import net.sf.saxon.om.NamespaceConstant;
import net.sf.saxon.xpath.XPathEvaluator;

import org.apache.ode.bpel.elang.xpath20.compiler.Constants;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.Namespaces;
import org.drools.WorkingMemory;
import org.drools.bpel.core.BPELProcess;
import org.drools.process.instance.ProcessInstance;
import org.drools.spi.ProcessContext;
import org.drools.spi.ReturnValueEvaluator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XPathReturnValueEvaluator implements ReturnValueEvaluator {
	
	private String expression;
	
	public void setExpression(String expression) {
		this.expression = expression;
	}

	public Object evaluate(WorkingMemory workingMemory, final ProcessContext processContext) throws Exception {
		return evaluate(workingMemory, processContext, XPathConstants.BOOLEAN);
	}
	
	public Object evaluate(WorkingMemory workingMemory, final ProcessContext processContext, QName type) throws Exception {
		XPathFactory xpf = XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON);
        xpf.setXPathFunctionResolver(new XPathFunctionResolver() {
			public XPathFunction resolveFunction(QName functionName, int arity) {
				if (functionName.getNamespaceURI() == null) {
		            throw new IllegalArgumentException("Undeclared namespace for " + functionName);
		        } else if (functionName.getNamespaceURI().equals(Namespaces.WSBPEL2_0_FINAL_EXEC)) {
		            String localName = functionName.getLocalPart();
		            if (Constants.EXT_FUNCTION_GETVARIABLEPROPRTY.equals(localName)) {
		                return new GetVariableProperty(processContext);
		            } else {
		            	//TODO: DoXSLTransform, GetLinkStatus, GetVariableData
		                throw new IllegalArgumentException(
	                		"Unknown Bpel function " + functionName.getLocalPart());
		            }
		        } 
				return null;
			}
        });
        xpf.setXPathVariableResolver(new XPathVariableResolver() {
        	public Object resolveVariable(QName name) {
        		String varName;
                String partName;
                int dotloc = name.getLocalPart().indexOf('.');
                if (dotloc == -1) {
                    varName = name.getLocalPart();
                    partName = null;
                } else {
                    varName = name.getLocalPart().substring(0, dotloc);
                    partName = name.getLocalPart().substring(dotloc + 1);
                }
                String value = (String) processContext.getVariable(varName);
                if (partName == null) {
                	return value;
                }
                try {
	                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    		Document fromDocument = factory.newDocumentBuilder().parse(new ByteArrayInputStream(value.getBytes()));
		        	return DOMUtils.findChildByName((Element) fromDocument.getDocumentElement(), new QName(partName));
                } catch (Throwable t) {
                	throw new IllegalArgumentException(
            			"Could not get part of variable", t);
                }
			}
        });
        XPathEvaluator xpe = (XPathEvaluator) xpf.newXPath();
        xpe.setNamespaceContext(((BPELProcess) ((ProcessInstance)
    		processContext.getNodeInstance().getProcessInstance())
    			.getProcess()).getNamespaceContext());
        XPathExpression xpathExpression = xpe.compile(expression);
		return xpathExpression.evaluate(DOMUtils.newDocument(), type);
	}
	
	public class GetVariableProperty implements XPathFunction {
		
		private ProcessContext processContext;
		   
		public GetVariableProperty(ProcessContext processContext) {
			this.processContext = processContext;
		}
		
		public Object evaluate(List params) throws XPathFunctionException {
			if (params.size() != 2) {
				throw new IllegalArgumentException(
					"Invalid number of arguments for the getVariable function");
			}
			String variableName = (String) params.get(0);
			String propertyName = (String) params.get(1);
			String variableValue = (String) processContext.getVariable(variableName);
			if (variableValue == null) {
				throw new IllegalArgumentException(
					"Could not find variable " + variableName);
			}
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				Document document = factory.newDocumentBuilder().parse(
					new ByteArrayInputStream(variableValue.getBytes()));
		    	Element element = DOMUtils.findChildByName(
	    			(Element) document.getDocumentElement(), new QName(propertyName));
				return DOMUtils.domToString(element.getFirstChild());
			} catch (Throwable t) {
				throw new IllegalArgumentException(
					"Could not parse variable value " + variableValue, t);
			}
		}
	}



}
