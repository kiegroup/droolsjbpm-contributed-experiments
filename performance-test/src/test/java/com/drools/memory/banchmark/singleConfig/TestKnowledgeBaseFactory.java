package com.drools.memory.banchmark.singleConfig;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.conf.EventProcessingOption;
import org.drools.conf.MBeansOption;
import org.drools.io.ResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * @author victorp
 */
public class TestKnowledgeBaseFactory {
    private static Logger log = LoggerFactory.getLogger(TestKnowledgeBaseFactory.class);
    private DrlsCreator drlsCreator;
    private String drlsDir;

    @Required
    public void setDrlsCreator(DrlsCreator drlsCreator) {
        this.drlsCreator = drlsCreator;
    }

    @Required
    public void setDrlsDir(String drlsDir) {
        this.drlsDir = drlsDir;
    }

    public KnowledgeBase create(){

        KnowledgeBuilderConfiguration builderConf =
                KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
        KnowledgeBaseConfiguration config = KnowledgeBaseFactory.newKnowledgeBaseConfiguration();
        config.setOption(EventProcessingOption.STREAM);
        config.setOption(MBeansOption.ENABLED);
        String[] drls = getDrls(drlsDir);
        try {
            return createKnowledgeBase(config, builderConf, drls);
        } catch (Exception e) {
            throw new RuntimeException("knowledgeBase init failed", e);
        }

    }

    private static String[] getDrls(String drlsDirectory) {
        File drlsDir = new File(drlsDirectory);
        if (!drlsDir.exists() || !drlsDir.isDirectory()) {
            log.warn("Cannot load drl files from drls directory, reason: " + drlsDirectory + " is not a directory or does not exists");
            return new String[0];
        }

        File[] files = drlsDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".drl");
            }
        });
        String[] drls = new String[files.length];
        int index = 0;
        for (File drlFile : files) {
            drls[index] = drlFile.getPath();
            index++;
        }
        return drls;
    }

    public static KnowledgeBase createKnowledgeBase(
            KnowledgeBaseConfiguration config,
            KnowledgeBuilderConfiguration knowledgeBuilderConfig,
            String... ruleFiles) throws
            IOException {
        KnowledgeBuilder knowledgeBuilder = KnowledgeBuilderFactory
                .newKnowledgeBuilder(knowledgeBuilderConfig);
        for (String ruleFile : ruleFiles) {
            knowledgeBuilder.add(ResourceFactory.newFileResource(ruleFile), ResourceType.DRL);
        }

        if (knowledgeBuilder.hasErrors()) {
            throw new RuntimeException(knowledgeBuilder.getErrors()
                    .toString());
        }

        KnowledgeBase knowledgeBase = KnowledgeBaseFactory
                .newKnowledgeBase(config);
        knowledgeBase.addKnowledgePackages(knowledgeBuilder
                .getKnowledgePackages());
        return knowledgeBase;
    }
}
