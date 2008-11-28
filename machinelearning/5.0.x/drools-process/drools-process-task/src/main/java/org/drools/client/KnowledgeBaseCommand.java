package org.drools.client;

import java.io.Serializable;
import java.util.List;

public class KnowledgeBaseCommand implements Serializable {
    
    private int id;
    
    private KnowledgeBaseCommandName name;    
    
    private List<Object> arguments;
    
    public KnowledgeBaseCommand(int id, KnowledgeBaseCommandName name, List<Object> arguments) {
        super();
        this.id = id;
        this.arguments = arguments;
        this.name = name;
    }        
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public KnowledgeBaseCommandName getName() {
        return name;
    }
    public void setName(KnowledgeBaseCommandName name) {
        this.name = name;
    }
    public List<Object> getArguments() {
        return arguments;
    }
    public void setArguments(List<Object> arguments) {
        this.arguments = arguments;
    }
            
}
