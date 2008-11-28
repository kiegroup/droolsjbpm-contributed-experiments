package org.drools.process.audit;


public class ProcessInstanceDbLogTest {
    
    public static final void main(String[] args) {
        try {
            System.out.println("Checking process instances for process 'com.sample.ruleflow'");
            for (Object result: ProcessInstanceDbLog.findProcessInstances("com.sample.ruleflow")) {
                System.out.println(result);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
