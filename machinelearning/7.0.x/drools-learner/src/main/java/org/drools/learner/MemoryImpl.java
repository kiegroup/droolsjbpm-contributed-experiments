package org.drools.learner;

public class MemoryImpl implements Memory {
    private Class<?>      cls;
    private InstanceList instances;
    private InstanceList trainInstances;
    private InstanceList testInstances;

    public MemoryImpl(Class<?> cls, InstanceList instances) {
        this.cls = cls;
        this.instances = instances;
    }

    @Override public InstanceList getInstances() {
        return instances;
    }

    @Override public InstanceList getTrainSet() {
        return trainInstances;
    }

    @Override public InstanceList getTestSet() {
        return testInstances;
    }

    public void processTestSet(double trainRatio, double testRatio) {
        InstanceList list = instances;
        int splitIdx = (int) (trainRatio * list.size());
        //int split_idx2 =  split_idx + (int)(testRatio * instances.get(this.clazzToClassify).size());
        int splitIdx2 = list.size();

        trainInstances = list.subList(0, splitIdx);
        testInstances = list.subList(splitIdx, splitIdx2);//class_instances.size());
        return;
    }
}
