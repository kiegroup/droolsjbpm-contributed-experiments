package org.drools.bpel.xpath;

import java.io.ByteArrayInputStream;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;

import net.sf.saxon.om.NamespaceConstant;
import net.sf.saxon.xpath.XPathEvaluator;

import org.apache.ode.bpel.elang.xpath20.compiler.Constants;
import org.apache.ode.utils.Namespaces;
import org.drools.bpel.core.BPELProcess;
import org.drools.compiler.ReturnValueDescr;
import org.drools.rule.builder.PackageBuildContext;
import org.drools.rule.builder.ProcessBuildContext;
import org.drools.rule.builder.ReturnValueEvaluatorBuilder;
import org.drools.workflow.instance.impl.ReturnValueConstraintEvaluator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class XPathReturnValueEvaluatorBuilder implements ReturnValueEvaluatorBuilder {

	public void build(PackageBuildContext context,
			ReturnValueConstraintEvaluator returnValueConstraintEvaluator,
			ReturnValueDescr returnValueDescr) {
		try {
			XPathReturnValueEvaluator evaluator = new XPathReturnValueEvaluator();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(returnValueDescr.getText().getBytes()));
			Node node = document.getFirstChild().getFirstChild();
	        if (node == null) {
	            throw new IllegalStateException();
	        }
	        if (node.getNodeType() != Node.TEXT_NODE) {
	            throw new IllegalArgumentException("Unexpected node type for XPath");
	        }
	        String xpathString = node.getNodeValue();
			XPathFactory xpf = XPathFactory.newInstance(NamespaceConstant.OBJECT_MODEL_SAXON);
	        xpf.setXPathFunctionResolver(new XPathFunctionResolver() {
				public XPathFunction resolveFunction(QName functionName, int arity) {
					if (functionName.getNamespaceURI() == null) {
			            throw new IllegalArgumentException("Undeclared namespace for " + functionName);
			        } else if (functionName.getNamespaceURI().equals(Namespaces.WSBPEL2_0_FINAL_EXEC)) {
			            String localName = functionName.getLocalPart();
			            if (Constants.EXT_FUNCTION_GETVARIABLEPROPRTY.equals(localName)) {
			                return new GetVariableProperty();
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
	        	public Object resolveVariable(QName arg0) {
					return "";
				}
	        });
	        XPathEvaluator xpe = (XPathEvaluator) xpf.newXPath();
	        xpe.setNamespaceContext(
        		((BPELProcess) ((ProcessBuildContext) context).getProcess()).getNamespaceContext());
	        xpe.compile(xpathString);
	        evaluator.setExpression(xpathString);
			returnValueConstraintEvaluator.setEvaluator(evaluator);
		} catch (Throwable t) {
			throw new IllegalArgumentException("Could not compile XPath expression " + returnValueDescr.getText(), t);
		}
	}
	
	public class GetVariableProperty implements XPathFunction {
		   
		public Object evaluate(List params) throws XPathFunctionException {
			if (params.size() != 2) {
				throw new IllegalArgumentException(
					"Invalid number of arguments for the getVariable function");
			}
			return "";
		}
	}

}
