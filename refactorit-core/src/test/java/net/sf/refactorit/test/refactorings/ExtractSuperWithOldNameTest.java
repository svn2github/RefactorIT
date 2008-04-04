/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.refactorings;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.refactorings.extractsuper.ExtractSuper;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 * @author  Arseni Grigorjev
 */
public class ExtractSuperWithOldNameTest extends ExtractSuperTest {
  
  public void performExtractTest(final ExtractSuper extractor,
      final Project project) throws Exception {
    extractor.setExtractWithOldName(true);
    super.performExtractTest(extractor, project);
  }
  
  public ExtractSuperWithOldNameTest(String name) {
    super(name);
  }
  
  public static Test suite() {
    final TestSuite suite = new TestSuite(ExtractSuperWithOldNameTest.class);
    suite.setName("ExtractSuper with old name tests");
    return suite;
  }
  
  public Project getExpectedProject() throws Exception {
    return getExpectedProject("_api");
  }
}
