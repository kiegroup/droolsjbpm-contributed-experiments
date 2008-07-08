package org.drools.jpdl.core.node;

import java.util.Collection;

import org.jbpm.graph.action.Script;

public class Join extends JpdlNode {

    private static final long serialVersionUID = 1L;
    
    private String parentLockMode;
    private boolean isDiscriminator = false;
    private Collection<String> tokenNames = null;
    private Script script = null;
    private int nOutOfM = -1;
    
    public String getParentLockMode() {
        return parentLockMode;
    }
    
    public void setParentLockMode(String parentLockMode) {
        this.parentLockMode = parentLockMode;
    }
    
    public boolean isDiscriminator() {
        return isDiscriminator;
    }
    
    public void setDiscriminator(boolean isDiscriminator) {
        this.isDiscriminator = isDiscriminator;
    }
    
    public Collection<String> getTokenNames() {
        return tokenNames;
    }
    
    public void setTokenNames(Collection<String> tokenNames) {
        this.tokenNames = tokenNames;
    }
    
    public Script getScript() {
        return script;
    }
    
    public void setScript(Script script) {
        this.script = script;
    }
    
    public int getNOutOfM() {
        return nOutOfM;
    }
    
    public void setNOutOfM(int outOfM) {
        nOutOfM = outOfM;
    }

}
