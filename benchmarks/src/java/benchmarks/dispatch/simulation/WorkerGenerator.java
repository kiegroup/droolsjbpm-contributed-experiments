package benchmarks.dispatch.simulation;

import java.util.Random;

import benchmarks.dispatch.fact.independent.VehicleSize;
import benchmarks.dispatch.fact.independent.Worker;
import benchmarks.dispatch.fact.independent.WorkerPosition;

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

public class WorkerGenerator {

    Random random;

    public WorkerGenerator(long seed) {
        random = new Random( seed );
    }

    public Worker generateWorker() {

        Worker w = new Worker();
        w.setWorkerId( new Integer( random.nextInt() ).toString() );
        w.setHasWrench( false );
        w.setNumberOfRocks( 1 );
        w.setNumberOfSticks( 2 );
        w.setStatus( Worker.Status.WAITING_FOR_JOB );
        w.setVehicleSize( VehicleSize.SMALL );

        return w;
    }

    public WorkerPosition generateWorkerPosition(Worker w) {
        WorkerPosition wp = new WorkerPosition();
        wp.setLatitude( -90D );
        wp.setLongitude( -90D );
        wp.setWorkerId( w.getWorkerId() );
        return wp;
    }
}
