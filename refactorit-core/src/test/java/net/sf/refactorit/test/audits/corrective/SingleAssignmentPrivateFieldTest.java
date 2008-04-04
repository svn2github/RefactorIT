/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.audits.corrective;

import net.sf.refactorit.audit.rules.modifiers.SingleAssignmentFinalRule;
import net.sf.refactorit.test.audits.CorrectiveActionTest;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Dmitri Grintshak
 */


public class SingleAssignmentPrivateFieldTest extends CorrectiveActionTest{
	
	public SingleAssignmentPrivateFieldTest(String name){
		super(name);
	}
	
	public static Test suite(){
		return new TestSuite(SingleAssignmentPrivateFieldTest.class);
	}
	
	public String getTemplate(){
		return "Audit/corrective/SingleAssignmentPrivate/"+
		"MyCorrectiveAction/<in_out>/<test_name>.java";
	}
	
	protected void performSimpleTest() throws Exception{
		super.performSimpleTest(SingleAssignmentFinalRule.class,
				"refactorit.audit.action.make_a_single_assignment_field_final");
	}
	
	public void testA() throws Exception{
		performSimpleTest();
	}
}
