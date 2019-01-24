package org.kiegroup.zenithr.drools;

import org.junit.Assert;
import org.junit.Test;

public class RuleServiceTest {

    @Test
    public void testGradeLetters() {
        String json = "{\"input\": [{\"name\": \"grade\", \"type\": \"double\"}], \"rules\": [{\"when\": \"grade >= 90 && grade <= 100\", \"then\": {\"output\": \"A\"}}, {\"when\": \"grade >= 80 && grade < 90\", \"then\": {\"output\": \"B\"}}, {\"when\": \"grade >= 70 && grade < 80\", \"then\": {\"output\": \"C\"}}, {\"when\": \"grade >= 60 && grade < 70\", \"then\": {\"output\": \"D\"}}, {\"when\": \"grade < 60\", \"then\": {\"output\": \"F\"}}], \"output\": {\"type\": \"string\"}}";
        System.setProperty("GET", json);

        Assert.assertEquals("A", RuleService.getGradeLetter(92.5));
        Assert.assertEquals("B", RuleService.getGradeLetter(84.5));
        Assert.assertEquals("C", RuleService.getGradeLetter(70d));
        Assert.assertEquals("D", RuleService.getGradeLetter(69.99));
        Assert.assertEquals("F", RuleService.getGradeLetter(45d));
    }
}
