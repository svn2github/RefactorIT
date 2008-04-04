/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings.rename;

import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.query.text.ManagingNonJavaIndexer;
import net.sf.refactorit.query.text.PackageQualifiedNameIndexer;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.RenamePackage;
import net.sf.refactorit.source.edit.DirCreator;
import net.sf.refactorit.source.edit.FileRenamer;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.test.refactorings.NullContext;
import net.sf.refactorit.test.refactorings.ParentFinderTest;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.NullDialogManager;
import net.sf.refactorit.vfs.Source;

import org.apache.log4j.Category;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;




/**
 * Test driver for {@link net.sf.refactorit.refactorings.rename.RenamePackage}.
 */
public class RenamePackageTest extends TestCase {
  private DialogManager oldInstance;
  private NullDialogManager nullDialogManager;

  /** Logger instance. */
  private static final Category cat =
      Category.getInstance(RenamePackageTest.class.getName());

  public RenamePackageTest(String name) {
    super(name);
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(RenamePackageTest.class);
    suite.addTest(ParentFinderTest.suite());
    suite.setName("Rename Package tests");
    return suite;
  }

  public void setUp() {
    oldInstance = DialogManager.getInstance();
    nullDialogManager = new NullDialogManager();
    DialogManager.setInstance(nullDialogManager);
  }

  public void tearDown() {
    DialogManager.setInstance(oldInstance);
  }

  public void testDeletingClassFiles() throws Exception {
    testRenamePackage("dir_rename_delete_classes", "a", "b");
  }

  /**
   * Tests for bug #1187: Rename Package does not rename directories.
   */
  public void testBug1187() throws Exception {
    testRenamePackage("dir_rename", "a", "b");
  }

  public void testBug1187_delete_classes() throws Exception {
    testRenamePackagePrefix("dir_rename_delete_classes", "a", "b");
  }

  public void testBug1187_prefix() throws Exception {
    testRenamePackagePrefix("dir_rename_prefix", "com.myco", "com.second.cmp");
  }

  public void testBug1187_prefix_two_sourceroots() throws Exception {
    Project before = RwRefactoringTestUtils.createMutableProject(
        Utils.createTestRbProjectFromXml(
        "dir_rename_prefix_two_sourceroots_before"));
    before.getProjectLoader().build();

    Project after = Utils.createTestRbProjectFromXml(
        "dir_rename_prefix_two_sourceroots_after");
    after.getProjectLoader().build();

    RenamePackage renamer = new RenamePackage(
        new NullContext(before), before.getPackageForName("a"));
    renamer.setPrefix("a");
    renamer.setRenamePrefix(true);
    renamer.setNewName("b");

    RwRefactoringTestUtils.assertRefactoring(renamer, before, after);
  }

  public void testRenamingBackAndForth() throws Exception {
    Project before = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit("package a;\n\n" + "public class X{}",
        "X.java", "a"),
    });

    Project after = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit("package b;\n\n" + "public class X{}",
        "X.java", "b"),
    });

    Project latest = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit("package a;\n\n" + "public class X{}",
        "X.java", "a"),
    });

    RenamePackage renamer = new RenamePackage(new NullContext(before),
        before.getPackageForName("a"));
    renamer.setNewName("b");
    RwRefactoringTestUtils.assertRefactoring(renamer, before, after);

    before.getProjectLoader().build(null, false);

    renamer = new RenamePackage(new NullContext(before),
        before.getPackageForName("b"));
    renamer.setNewName("a");
    RwRefactoringTestUtils.assertRefactoring(renamer, before, latest);
  }

  public void testRenamingBackAndForth_prefix() throws Exception {
    Project before = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit("package a;\n\n" + "public class X{}",
        "X.java", "a"),
    });

    Project after = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit("package b;\n\n" + "public class X{}",
        "X.java", "b"),
    });

    Project latest = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit("package a;\n\n" + "public class X{}",
        "X.java", "a"),
    });

    RenamePackage renamer = new RenamePackage(new NullContext(before),
        before.getPackageForName("a"));
    renamer.setRenamePrefix(true);
    renamer.setPrefix("a");
    renamer.setNewName("b");
    RwRefactoringTestUtils.assertRefactoring(renamer, before, after);

    before.getProjectLoader().build(null, false);

    renamer = new RenamePackage(new NullContext(before),
        before.getPackageForName("b"));
    renamer.setRenamePrefix(true);
    renamer.setPrefix("b");
    renamer.setNewName("a");
    RwRefactoringTestUtils.assertRefactoring(renamer, before, latest);
  }

  public void testBug1187_prefix_renameTopFolder() throws Exception {
    testRenamePackagePrefix("dir_rename_prefix_rename_top_folder", "a.b.c",
        "aa.b.c");
  }

  public void testBug1187_prefix_movedown() throws Exception {
    testRenamePackagePrefix("dir_rename_prefix_movedown", "com.myco",
        "com.myco.a");
  }

  public void testBug1187_prefix_movedown_long() throws Exception {
    testRenamePackagePrefix("dir_rename_prefix_movedown_long", "a.renametest",
        "a.renametest.addition");
  }

  public void testBug1187_prefix_movedown_middle_folder_has_files() throws
      Exception {
    testRenamePackagePrefix("dir_rename_prefix_middle_folder_has_files", "a",
        "z.a");
  }

  public void testBug1187_prefix_moveup() throws Exception {
    testRenamePackagePrefix("dir_rename_prefix_moveup", "com.myco", "com");
  }

  public void testSinglePackage_movingNonJavaFiles() throws Exception {
    Project before = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit("package a;\n\n" + "public class X{}",
        "X.java", "a"),
        new Utils.TempCompilationUnit("i", "somefile.txt", "a"),
    });

    Project after = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit("package b;\n\n" + "public class X{}",
        "X.java", "b"),
        new Utils.TempCompilationUnit("i", "somefile.txt", "b"),
    });

    RenamePackage renamer = new RenamePackage(new NullContext(before),
        before.getPackageForName("a"));
    renamer.setNewName("b");
    RwRefactoringTestUtils.assertRefactoring(renamer, before, after);
  }

  public void testSourcesRelocatedSilently() throws Exception {
    Project before = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit("package a;\n\n" + "public class X{}",
        "X.java", "a"),
    });

    Project after = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit("package b;\n\n" + "public class X{}",
        "X.java", "b"),
    });

    RenamePackage renamer = new RenamePackage(new NullContext(before),
        before.getPackageForName("a"));
    renamer.setNewName("b");
    RwRefactoringTestUtils.assertRefactoring(renamer, before, after);

    assertEquals("", nullDialogManager.asked);
  }

  public void testWarnOfRelocationForManualCvs_answerYes() throws Exception {
    Project before = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit("package a;\n\n" + "public class X{}",
        "X.java", "a"),
        new Utils.TempCompilationUnit("", "random.txt", "CVS")
    });

    Project after = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit("package b;\n\n" + "public class X{}",
        "X.java", "b"),
        new Utils.TempCompilationUnit("", "random.txt", "CVS")
    });

    RenamePackage renamer = new RenamePackage(new NullContext(before),
        before.getPackageForName("a"));
    renamer.setNewName("b");
    RwRefactoringTestUtils.assertRefactoring(renamer, before, after);

    assertEquals("rename.package.relocate.types.of.packages",
        nullDialogManager.asked);
  }

  public void testWarnOfRelocationForManualCvs_answerNo() throws Exception {
    Project before = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit("package a;\n\n" + "public class X{}",
        "X.java", "a"),
        new Utils.TempCompilationUnit("", "random.txt", "CVS")
    });

    Project after = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit("package b;\n\n" + "public class X{}",
        "X.java", "a"),
        new Utils.TempCompilationUnit("", "random.txt", "CVS")
    });

    nullDialogManager.answerToYesNoQuestion = DialogManager.NO_BUTTON;

    RenamePackage renamer = new RenamePackage(new NullContext(before),
        before.getPackageForName("a"));
    renamer.setNewName("b");
    RwRefactoringTestUtils.assertRefactoring(renamer, before, after);

    assertEquals("rename.package.relocate.types.of.packages",
        nullDialogManager.asked);
  }

  public void testNoWarningOfRelocationForCvsSupport() throws Exception {
    Project before = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit("package a;\n\n" + "public class X{}",
        "X.java", "a"),
        new Utils.TempCompilationUnit("", "random.txt", "CVS")
    });

    Project after = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit("package b;\n\n" + "public class X{}",
        "X.java", "b"),
        new Utils.TempCompilationUnit("", "random.txt", "CVS")
    });

    RenamePackage renamer = new RenamePackage(new NullContext(before),
        before.getPackageForName("a")) {
      protected boolean supportsVcs(Source source) {
        return true;
      }
    };
    renamer.setNewName("b");
    RwRefactoringTestUtils.assertRefactoring(renamer, before, after);

    assertEquals("", nullDialogManager.asked);
  }

  public void testWarningMessageForNotRelocatedFiles() throws Exception {
    Project before = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit("package a;\n\n" + "public class X{}",
        "X.java", "badfoldername")
    });

    Project after = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit("package b;\n\n" + "public class X{}",
        "X.java", "badfoldername")
    });

    RenamePackage renamer = new RenamePackage(new NullContext(before),
        before.getPackageForName("a"));
    renamer.setNewName("b");

    RefactoringStatus status =
      renamer.apply();

    assertTrue(status.isInfoOrWarning());
    assertEquals(DirCreator.PACKAGE_NOT_RELOCATED_MSG,
        status.getAllMessages());
    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testWarningMessageForNotRelocatedFiles_sourceFailure() throws
      Exception {
    Project before = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit("package a;\n\n" + "public class X{}",
        "X.java", "a"),
        new Utils.TempCompilationUnit(
        "// this-file-causes-an-error-messsage-on-rename", "X.java", "b")
    });

    RenamePackage renamer = new RenamePackage(new NullContext(before),
        before.getPackageForName("a"));
    renamer.setNewName("b");

    RefactoringStatus status =
      renamer.apply();

    assertTrue(status.isInfoOrWarning());
    assertEquals(FileRenamer.FILE_NOT_RELOCATED_MSG + "a/X.java",
        status.getAllMessages());
  }

  public void testWarningMessageForNotRelocatedFiles_renamePrefix() throws
      Exception {
    Project before = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit("package a;\n\n" + "public class X{}",
        "X.java", "badfoldername"),
        new Utils.TempCompilationUnit("package a.a;\n\n" + "public class X{}",
        "X.java", "badfoldername-subfolder")
    });

    Project after = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit("package b;\n\n" + "public class X{}",
        "X.java", "badfoldername"),
        new Utils.TempCompilationUnit("package b.a;\n\n" + "public class X{}",
        "X.java", "badfoldername-subfolder")
    });

    RenamePackage renamer = new RenamePackage(new NullContext(before),
        before.getPackageForName("a"));
    renamer.setRenamePrefix(true);
    renamer.setPrefix("a");
    renamer.setNewName("b");

    RefactoringStatus status =
      renamer.apply();

    assertTrue(status.isInfoOrWarning());
    assertEquals(
        FileRenamer.FILE_NOT_RELOCATED_MSG
        + "badfoldername-subfolder/X.java\n\n" +
        FileRenamer.FILE_NOT_RELOCATED_MSG + "badfoldername/X.java",
        status.getAllMessages());
    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testCannotRenamePrefixToMatchExistingPackage() throws Exception {
    Project before = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit("package a;\n\n" + "public class X{}",
        "X.java", "a"),
        new Utils.TempCompilationUnit("package b;\n\n" + "public class Y{}",
        "Y.java", "b")
    });

    RenamePackage renamer = new RenamePackage(new NullContext(before),
        before.getPackageForName("a"));
    renamer.setRenamePrefix(true);
    renamer.setPrefix("a");
    renamer.setNewName("b");
    RefactoringStatus status = renamer.checkUserInput();

    assertTrue(status.isErrorOrFatal());
  }

  public void testCanRenamePrefixToMatchParentOfExistingPackage() throws
      Exception {
    Project before = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit("package a;\n\n" + "public class X{}",
        "X.java", "a"),
        new Utils.TempCompilationUnit("package b.b;\n\n" + "public class Y{}",
        "Y.java", "b.b")
    });

    Project after = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit("package b;\n\n" + "public class X{}",
        "X.java", "b"),
        new Utils.TempCompilationUnit("package b.b;\n\n" + "public class Y{}",
        "Y.java", "b.b")
    });

    RenamePackage renamer = new RenamePackage(new NullContext(before),
        before.getPackageForName("a"));
    renamer.setRenamePrefix(true);
    renamer.setPrefix("a");
    renamer.setNewName("b");

    RwRefactoringTestUtils.assertRefactoring(renamer, before, after);
  }

  public void testCannotRenameToMatchExistingPackage() throws Exception {
    Project before = Utils.createTestRbProjectFromString(new Utils.
        TempCompilationUnit[] {
        new Utils.TempCompilationUnit("package a;\n\n" + "public class X{}",
        "X.java", "a"),
        new Utils.TempCompilationUnit("package b;\n\n" + "public class Y{}",
        "Y.java", "b")
    });

    RenamePackage renamer = new RenamePackage(new NullContext(before),
        before.getPackageForName("a"));
    renamer.setNewName("b");
    RefactoringStatus status = renamer.checkUserInput();

    assertTrue(status.isErrorOrFatal());
  }

  /*public void testProgressBarOnSourceRelocation() throws Exception {
    Project before = Utils.createTestRbProjectFromString( new Utils.TempCompilationUnit[] {
   new Utils.TempCompilationUnit("package a;\n\n" + "public class X{}", "X.java", "a"),
   new Utils.TempCompilationUnit("package b;\n\n" + "public class Y{}", "Y.java", "b")
    } );

    CFlowContext.set(ProgressListener.class.getName(), new ProgressListener() {

    } );

    SourceRelocator r = new SourceRelocator(before);
    r.re
     }*/

  /**
   * Tests renaming package when all packages are in the same directory.
   */
  public void testNoDirs() throws Exception {
    cat.info("Testing renaming package when all packages are in the same"
        + " directory");
    testRenamePackage("no_dirs", "a", "b", null, true);
    cat.info("SUCCESS");
  }

  public void testNoDirs1() throws Exception {
    testRenamePackage("no_dirs1", "x", "y", null, true);
  }

  /**
   * Bug 1566: Renaming a package removes asterisks from the end of import statements.
   */
  public void testBug1566() throws Exception {
    testRenamePackage("asterisk_preserving_rename", "a", "b");
  }

  public void testBug1566_WhitespaceAroundAsterisk() throws Exception {
    testRenamePackage(
        "asterisk_preserving_rename_with_whitespace_around_asterisk", "a", "b");
  }

  private void testRenamePackage(String projectName, String packageName,
      String newPackageName) throws Exception {
    testRenamePackage(projectName, packageName, newPackageName, null, false);
  }

  private void testRenamePackagePrefix(String projectName, String packageName,
      String newPackageName) throws Exception {
    testRenamePackage(projectName, packageName, newPackageName, packageName, false);
  }

  public void testPathsInNonJava() throws Exception {
    testRenamePackage("paths_in_non_java", "com.test.main", "com.test.simple");
  }
  
  public void testPathsInNonJavaPrefixes() throws Exception {
    testRenamePackage("paths_in_non_java_prefixes", 
        "com.test.externaldataloader", "com.test.externaldata", 
        "com.test.externaldataloader", false);
  }
  /**
   * Tests renaming package.
   *
   * @param projectName name of the project to test. <code>/before</code> is
   *        appended to the name to get name of "before" project.
   *        <code>/after</code> is appended to get name of "after" project.
   * @param packageName name of package to rename.
   * @param newPackageName new name of the package.
   */
  private void testRenamePackage(String projectName,
      String packageName,
      String newPackageName,
      String prefix,
      boolean expectErrors) throws Exception {

    final String beforeProjectName = projectName + "/before";
    cat.debug("\"Before\" project = " + beforeProjectName);
    final Project project = loadMutableProject(beforeProjectName);
    final BinPackage pkg = project.getPackageForName(packageName);
    assertTrue("Package \"" + packageName + "\" not found", pkg != null);

    cat.debug("Renaming package \"" + packageName + "\" to \""
        + newPackageName + "\"");

    final RenamePackage renamer
        = new RenamePackage(new NullContext(project), pkg);
    if (prefix != null) {
      renamer.setRenamePrefix(true);
      renamer.setPrefix(prefix);
    }
    renamer.setNewName(newPackageName);

    RefactoringStatus status =
      renamer.apply();

    assertEquals(status.getAllMessages(), !expectErrors, status.isOk());

    final String afterProjectName = projectName + "/after";
    cat.debug("\"After\" project = " + afterProjectName);
    RwRefactoringTestUtils.assertSameSources(
        "Renamed package \"" + packageName + "\" to \""
        + newPackageName + "\"",
        getProject(afterProjectName),
        project);
  }

  public void testErrorMessagesDoNotContainHashCode() throws Exception {
    Project p = Utils.createTestRbProjectFromString("class X{}");
    BinPackage nonExisting = p.createPackageForName("a", false);

    RenamePackage r = new RenamePackage(new NullContext(p), nonExisting);
    r.setRenamePrefix(true);
    r.setPrefix("a");
    r.setNewName("b");
    RefactoringStatus status = r.checkUserInput();

    assertFalse(status.isOk());
    assertEquals("Package is not from the source path\n  a\n",
        status.getAllMessages());
  }

  public void testFilenamesDifferInCaseOnly_windows() throws Exception {
    Project project = Utils.createTestRbProjectFromString(
        "package asd; class X{}", "X.java", "asd");
    BinPackage p = project.getPackageForName("asd");

    RenamePackage r = new RenamePackage(new NullContext(project), p);
    r.setIgnoreFilenameCase(true);
    r.setNewName("Asd");
    assertTrue(r.checkUserInput().isErrorOrFatal());
  }

  public void testFilenamesDifferInCaseOnly_nonWindows() throws Exception {
    Project project = Utils.createTestRbProjectFromString(
        "package asd; class X{}", "X.java", "asd");
    BinPackage p = project.getPackageForName("asd");

    RenamePackage r = new RenamePackage(new NullContext(project), p);
    r.setIgnoreFilenameCase(false);
    r.setNewName("Asd");
    assertTrue(r.checkUserInput().isOk());
  }

  public void testFilenamesDifferInCaseOnly_prefix_windows() throws Exception {
    Project project = Utils.createTestRbProjectFromString(
        "package asd; class X{}", "X.java", "asd");
    BinPackage p = project.getPackageForName("asd");

    RenamePackage r = new RenamePackage(new NullContext(project), p);
    r.setIgnoreFilenameCase(true);
    r.setRenamePrefix(true);
    r.setPrefix("asd");

    r.setNewName("Asd");
    assertTrue(r.checkUserInput().isErrorOrFatal());
  }

  public void testFilenamesDifferInCaseOnly_prefix_nonWindows() throws
      Exception {
    Project project = Utils.createTestRbProjectFromString(
        "package asd; class X{}", "X.java", "asd");
    BinPackage p = project.getPackageForName("asd");

    RenamePackage r = new RenamePackage(new NullContext(project), p);
    r.setIgnoreFilenameCase(false);
    r.setRenamePrefix(true);
    r.setPrefix("asd");

    r.setNewName("Asd");
    assertTrue(r.checkUserInput().isOk());
  }

  public void testRenamePackageInNonJavaFiles() throws Exception {
    Project project = Utils.createTestRbProjectFromString(
        "<package>class.test.package;</package>",
        "unitA.xml",
        null /* folder */);

    // creating packages:
    BinPackage oldP = project.createPackageForName("class.test.package");
    BinPackage newP = project.createPackageForName("class.test.newPackage");

    // do rename in non java files
    RenamePackage renamer = new RenamePackage(new NullContext(project),
        oldP);
    renamer.setNewName(newP.getQualifiedDisplayName());
        renamer.apply();

    // looking for renamed package
    ManagingNonJavaIndexer supervisor = new ManagingNonJavaIndexer(
        Project.getDefaultOptions().getNonJavaFilesPatterns());
    new PackageQualifiedNameIndexer(supervisor, newP);
    supervisor.visit(project);
    List results = supervisor.getOccurrences();

    // testing
    assertEquals(
        "class.test.package must rename to class.test.newPackage. " +
        "Must be only one occurence of new package.",
        1, results.size());
  }

  public void testMultipleLines() throws Exception {
    cat.info("Testing renaming package when some usages take several lines");
    testRenamePackage("multiple_lines", "a.b", "a.bx");
    cat.info("SUCCESS");
  }

  // Util methods



  /**
   * Loads Rename Package project that is mutable.
   *
   * @param name name of the project.
   *
   * @return project. Never returns <code>null</code>.
   */
  private Project loadMutableProject(String name) throws Exception {
    return loadMutableProject(getProject(name));
  }

  private Project loadMutableProject(Project p) throws Exception {
    final Project project =
        RwRefactoringTestUtils.createMutableProject(p);
    project.getProjectLoader().build();
    return project;
  }

  /**
   * Gets Rename Package project.
   *
   * @param name name of the project.
   *
   * @return project. Never returns <code>null</code>.
   */
  private Project getProject(String name) throws Exception {
    return Utils.createTestRbProject("rename_package/" + name);
  }
}
