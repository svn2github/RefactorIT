/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.audits.corrective;

import net.sf.refactorit.audit.rules.LoopConditionRule;
import net.sf.refactorit.test.audits.CorrectiveActionTest;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Oleg Tsernetsov
 */


public class ReplaceBoolEquationAssignmentTest extends CorrectiveActionTest{
	
	public ReplaceBoolEquationAssignmentTest(String name){
		super(name);
	}
	
	public static Test suite(){
		return new TestSuite(ReplaceBoolEquationAssignmentTest.class);
	}
	
	public String getTemplate(){
		return "Audit/corrective/LoopCondition/"+
		"ReplaceBoolEquationAssignment/<in_out>/<test_name>.java";
	}
	
	protected void performSimpleTest() throws Exception{
		super.performSimpleTest(LoopConditionRule.class,
				"refactorit.audit.action.loop_condition.replace");
	}
	
	public void testReplaceBoolEquation() throws Exception{
		performSimpleTest();
	}
	
}
