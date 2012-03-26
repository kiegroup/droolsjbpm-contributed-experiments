package com.drools.memory.banchmark.stats.impl;

import com.drools.memory.banchmark.stats.api.OutputWriter;
import com.drools.memory.banchmark.stats.api.StatisticsManager;



/**
 * @author victorp
 */
public abstract class WritableStatisticsManager<StatData> implements StatisticsManager<StatData> {
    private OutputWriter outputWriter;

    public void setOutputWriter(OutputWriter outputWriter) {
        this.outputWriter = outputWriter;
    }

    protected OutputWriter getOutputWriter(){
        return outputWriter;
    }
}