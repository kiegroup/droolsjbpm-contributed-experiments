package org.kiegroup.zenithr.drools;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class RuleServiceTest {

    @Test
    public void testGradeLetters() {
        String json = "{\"input\": [{\"name\": \"grade\", \"type\": \"double\"}], \"rules\": [{\"when\": \"grade >= 90 && grade <= 100\", \"then\": {\"output\": \"A\"}}, {\"when\": \"grade >= 80 && grade < 90\", \"then\": {\"output\": \"B\"}}, {\"when\": \"grade >= 70 && grade < 80\", \"then\": {\"output\": \"C\"}}, {\"when\": \"grade >= 60 && grade < 70\", \"then\": {\"output\": \"D\"}}, {\"when\": \"grade < 60\", \"then\": {\"output\": \"F\"}}], \"output\": {\"type\": \"string\"}}";
        System.setProperty("GET", json);
        SessionFactory.init();

        Assert.assertEquals("A", grade(92.5));
        Assert.assertEquals("B", grade(84.5));
        Assert.assertEquals("C", grade(70d));
        Assert.assertEquals("D", grade(69.99));
        Assert.assertEquals("F", grade(45d));
    }

    private String grade(double value) {
        Map<String, String[]> parameters = new HashMap<>();
        parameters.put("grade", new String[]{String.valueOf(value)});
        return (String) (RuleService.getOutput(parameters));
    }

    @Test
    public void testAverageGrade() {
        String json = "{ \"input\": [ { \"name\": \"math\", \"type\": \"double\" }, { \"name\": \"physics\", \"type\": \"double\" }, { \"name\": \"biology\", \"type\": \"double\" } ], \"rules\": [ { \"when\": \"biology >= 0\", \"then\": { \"output\": \"(math + physics + biology) / 3\" } } ], \"output\": { \"type\": \"double\" } }";
        System.setProperty("GET", json);
        SessionFactory.init();

        Map<String, String[]> parameters = new HashMap<>();
        parameters.put("math", new String[]{String.valueOf(96d)});
        parameters.put("physics", new String[]{String.valueOf(95d)});
        parameters.put("biology", new String[]{String.valueOf(91d)});
        Object object = RuleService.getOutput(parameters);
        System.out.println(object);
        Assert.assertNotNull(object);
        Assert.assertEquals(94d, (Double) object, 0d);
    }
}
