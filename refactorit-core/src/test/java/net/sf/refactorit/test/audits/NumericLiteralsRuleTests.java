/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.audits;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Arseni Grigorjev
 */
public class NumericLiteralsRuleTests extends TestCase {
  
  public NumericLiteralsRuleTests(String name){
    super("NumericLiteralsRule-related tests");
  }
  
  public static Test suite() {
    TestSuite result = new TestSuite("NumericLiteralsRule-related tests");
    //result.addTest(net.sf.refactorit.test.ui.audit.numericliterals
    //    .NumLitTreeTableModelTest.suite());
    result.addTest(net.sf.refactorit.test.utils.NumericLiteralsUtilsTest.suite());
    return result;
  }  
}
