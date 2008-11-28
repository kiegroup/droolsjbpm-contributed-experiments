package org.drools.process.workitem.email;

public class Recipient {
    private String type;

    private String displayName;

    private String email;

    private String mobile;

    public Recipient() {
    }

    public Recipient(String type,
                     String displayName,
                     String email,
                     String mobile) {
        this.type = type;
        this.displayName = displayName;
        this.email = email;
        this.mobile = mobile;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((displayName == null) ? 0 : displayName.hashCode());
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + ((mobile == null) ? 0 : mobile.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        final Recipient other = (Recipient) obj;
        if ( displayName == null ) {
            if ( other.displayName != null ) return false;
        } else if ( !displayName.equals( other.displayName ) ) return false;
        if ( email == null ) {
            if ( other.email != null ) return false;
        } else if ( !email.equals( other.email ) ) return false;
        if ( mobile == null ) {
            if ( other.mobile != null ) return false;
        } else if ( !mobile.equals( other.mobile ) ) return false;
        if ( type == null ) {
            if ( other.type != null ) return false;
        } else if ( !type.equals( other.type ) ) return false;
        return true;
    }

}