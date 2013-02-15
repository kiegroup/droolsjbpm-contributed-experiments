package com.drools.memory.banchmark.stats.api;

/**
 * @author victorp
 */
public interface StatisticsManager<StatData> {
    void addStat(StatData statData);
    void addError();
}