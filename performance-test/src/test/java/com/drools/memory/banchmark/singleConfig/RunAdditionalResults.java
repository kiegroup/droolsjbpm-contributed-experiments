package com.drools.memory.banchmark.singleConfig;

import org.springframework.beans.factory.annotation.Required;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * @author victorp
 */
public class RunAdditionalResults {
    private Properties properties = new Properties();
    static private String NETO_TOTAL_RUN_TIME = "netoTotalRunTime";
    static private String BRUTO_TOTAL_RUN_TIME = "brutoTotalRunTime";

    static private String TOTAL_MEMORY = "totalMemory";
    static private String USED_MEMORY_BEFORE_GC = "usedMemoryBeforeGc";
    static private String USED_MEMORY_AFTER_GC = "usedMemoryAfterGc";




    static private String MINOR_PER_VISITOR = "minorPerVisitor";
    static private String MAIN_PER_VISITOR = "mainPerVisitor";
    static private String NUM_OF_VISITOR = "numOfVisitors";
    static private String DELAY_BETWEEN_EACH_REQ_IN_SEC = "delayBetweenEachRequestInSeconds";
    static private String NUM_OF_DRLS = "numOfDrls";
    static private String TOTAL_RULES_COUNT = "totalRulesCount";
    static private int RULES_COUNT_PER_DRL = 4;







    private long startTime;

    private String additionalResultsFileName;



    @Required
    public void setMinorPerVisitor(int minorPerVisitor) {
        properties.setProperty(MINOR_PER_VISITOR,""+minorPerVisitor);
    }

    @Required
    public void setMainPerVisitor(int mainPerVisitor) {
        properties.setProperty(MAIN_PER_VISITOR,""+mainPerVisitor);
    }

    @Required
    public void setNumOfVisitors(int numOfVisitors) {
        properties.setProperty(NUM_OF_VISITOR,""+numOfVisitors);
    }

    @Required
    public void setDelayBetweenEachRequestInSeconds(int delayBetweenEachRequestInSeconds) {
        properties.setProperty(DELAY_BETWEEN_EACH_REQ_IN_SEC,""+delayBetweenEachRequestInSeconds);
    }

    @Required
    public void setNumOfDrls(int numOfDrls) {
        properties.setProperty(NUM_OF_DRLS,""+numOfDrls);
    }



    @Required
    public void setAdditionalResultsFileName(String additionalResultsFileName) {
        this.additionalResultsFileName = additionalResultsFileName;
    }

    public void setNetoTotalRunTime(long TotalRunTimeInMillis){
       properties.setProperty(NETO_TOTAL_RUN_TIME,""+TotalRunTimeInMillis);
    }

    public void setEndRunTime(long endTimeInMillis){
       properties.setProperty(BRUTO_TOTAL_RUN_TIME,""+(endTimeInMillis -startTime ));
    }

    @PostConstruct
    public void init(){
        properties.setProperty(TOTAL_RULES_COUNT,""+Integer.decode(properties.getProperty(NUM_OF_DRLS))*RULES_COUNT_PER_DRL);

        startTime = System.currentTimeMillis();
    }

    @PreDestroy
    public void flushInfoToDisk() throws IOException {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("_dd-MMM-yyyy_HH-mm-ss_");
        String additionalResultsFileNameWithDate = additionalResultsFileName + dateFormatter.format(new Date(startTime))+".properties";
        FileOutputStream fileOutputStream = new FileOutputStream(additionalResultsFileNameWithDate);

        properties.store(fileOutputStream, null);
        fileOutputStream.close();
    }

    public void setTotalHeapMemory(long totalMemory) {
        properties.setProperty(TOTAL_MEMORY,""+totalMemory);
    }

    public void setUsedMemoryBeforerGc(long usedMemoryBeforerGc) {
        properties.setProperty(USED_MEMORY_BEFORE_GC,""+usedMemoryBeforerGc);
    }

    public void setUsedMemoryAfterGc(long usedMemoryAfterGc) {
        properties.setProperty(USED_MEMORY_AFTER_GC,""+usedMemoryAfterGc);
    }
}
