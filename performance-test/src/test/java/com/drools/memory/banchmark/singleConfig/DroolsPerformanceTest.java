package com.drools.memory.banchmark.singleConfig;


import com.drools.memory.banchmark.dao.ConfigDao;
import com.drools.memory.banchmark.event.prcessor.VisitorEventProcessor;
import com.drools.memory.banchmark.event.prcessor.VisitorGenericEventFactory;
import com.drools.memory.banchmark.model.ConfigData;
import com.drools.memory.banchmark.stats.impl.ProcessingTimeStatsManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author victorp
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:com/drools/memory/banchmark/singleConfig/visitor-event-processor-test-singe-config.xml"})
public class DroolsPerformanceTest {
    private static Logger log = LoggerFactory.getLogger(DroolsPerformanceTest.class);

    @Autowired
    private VisitorEventProcessor visitorEventProcessor;

    @Autowired
    private VisitorGenericEventFactory visitorGenericEventFactory;

    @Autowired
    private ProcessingTimeStatsManager processingTimeStatsManager;

    @Autowired
    private ScheduledThreadPoolExecutor eventsExecutor;

    @Autowired
    private ConfigDao config;

    @Autowired
    private VisitorEventProcessorPerformanceTestConfig testConfig;

    @Autowired
    private RunAdditionalResults runAdditionalResults;


    @Before
    public void init() throws Exception {
        createAndStoreConfigData(testConfig.getConfigId(),10);
    }

     private ConfigData createAndStoreConfigData(String configId, int rate) throws Exception {
    	ConfigData configData = new ConfigData();
    	configData.setId(configId);
    	configData.setRate(rate);
    	config.updateConfigData(configData);
    	return configData;
    }



    @Test
    public void processXMinorThenYMainEvents() throws InterruptedException {
        log.info("Test is started");
        long startTimestamp = System.currentTimeMillis();
        String visitorPrefix = "visitor-test-";

        for (int visitor = 0; visitor < testConfig.getNumOfVisitors();visitor++ ){
            VisitorSimulatorXMinorYMainEvents visitorSimulator = new VisitorSimulatorXMinorYMainEvents(testConfig.getMinorPerVisitor(),testConfig.getMainPerVisitor(),visitorPrefix+visitor,testConfig.getConfigId(),visitorGenericEventFactory);
            VisitorSimulatorRunner visitorSimulatorRunner = new VisitorSimulatorRunner(visitorSimulator,visitorEventProcessor,processingTimeStatsManager);
            eventsExecutor.scheduleAtFixedRate(visitorSimulatorRunner,testConfig.getDelayBetweenEachRequestInSeconds(),testConfig.getDelayBetweenEachRequestInSeconds(),TimeUnit.SECONDS);
        }

        Thread.sleep((testConfig.getMinorPerVisitor()+testConfig.getMainPerVisitor())*(testConfig.getDelayBetweenEachRequestInSeconds())*1000+10*1000);

        long endTimestamp = System.currentTimeMillis();
        runAdditionalResults.setNetoTotalRunTime(endTimestamp - startTimestamp);
        runAdditionalResults.setEndRunTime(endTimestamp);


        /**
         * Memory
         */
        long totalMemory = Runtime.getRuntime().totalMemory();
        runAdditionalResults.setTotalHeapMemory(totalMemory);
        long freeMemoryBeforeGc = Runtime.getRuntime().freeMemory();
        runAdditionalResults.setUsedMemoryBeforerGc(totalMemory-freeMemoryBeforeGc);

        System.gc();

        long freeMemoryAfterGc = Runtime.getRuntime().freeMemory();
        runAdditionalResults.setUsedMemoryAfterGc(totalMemory-freeMemoryAfterGc);

    }


}
