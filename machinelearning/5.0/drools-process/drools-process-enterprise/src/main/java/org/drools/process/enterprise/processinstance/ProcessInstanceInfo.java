package org.drools.process.enterprise.processinstance;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Transient;

import org.drools.marshalling.InputMarshaller;
import org.drools.marshalling.MarshallerReaderContext;
import org.drools.marshalling.MarshallerWriteContext;
import org.drools.marshalling.OutputMarshaller;
import org.drools.process.instance.ProcessInstance;
import org.drools.ruleflow.instance.RuleFlowProcessInstance;

@Entity
public class ProcessInstanceInfo {

	private @Id @GeneratedValue(strategy=GenerationType.AUTO) Long processInstanceId;
	private String processId;
	private @Lob byte[] processInstanceByteArray;
	private @Transient ProcessInstance processInstance;
	
	public long getId() {
		return processInstanceId;
	}
	
	public String getProcessId() {
		return processId;
	}
	
	public ProcessInstance getProcessInstance() {
		if (processInstance == null) {
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(processInstanceByteArray);
				MarshallerReaderContext context = new MarshallerReaderContext(bais, null, null, null);
				processInstance = InputMarshaller.readProcessInstance(context);
				context.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new IllegalArgumentException(
					"IOException while loading process instance: " + e.getMessage());
			}
		}
		return processInstance;
	}
	
	public void setProcessInstance(ProcessInstance processInstance) {
		this.processInstance = processInstance;
//		this.processInstanceId = processInstance.getId();
		this.processId = processInstance.getProcessId();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			MarshallerWriteContext context = new MarshallerWriteContext(baos, null, null, null, null);
			OutputMarshaller.writeProcessInstance(context, (RuleFlowProcessInstance) processInstance);
			context.close();
			this.processInstanceByteArray = baos.toByteArray();
		} catch (IOException e) {
			throw new IllegalArgumentException(
				"IOException while storing process instance " + processInstance.getId() + ": " + e.getMessage());
		}
	}
	
}
