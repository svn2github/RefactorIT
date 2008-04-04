/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.audits.corrective;

import net.sf.refactorit.audit.rules.j2se5.ForinRule;
import net.sf.refactorit.test.audits.CorrectiveActionTest;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Juri Reinsalu
 */
public class ForinTest extends CorrectiveActionTest {

  public ForinTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(ForinTest.class);
  }

  public String getTemplate() {
    return "Audit/corrective/Forin/<in_out>/<test_name>.java";
  }

  /** ********************** For-loop Array Traversal Tests ******************************* */

  public void testDuplicateNames() throws Exception {
    super.performSimpleTest(ForinRule.class,
            "refactorit.audit.action.forin.introduce.from.for.arr");
  }
  
  protected void performArrayForTraversalTest() throws Exception {
    super.performSimpleTest(ForinRule.class,
            "refactorit.audit.action.forin.introduce.from.for.arr");
  }

  public void testIntArrTraversal() throws Exception {
    performArrayForTraversalTest();
  }
  
  public void testStrFieldArrTraversal() throws Exception {
    performArrayForTraversalTest();
  }
  
  public void testExternalStrFieldArrTraversal() throws Exception {
    performArrayForTraversalTest();
  }

  /** ********************** For-loop Iterator Traversal Tests ******************************* */

  protected void performIteratorForTraversalTest() throws Exception {
    super.performSimpleTest(ForinRule.class,
            "refactorit.audit.action.forin.introduce.from.for.iterator");
  }

  public void testForIteratorTraversalNoCast() throws Exception{
    performIteratorForTraversalTest();
  }
  
  /** ********************** While-loop Iterator Traversal Tests ******************************* */


  protected void performIteratorWhileTraversalTest() throws Exception {
    super.performSimpleTest(ForinRule.class,
            "refactorit.audit.action.forin.introduce.from.while.iterator");
  }

  public void testWhileTraversalWithCast() throws Exception {
    performIteratorWhileTraversalTest();
  }
  
}
