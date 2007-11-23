package benchmarks.dispatch.fact.independent;

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

import java.util.Date;

public class DispatchState {
    private Date currentTime;
    private Date cutoff;
    private Date positionCutOff;

    public DispatchState() {

    }

    public Date getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(Date currentTime) {
        this.currentTime = currentTime;
        
        //3 days
        cutoff = new Date( currentTime.getTime() + 1000 * 60 * 60 * 24 * 3 );

        // 2 hours
        positionCutOff = new Date( currentTime.getTime() - 1000 * 60 * 60 * 2 );
    }

    public Date getStartTimeCutOff() {
        return cutoff;
    }

    public Date getPositionCutOff() {
        return positionCutOff;
    }
    
}
