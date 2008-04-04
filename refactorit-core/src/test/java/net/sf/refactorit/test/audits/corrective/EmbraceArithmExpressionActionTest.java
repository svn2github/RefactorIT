/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.audits.corrective;

import net.sf.refactorit.audit.rules.StringConcatOrderRule;
import net.sf.refactorit.test.audits.CorrectiveActionTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Oleg Tsernetsov
 */


public class EmbraceArithmExpressionActionTest extends CorrectiveActionTest{
	
	public EmbraceArithmExpressionActionTest(String name){
		super(name);
	}
	
	public static Test suite(){
		return new TestSuite(ConcatEmptyStringExpressionTest.class);
	}
	
	public String getTemplate(){
		return "Audit/corrective/StringConcatOrder/"+
		"EmbraceArithmExpressionAction/<in_out>/<test_name>.java";
	}
	
	/******************************* Simple Tests *******************************/
	
	protected void performSimpleTest() throws Exception{
		super.performSimpleTest(StringConcatOrderRule.class,
				"refactorit.audit.action.string_concat_order.embracearithm");
	}
	
	public void testEmbraceArithmExpression() throws Exception{
		performSimpleTest();
	}
}
