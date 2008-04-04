/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.inlinevariable;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.query.ItemByNameFinder;
import net.sf.refactorit.refactorings.inlinevariable.InlineVariable;
import net.sf.refactorit.test.refactorings.NullContext;

import junit.framework.Test;
import junit.framework.TestSuite;


/** @author  RISTO A */
public class AllTests {
  /** Hidden constructor. */
  private AllTests() {}

  public static Test suite() {
    TestSuite result = new TestSuite();
    result.addTest(CanInlineTest.suite());
    result.addTest(CannotInlineTest.suite());
    result.addTest(InlineGenericsTest.suite());
    return result;
  }

  public static InlineVariable createRefactoring(String fieldOrTempName,
      Project project) {
    return new InlineVariable(new NullContext(project),
        ItemByNameFinder.findVariable(project, fieldOrTempName));
  }
}
