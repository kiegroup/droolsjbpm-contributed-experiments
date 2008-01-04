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

public class Worker {

    public enum Status {
        WAITING_FOR_JOB, DISPATCHED_FOR_JOB, OUT_OF_SERVICE, INACTIVE
    }

    private String      workerId;
    private Status      status;
    private Date        timeAvailable;
    private Double      currentJobLatitude, currentJobLongitude;
    private VehicleSize vehicleSize;
    private Boolean     hasWrench = new Boolean(false);
    private Integer     numberOfSticks, numberOfRocks;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public Date getTimeAvailable() {
        return timeAvailable;
    }

    public void setTimeAvailable(Date timeAvailable) {
        this.timeAvailable = timeAvailable;
    }

    public Double getCurrentJobLatitude() {
        return currentJobLatitude;
    }

    public void setCurrentJobLatitude(Double currentJobLatitude) {
        this.currentJobLatitude = currentJobLatitude;
    }

    public Double getCurrentJobLongitude() {
        return currentJobLongitude;
    }

    public void setCurrentJobLongitude(Double currentJobLongitude) {
        this.currentJobLongitude = currentJobLongitude;
    }

    public Boolean getHasWrench() {
        return hasWrench;
    }

    public void setHasWrench(Boolean hasWrench) {
        this.hasWrench = hasWrench;
    }

    public Integer getNumberOfSticks() {
        return numberOfSticks;
    }

    public void setNumberOfSticks(Integer numberOfSticks) {
        this.numberOfSticks = numberOfSticks;
    }

    public Integer getNumberOfRocks() {
        return numberOfRocks;
    }

    public void setNumberOfRocks(Integer numberOfRocks) {
        this.numberOfRocks = numberOfRocks;
    }

    public VehicleSize getVehicleSize() {
        return vehicleSize;
    }

    public void setVehicleSize(VehicleSize vehicleSize) {
        this.vehicleSize = vehicleSize;
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
        final Worker other = (Worker) obj;
        if ( workerId == null ) {
            if ( other.workerId != null ) return false;
        } else if ( !workerId.equals( other.workerId ) ) return false;
        return true;
    }

}
