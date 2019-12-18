package org.kiegroup.zenithr.drools;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kiegroup.zenithr.drools.service.TestSessionFactory;

import static org.junit.jupiter.api.Assertions.fail;

public class FileUtils {

    public static String readRulesDefinition(String fileName) {
        return readFile("definitions", fileName);
    }

    public static String readObject(String fileName) {
        return readFile("objects", fileName);
    }

    private static String readFile(String base, String name) {
        String file = null;
        try {
            Path path = Paths.get(TestSessionFactory.class.getClassLoader().getResource(base + "/" + name).toURI());
            Stream<String> lines = Files.lines(path);
            file = lines.collect(Collectors.joining("\n"));
            lines.close();
        } catch (Exception e) {
            fail("Error trying to read json object definition", e);
        }
        return file;
    }
}
