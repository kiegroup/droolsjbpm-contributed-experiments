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

public class Job {

    public enum Status {
        PENDING, DISPATCHED
    }

    private String      jobId;
    private Status      status;
    private Date        startTime;
    private Double      latitude, longitude;
    private VehicleSize vehicleSizeRequired;
    private Boolean     wrenchRequired = new Boolean(false);
    private Integer     numberOfSticksRequired;
    private Integer     numberOfRocksRequired;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Boolean getWrenchRequired() {
        return wrenchRequired;
    }

    public void setWrenchRequired(Boolean wrenchRequired) {
        this.wrenchRequired = wrenchRequired;
    }

    public Integer getNumberOfSticksRequired() {
        return numberOfSticksRequired;
    }

    public void setNumberOfSticksRequired(Integer numberOfSticksRequired) {
        this.numberOfSticksRequired = numberOfSticksRequired;
    }

    public Integer getNumberOfRocksRequired() {
        return numberOfRocksRequired;
    }

    public void setNumberOfRocksRequired(Integer numberOfRocksRequired) {
        this.numberOfRocksRequired = numberOfRocksRequired;
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

    public VehicleSize getVehicleSizeRequired() {
        return vehicleSizeRequired;
    }

    public void setVehicleSizeRequired(VehicleSize vehicleSizeRequired) {
        this.vehicleSizeRequired = vehicleSizeRequired;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((jobId == null) ? 0 : jobId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        final Job other = (Job) obj;
        if ( jobId == null ) {
            if ( other.jobId != null ) return false;
        } else if ( !jobId.equals( other.jobId ) ) return false;
        return true;
    }

}
