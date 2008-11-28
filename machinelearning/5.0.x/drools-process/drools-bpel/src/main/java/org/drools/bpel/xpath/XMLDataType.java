package org.drools.bpel.xpath;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.drools.process.core.datatype.impl.type.StringDataType;

public class XMLDataType extends StringDataType {

	private String typeDefinition;
	
	public XMLDataType(String typeDefinition) {
		this.typeDefinition = typeDefinition;
	}
	
	public String getTypeDefinition() {
		return typeDefinition;
	}

	public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
		typeDefinition = input.readUTF();
	}

	public void writeExternal(ObjectOutput output) throws IOException {
		output.writeUTF(typeDefinition);
	}

}
