package org.drools.process.workitem.email;

public class Connection {
	
    private String host;
    private String port;
    private String userName;
    private String password;

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((port == null) ? 0 : port.hashCode());
        result = prime * result + ((userName == null) ? 0 : userName.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        final Connection other = (Connection) obj;
        if ( host == null ) {
            if ( other.host != null ) return false;
        } else if ( !host.equals( other.host ) ) return false;
        if ( password == null ) {
            if ( other.password != null ) return false;
        } else if ( !password.equals( other.password ) ) return false;
        if ( port == null ) {
            if ( other.port != null ) return false;
        } else if ( !port.equals( other.port ) ) return false;
        if ( userName == null ) {
            if ( other.userName != null ) return false;
        } else if ( !userName.equals( other.userName ) ) return false;
        return true;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
