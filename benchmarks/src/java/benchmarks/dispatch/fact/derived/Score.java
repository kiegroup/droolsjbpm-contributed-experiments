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

import benchmarks.dispatch.fact.independent.Job;
import benchmarks.dispatch.fact.independent.Worker;

public class Score extends Compatibility {

	private static final long serialVersionUID = 1L;
	
	private double score;
	
	public Score(Job job, Worker worker) {
		super(job, worker);
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

}
