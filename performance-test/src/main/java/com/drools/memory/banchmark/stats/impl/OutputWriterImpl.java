package com.drools.memory.banchmark.stats.impl;

import com.drools.memory.banchmark.stats.api.OutputWriter;
import org.apache.log4j.*;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author victorp
 */
public class OutputWriterImpl implements OutputWriter {
    private static Logger log = Logger.getLogger(OutputWriterImpl.class);

    private Logger WRITER;
    private String outputFileName;
    private String outputFileNameWithDate;
    private String layoutPattern;


    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }


    public void setLayoutPattern(String layoutPattern) {
        this.layoutPattern = layoutPattern;
    }

    @PostConstruct
    public void init() throws IOException {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("dd-MMM-yyyy_HH-mm-ss_");
        outputFileNameWithDate = outputFileName + dateFormatter.format(new Date())+".csv";
        log.info(String.format("The output writer for %s is initializing",outputFileNameWithDate));
        WRITER = Logger.getLogger(outputFileNameWithDate);
        WRITER.setLevel(Level.INFO);
        Layout layout = new PatternLayout(layoutPattern);
        Appender writerAppender = new FileAppender(layout, outputFileNameWithDate,false);
        WRITER.addAppender(writerAppender);
        log.info(String.format("The output writer for %s is successfully initialized",outputFileNameWithDate));
    }


    public void writeLine(String msg) {
        WRITER.info(msg);
    }
}
