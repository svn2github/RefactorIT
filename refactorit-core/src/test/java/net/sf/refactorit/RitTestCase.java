/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit;

import net.sf.refactorit.test.Utils;

import junit.framework.TestCase;

/**
 * RitTestCase -- generic rit test case, initializes test environment.
 * For refactoring test use {@link net.sf.refactorit.test.refactorings.RefactoringTestCase} class
 * 
 * @author <a href="mailto:tonis.vaga@aqris.com>Tonis Vaga</a>
 * @version $Revision: 1.3 $ $Date: 2004/12/01 10:24:35 $
 */
public class RitTestCase extends TestCase {
  static {
    Utils.setUpTestingEnvironment();
  }
  
  /**
   * 
   */
  public RitTestCase() {
  }
  /**
   * @param name
   */
  public RitTestCase(String name) {
    super(name);
  }
}
