package org.drools.learner;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.drools.learner.builder.Learner.DataType;
import org.drools.learner.tools.FeatureNotSupported;
import org.drools.learner.tools.Util;

public class Memory {

    //// class specification
    //private Schema schema;
    Class<?> clazzToClassify;
    // instance list used to train
    private HashMap<Class<?>, InstanceList> instances;
    private InstanceList trainInstances, testInstances;
    //	// instance list used to test
    //	private HashMap<Class<?>,InstanceList> test_instances;
    private double trainRatio = Util.TRAINING_RATIO;
    private double testRatio  = Util.TESTING_RATIO;
    private Memory() {
        this.instances = new HashMap<Class<?>, InstanceList>();
    }

    public static Memory createFromObjects(List<Object> objects, Class<?> clazz,
                                           DataType data) throws FeatureNotSupported {
        // if mem == null
        Memory mem = new Memory();

        mem.setClassToClassify(clazz);
        // create schema from clazz
        Schema instSchema = null;
        try {
            instSchema = Schema.createSchemaStructure(clazz, data);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            System.exit(0);
        }

        // create a instance list that can hold objects from our schema
        mem.instances.put(clazz, new InstanceList(instSchema));

        Iterator<Object> itObject = objects.iterator();
        while (itObject.hasNext()) {
            Object obj = itObject.next();
            // validating in the the factory during instantiation
            //if (clazz.isAssignableFrom(obj.getClass()))
            mem.instances.get(clazz).addStructuredInstance(obj);
        }

        return mem;
    }

    public void setTrainRatio(double ratio) {
        trainRatio = ratio;
    }

    public void setTestRatio(double ratio) {
        testRatio = ratio;
    }

    public InstanceList getTrainSet() {
        return trainInstances;
    }

    public InstanceList getTestSet() {
        return testInstances;
    }

    public void processTestSet() {
        InstanceList list = instances.get(this.clazzToClassify);
        int splitIdx = (int) (trainRatio * list.getSize());
        //int split_idx2 =  split_idx + (int)(testRatio * instances.get(this.clazzToClassify).getSize());
        int splitIdx2 = list.getSize();

        trainInstances = list.subList(0, splitIdx);
        testInstances = list.subList(splitIdx, splitIdx2);//class_instances.getSize());
        return;
    }

    private void setClassToClassify(Class<?> clazz) {
        this.clazzToClassify = clazz;
    }

    // target class instances
    public InstanceList getClassInstances() {
        return instances.get(this.clazzToClassify);
    }
}
