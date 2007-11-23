package benchmarks.dispatch.accumulator;

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

import java.util.Comparator;
import java.util.TreeSet;

import org.drools.base.accumulators.AccumulateFunction;

import benchmarks.dispatch.fact.derived.Score;


@SuppressWarnings("unchecked")
public class TopWorkerAccumulator implements AccumulateFunction {

    private static final long serialVersionUID = 1L;

    public void accumulate(Object context, Object value) {
	    TreeSet<Score> ts = (TreeSet<Score>)context;
		ts.add( (Score)value);	
	}
	
	public void reverse(Object context, Object value) throws Exception {
	    TreeSet<Score> ts = (TreeSet<Score>)context;
	    ts.remove( value );	
	}

	public Object createContext() {
		return new TreeSet<Score>(new ScoreComparator());
	}

	public Object getResult(Object context) throws Exception {
		return context;
	}

	public void init(Object context) throws Exception {
	    TreeSet<Score> ts = (TreeSet<Score>)context;
		ts.clear();
	}

	public boolean supportsReverse() {
		return true;
	}

	private class ScoreComparator implements Comparator<Score> {

		public int compare(Score o1, Score o2) {
			if (o1.getScore() < o2.getScore()){
				return -1;
			} else {
				return 1;
			}
		}	
	}
	
}
