package com.drools.memory.banchmark.singleConfig;

import org.apache.log4j.lf5.util.StreamUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.FileCopyUtils;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URISyntaxException;

/**
 * @author victorp
 */
public class DrlsCreator {
    private String drlsDir;
    private int numOfDrls;
    private File drlsDirectory;
    private String flowsDrlTemplate;

    @Required
    public void setNumOfDrls(int numOfDrls) {
        this.numOfDrls = numOfDrls;
    }

    @Required
    public void setDrlsDir(String drlsDir) {
        this.drlsDir = drlsDir;
    }

    @PostConstruct
    public void init() throws URISyntaxException,IOException {
        drlsDirectory = new File(drlsDir);
        if (!drlsDirectory.exists()){
            drlsDirectory.mkdirs();
        }
        cleanDir(drlsDirectory);
        flowsDrlTemplate = createFlowsDrlTemplate();

        saveAcceptedDrl();

        for(int drlNum = 0; drlNum<numOfDrls; drlNum++){
            String drlContent = createWebFlowDrlFromTemplate(drlNum);

            File webFlowDrlFile = new File(String.format("%s/webFlow_%s_.drl",drlsDirectory.getPath(),drlNum));
            webFlowDrlFile.createNewFile();
            FileCopyUtils.copy(drlContent.getBytes(),webFlowDrlFile);
        }

    }

    private void cleanDir(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (!files[i].isDirectory()) {
                    files[i].delete();
                }
            }
        }
    }


    private void saveAcceptedDrl() throws IOException {
        InputStream acceptedDrlInputStream = this.getClass().getClassLoader().getResourceAsStream("com/drools/memory/banchmark/singleConfig/accepted.drl");
        saveDrlToFile(acceptedDrlInputStream,"accepted.drl");
    }

    private String createFlowsDrlTemplate() throws URISyntaxException, IOException {
        InputStream webPagesDrlTemplateInputStream = this.getClass().getClassLoader().getResourceAsStream("com/drools/memory/banchmark/singleConfig/web-pages-flow-template.drl");
        return new String(StreamUtils.getBytes(webPagesDrlTemplateInputStream));
    }

    private String createWebFlowDrlFromTemplate(int drlNum) throws URISyntaxException, IOException {
         return flowsDrlTemplate.replace("${flow}","flow"+drlNum);
    }

    private void saveDrlToFile(InputStream acceptedDrlInputStream,String fileName) throws IOException {
        File target = new File(drlsDirectory.getPath()+"/"+fileName);
        target.createNewFile();
        FileCopyUtils.copy(StreamUtils.getBytes(acceptedDrlInputStream),target);
    }
}
