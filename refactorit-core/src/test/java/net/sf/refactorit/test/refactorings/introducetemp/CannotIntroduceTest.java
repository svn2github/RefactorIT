/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
/* $Id: CannotIntroduceTest.java,v 1.2 2004/12/16 09:56:11 tanel Exp $ */
package net.sf.refactorit.test.refactorings.introducetemp;

import net.sf.refactorit.refactorings.RefactoringStatus;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Anton Safonov
 */
public class CannotIntroduceTest extends AllTests {
  public CannotIntroduceTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(CannotIntroduceTest.class);
  }

  public String getTemplate() {
    return "IntroduceTemp/cannotExtract/A_<test_name>.java";
  }

  public void failTest(int startLine, int startColumn, int endLine,
      int endColumn, boolean replaceAll, boolean makeFinal,
      String tempName, int expectedStatus) throws Exception {
    runTestWithProblems(startLine, startColumn, endLine, endColumn, replaceAll,
        makeFinal, tempName, null, expectedStatus);
  }

  public void testFail0() throws Exception {
    failTest(5, 17, 5, 18, false, false, "temp", RefactoringStatus.ERROR);
  }

  public void testFail1() throws Exception {
    failTest(4, 9, 5, 13, false, false, "temp", RefactoringStatus.ERROR);
  }

  public void testFail2() throws Exception {
    failTest(4, 9, 4, 20, false, false, "temp", RefactoringStatus.ERROR);
  }

  public void testFail3() throws Exception {
    failTest(4, 9, 4, 20, false, false, "temp", RefactoringStatus.ERROR);
  }

  public void testFail4() throws Exception {
    failTest(5, 9, 5, 12, false, false, "temp", RefactoringStatus.ERROR);
  }

  public void testFail5() throws Exception {
    failTest(3, 12, 3, 15, false, false, "temp", RefactoringStatus.ERROR);
  }

  public void testFail6() throws Exception {
    failTest(4, 14, 4, 19, false, false, "temp", RefactoringStatus.ERROR);
  }

  public void testFail7() throws Exception {
    failTest(4, 15, 4, 20, false, false, "temp", RefactoringStatus.ERROR);
  }

  public void testFail9() throws Exception {
    failTest(4, 19, 4, 23, false, false, "temp", RefactoringStatus.ERROR);
  }

  public void testFail10() throws Exception {
    failTest(4, 33, 4, 39, false, false, "temp", RefactoringStatus.ERROR);
  }

  public void testFail11() throws Exception {
    failTest(4, 19, 4, 20, false, false, "temp", RefactoringStatus.ERROR);
  }

// strange, why should it fail?
//  public void testFail12() throws Exception {
//    failTest(4, 16, 4, 29, false, false, "temp", RefactoringStatus.ERROR);
//  }

  public void testFail18() throws Exception {
    failTest(4, 28, 4, 29, false, false, "temp", RefactoringStatus.ERROR);
  }

  public void testFail19() throws Exception {
//		failHelper1(6, 16, 6, 18, false, false, "temp", RefactoringStatus.ERROR);
  }

  public void testFail20() throws Exception {
    failTest(3, 9, 3, 41, false, false, "temp", RefactoringStatus.ERROR);
  }

  public void testFail21() throws Exception {
    failTest(6, 9, 6, 24, false, false, "temp", RefactoringStatus.ERROR);
  }

  public void testFail22() throws Exception {
    failTest(5, 9, 5, 12, false, false, "temp", RefactoringStatus.ERROR);
  }

  public void testFail23() throws Exception {
    failTest(4, 13, 4, 14, false, false, "temp", RefactoringStatus.ERROR);
  }

  public void testFail24() throws Exception {
    failTest(4, 13, 4, 14, false, false, "temp", RefactoringStatus.ERROR);
  }

  public void testFail25() throws Exception {
    failTest(4, 17, 4, 19, false, false, "temp", RefactoringStatus.ERROR);
  }

  public void testFail26() throws Exception {
    failTest(4, 15, 4, 20, false, false, "temp", RefactoringStatus.ERROR);
  }

// Found no real problem with these 2, Anton
//  public void testFail27() throws Exception {
//    failTest(7, 13, 7, 24, true, false, "temp", RefactoringStatus.WARNING);
//  }
//
//  public void testFail28() throws Exception {
//    failTest(7, 17, 7, 28, true, false, "temp", RefactoringStatus.WARNING);
//  }

  public void testFail29() throws Exception {
    failTest(5, 32, 5, 35, true, false, "temp", RefactoringStatus.ERROR);
  }

  public void testFail30() throws Exception {
    failTest(5, 25, 5, 30, true, false, "temp", RefactoringStatus.ERROR);
  }

  public void testFail31() throws Exception {
    failTest(5, 32, 5, 33, true, false, "temp", RefactoringStatus.ERROR);
  }

  public void testFail32() throws Exception {
    failTest(6, 35, 6, 36, true, false, "temp", RefactoringStatus.ERROR);
  }

  public void testFail33() throws Exception {
    failTest(6, 17, 6, 21, true, false, "temp", RefactoringStatus.ERROR);
  }

  public void testFail34() throws Exception {
    failTest(9, 20, 9, 24, true, false, "temp", RefactoringStatus.ERROR);
  }

  public void testFailIssue310() throws Exception {
    failTest(4, 25, 4, 28, true, false, "temp", RefactoringStatus.ERROR);
  }
  
  public void testFailIssue310b() throws Exception {
    failTest(5, 21, 5, 24, true, false, "temp", RefactoringStatus.ERROR);
  }


  public int foo(int i) {
  	System.out.println("Boo: " + (i--)  + (i++));
  	return i;
  }
  
}
