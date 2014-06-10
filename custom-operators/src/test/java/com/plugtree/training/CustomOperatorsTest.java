package com.plugtree.training;

import java.util.ArrayList;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.audit.WorkingMemoryInMemoryLogger;
import org.drools.audit.event.ActivationLogEvent;
import org.drools.audit.event.LogEvent;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.builder.conf.AccumulateFunctionOption;
import org.drools.builder.conf.EvaluatorOption;
import org.drools.event.KnowledgeRuntimeEventManager;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.Assert;
import org.junit.Test;

public class CustomOperatorsTest {

	@Test
	public void testDisjoint() throws Exception {
		KnowledgeBuilderConfiguration kbconf = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
		kbconf.setOption(EvaluatorOption.get("setop", new DisjointEvaluatorsDefinition()));
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(kbconf);
		kbuilder.add(ResourceFactory.newClassPathResource("disjoint-rules.drl"), ResourceType.DRL);
		if (kbuilder.hasErrors()) {
			for (KnowledgeBuilderError error : kbuilder.getErrors()) {
				System.out.println(error);
			}
			throw new IllegalArgumentException("Can't build knowledge package");
		}
		KnowledgeBase kbase = kbuilder.newKnowledgeBase();
		StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
		WorkingMemoryInMemoryLogger logger = new WorkingMemoryInMemoryLogger((KnowledgeRuntimeEventManager) ksession);
		List<String> list1 = new ArrayList<String>();
		list1.add("1");
		list1.add("2");
		list1.add("3");
		List<String> list2 = new ArrayList<String>();
		list2.add("2");
		list2.add("3");
		list2.add("4");
		
		ksession.insert(list1);
		ksession.insert(list2);
		ksession.fireAllRules();
		
		List<LogEvent> events = logger.getLogEvents();
		
		boolean ruleExecuted = false;
		for (LogEvent event : events) {
			if (event.getType() == LogEvent.AFTER_ACTIVATION_FIRE) {
				ActivationLogEvent actEvent = (ActivationLogEvent) event;
				if ("Disjoint is not empty".equals(actEvent.getRule())) {
					ruleExecuted = true;
					break;
				}
			}
		}
		Assert.assertTrue(ruleExecuted);
		
		ksession.dispose();
	}
	
	@Test
	public void testNotDisjoint() throws Exception {
		KnowledgeBuilderConfiguration kbconf = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
		DisjointEvaluatorsDefinition disjointdef = new DisjointEvaluatorsDefinition();
		kbconf.setOption(EvaluatorOption.get("disjoint", disjointdef));
		kbconf.setOption(EvaluatorOption.get("not disjoint", disjointdef));
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(kbconf);
		kbuilder.add(ResourceFactory.newClassPathResource("disjoint-rules.drl"), ResourceType.DRL);
		if (kbuilder.hasErrors()) {
			for (KnowledgeBuilderError error : kbuilder.getErrors()) {
				System.out.println(error);
			}
			throw new IllegalArgumentException("Can't build knowledge package");
		}
		KnowledgeBase kbase = kbuilder.newKnowledgeBase();
		StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
		WorkingMemoryInMemoryLogger logger = new WorkingMemoryInMemoryLogger((KnowledgeRuntimeEventManager) ksession);
		List<String> list1 = new ArrayList<String>();
		list1.add("1");
		list1.add("2");
		list1.add("3");
		List<String> list2 = new ArrayList<String>();
		list2.add("4");
		list2.add("5");
		list2.add("6");
		
		ksession.insert(list1);
		ksession.insert(list2);
		ksession.fireAllRules();
		
		List<LogEvent> events = logger.getLogEvents();
		
		boolean ruleExecuted = false;
		for (LogEvent event : events) {
			if (event.getType() == LogEvent.AFTER_ACTIVATION_FIRE) {
				ActivationLogEvent actEvent = (ActivationLogEvent) event;
				if ("Disjoint is empty".equals(actEvent.getRule())) {
					ruleExecuted = true;
					break;
				}
			}
		}
		Assert.assertTrue(ruleExecuted);
		
		ksession.dispose();
	}
	
	@Test
	public void testExponentialAccumulate() throws Exception {
		KnowledgeBuilderConfiguration kbconf = KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
		kbconf.setOption(AccumulateFunctionOption.get("exponential", new ExpAccumulateFunction()));
		KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(kbconf);
		kbuilder.add(ResourceFactory.newClassPathResource("exp-accum-rules.drl"), ResourceType.DRL);
		if (kbuilder.hasErrors()) {
			for (KnowledgeBuilderError error : kbuilder.getErrors()) {
				System.out.println(error);
			}
			throw new IllegalArgumentException("Can't build knowledge package");
		}
		KnowledgeBase kbase = kbuilder.newKnowledgeBase();
		StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
		ResultHolder holder = new ResultHolder();
		holder.setResult(-1.0);
		ksession.setGlobal("resultHolder", holder);
		WorkingMemoryInMemoryLogger logger = new WorkingMemoryInMemoryLogger((KnowledgeRuntimeEventManager) ksession);
		
		ksession.insert(Double.valueOf(1));
		ksession.insert(Double.valueOf(2));
		ksession.insert(Double.valueOf(3));
		ksession.insert(Double.valueOf(4));
		ksession.insert(Double.valueOf(5));
		ksession.insert(Double.valueOf(6));
		ksession.fireAllRules();
		
		List<LogEvent> events = logger.getLogEvents();
		
		boolean ruleExecuted = false;
		for (LogEvent event : events) {
			if (event.getType() == LogEvent.AFTER_ACTIVATION_FIRE) {
				ActivationLogEvent actEvent = (ActivationLogEvent) event;
				if ("Accumulation rule".equals(actEvent.getRule())) {
					ruleExecuted = true;
					break;
				}
			}
		}
		Assert.assertTrue(ruleExecuted);
		Assert.assertTrue(holder.getResult() > 719.99);
		Assert.assertTrue(holder.getResult() < 720.01);
		
		ksession.dispose();
	}
}
