package org.drools.learner;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.drools.learner.builder.Learner;
import org.drools.learner.tools.Util;

public interface Memory {

    static Memory createFromObjects(List<Object> objects, Class<?> clazz, Learner.DataType data) {
        return createFromObjects(objects, clazz, data, Util.TRAINING_RATIO, Util.TESTING_RATIO);
    }

    static Memory createFromObjects(List<Object> objects, Class<?> clazz, Learner.DataType data,
                                    double trainRatio, double testRatio)  {
        Schema instSchema = null;
        instSchema = Schema.createSchemaStructure(clazz, data);

        InstanceList instances = new InstanceList(instSchema);
        Iterator<Object> itObject = objects.iterator();
        while (itObject.hasNext()) {
            Object obj = itObject.next();
            // validating in the the factory during instantiation
            //if (clazz.isAssignableFrom(obj.getClass()))
            instances.addStructuredInstance(obj);
        }

        MemoryImpl mem = new MemoryImpl(clazz, instances);

        mem.processTestSet(trainRatio, testRatio);

        return mem;
    }

    InstanceList getInstances();

    InstanceList getTrainSet();

    InstanceList getTestSet();
}
