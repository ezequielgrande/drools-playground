package com.plugtree.training;

import java.io.File;
import java.io.IOException;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.impl.ClassPathResource;
import org.drools.logger.KnowledgeRuntimeLogger;
import org.drools.logger.KnowledgeRuntimeLoggerFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkflowProcessInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SingleEventProcessWithDataTest {

    private KnowledgeRuntimeLogger fileLogger;
    private StatefulKnowledgeSession ksession;
    
    @Before
    public void setup() throws IOException{
        this.ksession = this.createKnowledgeSession();
        
        //Console log. Try to analyze it first
        KnowledgeRuntimeLoggerFactory.newConsoleLogger(ksession);
        
        //File logger: try to open its output using Audit View in eclipse
        File logFile = File.createTempFile("process-output", "");
        System.out.println("Log file= "+logFile.getAbsolutePath()+".log");
        fileLogger = KnowledgeRuntimeLoggerFactory.newFileLogger(ksession,logFile.getAbsolutePath());
    }

    @After
    public void cleanup(){
        if (this.fileLogger != null){
            this.fileLogger.close();
        }
    } 
    
    @Test
    public void validPaymentTest(){
        //Start the process using its id
        ProcessInstance process = ksession.startProcess("com.plugtree.training.singleEventProcessWithData");
        
        //The process is in the gateway waiting for the event
        Assert.assertEquals(ProcessInstance.STATE_ACTIVE, process.getState());
        
        //The event arrives
        ksession.signalEvent("payment", 110);
        
        //The process continues until it reaches the end node
        Assert.assertEquals(ProcessInstance.STATE_COMPLETED, process.getState());
        
        //Validate paymentAmount process variable
        WorkflowProcessInstance wfProcess = (WorkflowProcessInstance) process;
        Assert.assertNotNull(wfProcess.getVariable("paymentAmount"));
        Assert.assertEquals(wfProcess.getVariable("paymentAmount"), 110);
    }
    
    @Test
    public void invalidPaymentTest(){
        //Start the process using its id
        ProcessInstance process = ksession.startProcess("com.plugtree.training.singleEventProcessWithData");
        
        //The process is in the gateway waiting for the event
        Assert.assertEquals(ProcessInstance.STATE_ACTIVE, process.getState());
        
        //The event arrives
        ksession.signalEvent("payment", 90);
        
        //The process continues until it reaches the end node
        Assert.assertEquals(ProcessInstance.STATE_COMPLETED, process.getState());

        //Validate paymentAmount process variable
        WorkflowProcessInstance wfProcess = (WorkflowProcessInstance) process;
        Assert.assertNotNull(wfProcess.getVariable("paymentAmount"));
        Assert.assertEquals(wfProcess.getVariable("paymentAmount"), 90);
    }
    
    /**
     * Creates a ksession from a kbase containing process definition
     * @return 
     */
    public StatefulKnowledgeSession createKnowledgeSession() {
        //Create the kbuilder
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        //Add simpleProcess.bpmn to kbuilder
        kbuilder.add(new ClassPathResource("singleEventProcessWithData.bpmn2"), ResourceType.BPMN2);
        System.out.println("Compiling resources");
        
        //Check for errors
        if (kbuilder.hasErrors()) {
            if (kbuilder.getErrors().size() > 0) {
                for (KnowledgeBuilderError error : kbuilder.getErrors()) {
                    System.out.println("Error building kbase: " + error.getMessage());
                }
            }
            throw new RuntimeException("Error building kbase!");
        }

        //Create a knowledge base and add the generated package
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

        //return a new statefull session
        return kbase.newStatefulKnowledgeSession();
    }
}
