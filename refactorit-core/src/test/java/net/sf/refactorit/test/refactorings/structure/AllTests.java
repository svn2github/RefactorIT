/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.structure;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * All tests for structure search
 * 
 * @author Sergey Fedulov
 */
public class AllTests {
  private AllTests(){
    //Private constructor to avoid creating instances and inheritance
  }
  
  public static Test suite(){
    final TestSuite suite = new TestSuite("Structure search");
    
    suite.addTest(StructureSearchFieldTest.suite());
    suite.addTest(StructureSearchParamTest.suite());
    suite.addTest(StructureSearchReturnTest.suite());
    suite.addTest(StructureSearchTypeCastTest.suite());
    suite.addTest(StructureSearchInstanceofTest.suite());
    suite.addTest(StructureSearchComparisonEqTest.suite());
    
    return suite;
  }
}
