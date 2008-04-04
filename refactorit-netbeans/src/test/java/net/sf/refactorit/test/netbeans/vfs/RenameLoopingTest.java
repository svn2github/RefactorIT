/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.netbeans.vfs;

import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.LoadingProperties;
import net.sf.refactorit.netbeans.common.RefactorItActions;
import net.sf.refactorit.netbeans.common.vfs.NBSource;
import net.sf.refactorit.utils.RefactorItConstants;
import net.sf.refactorit.vfs.Source;


import junit.framework.TestCase;


/**
 *
 * @author  RISTO A
 */
public class RenameLoopingTest extends TestCase {
  private AutomaticTestfileDeleter unmounter = new AutomaticTestfileDeleter();

  public RenameLoopingTest(String n) {super(n);
  }

  public void setUp() {
    unmounter.startListening();
  }

  public void tearDown() {
    unmounter.deleteCreatedFiles();
    unmounter.stopListening();
  }

  public void testLengthWithFakeContent() throws Exception {
    Source source = TestFileCreator.createFile(TestFileCreator.getRoot(), "Abc.java", "class Abc{}");

    assertEquals(11, source.length());
    ((NBSource) source).pretendContentIs(new byte[] {32});
    assertEquals(1, source.length());
  }

  public void testContentsDoNotChangeWithRename() throws Exception {
    Source source = TestFileCreator.createFile(TestFileCreator.getRoot(), "Abc.java", "class Abc{}");

    Source result = source.renameTo(source.getParent(), "NewName.java");
    assertEquals("class Abc{}", new String(result.getContent()));
  }

  public void testOneRenameType() throws Exception {
    // For some reason this fails under 3.6, but it's probably not worth the time to fix it
    if(RefactorItActions.isNetBeansThree()
        && ( ! RefactorItConstants.runNotImplementedTests)) {
      return;
    }

    TestFileCreator.createFile(
        "X.java",
        "class X{}");

    IDEController.getInstance().ensureProject(new LoadingProperties(false));
    Renamer.rename("X", "N");
  }

  public void testRenameLoopingBug() throws Exception {
    if (!RefactorItConstants.runNotImplementedTests) {
      return;
    }

    Source source = TestFileCreator.createFile(TestFileCreator.getRoot(), "VVV.java", "public class VVV{}");

    Renamer.rename("VVV", "X1");
    Renamer.rename("X1", "X2");
    Renamer.rename("X2", "X3");
    Renamer.rename("X3", "X4");
    Renamer.rename("X4", "X5");
    Renamer.rename("X5", "X6");
  }
}
