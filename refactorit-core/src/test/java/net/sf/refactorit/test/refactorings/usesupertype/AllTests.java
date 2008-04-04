/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.usesupertype;

import net.sf.refactorit.refactorings.usesupertype.UseSuperTypeTest;
import net.sf.refactorit.refactorings.usesupertype.UseSuperTypeUnitTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Aqris Software AS</p>
 * @author Tonis Vaga
 * @version 1.0
 */

public class AllTests extends TestCase {
  public AllTests() {
  }

  public static Test suite() {
    TestSuite result = new TestSuite("Use Super Type Tests");
    result.addTest(UseSuperTypeUnitTest.suite());
    result.addTest(UseSuperTypeTest.suite());

    return result;
  }

}
