package com.drools.memory.banchmark.stats.impl;

/**
 * @author victorp
 */
public class ProcessingTimeData implements Comparable{
    private final String context;
    private final Long processingTimeInNanos;

    public ProcessingTimeData(String context, long processingTimeInNanos) {
        this.context = context;
        this.processingTimeInNanos = processingTimeInNanos;
    }

    public String getContext() {
        return context;
    }

    public long getProcessingTimeInNanos() {
        return processingTimeInNanos;
    }

    @Override
    public int compareTo(Object other) {
        return processingTimeInNanos.compareTo(((ProcessingTimeData)other).getProcessingTimeInNanos());
    }
}
