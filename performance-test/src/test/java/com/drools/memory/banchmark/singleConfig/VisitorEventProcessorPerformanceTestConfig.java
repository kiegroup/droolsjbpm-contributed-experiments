package com.drools.memory.banchmark.singleConfig;

import org.springframework.beans.factory.annotation.Required;

/**
 * @author victorp
 */
public class VisitorEventProcessorPerformanceTestConfig {
    private int minorPerVisitor = 5;
    private int mainPerVisitor = 20;
    private int numOfVisitors = 2000;
    private int delayBetweenEachRequestInSeconds = 5;
    private String drlsDir;
    private String configId;



    public String getDrlsDir() {
        return drlsDir;
    }


    public String getConfigId() {
        return configId;
    }

    @Required
    public void setConfigId(String configId) {
        this.configId = configId;
    }

    @Required
    public void setDrlsDir(String drlsDir) {
        this.drlsDir = drlsDir;
    }

    public int getMinorPerVisitor() {
        return minorPerVisitor;
    }

    @Required
    public void setMinorPerVisitor(int minorPerVisitor) {
        this.minorPerVisitor = minorPerVisitor;
    }

    public int getMainPerVisitor() {
        return mainPerVisitor;
    }

    @Required
    public void setMainPerVisitor(int mainPerVisitor) {
        this.mainPerVisitor = mainPerVisitor;
    }

    public int getNumOfVisitors() {
        return numOfVisitors;
    }

    @Required
    public void setNumOfVisitors(int numOfVisitors) {
        this.numOfVisitors = numOfVisitors;
    }

    public int getDelayBetweenEachRequestInSeconds() {
        return delayBetweenEachRequestInSeconds;
    }

    @Required
    public void setDelayBetweenEachRequestInSeconds(int delayBetweenEachRequestInSeconds) {
        this.delayBetweenEachRequestInSeconds = delayBetweenEachRequestInSeconds;
    }
}
