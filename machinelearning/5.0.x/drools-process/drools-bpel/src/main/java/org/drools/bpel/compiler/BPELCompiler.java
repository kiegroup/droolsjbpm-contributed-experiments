package org.drools.bpel.compiler;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.ode.bpel.compiler.bom.Activity;
import org.apache.ode.bpel.compiler.bom.AssignActivity;
import org.apache.ode.bpel.compiler.bom.BpelObjectFactory;
import org.apache.ode.bpel.compiler.bom.Catch;
import org.apache.ode.bpel.compiler.bom.Copy;
import org.apache.ode.bpel.compiler.bom.EmptyActivity;
import org.apache.ode.bpel.compiler.bom.FlowActivity;
import org.apache.ode.bpel.compiler.bom.From;
import org.apache.ode.bpel.compiler.bom.IfActivity;
import org.apache.ode.bpel.compiler.bom.InvokeActivity;
import org.apache.ode.bpel.compiler.bom.Link;
import org.apache.ode.bpel.compiler.bom.LinkSource;
import org.apache.ode.bpel.compiler.bom.LinkTarget;
import org.apache.ode.bpel.compiler.bom.OnAlarm;
import org.apache.ode.bpel.compiler.bom.OnMessage;
import org.apache.ode.bpel.compiler.bom.PickActivity;
import org.apache.ode.bpel.compiler.bom.Process;
import org.apache.ode.bpel.compiler.bom.ReceiveActivity;
import org.apache.ode.bpel.compiler.bom.RepeatUntilActivity;
import org.apache.ode.bpel.compiler.bom.ReplyActivity;
import org.apache.ode.bpel.compiler.bom.RethrowActivity;
import org.apache.ode.bpel.compiler.bom.ScopeActivity;
import org.apache.ode.bpel.compiler.bom.SequenceActivity;
import org.apache.ode.bpel.compiler.bom.SwitchActivity;
import org.apache.ode.bpel.compiler.bom.TerminateActivity;
import org.apache.ode.bpel.compiler.bom.ThrowActivity;
import org.apache.ode.bpel.compiler.bom.To;
import org.apache.ode.bpel.compiler.bom.WaitActivity;
import org.apache.ode.bpel.compiler.bom.WhileActivity;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.StreamUtils;
import org.drools.RuleBase;
import org.drools.bpel.core.BPELActivity;
import org.drools.bpel.core.BPELAssign;
import org.drools.bpel.core.BPELEmpty;
import org.drools.bpel.core.BPELExit;
import org.drools.bpel.core.BPELFaultHandler;
import org.drools.bpel.core.BPELFlow;
import org.drools.bpel.core.BPELIf;
import org.drools.bpel.core.BPELInvoke;
import org.drools.bpel.core.BPELPick;
import org.drools.bpel.core.BPELProcess;
import org.drools.bpel.core.BPELReceive;
import org.drools.bpel.core.BPELRepeatUntil;
import org.drools.bpel.core.BPELReply;
import org.drools.bpel.core.BPELRethrow;
import org.drools.bpel.core.BPELScope;
import org.drools.bpel.core.BPELSequence;
import org.drools.bpel.core.BPELThrow;
import org.drools.bpel.core.BPELWait;
import org.drools.bpel.core.BPELWhile;
import org.drools.bpel.core.BPELActivity.SourceLink;
import org.drools.bpel.core.BPELActivity.TargetLink;
import org.drools.bpel.xpath.XMLDataType;
import org.drools.bpel.xpath.XPathDialectConfiguration;
import org.drools.common.AbstractRuleBase;
import org.drools.compiler.DroolsError;
import org.drools.compiler.PackageBuilder;
import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.compiler.ProcessBuilder;
import org.drools.process.core.context.variable.Variable;
import org.drools.process.core.context.variable.VariableScope;
import org.drools.process.core.datatype.DataType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

public class BPELCompiler {
	
	public void loadProcess(RuleBase ruleBase, String fileName) {
		try {
			BPELCompiler compiler = new BPELCompiler();
			BPELProcess process = compiler.compileProcess(
				BPELCompiler.class.getResource(fileName));
			PackageBuilderConfiguration configuration = new PackageBuilderConfiguration();
			configuration.addDialect("XPath2.0", new XPathDialectConfiguration());
			PackageBuilder packageBuilder = new PackageBuilder(configuration);
			ProcessBuilder processBuilder = new ProcessBuilder(packageBuilder);
			processBuilder.buildProcess(process, fileName);
	        if (!processBuilder.getErrors().isEmpty()) {
	        	for (DroolsError error: processBuilder.getErrors()) {
	        		System.err.println(error);
	        	}
	        	throw new IllegalArgumentException("Could not build process " + fileName);
	        }
	        ((AbstractRuleBase) ruleBase).addProcess(process);
		} catch (Throwable t) {
			throw new IllegalArgumentException("Could not load process " + fileName, t);
		}
	}

	public BPELProcess compileProcess(URL bpelFile) throws Exception {
		InputSource isrc = new InputSource(new ByteArrayInputStream(StreamUtils.read(bpelFile)));
		Process process = BpelObjectFactory.getInstance().parse(isrc, bpelFile.toURI());
		return compileProcess(process);
	}
	
	private BPELProcess compileProcess(Process process) {
		BPELProcess result = new BPELProcess();
		result.setName(process.getName());
		result.setId(process.getTargetNamespace());
		result.setPackageName("org.drools.bpel");
		result.setVersion("1.0");
		result.setNamespaceContext(process.getNamespaceContext());
		BPELActivity activity = compileActivity(process.getRootActivity());
		result.setActivity(activity);
		// variables
		VariableScope variableScope = result.getVariableScope();
        List<Variable> variables = new ArrayList<Variable>();
		for (org.apache.ode.bpel.compiler.bom.Variable variable: process.getVariables()) {
	        Variable bpelVariable =	new Variable();
	        bpelVariable.setName(variable.getName());
	        bpelVariable.setType(getDataType(variable.getTypeName().toString()));
	        variables.add(bpelVariable);
		}
		variableScope.setVariables(variables);
		// fault handlers
		if (process.getFaultHandler() != null) {
	        List<BPELFaultHandler> faultHandlers = new ArrayList<BPELFaultHandler>();
	        for (Catch catcher: process.getFaultHandler().getCatches()) {
	            BPELFaultHandler faultHandler = new BPELFaultHandler();
	            faultHandler.setFaultName(
            		catcher.getFaultName() == null ? null : catcher.getFaultName().toString());
	            faultHandler.setFaultVariable(catcher.getFaultVariable());
	            faultHandler.setActivity(compileActivity(catcher.getActivity()));
	            faultHandlers.add(faultHandler);
	        }
	        result.setFaultHandlers(faultHandlers);
		}
		return result;
	}
	
	private BPELActivity compileActivity(Activity activity) {
		BPELActivity result = null;
		if (activity instanceof ReceiveActivity) {
			ReceiveActivity receiveActivity = (ReceiveActivity) activity;
			BPELReceive receive = new BPELReceive();
			receive.setCreateInstance(receiveActivity.isCreateInstance());
			receive.setOperation(
				receiveActivity.getPartnerLink(),
				receiveActivity.getPortType().toString(),
				receiveActivity.getOperation());
			receive.setVariable(receiveActivity.getVariable());
			result = receive;
		} else if (activity instanceof ReplyActivity) {
			ReplyActivity replyActivity = (ReplyActivity) activity;
			BPELReply reply = new BPELReply();
			reply.setPartnerLink(replyActivity.getPartnerLink());
			reply.setPortType(replyActivity.getPortType().toString());
			reply.setOperation(replyActivity.getOperation());
			reply.setVariable(replyActivity.getVariable());
			if (replyActivity.getFaultName() != null) {
				reply.setFaultName(replyActivity.getFaultName().toString());
			}
			result = reply;
		} else if (activity instanceof InvokeActivity) {
			InvokeActivity invokeActivity = (InvokeActivity) activity;
			BPELInvoke invoke = new BPELInvoke();
			invoke.setPartnerLink(invokeActivity.getPartnerLink());
			invoke.setPortType(invokeActivity.getPortType().toString());
			invoke.setOperation(invokeActivity.getOperation());
			invoke.setInputVariable(invokeActivity.getInputVar());
			if (invokeActivity.getOutputVar() != null) { 
				invoke.setOutputVariable(invokeActivity.getOutputVar());
			}
			result = invoke;
		} else if (activity instanceof SequenceActivity) {
			SequenceActivity sequenceActivity = (SequenceActivity) activity;
			BPELSequence sequence = new BPELSequence();
			List<BPELActivity> subActivities = new ArrayList<BPELActivity>();
			for (Activity subActivity: sequenceActivity.getActivities()) {
				subActivities.add(compileActivity(subActivity));
			}
			sequence.setActivities(subActivities);
			result = sequence;
		} else if (activity instanceof FlowActivity) {
			FlowActivity flowActivity = (FlowActivity) activity;
			BPELFlow flow = new BPELFlow();
			List<BPELActivity> subActivities = new ArrayList<BPELActivity>();
			for (Activity subActivity: flowActivity.getActivities()) {
				subActivities.add(compileActivity(subActivity));
			}
			flow.setActivities(subActivities);
			String[] links = new String[flowActivity.getLinks().size()];
			int i = 0;
			for (Link link: flowActivity.getLinks()) {
				links[i++] = link.getLinkName();
			}
			flow.setLinks(links);
			result = flow;
		} else if (activity instanceof AssignActivity) {
			AssignActivity assignActivity = (AssignActivity) activity;
			BPELAssign assign = new BPELAssign();
			for (Copy copy: assignActivity.getCopies()) {
				BPELAssign.Copy bpelCopy = assign.new Copy();
				From from = copy.getFrom();
				To to = copy.getTo();
				if (from.isVariableVal()) {
					BPELAssign.VariableRef bpelFrom = assign.new VariableRef(
						from.getAsVariableVal().getVariable(), 
						from.getAsVariableVal().getPart(),
						from.getAsVariableVal().getHeader(),
						from.getAsVariableVal().getLocation() == null ? null : from.getAsVariableVal().getLocation().toString());
					bpelCopy.setFrom(bpelFrom);
				} else if (from.isLiteralVal()) {
					Element literal = from.getAsLiteralVal().getLiteral();
					org.w3c.dom.Node child = literal.getFirstChild();
					short type;
					if (child.getNodeType() == org.w3c.dom.Node.TEXT_NODE && "".equals(child.getTextContent().trim())) {
						child = child.getNextSibling();
					}
					BPELAssign.Literal bpelFrom = assign.new Literal(DOMUtils.domToString(child));
					bpelCopy.setFrom(bpelFrom);
				} else {
					BPELAssign.Expression bpelFrom = assign.new Expression(from.getAsExpression().toString());
					bpelCopy.setFrom(bpelFrom);
				}
				if (to.isVariableVal()) {
					BPELAssign.VariableRef bpelTo = assign.new VariableRef(
						to.getAsVariableVal().getVariable(), 
						to.getAsVariableVal().getPart(),
						to.getAsVariableVal().getHeader(),
						to.getAsVariableVal().getLocation() == null ? null : to.getAsVariableVal().getLocation().toString());
					bpelCopy.setTo(bpelTo);
				} else {
					String expression = to.getAsExpression().toString();
					try {
						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
						Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(expression.getBytes()));
						expression = DOMUtils.domToString(document.getFirstChild().getFirstChild());
						String varName;
		                String partName;
		                int dotloc = expression.indexOf('.');
		                if (dotloc == -1) {
		                    varName = expression;
		                    partName = null;
		                } else {
		                    varName = expression.substring(1, dotloc);
		                    partName = expression.substring(dotloc + 1);
		                }
						BPELAssign.VariableRef bpelTo = assign.new VariableRef(varName, partName, null, null);
						bpelCopy.setTo(bpelTo);
					} catch (Throwable t) {
						throw new IllegalArgumentException(
							"Could not parse expression " + expression, t);
					}
				}
				assign.addCopy(bpelCopy);
			}
			result = assign;
		} else if (activity instanceof EmptyActivity) {
			result = new BPELEmpty();
		} else if (activity instanceof ThrowActivity) {
			ThrowActivity throwActivity = (ThrowActivity) activity;
			BPELThrow bpelThrow = new BPELThrow();
			bpelThrow.setFaultName(throwActivity.getFaultName().toString());
			bpelThrow.setFaultVariable(throwActivity.getFaultVariable());
			result = bpelThrow;
		} else if (activity instanceof RethrowActivity) {
			result = new BPELRethrow();
		} else if (activity instanceof PickActivity) {
			PickActivity pickActivity = (PickActivity) activity;
			BPELPick pick = new BPELPick();
			pick.setCreateInstance(pickActivity.isCreateInstance());
			for (OnMessage onMessage: pickActivity.getOnMessages()) {
				BPELPick.OnMessage bpelOnMessage = pick.new OnMessage();
				bpelOnMessage.setPartnerLink(onMessage.getPartnerLink());
				bpelOnMessage.setPortType(onMessage.getPortType().toString());
				bpelOnMessage.setOperation(onMessage.getOperation());
				bpelOnMessage.setVariable(onMessage.getVariable());
				bpelOnMessage.setActivity(compileActivity(onMessage.getActivity()));
				pick.addOnMessage(bpelOnMessage);
			}
			for (OnAlarm onAlarm: pickActivity.getOnAlarms()) {
				BPELPick.OnAlarm bpelOnAlarm = pick.new OnAlarm();
				if (onAlarm.getFor() != null) {
					try {
						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
						Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(onAlarm.getFor().toString().getBytes()));
						String expression = DOMUtils.domToString(document.getFirstChild().getFirstChild());
						bpelOnAlarm.setForExpression(expression);
					} catch (Throwable t) {
						throw new IllegalArgumentException(
							"Could not parse for expression", t);
					}
				}
				if (onAlarm.getUntil() != null) {
					try {
						DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
						Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(onAlarm.getUntil().toString().getBytes()));
						String expression = DOMUtils.domToString(document.getFirstChild().getFirstChild());
						bpelOnAlarm.setUntilExpression(expression);
					} catch (Throwable t) {
						throw new IllegalArgumentException(
							"Could not parse for expression", t);
					}
				}
				bpelOnAlarm.setActivity(compileActivity(onAlarm.getActivity()));
				pick.addOnAlarm(bpelOnAlarm);
			}
			result = pick;
		} else if (activity instanceof ScopeActivity) {
			ScopeActivity scopeActivity = (ScopeActivity) activity;
			BPELScope scope = new BPELScope();
			VariableScope variableScope = scope.getVariableScope();
	        List<Variable> variables = new ArrayList<Variable>();
			for (org.apache.ode.bpel.compiler.bom.Variable variable: scopeActivity.getScope().getVariables()) {
		        Variable bpelVariable =	new Variable();
		        bpelVariable.setName(variable.getName());
		        bpelVariable.setType(getDataType(variable.getTypeName().toString()));
		        variables.add(bpelVariable);
			}
			variableScope.setVariables(variables);
			List<BPELFaultHandler> faultHandlers = new ArrayList<BPELFaultHandler>();
			if (scopeActivity.getScope().getFaultHandler() != null) {
		        for (Catch catcher: scopeActivity.getScope().getFaultHandler().getCatches()) {
		            BPELFaultHandler faultHandler = new BPELFaultHandler();
		            faultHandler.setFaultName(
	            		catcher.getFaultName() == null ? null : catcher.getFaultName().toString());
		            faultHandler.setFaultVariable(catcher.getFaultVariable());
		            faultHandler.setActivity(compileActivity(catcher.getActivity()));
		            faultHandlers.add(faultHandler);
		        }
		        scope.setFaultHandlers(faultHandlers);
			}
			scope.setActivity(compileActivity(scopeActivity.getChildActivity()));
			result = scope;
		} else if (activity instanceof SwitchActivity) {
			SwitchActivity switchActivity = (SwitchActivity) activity;
			BPELIf bpelSwitch = new BPELIf();
			for (SwitchActivity.Case bpelCase: switchActivity.getCases()) {
				bpelSwitch.addCase(bpelCase.getCondition().toString(), compileActivity(bpelCase.getActivity()));
			}
			result = bpelSwitch;
		} else if (activity instanceof IfActivity) {
			IfActivity ifActivity = (IfActivity) activity;
			BPELIf bpelSwitch = new BPELIf();
			bpelSwitch.addCase(ifActivity.getCondition().toString(), compileActivity(ifActivity.getActivity()));
			for (IfActivity.Case bpelCase: ifActivity.getCases()) {
				bpelSwitch.addCase(
					bpelCase.getCondition() == null ? null : bpelCase.getCondition().toString(), 
					compileActivity(bpelCase.getActivity()));
			}
			result = bpelSwitch;
		} else if (activity instanceof WaitActivity) {
			WaitActivity waitActivity = (WaitActivity) activity;
			BPELWait wait = new BPELWait();
			if (waitActivity.getFor() != null) {
				try {
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(waitActivity.getFor().toString().getBytes()));
					String expression = DOMUtils.domToString(document.getFirstChild().getFirstChild());
					wait.setForExpression(expression);
				} catch (Throwable t) {
					throw new IllegalArgumentException(
						"Could not parse for expression", t);
				}
			}
			if (waitActivity.getUntil() != null) {
				try {
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(waitActivity.getUntil().toString().getBytes()));
					String expression = DOMUtils.domToString(document.getFirstChild().getFirstChild());
					wait.setUntilExpression(expression);
				} catch (Throwable t) {
					throw new IllegalArgumentException(
						"Could not parse for expression", t);
				}
			}
			result = wait;
		} else if (activity instanceof WhileActivity) {
			WhileActivity whileActivity = (WhileActivity) activity;
			BPELWhile bpelWhile = new BPELWhile();
			bpelWhile.setActivity(
				whileActivity.getCondition().toString(),
				compileActivity(whileActivity.getActivity()));
			result = bpelWhile;
		} else if (activity instanceof RepeatUntilActivity) {
			RepeatUntilActivity repeatUntilActivity = (RepeatUntilActivity) activity;
			BPELRepeatUntil bpelRepeatUntil = new BPELRepeatUntil();
			bpelRepeatUntil.setActivity(compileActivity(repeatUntilActivity.getActivity()));
			bpelRepeatUntil.setCondition(repeatUntilActivity.getCondition().toString());
			result = bpelRepeatUntil;
		} else if (activity instanceof TerminateActivity) {
			result = new BPELExit();
		} else {
			// TODO: compensate
			// BPEL2.0: ForEach, CompensateScope, Rethrow, Validate
			throw new IllegalArgumentException("Unknown activity type " + activity.getClass());
		}
		result.setName(activity.getName());
		SourceLink[] sourceLinks = new SourceLink[activity.getLinkSources().size()];
		int i = 0;
		for (LinkSource linkSource: activity.getLinkSources()) {
			sourceLinks[i] = new SourceLink(linkSource.getLinkName());
			if (linkSource.getTransitionCondition() != null) {
				sourceLinks[i].setTransitionCondition(linkSource.getTransitionCondition().getTextValue());
			}
			i++;
		}
		if (sourceLinks.length > 0) {
			result.setSourceLinks(sourceLinks);
		}
		TargetLink[] targetLinks = new TargetLink[activity.getLinkTargets().size()];
		i = 0;
		for (LinkTarget linkTarget: activity.getLinkTargets()) {
			targetLinks[i++] = new TargetLink(linkTarget.getLinkName());
		}
		if (targetLinks.length > 0) {
			result.setTargetLinks(targetLinks);
		}
		return result;
	}
	
	private DataType getDataType(String typeName) {
		return new XMLDataType(typeName);
	}

}
