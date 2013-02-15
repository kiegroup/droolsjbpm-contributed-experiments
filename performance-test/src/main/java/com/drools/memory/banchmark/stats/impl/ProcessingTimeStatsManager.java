package com.drools.memory.banchmark.stats.impl;

import org.apache.log4j.Logger;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author victorp
 */
public class ProcessingTimeStatsManager extends WritableStatisticsManager<ProcessingTimeData> {
    private static Logger log = Logger.getLogger(ProcessingTimeStatsManager.class);

    private int statsFlushPeriodInMillis;
    private List<ProcessingTimeData> stats;
    private int errorCount = 0;
    private ScheduledExecutorService writerScheduledExecutorService;


    public void setWriterScheduledExecutorService(ScheduledExecutorService writerScheduledExecutorService) {
        this.writerScheduledExecutorService = writerScheduledExecutorService;
    }


    public void setStatsFlushPeriodInMillis(int statsFlushPeriodInMillis) {
        this.statsFlushPeriodInMillis = statsFlushPeriodInMillis;
    }

    @PostConstruct
    public void init() {
        initStats();
        writerScheduledExecutorService.scheduleAtFixedRate(new StatsFlusher(), statsFlushPeriodInMillis, statsFlushPeriodInMillis, TimeUnit.MILLISECONDS);
        getOutputWriter().writeLine("Count, Avg, Median, Max, Min, Errors");
    }

    public synchronized void addError() {
        errorCount++;
    }


    public synchronized void addStat(ProcessingTimeData statData) {
        stats.add(statData);
    }

    /**
     * Init the current stats and return the old stats
     *
     * @return
     */
    private synchronized Stats initStats() {
        List<ProcessingTimeData> currentTopologyStatsData = stats;
        int currentErrorCount = this.errorCount;
        stats = new ArrayList<ProcessingTimeData>();
        Stats currentStats = new Stats(currentTopologyStatsData, currentErrorCount);
        return currentStats;
    }


    private static class Stats {
        public List<ProcessingTimeData> statData;
        public int errorCount;

        private Stats(List<ProcessingTimeData> statData, int errorCount) {
            this.statData = statData;
            this.errorCount = errorCount;
        }
    }

    private class StatsFlusher implements Runnable {
        public void run() {
            try {
                Stats currentStats = initStats();
                List<ProcessingTimeData> currentStatsData = currentStats.statData;

                long readCount = currentStatsData.size() + 1;
                long readSum = 0;
                long median = 0;
                long avg = 0;
                long max = 0;
                long min = Integer.MAX_VALUE;

                Collections.sort(currentStatsData);
                int ix = 0;
                for (ProcessingTimeData statData : currentStatsData) {
                    /**
                     * Sum
                     */
                    readSum += statData.getProcessingTimeInNanos();

                    /**
                     * Max
                     */
                    if (max < statData.getProcessingTimeInNanos()) {
                        max = statData.getProcessingTimeInNanos();
                    }

                    /**
                     * Min
                     */
                    if (min > statData.getProcessingTimeInNanos()) {
                        min = statData.getProcessingTimeInNanos();
                    }


                    /**
                     * Median
                     */
                    ix++;
                    if (ix == (readCount / 2)) {
                        median = statData.getProcessingTimeInNanos();
                    }
                }
                avg = readSum / readCount;
                if (min == Integer.MAX_VALUE){
                   min = 0; 
                }

                String msg = String.format("%s, %s, %s, %s, %s, %s, ", readCount, avg, median, max, min, currentStats.errorCount);
                getOutputWriter().writeLine(msg);
            } catch (Throwable e) {
                log.error("Failed to write stats", e);
            }

        }
    }
}