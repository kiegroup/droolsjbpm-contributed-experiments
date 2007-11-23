package benchmarks.dispatch.fact.independent;

import java.util.Date;

/*
 * Copyright 2007 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class WorkerPosition {
    private String workerId;
    private Double latitude, longitude;
    private Date positionObservationDate;

    public WorkerPosition() {

    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public Date getPositionObservationDate() {
        return positionObservationDate;
    }

    public void setPositionObservationDate(Date positionObservationDate) {
        this.positionObservationDate = positionObservationDate;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((workerId == null) ? 0 : workerId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        final WorkerPosition other = (WorkerPosition) obj;
        if ( workerId == null ) {
            if ( other.workerId != null ) return false;
        } else if ( !workerId.equals( other.workerId ) ) return false;
        return true;
    }


}
