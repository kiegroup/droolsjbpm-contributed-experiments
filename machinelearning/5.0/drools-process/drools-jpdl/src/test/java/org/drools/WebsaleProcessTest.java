package org.drools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.jpdl.JpdlParser;
import org.drools.jpdl.core.JpdlProcess;
import org.drools.process.core.context.variable.Variable;
import org.drools.process.core.context.variable.VariableScope;
import org.drools.process.core.datatype.impl.type.StringDataType;
import org.drools.process.core.validation.ProcessValidationError;
import org.drools.process.instance.impl.demo.UIWorkItemHandler;
import org.drools.rule.Package;

public class WebsaleProcessTest {

	public static void main(String[] args) throws Exception {
	    JpdlParser parser = new JpdlParser();
	    JpdlProcess process = parser.loadJpdlProcess("websale/processdefinition.xml");
	    VariableScope variableScope = (VariableScope)
	        process.getDefaultContext(VariableScope.VARIABLE_SCOPE);
	    // TODO: do we really need to define all process variables ?
	    List<Variable> variables = new ArrayList<Variable>();
	    Variable item = new Variable();
	    item.setName("item");
	    item.setType(new StringDataType());
	    variables.add(item);
	    Variable quantity = new Variable();
	    quantity.setName("quantity");
	    quantity.setType(new StringDataType());
        variables.add(quantity);
        Variable address = new Variable();
        address.setName("address");
        address.setType(new StringDataType());
        variables.add(address);
	    variableScope.setVariables(variables);
	    ProcessValidationError[] errors = parser.getErrors();
	    for (ProcessValidationError error: errors) {
            System.err.println(error);
        }
        if (errors.length != 0) {
            throw new IllegalArgumentException("Errors while parsing websale process");
        }
        
        RuleBase ruleBase = RuleBaseFactory.newRuleBase();
        Package p = new Package("com.sample");
        p.addProcess(process);
        ruleBase.addPackage( p );
        
        WorkingMemory workingMemory = ruleBase.newStatefulSession();
        UIWorkItemHandler uiHandler = new UIWorkItemHandler();
        workingMemory.getWorkItemManager().registerWorkItemHandler(
            "JpdlTask", uiHandler);
        uiHandler.setVisible(true);
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("item", "Drools Manual");
        parameters.put("quantity", "2");
        parameters.put("item", "Drools Manual");
        parameters.put("address", 
            "Red Hat Corporate Headquarters, " +
            "1801 Varsity Drive, " +
            "Raleigh, North Carolina 27606" +
            "USA");
        workingMemory.startProcess("websale", parameters);
    }

}
