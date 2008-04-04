/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.netbeans.vcs;


import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.FileCopier;
import net.sf.refactorit.netbeans.common.projectoptions.FileObjectUtil;
import net.sf.refactorit.netbeans.common.testmodule.NBTempFileCreator;
import net.sf.refactorit.netbeans.common.testmodule.NBTestRunnerModule;
import net.sf.refactorit.netbeans.common.vcs.FileSystemProperties;
import net.sf.refactorit.netbeans.common.vcs.Vcs;
import net.sf.refactorit.netbeans.common.vcs.VcsRunner;
import net.sf.refactorit.netbeans.common.vfs.NBSource;
import net.sf.refactorit.refactorings.Refactoring;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.RenamePackage;
import net.sf.refactorit.refactorings.undo.IUndoableEdit;
import net.sf.refactorit.refactorings.undo.IUndoableTransaction;
import net.sf.refactorit.refactorings.undo.RitUndoManager;
import net.sf.refactorit.refactorings.undo.SourceInfo;
import net.sf.refactorit.test.LocalTempFileCreator;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.TempFileCreator;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.test.netbeans.vcs.testutil.TestRepository;
import net.sf.refactorit.test.netbeans.vfs.AutomaticTestfileDeleter;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.NullDialogManager;
import net.sf.refactorit.ui.RuntimePlatform;
import net.sf.refactorit.utils.ClasspathUtil;
import net.sf.refactorit.utils.FileUtil;
import net.sf.refactorit.utils.LinePositionUtil;
import net.sf.refactorit.utils.RefactorItConstants;
import net.sf.refactorit.utils.SwingUtil;
import net.sf.refactorit.utils.cvsutil.CvsFileStatus;
import net.sf.refactorit.vfs.AbstractSource;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePath;
import net.sf.refactorit.vfs.local.LocalClassPath;
import net.sf.refactorit.vfs.local.LocalSourcePath;

import org.apache.log4j.Logger;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;


import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Random;

import junit.framework.TestCase;


/** @author  risto */
public class CvsTest extends TestCase {
  private static final Logger log = Logger.getLogger(NBSource.class);

  private TempFileCreator oldTempFileCreator;
  private AutomaticTestfileDeleter unmounter = new AutomaticTestfileDeleter();

  private FileObject localFolder;
  private NBSource localFile;
  private NBSource root;

  private Project activeProject;

  public CvsTest(String name) throws Exception {
    super(name);
  }

  public void setUp() {
    activeProject = NBTestRunnerModule.testingEnvironment.activeProject;

    oldTempFileCreator = TempFileCreator.getInstance();
    TempFileCreator.setInstance(new LocalTempFileCreator());
    unmounter.startListening();
  }

  public void tearDown() {
    TempFileCreator.setInstance(oldTempFileCreator);

    unmounter.deleteCreatedFiles();
    unmounter.stopListening();
  }

  // Just sets up the testing environment -- prepares the CVS repository, etc
  public void testSetUp() throws Exception {
    deleteAllLocalFilesAndFoldersLeftOverFromLastRun();
    TestRepository.setUp();
    Utils.initialized = true;
  }

  // First modify the sources...

  public void testLocalFsIsNotVcsFileSystem() throws Exception {
    if( ! RefactorItConstants.runNotImplementedTests) {
      return;
    }

    LocalFileSystem localFileSystem = createLocalFileSystem(new File("."));

    assertFalse(FileSystemProperties.isVcsFileSystem(localFileSystem));
    assertFalse(Vcs.commit(localFileSystem.getRoot()));
  }

  public void testBadCommandName() {
    assertFalse(VcsRunner.execute(getFolderA().getFileObject(),
        "bad-command-name-rit"));
  }

  public void testDelegatingFilesystemIsVcsFilesystem() throws Exception {
    if( ! RefactorItConstants.runNotImplementedTests) {
      return;
    }

    Source aSource = TestRepository.nbProject.getPaths().getSourcePath().getRootSources()[0];

    new CvsDelegatingFilesystemTest(aSource).testDelegatingFilesystemIsVcsFilesystem();
  }

  public void testCvsFileStatus_simple() throws Exception {
    CompilationUnit binaryFile = TestRepository.nbProject.getCompilationUnitForName(
        "a/CrLfBinary.java");
    assertTrue(CvsFileStatus.getInstance().isBinary(binaryFile.
        getSource().getFileOrNull()));

    CompilationUnit textFile = TestRepository.nbProject.getCompilationUnitForName(
        "a/CrLfText.java");
    assertFalse(CvsFileStatus.getInstance().isBinary(textFile.
        getSource().getFileOrNull()));
  }

  public void testCvsFileStatus_allParents() throws Exception {
    if( ! RefactorItConstants.runNotImplementedTests) {
      return;
    }

    Source source = (NBSource) TestRepository.nbProject.
        getCompilationUnitForName("a/CrLfBinary.java").getSource();

    while (source != null) {
      assertTrue(CvsFileStatus.getInstance().isKnown(source.getFileOrNull()));
      source = source.getParent();
    }
  }

  public void testCvsFileStatus_localFiles() throws Exception {
    createLocalFolderAndFile("testCvsFileStatus_localFiles", "X.java");
    assertFalse(CvsFileStatus.getInstance().isKnown(
        localFile.getFileOrNull()));
    assertFalse(CvsFileStatus.getInstance().isKnown(
        FileObjectUtil.getFileOrNull(localFolder)));
  }

  public void testFolderNotInCvsButHasCvsSubfolder() throws Exception {
    if( ! RefactorItConstants.runNotImplementedTests) {
      return;
    }

    createLocalFolderAndFile("testFolderNotInCvsButHasCvsSubfolder", "X.java");
    createLocalFolder("subfolder", NBSource.getSource(localFolder));
    NBSource subfolder = NBSource.getSource(localFolder);
    createLocalFolder("CVS", subfolder); // this goes under "subfolder"
    assertFalse(CvsFileStatus.getInstance().isKnown(
        subfolder.getFileOrNull()));
  }

  public void testCrLfBinary_prepare() throws Exception {
    CrLfTest.testCrLfBinary_prepare();
  }

  public void testWaitUntilInCvs_ok() throws Exception {
    DialogManager oldDialogManager = DialogManager.getInstance();
    NullDialogManager d = new NullDialogManager();
    DialogManager.setInstance(d);

    long oldTimeout = NBSource.CVS_DIR_ADD_TIMEOUT;
    NBSource.CVS_DIR_ADD_TIMEOUT = 200;

    try {
      NBSource cvsFile = (NBSource) TestRepository.nbProject.
          getCompilationUnitForName("waituntilincvs/X.java").getSource();
      assertTrue(CvsFileStatus.getInstance().isKnown(cvsFile.getFileOrNull()));

      NBSource.waitUntilInCvs(cvsFile);
      assertEquals("", d.customErrorString);

    } finally {
      DialogManager.setInstance(oldDialogManager);
      NBSource.CVS_DIR_ADD_TIMEOUT = oldTimeout;
    }
  }

  public void testWaitUntilInCvs_timeout() throws Exception {
    DialogManager oldDialogManager = DialogManager.getInstance();
    NullDialogManager d = new NullDialogManager();
    DialogManager.setInstance(d);

    long oldTimeout = NBSource.CVS_DIR_ADD_TIMEOUT;
    NBSource.CVS_DIR_ADD_TIMEOUT = 200;

    try {
      NBSource dir = (NBSource) TestRepository.nbProject.
          getCompilationUnitForName("waituntilincvs/X.java").getSource().getParent();
      createLocalFile(dir.getFileObject(), "local.txt");
      assertFalse(CvsFileStatus.getInstance().isKnown(localFile.getFileOrNull()));

      NBSource.waitUntilInCvs(localFile);
      assertEquals("Timeout: failed to add to VCS: local.txt",
          d.customErrorString);

    } finally {
      DialogManager.setInstance(oldDialogManager);
      NBSource.CVS_DIR_ADD_TIMEOUT = oldTimeout;
    }
  }

  public void testRenamePackageToCvsFolder_2162() throws Exception {
    createLocalFolderAndFile("p", "X.java", "package p; class X{}");
    renamePackage("p", "p2", root, true);

    NBSource newPackage = (NBSource) root.getChild("p2");

    assertFalse(localFile.exists());
    assertTrue(newPackage.exists());
    assertFalse(CvsFileStatus.getInstance().isKnown(newPackage.getFileOrNull()));
  }

  public void testRenamePackageToCvsFolder_2162_nonPrefix() throws Exception {
    createLocalFolderAndFile("b", "X.java", "package b; class X{}");
    renamePackage("b", "b2", root, false);

    NBSource newPackage = (NBSource) root.getChild("b2");

    assertFalse(localFile.exists());
    assertTrue(newPackage.exists());
    assertFalse(CvsFileStatus.getInstance().isKnown(newPackage.getFileOrNull()));
  }

  public void testRenameCvsPackageIntoSubfolderThatsNotInCvs() throws Exception {
    // We need a new package name each time because once we've added a package to
    // CVS, there is no simple way to *really* delete it from the repository.
    // (We'd have to `rm -rf` in some repository folder I guess.)
    String packagename = "noncvsdestination" + generateUniqueId();

    createLocalFolder(packagename);
    NBSource noncvsdestination = NBSource.getSource(localFolder);

    assertFalse(CvsFileStatus.getInstance().isKnown(noncvsdestination.getFileOrNull()));
    renamePackage("a.pkgtolocalsuper", "a." + packagename, root, false);
    assertTrue(CvsFileStatus.getInstance().isKnown(noncvsdestination.getFileOrNull()));
  }

  public void testRenamePackageChangesOnlyFolderNameCaseUnderWindows() throws
      Exception {
    BinPackage p = TestRepository.nbProject.getPackageForName("bug2128");
    assertNotNull(p);

    RenamePackage r = new RenamePackage(
        new NullContext(TestRepository. nbProject), p);
    r.setNewName("Bug2128");
    assertEquals(RuntimePlatform.isWindows(), r.checkUserInput().isErrorOrFatal());
  }

  public void testRenameBackAndForth_prepare() throws Exception {
    NBSource source = (NBSource) TestRepository.nbProject.
        getCompilationUnitForName("a/RenameBackAndForth.java").getSource();
    source.setContent(
        "package a; class RenameBackAndForth{} // some new content".
        getBytes());

    source = (NBSource) source.renameTo(source.getParent(),
        "RenameBackAndForth2.java");
    assertNotNull(source);

    source = (NBSource) source.renameTo(source.getParent(),
        "RenameBackAndForth.java");
    assertNotNull(source);

    assertTrue(CvsFileStatus.getInstance().isKnown(source.getFileOrNull()));
    assertEquals("package a; class RenameBackAndForth{} // some new content",
        source.getContentString());
    assertFalse(CvsFileStatus.getInstance().isBinary(source.getFileOrNull()));
  }

  public void testRenameToExistingFile() throws Exception {
    NBSource a = (NBSource) TestRepository.nbProject.
        getCompilationUnitForName("renametoexisting/A.java").getSource();
    NBSource b = (NBSource) TestRepository.nbProject.
        getCompilationUnitForName("renametoexisting/B.java").getSource();

    assertNull(a.renameTo(a.getParent(), "B.java"));

    assertTrue(a.exists());
    assertTrue(b.exists());
    assertTrue(!a.getName().equals(b.getName()));
  }

  public void testBinaryFlagSettingFailureOnAdd() throws Exception {
    CvsFileStatus old = CvsFileStatus.getInstance();
    DialogManager oldDm = DialogManager.getInstance();

    try {
      CvsFileStatus.setInstance(new CvsFileStatus() {
        public boolean isBinary(File f) {
          return true;
        }
      });

      NullDialogManager dm = new NullDialogManager();
      DialogManager.setInstance(dm);

      NBSource dir = (NBSource) TestRepository.nbProject.
          getCompilationUnitForName("renamenoncvsfile/Placeholder.java").getSource().
          getParent();
      Source result = dir.createNewFile("Tbfsfoa.java");
      assertTrue(result.inVcs());
      assertEquals("info.failed.cvs.command", dm.infoString);
    } finally {
      CvsFileStatus.setInstance(old);
      DialogManager.setInstance(oldDm);
    }
  }

  public void testRenamePackagePrefix_prepare() throws Exception {
    BinPackage aPackage = TestRepository.nbProject.getPackageForName(
        "a.renametest");
    RenamePackage renamer = new RenamePackage(
        new NullContext(TestRepository.nbProject), aPackage);
    renamer.setRenamePrefix(true);
    renamer.setPrefix(aPackage.getQualifiedName());
    renamer.setNewName("a.renametest.addition");

    RefactoringStatus result = renamer.checkPreconditions();
    assertTrue(result.getAllMessages(), result.isOk());

    result = renamer.apply();

    assertTrue(result.getAllMessages(), result.isOk());
  }

  public void testInputStream_prepare() throws Exception {
    String content = LinePositionUtil.useUnixNewlines(TestRepository.nbProject.
        getCompilationUnitForName("a/X.java").getContent());

    assertEquals("package a;\n\n" + "public class X {}\n",
        content);
  }

  public void testOutputStream_prepare() throws Exception {
    OutputStream stream = TestRepository.nbProject.getCompilationUnitForName(
        "a/X.java").
        getSource().getOutputStream();
    stream.write("package a;\n\npublic class X {} // new stuff".getBytes());
    stream.close();
  }

  public void testDeleteFile_prepare() throws Exception {
    TestRepository.nbProject.getCompilationUnitForName("a/ToDelete.java").getSource().
        delete();
  }

  public void testDeleteFolder_prepare() throws Exception {
    CompilationUnit compilationUnit = TestRepository.nbProject.getCompilationUnitForName(
        "todelete/X.java");
    Source dir = compilationUnit.getSource().getParent();

    compilationUnit.getSource().delete();
    dir.delete();
  }

  public void testCreatingFile_prepare() throws Exception {
    Source newFile = getFolderA().createNewFile("New.java");

    FileCopier.writeStringToWriter("package a; public class New{}",
        new OutputStreamWriter(newFile.getOutputStream()));
  }

  public void testDeletingLocalFile() throws Exception {
    createLocalFolderAndFile("testDeletingLocalFile", "X.java");
    assertTrue(localFile.delete());
  }

  public void testDeletingLocalFolder() throws Exception {
    createLocalFolder("testDeletingLocalFolder");
    assertTrue(NBSource.getSource(localFolder).delete());
  }

  public void testCreatingFileInLocalDir() throws Exception {
    createLocalFolder("testCreatingFileInLocalDir");
    NBSource localDir = NBSource.getSource(localFolder);

    Source newFile = localDir.createNewFile("X.java");
    assertFalse(newFile.inVcs());
  }

  public void testCreatingFileAndRenamingIt_prepare() throws Exception {
    NBSource newFile = (NBSource) getFolderA().createNewFile("New2.java");

    FileCopier.writeStringToWriter("package a; public class New2{}",
        new OutputStreamWriter(newFile.getOutputStream()));

    newFile = (NBSource) newFile.renameTo(newFile.getParent(),
        "NewRenamed.java");
    assertNotNull(newFile);
  }

  public void testMakeDir_prepare() throws Exception {
    // Does not actually make the dir anymore: actually just asserts that a dir has been made
    // (and makes it again if it were ever removed).
    String filename = "somefile.txt";
    Source newDir = getFolderA().mkdir("newdir");

    IUndoableTransaction trans = RitUndoManager.getCurrentTransaction();
    IUndoableEdit undo = null;

    if (trans != null) {
      undo = trans.createCreateFileUndo(new SourceInfo(newDir, filename));
    }

    Source result = newDir.createNewFile(filename);

    if (trans != null && result !=null) {
      trans.addEdit(undo);
    }
  }

  public void testRenameFile_prepare() throws Exception {
    NBSource source = (NBSource) TestRepository.nbProject.
        getCompilationUnitForName("a/ToRename.java").getSource();
    source.renameTo(source.getParent(), "ToRename_NewName.java");
  }

  public void testSeveralRenames_prepare() throws Exception {
    NBSource source = (NBSource) TestRepository.nbProject.
        getCompilationUnitForName("a/SeveralRenames.java").getSource();
    for (int i = 0; i < 10; i++) {
      source = (NBSource) source.renameTo(source.getParent(),
          "SeveralRenames_" + i + ".java");
    }

    source.renameTo(source.getParent(), "SeveralRenames_final.java");
  }

  public void testChangeFileNameCase_prepare() throws Exception {
    NBSource source = (NBSource) TestRepository.nbProject.
        getCompilationUnitForName("a/CaseRename.java").getSource();
    Source result = source.renameTo(source.getParent(), "Caserename.java");

    if (RuntimePlatform.isWindows()) {
      assertNull(result);
    } else {
      assertEquals("Caserename.java", result.getName());
    }
  }

  // This was meant to reproduce a bug, but it didn't... Anyway, I'll lave it here for regression testing
  public void testCreateNewFileInFolderWhichIsNotAddedToCvs() throws Exception {
    createLocalFolderAndFile("testCreateNewFileInFolderWhichIsNotAddedToCvs",
        "A.java");
  }

  public void testGetInputAndOuputStreamOnNonCvsFile() throws Exception {
    createLocalFolderAndFile("testGetInputAndOuputStreamOnNonCvsFile", "A.java");

    final String fileContents = "class A{}";
    FileCopier.writeStringToWriter(fileContents,
        new OutputStreamWriter(localFile.getOutputStream()));
    String contents = FileCopier.readReaderToString(new InputStreamReader(
        localFile.getInputStream()));
    assertEquals(fileContents, contents);
  }

  public void testMkdirAndDeleteItInNonCvsFolder() throws Exception {
    createLocalFolder("testMkdirInNonCvsFolder");

    Source childFolder = NBSource.getSource(localFolder).mkdir("childFolder");
    assertFalse(childFolder.inVcs());

    childFolder.delete();
  }

  public void testMkdirDoesNotDeadlockInSwingThread() throws Exception {
    // This also tests mkdir in general

    final Source newDir[] = new Source[] {null};

    SwingUtil.invokeAndWaitFromAnyThread(new Runnable() {
      public void run() {
        newDir[0] = getFolderA().mkdir("swingnewdir" + generateUniqueId());
      }
    });

    assertNotNull(newDir[0]);
    assertTrue(newDir[0].inVcs());
  }

  public void testRenameRegularFileIntoCvsFolder_prepare() throws Exception {
    createLocalFolderAndFile("trrficf", "X.java");

    FileCopier.writeStringToWriter("package trrficf; public class X{}",
        new OutputStreamWriter(localFile.getOutputStream()));

    Source dest = TestRepository.nbProject.
        getCompilationUnitForName("renamefromlocal/Placeholder.java").getSource().
        getParent();
    Source result = localFile.renameTo(dest, "X.java");

    assertFalse(result.inVcs());
    assertFalse(localFile.exists());
  }

  public void testRenameFromCvsIntoNonCvsFolder_prepare() throws Exception {
    createLocalFolder("testRenameFromCvsIntoNonCvsFolder" + generateUniqueId());

    NBSource source = (NBSource) TestRepository.nbProject.
        getCompilationUnitForName("renametolocal/Placeholder.java").getSource();
    Source dest = source.renameTo(NBSource.getSource(localFolder), "X.java");

    assertFalse(dest.inVcs());
    assertFalse(dest.getParent().inVcs());

    assertFalse(source.exists());
  }

  public void testRenamingFromLocalFolderToLocalFolder() throws Exception {
    createLocalFolderAndFile("testRenamingFromLocalFolderToLocalFolder_source",
        "X.java");
    createLocalFolder("testRenamingFromLocalFolderToLocalFolder_dest");

    Source result = localFile.renameTo(NBSource.getSource(localFolder),
        localFile.getName());
    assertFalse(result.inVcs());
    assertFalse(result.getParent().inVcs());
  }

  public void testRenameInNonCvsFolder_prepare() throws Exception {
    createLocalFolderAndFile("testRenameInNonCvsFolder", "X.java");
    Source newFile = localFile.renameTo(localFile.getParent(), "Y.java");

    assertEquals(1, newFile.getParent().getChildren().length);
    assertTrue(((NBSource) newFile).exists());
  }

  public void testRenameOfNonCvsFileInCvsFolder_prepare() throws Exception {
    createLocalFileInCvsFolder("X.java");

    Source newFile = localFile.renameTo(localFile.getParent(), "Y.java");
    assertEquals("Y.java", newFile.getName());
  }

  public void testGetInputStreamAndGetOutputStreamOnANonCvsFileInCvsFolder() throws
      Exception {
    NBSource dir = (NBSource) TestRepository.nbProject.
        getCompilationUnitForName("renamenoncvsfile/Placeholder.java").getSource().
        getParent();
    FileObject f = dir.getFileObject().createData("TGISAGOSOANCFICF.java");
    FileLock l = null;

    try {
      l = f.lock();

      final String fileContents = "class TGISAGOSOANCFICF{}";
      FileCopier.writeStringToWriter(fileContents,
          new OutputStreamWriter(f.getOutputStream(l)));
      String contents = FileCopier.readReaderToString(new InputStreamReader(f.
          getInputStream()));
      assertEquals(fileContents, contents);
    } finally {
      if (l != null) {
        l.releaseLock();
      }
      f.delete();
    }
  }

  // ... then commit and check out the modifications all at once (for speed) ....

  public void testCommitChanges() throws Exception {
    TestRepository.commitChanges();
  }

  // ... then run the assertions.

  public void testCrLfBinary_assert() throws Exception {
    CrLfTest.testCrLfBinary_assert();
  }

  public void testRenamePackagePrefix_assert() throws Exception {
    Project expected = Utils.createTestRbProject("NbCvs_after_dir_rename");
    expected.getProjectLoader().build(null, false);

    Source parentDir = TestRepository.localCheckoutDir.getChild("a").getChild(
        "renametest");
    Project actual = new Project("",
        new LocalSourcePath(parentDir.getFileOrNull().
            getAbsolutePath()), null, null);

    assertSameSourcesIgnoreNewline(expected, actual);
  }

  public void testRenameBackAndForth_assert() throws Exception {
    Source folder = TestRepository.localCheckoutDir.getChild("a");
    assertNotNull(folder.getChild("RenameBackAndForth.java"));
    assertNull(folder.getChild("RenameBackAndForth2.java"));
  }

  public void testOutputStream_assert() throws Exception {
    Source newCopy = TestRepository.localCheckoutDir.getChild("a").getChild(
        "X.java");
    String content = new String(newCopy.getContent());

    assertEquals("package a;\n\npublic class X {} // new stuff", content);
  }

  public void testDeleteFile_assert() throws Exception {
    assertNull(TestRepository.localCheckoutDir.getChild("a").getChild(
        "ToDelete.java"));
  }

  public void testDeleteFolder_assert() throws Exception {
    Source dir = TestRepository.localCheckoutDir.getChild("todelete");
    assertNull(dir);
  }

  public void testCreatingFile_assert() throws Exception {
    Source newFile = TestRepository.localCheckoutDir.getChild("a").getChild(
        "New.java");
    assertNotNull(newFile);

    assertEquals("package a; public class New{}", FileCopier.readReaderToString(
        new InputStreamReader(newFile.getInputStream())));
  }

  public void testCreatingFileAndRenamingIt_assert() throws Exception {
    Source newFile = TestRepository.localCheckoutDir.getChild("a").getChild(
        "New2.java");
    assertNull(newFile);

    newFile = TestRepository.localCheckoutDir.getChild("a").getChild(
        "NewRenamed.java");
    assertNotNull(newFile);

    assertEquals("package a; public class New2{}", FileCopier.readReaderToString(
        new InputStreamReader(newFile.getInputStream())));
  }

  public void testMakeDir_assert() throws Exception {
    Source parentDir = TestRepository.localCheckoutDir.getChild("a");
    Source dir = parentDir.getChild("newdir");
    assertNotNull(dir);

    Source newSource = dir.getChild("somefile.txt");
    assertNotNull(newSource);
  }

  public void testRenameFile_assert() throws Exception {
    Source parentDir = TestRepository.localCheckoutDir.getChild("a");

    Source source = parentDir.getChild("ToRename_NewName.java");
    assertNotNull(source);

    Source oldSource = parentDir.getChild("ToRename.java");
    assertNull(oldSource);
  }

  public void testSeveralRenames_assert() throws Exception {
    Source parentDir = TestRepository.localCheckoutDir.getChild("a");

    Source source = parentDir.getChild("SeveralRenames_final.java");
    assertNotNull(source);

    for (int i = 0; i < 100; i++) {
      Source middleSource = parentDir.getChild("SeveralRenames_" + i + ".java");
      assertNull(middleSource);
    }

    Source oldSource = parentDir.getChild("SeveralRenames.java");
    assertNull(oldSource);
  }

  public void testChangeFileNameCase_assert() throws Exception {
    Source parentDir = TestRepository.localCheckoutDir.getChild("a");

    if (RuntimePlatform.isWindows()) {
      Source source = parentDir.getChild("CaseRename.java");
      assertNotNull(source);
    } else {
      Source source = parentDir.getChild("Caserename.java");
      assertNotNull(source);
    }
  }

  public void testRenameRegularFileIntoCvsFolder_assert() throws Exception {
    assertNull(TestRepository.localCheckoutDir.getChild("renamefromlocal").
        getChild("X.java"));
  }

  public void testRenameFromCvsIntoNonCvsFolder_assert() throws Exception {
    assertNull(TestRepository.localCheckoutDir.getChild("renametolocal").
        getChild("Placeholder.java"));
  }

  public void testRenameInNonCvsFolder_assert() throws Exception {
    assertNull(TestRepository.localCheckoutDir.getChild(
        "testRenameInNonCvsFolder"));
  }

  public void testRenameOfNonCvsFileInCvsFolder_assert() throws Exception {
    assertNull(TestRepository.localCheckoutDir.getChild("renamenoncvsfile").
        getChild("X.java"));
    assertNull(TestRepository.localCheckoutDir.getChild("renamenoncvsfile").
        getChild("Y.java"));
  }

  // FIXME Commented out -- does not run for some reason, and it is not that important.
  /*public void testCrLfText_prepare() throws Exception {
    CrLfTest.testCrLfText_prepare();
     }*/
  /*public void testCrLfText_assert() throws Exception {
    CrLfTest.testCrLfText_assert();
     }*/

  /* Util methods */

  public void deleteAllLocalFilesAndFoldersLeftOverFromLastRun() {
    Source[] rootSources = getRootSourcesOnSourcepath();
    if(rootSources.length == 0) {
      throw new RuntimeException("No mounted filesystems found");
    }
    // FIXME: Just selecting the first one is not ideal
    Source rootSource = rootSources[0];

    Source[] children = rootSource.getChildren();
    for (int i = 0; i < children.length; i++) {
      deleteChildrenRecursive(children[i], new LocalFileFinder());
    }
  }

  private Source[] getRootSourcesOnSourcepath() {
    SourcePath sourcePath = activeProject.getPaths().getSourcePath();
    return sourcePath.getRootSources();
  }

  private static void deleteChildrenRecursive(Source s, Filter f) {
    if(AbstractSource.inVersionControlDirList(s.getName()) && s.isDirectory()) {
      return;
    }

    Source[] children = s.getChildren();
    for (int i = 0; i < children.length; i++) {
      Source child = children[i];

      if(AbstractSource.inVersionControlDirList(child.getName()) && child.isDirectory()) {
        continue;
      }

      if (child.isDirectory()) {
        deleteChildrenRecursive(child, f);
      }
      if (f.shouldDelete(child)) {
        assertTrue("Must delete: " + child.getAbsolutePath(), child.delete());
      }
    }
  }

  public static void assertEmpty(FileObject folder) {
    assertEquals(0, NBSource.getSource(folder).getChildren().length);
  }

  public static void assertSameSourcesIgnoreNewline(Project expected,
      Project actual) {
    Project expectedWithRightNewline = RwRefactoringTestUtils.
        createMutableProject(expected);
    fixCrLf(expectedWithRightNewline);

    Project actualWithRightNewline = RwRefactoringTestUtils.
        createMutableProject(actual);
    fixCrLf(actualWithRightNewline);

    RwRefactoringTestUtils.assertSameSources("", expectedWithRightNewline,
        actualWithRightNewline);
  }

  private static void fixCrLf(Project p) {
    FileUtil.fixCrLf(p.getPaths().getSourcePath().getRootSources()[0].getFileOrNull());
  }

  public void createLocalFolderAndFile(String folderName, String fileName,
      String fileContents) throws IOException {
    createLocalFolderAndFile(folderName, fileName);
    FileCopier.writeStringToWriter(fileContents,
        new OutputStreamWriter(localFile.getOutputStream()));
  }

  private void createLocalFolderAndFile(final String folderName,
      final String filename) throws IOException {
    createLocalFolder(folderName);
    createLocalFile(localFolder, filename);
  }

  private void createLocalFile(FileObject parent,
      String filename) throws IOException {
    FileObject newFile = parent.createData(filename);
    localFile = NBSource.getSource(newFile);
  }

  public void createLocalFolder(final String folderName) throws IOException {
    Source dir = getFolderA();
    createLocalFolder(folderName, dir);
    root = NBSource.getSource(localFolder.getParent());
  }

  private NBSource getFolderA() {
    return (NBSource) TestRepository.nbProject.getCompilationUnitForName(
        "a/CaseRename.java").
        getSource().getParent();
  }

  public void createLocalFolder(final String folderName,
      final Source parent) throws IOException {
    localFolder = ((NBSource) parent).getFileObject().createFolder(folderName);
  }

  private void createLocalFileInCvsFolder(final String filename) throws
      IOException {
    NBSource dir = (NBSource) TestRepository.nbProject.
        getCompilationUnitForName("renamenoncvsfile/Placeholder.java").getSource().
        getParent();
    createLocalFile(dir.getFileObject(), filename);
  }

  private void renamePackage(final String from, final String to,
      final NBSource sourcepathRoot, boolean prefix) throws Exception {

    DialogManager oldDialogManager = DialogManager.getInstance();

    try {
      DialogManager.setInstance(new NullDialogManager());

      Project p = new Project("",
          new NBTempFileCreator().createSourcePath(
              sourcepathRoot), new LocalClassPath(ClasspathUtil.getDefaultClasspath()), null);
      p.getProjectLoader().build(null, false);
      RenamePackage renamer = new RenamePackage(new NullContext(p),
          p.createPackageForName(from));
      renamer.setNewName(to);
      if (prefix) {
        renamer.setRenamePrefix(true);
        renamer.setPrefix(from);
      }
      RefactoringStatus status =
        renamer.apply();

      assertTrue(status.getAllMessages(), status.isOk());
    } finally {
      DialogManager.setInstance(oldDialogManager);
    }
  }

  public static LocalFileSystem createLocalFileSystem(File root) throws IOException, PropertyVetoException {
    LocalFileSystem result = new LocalFileSystem();
    result.setRootDirectory(root);
    Repository.getDefault().addFileSystem(result);
    return result;
  }

  private static long counter = 0;
  static String generateUniqueId() {
    return
        ("R" + System.currentTimeMillis()) +
        ("E" + new Random().nextInt(Integer.MAX_VALUE)) +
        ("F" + ((long)new Object().hashCode())) + ("A" + ++counter);
  }

  private interface Filter {
    boolean shouldDelete(Source s);
  }


  private static class LocalFileFinder implements Filter {
    public boolean shouldDelete(Source s) {
      return ! CvsFileStatus.getInstance().isKnown(s.getFileOrNull());
    }
  }
}
