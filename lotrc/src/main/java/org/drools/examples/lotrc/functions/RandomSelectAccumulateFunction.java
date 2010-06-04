/*
 * Copyright 2008 Red Hat
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
 *
 */
package org.drools.examples.lotrc.functions;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.drools.base.accumulators.AccumulateFunction;

/**
 * An accumulate function that random selects one object from a list of them
 * 
 * @author etirelli
 */
public class RandomSelectAccumulateFunction
    implements
    AccumulateFunction {

    public void readExternal(ObjectInput in) throws IOException,
                                            ClassNotFoundException {
        // functions are stateless, so nothing to serialize
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        // functions are stateless, so nothing to serialize
    }

    private static class RandomSelectData
        implements
        Serializable {
        private static final long serialVersionUID = 7632007789321541884L;
        public List<Object> list = new ArrayList<Object>();
        public transient Random random = new Random(System.currentTimeMillis());
        public RandomSelectData() {
        }
    }

    /* (non-Javadoc)
     * @see org.drools.base.accumulators.AccumulateFunction#createContext()
     */
    public Serializable createContext() {
        return new RandomSelectData();
    }

    /* (non-Javadoc)
     * @see org.drools.base.accumulators.AccumulateFunction#init(java.lang.Object)
     */
    public void init(Serializable context) throws Exception {
        RandomSelectData data = (RandomSelectData) context;
        data.list.clear();
    }

    /* (non-Javadoc)
     * @see org.drools.base.accumulators.AccumulateFunction#accumulate(java.lang.Object, java.lang.Object)
     */
    public void accumulate(Serializable context,
                           Object value) {
        RandomSelectData data = (RandomSelectData) context;
        data.list.add( value );
    }

    /* (non-Javadoc)
     * @see org.drools.base.accumulators.AccumulateFunction#reverse(java.lang.Object, java.lang.Object)
     */
    public void reverse(Serializable context,
                        Object value) throws Exception {
        RandomSelectData data = (RandomSelectData) context;
        data.list.remove( value );
    }

    /* (non-Javadoc)
     * @see org.drools.base.accumulators.AccumulateFunction#getResult(java.lang.Object)
     */
    public Object getResult(Serializable context) throws Exception {
        RandomSelectData data = (RandomSelectData) context;
        return data.list.isEmpty() ? null : data.list.get( data.random.nextInt( data.list.size() ) );
    }

    /* (non-Javadoc)
     * @see org.drools.base.accumulators.AccumulateFunction#supportsReverse()
     */
    public boolean supportsReverse() {
        return true;
    }

}
