/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
/* $Id: CanIntroduceTest.java,v 1.5 2005/12/09 12:05:26 anton Exp $ */
package net.sf.refactorit.test.refactorings.introducetemp;

import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.utils.RefactorItConstants;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * @author Anton Safonov
 */
public class CanIntroduceTest extends AllTests {

  public CanIntroduceTest(String name) {
    super(name);
  }

  public static Test suite() {
    return new TestSuite(CanIntroduceTest.class);
  }

  public String getTemplate() {
    return "IntroduceTemp/canExtract/A_<test_name>_<in_out>.java";
  }

  public void test0() throws Exception {
    runTest(4, 17, 4, 18, false, false, "temp", "j");
  }

  public void test1() throws Exception {
    runTest(4, 17, 4, 18, true, false, "temp", "j");
  }

  public void test2() throws Exception {
    runTest(4, 17, 4, 18, true, true, "temp", "j");
  }

  public void test3() throws Exception {
    runTest(4, 17, 4, 18, false, true, "temp", "j");
  }

  public void test4() throws Exception {
    runTest(4, 17, 4, 22, false, false, "temp", "j");
  }

  public void test5() throws Exception {
    runTest(4, 17, 4, 22, true, false, "temp", "j");
  }

  public void test6() throws Exception {
    runTest(4, 17, 4, 22, true, true, "temp", "j");
  }

  public void test7() throws Exception {
    runTest(4, 17, 4, 22, false, true, "temp", "j");
  }

  public void test8() throws Exception {
    runTest(5, 21, 5, 26, true, false, "temp", "j");
  }

  public void test9() throws Exception {
    runTest(5, 21, 5, 26, false, false, "temp", "j");
  }

  public void test10() throws Exception {
    runTest(5, 21, 5, 26, true, false, "temp", "i");
  }

  public void test11() throws Exception {
    runTest(5, 21, 5, 26, true, false, "temp", "i");
  }

  public void test12() throws Exception {
    runTest(5, 18, 5, 23, true, false, "temp", "i");
  }

  public void test13() throws Exception {
    runTest(7, 16, 7, 42, true, false, "temp", "iterator");
  }

  public void test14() throws Exception {
    runTest(6, 15, 6, 20, false, false, "temp", "i");
  }

  public void test15() throws Exception {
    runTest(7, 23, 7, 28, false, false, "temp", "i");
  }

  public void test16() throws Exception {
    runTest(7, 23, 7, 28, false, false, "temp", "i");
  }

  public void test17() throws Exception {
    runTest(5, 21, 5, 26, true, false, "temp", "j");
  }

  public void test18() throws Exception {
    runTest(6, 21, 6, 26, true, false, "temp", "i");
  }

  public void test19() throws Exception {
    runTest(5, 20, 5, 23, true, false, "temp", "j");
  }

//cannot do it - see testFail16
//	public void test20() throws Exception{
//		helper1(5, 9, 5, 12, false, false, "temp", "temp");
//	}

  public void test21() throws Exception {
    runTest(5, 17, 5, 18, false, false, "temp", "j");
  }

//cannot do it - see testFail17
//	public void test22() throws Exception{
//		helper1(6, 13, 6, 16, false, false, "temp", "temp");
//	}

  public void test23() throws Exception {
    runTest(7, 17, 7, 20, false, false, "temp", "b");
  }

//	public void test24() throws Exception{
//test disabled - trainling semicolons are disallowed now
//		//regression test for bug#8116
//		helper1(4, 16, 4, 18, false, false, "temp", "temp");
//	}

  public void test25() throws Exception {
    runTest(4, 17, 4, 22, true, false, "temp", "i");
  }

  public void test26() throws Exception {
    runTest(5, 19, 5, 23, true, false, "temp", "i");
  }

  public void test27() throws Exception {
    runTest(4, 17, 4, 20, true, false, "temp", "j");
  }

  public void test28() throws Exception {
    runTest(4, 16, 4, 31, true, false, "temp", "b");
  }

  public void test29() throws Exception {
    runTest(4, 19, 4, 22, true, false, "temp", "str");
  }

  public void test30() throws Exception {
    runTest(5, 16, 5, 20, true, false, "temp", "i");
  }

  public void test31() throws Exception {
    runTest(5, 16, 5, 20, true, false, "temp", "j");
  }

  public void test32() throws Exception {
    runTest(4, 16, 4, 23, true, false, "temp", "j");
  }

  public void test33() throws Exception {
    runTest(4, 19, 4, 33, true, false, "temp", "object");
  }

  public void test34() throws Exception {
    // this will go with fixing of ImportManager
    if (RefactorItConstants.runNotImplementedTests) {
      runTest(4, 19, 4, 46, true, false, "temp", "list");
    }
  }

  public void test35() throws Exception {
    runTest(8, 20, 8, 29, true, false, "temp", "lists");
  }

  public void test36() throws Exception {
    runTest(11, 15, 11, 25, true, false, "temp", "foo");
  }

  public void test37() throws Exception {
    runTest(8, 21, 8, 26, true, false, "temp", "j");
  }

  public void test38() throws Exception {
    runTest(5, 28, 5, 32, true, false, "temp1", "i");
  }

  public void test39() throws Exception {
    runTest(4, 14, 4, 26, true, false, "temp", "object");
  }

  public void test40() throws Exception {
    runTest(4, 9, 4, 16, true, false, "temp", "a");
  }

  public void test41() throws Exception {
    runTest(4, 9, 4, 36, true, false, "temp", "i");
  }

  public void test42() throws Exception {
    runTest(5, 17, 5, 36, true, false, "temp", "length");
  }

  public void test43() throws Exception {
    runTest(5, 21, 5, 37, true, false, "temp", "fred");
  }

  public void test44() throws Exception {
    runTest(5, 21, 5, 29, true, false, "temp", "fred");
  }

  public void test45() throws Exception {
    runTest(4, 16, 4, 19, true, false, "temp", "i");
  }

  public void test46() throws Exception {
    runTest(4, 9, 4, 12, true, false, "temp", "i");
  }

  public void test47() throws Exception {
    runTest(5, 9, 5, 12, true, false, "temp", "i");
  }

  public void test48() throws Exception {
    runTest(4, 16, 4, 32, true, false, "temp", "str");
  }

  public void test49() throws Exception {
    runTest(5, 15, 5, 20, true, false, "temp", "b");
  }

  public void test50() throws Exception {
    runTest(5, 15, 5, 20, true, false, "temp", "b");
  }

  public void test51() throws Exception {
    runTest(5, 15, 5, 18, true, false, "temp", "i");
  }

  public void test52() throws Exception {
    runTest(15, 47, 15, 60, true, false, "valueOnIndexI", "object");
  }

  public void test53() throws Exception {
    runTest(6, 17, 6, 22, true, false, "temp", "i");
  }

  public void test54() throws Exception {
    runTest(6, 37, 6, 43, true, false, "temp", "i");
  }

  public void test55() throws Exception {
    runTest(6, 19, 6, 24, true, false, "temp", "i");
  }

  public void test56() throws Exception {
    runTest(6, 24, 6, 29, true, false, "temp", "i");
  }

  public void test57() throws Exception {
    runTest(8, 30, 8, 54, true, false, "newVariable", "str");
  }

  public void test58() throws Exception {
    runTest(7, 14, 7, 30, true, false, "temp", "b");
  }

  public void test59() throws Exception {
    runTest(7, 17, 7, 18, true, false, "temp", "i");
  }

  public void test60() throws Exception {
    runTest(7, 17, 7, 18, true, false, "temp", "i");
  }

  public void test61() throws Exception {
    runTest(7, 17, 7, 18, true, false, "temp", "i");
  }

  public void test62() throws Exception {
    runTest(10, 17, 10, 28, true, false, "temp", "str");
  }

  public void test63() throws Exception {
    runTest(9, 21, 9, 24, true, false, "temp", "str");
  }

  public void test64() throws Exception {
    runTest(10, 17, 10, 28, true, false, "temp", "str");
  }

  public void test65() throws Exception {
    runTest(6, 20, 6, 23, true, false, "temp", "str");
  }

  public void test66() throws Exception {
    runTest(7, 32, 7, 33, true, false, "temp", "exception");
  }

  public void test67() throws Exception {
    runTest(6, 16, 6, 21, true, false, "temp", "integer");
  }

  public void test68() throws Exception {
    runTest(6, 14, 6, 21, true, false, "temp", "d2");
  }

  public void test69() throws Exception {
    runTest(5, 24, 5, 26, true, false, "temp", "str2");
  }

  public void test70() throws Exception {
    runTest(7, 29, 7, 43, true, true, "temp", "j");
  }

  public void test71() throws Exception {
    runTest(8, 24, 8, 34, true, false, "temp", "str");
  }

  public void test72() throws Exception {
    runTest(8, 32, 8, 33, true, false, "temp", "j");
  }

  public void test73() throws Exception {
    runTest(6, 39, 6, 40, true, false, "temp", "j");
  }

  public void test74() throws Exception {
    runTest(7, 36, 7, 49, true, false, "temp", "str");
  }

  public void test75() throws Exception {
    runTest(7, 36, 7, 39, true, false, "temp", "j");
  }

  public void test76() throws Exception {
    runTest(7, 48, 7, 49, true, false, "temp", "j");
  }

  public void test77() throws Exception {
    runTest(10, 13, 10, 17, true, false, "temp", "i");
  }

  public void test78() throws Exception {
    runTest(6, 28, 6, 59, true, false, "temp", "temp");
  }
  
  public void test79() throws Exception {
    runTest(4, 21, 4, 35, true, false, "temp", "temp");
  }

  public void test80() throws Exception {
    runTest(5, 22, 5, 36, true, false, "temp", "temp");
  }
  
  public void test81() throws Exception {
    runTest(5, 23, 5, 35, true, false, "temp", "temp");
  }

  
  public void testZeroLengthSelection0() throws Exception {
    runTestWithProblems(4, 18, 4, 18, true, false, "temp", "j",
        RefactoringStatus.ERROR);
  }
}
