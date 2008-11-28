package org.drools.process.workitem.email;

public class Email {
	
    private Message    message;
    private Connection connection;

    public String[] getUsedIdentigiers() {
        return null;
    }

    public Email() {
        this.message = new Message();
        this.connection = new Connection();
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((connection == null) ? 0 : connection.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        final Email other = (Email) obj;
        if ( connection == null ) {
            if ( other.connection != null ) return false;
        } else if ( !connection.equals( other.connection ) ) return false;
        if ( message == null ) {
            if ( other.message != null ) return false;
        } else if ( !message.equals( other.message ) ) return false;
        return true;
    }

}
