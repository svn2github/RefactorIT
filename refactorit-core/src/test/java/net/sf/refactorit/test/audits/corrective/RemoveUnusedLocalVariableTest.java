/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.audits.corrective;

import net.sf.refactorit.audit.rules.UnusedLocalVariableRule;
import net.sf.refactorit.test.audits.CorrectiveActionTest;

import junit.framework.Test;
import junit.framework.TestSuite;




public class RemoveUnusedLocalVariableTest extends CorrectiveActionTest{
	
	public RemoveUnusedLocalVariableTest(String name){
		super(name);
	}
	
	
	public static Test suite(){
		return new TestSuite(RemoveUnusedLocalVariableTest.class);
	}
	
	public String getTemplate(){
		return "Audit/corrective/UnusedLocalVariable/"+
		"RemoveUnusedVariableAction/<in_out>/<test_name>.java";
	}
	
	//******************************* Simple Tests ******************************
	
	protected void performSimpleTest() throws Exception{
		super.performSimpleTest(UnusedLocalVariableRule.class,
				"refactorit.audit.action.unused_variable.remove_unused_local");
	}
	
	
	public void testRemoveUnusedLocalVariable() throws Exception{
		performSimpleTest();
	}
}
