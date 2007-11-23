package benchmarks.dispatch.simulation;

import java.util.Date;
import java.util.Random;

import benchmarks.dispatch.fact.independent.Job;
import benchmarks.dispatch.fact.independent.VehicleSize;

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

public class JobGenerator {
    Random random;
    public JobGenerator(long seed){
        random = new Random(seed);
    }
    
    public Job generateJob(){
        Job j = new Job();
        
        j.setEndTime( new Date() );
        j.setStartTime( new Date() );
        j.setJobId( new Integer(random.nextInt()).toString() );
        j.setLatitude( -100D );
        j.setLongitude( -100D );
        j.setNumberOfRocksRequired( 1 );
        j.setNumberOfSticksRequired( 1 );
        j.setStatus( Job.Status.PENDING );
        j.setVehicleSizeRequired( VehicleSize.SMALL );
        j.setWrenchRequired( false );
        return j;
    }

}
