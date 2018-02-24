package org.drools.learner;

import java.util.ArrayList;
import java.util.List;

import org.drools.learner.builder.Learner;
import org.drools.learner.eval.InstDistribution;
import org.drools.learner.tools.Util;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class MemoryTest {


    @Test
    public void testTrainingandTestSetCreationRatio() {
        List<Object>     list  = Weather.getDataAsList();
        Memory mem = Memory.createFromObjects(list, Weather.class, Learner.DataType.STRUCTURED, Util.DEFAULT_TRAINING_RATIO);
        assertEquals(14, list.size() );
        assertEquals( 11, mem.getTrainSet().size());
        assertEquals( 3, mem.getTestSet().size());
    }


}
