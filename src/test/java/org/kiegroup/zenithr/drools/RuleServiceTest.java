package org.kiegroup.zenithr.drools;

import org.junit.Assert;
import org.junit.Test;

public class RuleServiceTest {

    @Test
    public void testGradeLetters() {
        Assert.assertEquals("A", RuleService.getGradeLetter(92.5));
        Assert.assertEquals("B", RuleService.getGradeLetter(84.5));
        Assert.assertEquals("C", RuleService.getGradeLetter(70d));
        Assert.assertEquals("D", RuleService.getGradeLetter(69.99));
        Assert.assertEquals("F", RuleService.getGradeLetter(45d));
    }
}
