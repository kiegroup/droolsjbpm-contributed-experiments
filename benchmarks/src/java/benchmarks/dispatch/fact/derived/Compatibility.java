package benchmarks.dispatch.fact.derived;

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

import java.io.Serializable;

import benchmarks.dispatch.fact.independent.Job;
import benchmarks.dispatch.fact.independent.Worker;

public class Compatibility
    implements
    Serializable {

    private static final long serialVersionUID = 1L;

    private String            workerId, jobId;

    public Compatibility(Job job,
                         Worker worker) {
        this.jobId = job.getJobId();
        this.workerId = worker.getWorkerId();
    }
    
    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((jobId == null) ? 0 : jobId.hashCode());
        result = prime * result + ((workerId == null) ? 0 : workerId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        final Compatibility other = (Compatibility) obj;
        if ( jobId == null ) {
            if ( other.jobId != null ) return false;
        } else if ( !jobId.equals( other.jobId ) ) return false;
        if ( workerId == null ) {
            if ( other.workerId != null ) return false;
        } else if ( !workerId.equals( other.workerId ) ) return false;
        return true;
    }

}
