/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.wtk;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.loader.ClassFilesLoader;
import net.sf.refactorit.loader.ClassFilesLoaderFactory;
import net.sf.refactorit.test.Utils;

import junit.framework.TestCase;


/**
 * @author risto
 */
public class LoadingTest extends TestCase {
  public LoadingTest(String name) {
    super(name);
  }
  
  public void testBugRim256() throws Exception {
    Project p = Utils.createTestRbProject("wtk");
    p.getProjectLoader().setClassFilesLoaderFactory(
        new ClassFilesLoaderFactory() {
      public ClassFilesLoader createFor(Project p) {
        ClassFilesLoader result = new ClassFilesLoader(p);
        
        result.putToCacheAsNotFound("java.io.Serializable");
        result.putToCacheAsNotFound("java.lang.Cloneable");
        
        return result;
      }
    } );

    p.getProjectLoader().build(null, false);

    assertFalse((p.getProjectLoader().getErrorCollector()).hasErrors());
  }
}
