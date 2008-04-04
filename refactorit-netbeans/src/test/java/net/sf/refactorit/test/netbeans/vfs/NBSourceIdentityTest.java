/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.test.netbeans.vfs;


import junit.framework.TestCase;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.LoadingProperties;
import net.sf.refactorit.netbeans.common.vfs.NBSource;
import net.sf.refactorit.vfs.Source;

import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


/**
 *
 * @author  RISTO A
 */
public class NBSourceIdentityTest extends TestCase {
  private AutomaticTestfileDeleter u = new AutomaticTestfileDeleter();

  public NBSourceIdentityTest(String name) {super(name);
  }

  public void setUp() {
    u.startListening();
  }

  public void tearDown() {
    u.deleteCreatedFiles();
    u.stopListening();
  }

  public void testFileContentOnRename() throws Exception {
    TestFileCreator.createFile("First.java", "class First {}");
    TestFileCreator.createFile("Other.java", "class Other {}");

    IDEController.getInstance().getActiveProject().clean(); // @@@ see if we can remove this one
    IDEController.getInstance().ensureProject(new LoadingProperties(false));

    Renamer.rename("First", "FirstRenamed");
    Renamer.rename("Other", "First");

    IDEController.getInstance().ensureProject(new LoadingProperties(false));
    Project p = IDEController.getInstance().getActiveProject();

    assertEquals("class First {}",
        p.getCompilationUnitForName("First.java").getContent());
    assertEquals("class FirstRenamed {}",
        p.getCompilationUnitForName("FirstRenamed.java").getContent());
  }

  public void testParentReferencesNotObsolete() throws Exception {
    Source source = TestFileCreator.createFile(
        "f",
        "X.java",
        "package f; class X{}");
    Source parent = source.getParent();

    List parentAndItsSiblings = Arrays.asList(parent.getParent().getChildren());
    int parentIndex = parentAndItsSiblings.indexOf(parent);

    assertTrue(parentAndItsSiblings.get(parentIndex) == parent);
  }

  public void testFileObjectDelete() throws Exception {
    LocalFileSystem fs = TestFileCreator.mountNewTempFilesystem();

    NBSource oldSource = TestFileCreator.createSourceInRoot(fs, "X.java");
    oldSource.getFileObject().delete();
    NBSource newSource = TestFileCreator.createSourceInRoot(fs, "X.java");

    assertNotSame(oldSource, newSource);
  }

  public void testFileObjectRename() throws Exception {
    LocalFileSystem fs = TestFileCreator.mountNewTempFilesystem();

    NBSource oldSource = TestFileCreator.createSourceInRoot(fs, "X.java");
    rename(oldSource.getFileObject(), "X2", "java");
    NBSource newSource = TestFileCreator.createSourceInRoot(fs, "X.java");

    assertNotSame(oldSource, newSource);
  }

  private static void rename(FileObject fo, String name,
      String ext) throws IOException {
    FileLock lock = fo.lock();
    try {
      fo.rename(lock, name, ext);
    } finally {
      lock.releaseLock();
    }
  }
}
