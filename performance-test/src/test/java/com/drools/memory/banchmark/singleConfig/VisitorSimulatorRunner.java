package com.drools.memory.banchmark.singleConfig;


import com.drools.memory.banchmark.event.prcessor.VisitorEventProcessor;
import com.drools.memory.banchmark.event.prcessor.VisitorGenericEvent;
import com.drools.memory.banchmark.stats.impl.ProcessingTimeData;
import com.drools.memory.banchmark.stats.impl.ProcessingTimeStatsManager;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author victorp
 */
public class VisitorSimulatorRunner implements Runnable {
    private final VisitorSimulator visitorSimulator;
    private final VisitorEventProcessor visitorEventProcessor;
    private AtomicBoolean simulationEnded = new AtomicBoolean(false);
    private final ProcessingTimeStatsManager processingTimeStatsManager;

    public VisitorSimulatorRunner(VisitorSimulator visitorSimulator,
                                  VisitorEventProcessor visitorEventProcessor,
                                  ProcessingTimeStatsManager processingTimeStatsManager) {
        this.visitorSimulator = visitorSimulator;
        this.visitorEventProcessor = visitorEventProcessor;
        this.processingTimeStatsManager = processingTimeStatsManager;
    }

    @Override
    public void run() {
        VisitorGenericEvent visitorGenericEvent = visitorSimulator.getNextEvent();
        if (visitorGenericEvent != null) {
            long startTime = System.nanoTime();
            try {
                visitorEventProcessor.processEvent(visitorGenericEvent);
                processingTimeStatsManager.addStat(new ProcessingTimeData("visitorEvent",(System.nanoTime() - startTime)));
            } catch (Exception e) {
                processingTimeStatsManager.addError();
            }

        } else {
            simulationEnded.set(true);
        }
    }

    boolean isSimulationEnded() {
        return simulationEnded.get();
    }
}
