package com.drools.memory.banchmark.model.state;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Victor
 */

public class VisitorRTData{
    private String visitorId;
    private boolean isAccepted;
    private boolean isInWebSite;
    private int mainEventsCount;
    private Map<String,Integer> flowsStages = new HashMap<String,Integer>();

    public VisitorRTData(String visitorId) {
        this.visitorId = visitorId;
    }

    public int getMainEventsCount() {
        return mainEventsCount;
    }

    public void setMainEventsCount(int mainEventsCount) {
        this.mainEventsCount = mainEventsCount;
    }

    public void incMainCount(){
        mainEventsCount++;
    }


    public String getVisitorId() {
        return visitorId;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }


    public void setInWebSite(boolean inWebSite) {
        isInWebSite = inWebSite;
    }

    public boolean isInWebSite() {
        return isInWebSite;
    }

    public void setInFlow(String flowName,int stage){
        flowsStages.put(flowName,stage);
    }
    public void setNotInFlow(String flowName){
        flowsStages.remove(flowName);
    }

    public Integer getInFlowStage(String flowName){
        return flowsStages.get(flowName);
    }

    public Map<String, Integer> getWebFlows() {
        return flowsStages;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
