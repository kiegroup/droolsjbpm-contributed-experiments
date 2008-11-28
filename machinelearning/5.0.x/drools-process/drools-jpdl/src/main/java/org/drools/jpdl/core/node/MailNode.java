package org.drools.jpdl.core.node;

public class MailNode extends JpdlNode {

    private static final long serialVersionUID = 1L;
    
    private String template;
    private String actors;
    private String to;
    private String subject;
    private String text;
    
    public String getTemplate() {
        return template;
    }
    
    public void setTemplate(String template) {
        this.template = template;
    }
    
    public String getActors() {
        return actors;
    }
    
    public void setActors(String actors) {
        this.actors = actors;
    }
    
    public String getTo() {
        return to;
    }
    
    public void setTo(String to) {
        this.to = to;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
}
