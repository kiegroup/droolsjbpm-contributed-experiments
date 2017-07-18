package org.jbpm.runtime.manager.mapdb.model;

import java.io.Serializable;

public class MapDBContextMappingInfo implements Serializable, Comparable<MapDBContextMappingInfo> {

    private static final long serialVersionUID = 533985957655465840L;
    
    private String contextId;
    private Long ksessionId;
    private String ownerId;

    public MapDBContextMappingInfo() {
        
    }

    public MapDBContextMappingInfo(String contextId, Long ksessionId, String ownerId) {
        this.contextId = contextId;
        this.ksessionId = ksessionId;
        this.ownerId = ownerId;
    }
    
    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public Long getKsessionId() {
        return ksessionId;
    }

    public void setKsessionId(Long ksessionId) {
        this.ksessionId = ksessionId;
    }
        
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((contextId == null) ? 0 : contextId.hashCode());
        result = prime * result + ((ksessionId == null) ? 0 : ksessionId.hashCode());
        result = prime * result + ((ownerId == null) ? 0 : ownerId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MapDBContextMappingInfo other = (MapDBContextMappingInfo) obj;
        if (contextId == null) {
            if (other.contextId != null)
                return false;
        } else if (!contextId.equals(other.contextId))
            return false;
        if (ksessionId == null) {
            if (other.ksessionId != null)
                return false;
        } else if (!ksessionId.equals(other.ksessionId))
            return false;
        if (ownerId == null) {
            if (other.ownerId != null)
                return false;
        } else if (!ownerId.equals(other.ownerId))
            return false;
        return true;
    }

    @Override
    public int compareTo(MapDBContextMappingInfo other) {
        if (other == null) {
            return 1;
        }
        int sum = 0;
        if (this.ksessionId != null && other.ksessionId == null) {
            sum += 100;
        } else if (this.ksessionId == null && other.ksessionId != null) {
            sum -= 100;
        } else if (this.ksessionId != null && other.ksessionId != null) {
            sum += (this.ksessionId.compareTo(other.ksessionId) * 100);
        }
        
        if (this.contextId != null && other.contextId == null) {
            sum += 100;
        } else if (this.contextId == null && other.contextId != null) {
            sum -= 100;
        } else if (this.contextId != null && other.contextId != null) {
            sum += (this.contextId.compareTo(other.contextId) * 100);
        }
        
        if (this.ownerId != null && other.ownerId == null) {
            sum += 100;
        } else if (this.ownerId == null && other.ownerId != null) {
            sum -= 100;
        } else if (this.ownerId != null && other.ownerId != null) {
            sum += (this.ownerId.compareTo(other.ownerId) * 100);
        }
        return sum;
    }

}
