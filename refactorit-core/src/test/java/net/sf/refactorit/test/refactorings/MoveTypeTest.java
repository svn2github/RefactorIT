/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 *
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.CFlowContext;
import net.sf.refactorit.common.util.ProgressListener;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.movetype.MoveType;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.test.refactorings.rename.RenameTestUtil;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.NullDialogManager;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.utils.RefactorItConstants;
import net.sf.refactorit.vfs.Source;

import org.apache.log4j.Category;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test driver for {@link net.sf.refactorit.refactorings.movetype.MoveType}.
 */
public final class MoveTypeTest extends RefactoringTestCase {
  /** Logger instance. */
  private static final Category cat
      = Category.getInstance(MoveTypeTest.class.getName());

  /**
   * Stores last message intended for the user.
   */
  static class KeyStoringDialogManager extends NullDialogManager {
    protected String lastMessageKey = null;

    public KeyStoringDialogManager() {
      super();
    }

    public void showError(IdeWindowContext context, String key) {
      lastMessageKey = key;
    }

    public void showWarning(IdeWindowContext context, String key) {
      lastMessageKey = key;
    }

    public void showWarning(
        IdeWindowContext context, String key, String message
    ) {
      lastMessageKey = key;
    }

    public void showInformation(IdeWindowContext context, String key) {
      lastMessageKey = key;
    }

    public void showInformation(
        IdeWindowContext context, String key, String message
    ) {
      lastMessageKey = key;
    }

    public int showYesNoCancelQuestion(IdeWindowContext context, String key) {
      lastMessageKey = key;
      return super.showYesNoCancelQuestion(context, key);
    }

    public int showYesNoCancelQuestion(
        IdeWindowContext context,
        String key, String message,
        int defaultSelectedButton
    ) {
      lastMessageKey = key;
      return super.showYesNoCancelQuestion(
          context, key, message, defaultSelectedButton);
    }

    public int showYesNoQuestion(IdeWindowContext context, String key) {
      lastMessageKey = key;
      return super.showYesNoQuestion(context, key);
    }

    public int showYesNoQuestion(
        IdeWindowContext context,
        String key, String message,
        int defaultSelectedButton
    ) {
      lastMessageKey = key;
      return super.showYesNoQuestion(
          context, key, message, defaultSelectedButton);
    }

    /**
     * Gets key of last message intended for the user.
     *
     * @return key or <code>null</code> if no message available.
     */
    String getLastMessageKey() {
      return lastMessageKey;
    }
  }

  public MoveTypeTest(String name) {
    super(name);
  }

  public static Test suite() {
    final TestSuite suite = new TestSuite(MoveTypeTest.class);
    suite.setName("MoveType tests");
    return suite;
  }

  protected DialogManager originalDialogManager;

  protected MoveType.FilesToMoveWithJavaCompilationUnits originallyMovedTogether;

  protected void setUp() throws Exception {
    originalDialogManager = DialogManager.getInstance();
    originallyMovedTogether = MoveType.getFilesToMoveWithJavaCompilationUnits();

    FormatSettings.applyStyle(new FormatSettings.AqrisStyle());
  }

  protected void tearDown() throws Exception {
    DialogManager.setInstance(originalDialogManager);
    MoveType.setFilesToMoveWithJavaCompilationUnits(originallyMovedTogether);
  }

  public String getTemplate() {
    return "MoveType/<stripped_test_name>/<in_out>";
  }

  private Project loadMutableProject(final String projectName) throws Exception {
    Project project = Utils.createTestRbProjectFromXml(projectName);
    project = RwRefactoringTestUtils.createMutableProject(project);
    project.getProjectLoader().build();

    assertTrue("New mutable project (" + project + ") contains types: "
        + project.getDefinedTypes().size(),
        project.getDefinedTypes().size() > 0);

    return project;
  }

  public void testProgressBar() throws Exception {
    final StringBuffer progressLog = new StringBuffer();
    final RefactoringStatus status = new RefactoringStatus();
    final Project p = Utils.createTestRbProjectFromString(
        "package a;\n public class C{}", "C.java", "a");

    // We need EDT for NB because CFlowContext works on a per-thread basis so
    // we need to set the progress listener in the same thread as the
    // move operation works in. OTOH, under karkass we need no Swing.
    /*SwingUtil.invokeInEdtUnderNetBeans(new Runnable() {
     public void run() {*/
    MoveType mover = new MoveType(new NullContext(p), p
        .getTypeRefForName("a.C").getBinCIType());
    mover.setTargetPackage(p.createPackageForName("b"));

    CFlowContext.add(ProgressListener.class.getName(), new ProgressListener() {

      public void progressHappened(float percentage) {
        progressLog.append(percentage);
        progressLog.append(" ");
      }

      public void showMessage(String message) {}
    });

    status.merge(mover.apply());
    //} } );

    assertTrue(status.isOk());
    assertEquals(//"55.0 80.0 33.333332 33.333332 66.666664 100.0 "
        "55.0 80.0 25.0 25.0 50.0 75.0 100.0 ",
        progressLog.toString());
  }

  // Bug 2036
  public void testNotCreatingFileToExtractToInCaseOfErrors() throws Exception {
    Project before = Utils.createTestRbProjectFromString("public class X {\n"
        + "  public int field;\n\n" +

        "  public class Inner{ public void m() {System.out.println(field);} }\n"
        + "}\n", "X.java", null);

    Project sameAsBefore = RwRefactoringTestUtils.createMutableProject(before);

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("X$Inner").getBinType());
    mover.setTargetPackage(before.createPackageForName("a"));

    RefactoringStatus status =
      mover.apply();
    assertTrue(status.isErrorOrFatal());
    RwRefactoringTestUtils.assertSameSources("", sameAsBefore, before);
  }

  public void testTypeAlreadyInDestinationFolder() throws Exception {
    Project before = Utils.createTestRbProjectFromString("package a;\n\n"
        + "public class X {}", "X.java", "b");

    Project after = Utils.createTestRbProjectFromString("package b;\n\n"
        + "public class X {}", "X.java", "b");

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.X").getBinType());
    mover.setTargetPackage(before.createPackageForName("b"));

    RwRefactoringTestUtils.assertRefactoring(mover, before, after);
  }

  public void testMovingTwoClassesInSameFile() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "public class X {\n" + "  Other instance = new Other();\n" + "}\n"
                + "class Other {}\n", "X.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package a;\n" + "public class X {\n"
                + "  Other instance = new Other();\n" + "}\n"
                + "class Other {}\n", "X.java", "a") });

    MoveType mover = new MoveType(new NullContext(before), new Object[] {
        before.getTypeRefForName("X").getBinType(),
        before.getTypeRefForName("Other").getBinType() });
    mover.setTargetPackage(before.createPackageForName("a"));

    RwRefactoringTestUtils.assertRefactoring(mover, before, after);
  }

  public void testConflictResolve() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "public class X {\n" + "  Other instance = new Other();\n" + "}\n"
                + "class Other {}\n", "X.java", null) });
    BinType x = before.getTypeRefForName("X").getBinType();
    BinType other = before.getTypeRefForName("Other").getBinType();

    MoveType mover = new MoveType(new NullContext(before), null);
    mover.setTargetPackage(before.createPackageForName("a"));

    // MoveType can't move class X without the Other...
    mover.setTypes(Arrays.asList(new Object[] { x }));
    assertFalse(mover.checkUserInput().isOk());

    List otherTypesToMove = mover.resolveConflicts();
    assertEquals(1, otherTypesToMove.size());
    assertEquals(other, otherTypesToMove.get(0));

    // MoveType *can* move X and Other together
    assertTrue(mover.checkUserInput().isOk());
  }

  public void testConflictResolveForBinMember() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X {\n"
                + "  public void m() { new Other().m(); }\n" + // User Other.m() which can't be public
                "}", "X.java", null),
            new Utils.TempCompilationUnit("public class Other { void m(){} }",
                "Other.java", null),
            new Utils.TempCompilationUnit(
                "public class OtherSubclass extends Other { void m(){} }", // Prevents Other.m() from getting public
                "OtherSubclass.java", null) });
    BinType x = before.getTypeRefForName("X").getBinType();
    BinType other = before.getTypeRefForName("Other").getBinType();
    BinType otherSubclass = before.getTypeRefForName("OtherSubclass")
        .getBinType();

    MoveType mover = new MoveType(new NullContext(before), null, false); // does not change member access
    mover.setTargetPackage(before.createPackageForName("a"));

    // MoveType can't move class X without the Other, which can't be moved without its subclass
    mover.setTypes(Arrays.asList(new Object[] { x }));
    assertFalse(mover.checkUserInput().isOk());
    List otherTypesToMove = mover.resolveConflicts();
    assertEquals(2, otherTypesToMove.size());
    assertEquals(other, otherTypesToMove.get(0));
    assertEquals(otherSubclass, otherTypesToMove.get(1));

    // MoveType *can* move X and Other together
    assertTrue(mover.checkUserInput().isOk());
  }

  public void testRenamePackageInJspImport() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "<%@page contentType=\"text/html\" import=\"a.A\"%>\n"
                    + "<%A instance;%>", "a.jsp", null),
            new Utils.TempCompilationUnit("package a; public class A{}",
                "A.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "<%@page contentType=\"text/html\" import=\"b.A\"%>\n"
                    + "<%A instance;%>", "a.jsp", null),
            new Utils.TempCompilationUnit("package b; public class A{}",
                "A.java", "b") });

    MoveType mover = new MoveType(new NullContext(before),
        before.getTypeRefForName("a.A").getBinCIType());
    mover.setTargetPackage(before.createPackageForName("b"));
    RefactoringStatus status =
      mover.apply();

    assertTrue(status.getAllMessages(), status.isOk());
    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testInsertJspImport() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "<%@page contentType=\"text/html\" import=\"a.*\"%>\n"
                    + "<%A1 instance; A2 instance;%>", "a.jsp", null),
            new Utils.TempCompilationUnit("package a; public class A1{}",
                "A1.java", "a"),
            new Utils.TempCompilationUnit("package a; public class A2{}",
                "A2.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "<%@page contentType=\"text/html\" import=\"a.*, b.A1\"%>\n"
                    + "<%A1 instance; A2 instance;%>", "a.jsp", null),
            new Utils.TempCompilationUnit("package b; public class A1{}",
                "A1.java", "b"),
            new Utils.TempCompilationUnit("package a; public class A2{}",
                "A2.java", "a") });

    MoveType mover = new MoveType(new NullContext(before),
        before.getTypeRefForName("a.A1").getBinCIType());
    mover.setTargetPackage(before.createPackageForName("b"));
    RefactoringStatus status =
      mover.apply();

    assertTrue(status.getAllMessages(), status.isOk());
    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testInsertJspImport_removeOldWholePackageImport()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "<%@page contentType=\"text/html\" import=\"a.*\"%>\n"
                    + "<%A1 instance;%>", "a.jsp", null),
            new Utils.TempCompilationUnit("package a; public class A1{}",
                "A1.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "<%@page contentType=\"text/html\" import=\"a.*, b.A1\"%>\n"
                    + "<%A1 instance;%>", "a.jsp", null),
            new Utils.TempCompilationUnit("package b; public class A1{}",
                "A1.java", "b") });

    MoveType mover = new MoveType(new NullContext(before),
        before.getTypeRefForName("a.A1").getBinCIType());
    mover.setTargetPackage(before.createPackageForName("b"));
    RefactoringStatus status =
      mover.apply();

    assertTrue(status.isInfoOrWarning());
    assertEquals("Remove manually: import of 'a.*' at a.jsp:1", status
        .getFirstMessage());
    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testInsertJspImport_fromDefaultPackage() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "<%@page contentType=\"text/html\" import=\"javax.swing.*\"%>\n"
                    + "<%A instance;%>", "a.jsp", null),
            new Utils.TempCompilationUnit("public class A{}", "A.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "<%@page contentType=\"text/html\" import=\"javax.swing.*, b.A\"%>\n"
                    + "<%A instance;%>", "a.jsp", null),
            new Utils.TempCompilationUnit("package b;\n" + "public class A{}",
                "A.java", "b") });

    MoveType mover = new MoveType(new NullContext(before),
        before.getTypeRefForName("A").getBinCIType());
    mover.setTargetPackage(before.createPackageForName("b"));
    RefactoringStatus status =
      mover.apply();

    assertTrue(status.getAllMessages(), status.isOk());
    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testMovingLocalTypesNotAllowed() throws Exception {
    Project p = Utils.createTestRbProjectFromString("public class X {\n"
        + "  public void m() {\n" + "    public class Local{}\n" + "  }\n"
        + "}\n", "X.java", null);

    BinCIType x = p.getTypeRefForName("X").getBinCIType();
    BinCIType localType = (BinCIType) x.getDeclaredMethods()[0]
        .getDeclaredTypes().get(0);

    MoveType mover = new MoveType(new NullContext(p), localType);
    mover.setTargetPackage(p.getPackageForName(""));

    RefactoringStatus status =
      mover.apply();
    assertFalse(status.isOk());
    assertEquals("Moving of local types not supported: Local", status
        .getAllMessages());
  }

  public void testInsertJspImport_fromDefaultPackage_noImportsYet()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "<%@page contentType=\"text/html\"%>\n" + "<%A instance;%>",
                "a.jsp", null),
            new Utils.TempCompilationUnit("public class A{}\n", "A.java", null) });

    Project after = Utils.createTestRbProjectFromString(
        new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "<%@page contentType=\"text/html\"%>\n" + "<%A instance;%>",
                "a.jsp", null),
            new Utils.TempCompilationUnit(
                "package b;\n" + "public class A{}\n", "A.java", "b") }, true,
        true);

    MoveType mover = new MoveType(new NullContext(before),
        before.getTypeRefForName("A").getBinCIType());
    mover.setTargetPackage(before.createPackageForName("b"));
    RefactoringStatus status =
      mover.apply();

    assertTrue(status.getAllMessages(), status.isInfoOrWarning());
    assertEquals("Add manually to a.jsp: import statement of 'b.A'", status
        .getFirstMessage());
    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testInsertJspImport_intoDefaultPackage() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "<%@page contentType=\"text/html\" import=\"javax.swing.*, b.A\"%>\n"
                    + "<%A instance;%>", "a.jsp", null),
            new Utils.TempCompilationUnit("package b;\n" + "public class A{}",
                "A.java", "b") });

    Project after = Utils
        .createTestRbProjectFromString(
            new Utils.TempCompilationUnit[] {
                new Utils.TempCompilationUnit(
                    "<%@page contentType=\"text/html\" import=\"javax.swing.*, b.A\"%>\n"
                        + "<%A instance;%>", "a.jsp", null),
                new Utils.TempCompilationUnit("public class A{}", "A.java",
                    null) }, true, true);

    MoveType mover = new MoveType(new NullContext(before),
        before.getTypeRefForName("b.A").getBinCIType());
    mover.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      mover.apply();

    assertTrue(status.isInfoOrWarning());
    assertEquals("Remove manually: import of 'b.A' at a.jsp:1", status
        .getFirstMessage());
    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testBug1954() throws Exception {
    cat.info("Testing bug #1954.");

    Project project = getMutableProject();

    BinPackage newPackage = project.createPackageForName("p2", true);

    BinCIType testType = project
        .getTypeRefForName("p1.A$Inner").getBinCIType();

    assertTrue("type with name p1.A$Inner was not found", testType != null);

    MoveType mover = new MoveType(new NullContext(project), testType,
        true);
    mover.setTargetPackage(newPackage);

    RefactoringStatus status =
      mover.apply();
    assertTrue("Moved A$Inner to new package: " + status.getAllMessages(),
        status.isOk());

    RwRefactoringTestUtils
        .assertSameSources("after moving of classes to new package",
            getExpectedProject(), project);

    cat.info("SUCCESS");
  }

  /**
   * Tests that new imports has correct location when type is moved.
   */
  public void testImportsPlacing() throws Exception {
    cat
        .info("Testing that new imports has correct location when type is moved.");

    final String projectName = "MoveType_imports_placing_initial";
    Project project = loadMutableProject(projectName);
    assertNotNull("Created mutable project", project);

    final BinTypeRef testTypeRef = project
        .getTypeRefForName("a.Test");
    assertNotNull("Found type", testTypeRef);
    BinCIType testType = testTypeRef.getBinCIType();

    BinPackage newPackage = project.createPackageForName("b", true);

    MoveType mover = new MoveType(new NullContext(project), testType);
    mover.setTargetPackage(newPackage);
    RefactoringStatus status =
      mover.apply();

    assertTrue("Moved Test a->b: " + status.getAllMessages(), status.isOk());

    Project expected = Utils.createTestRbProject(Utils.getTestProjects()
        .getProject("MoveType_imports_placing_expected"));

    RwRefactoringTestUtils.assertSameSources("after moving of Test a->b",
        expected, project);

    cat.info("SUCCESS");
  }

  /**
   * Tests export of a type from shared source file to another package.
   */
  public void testExportToAnotherPackage() throws Exception {
    cat
        .info("Testing export of a type from shared source file to another package.");

    final String projectName = "MoveType_extract_initial";
    Project project = loadMutableProject(projectName);

    BinCIType testType = project.getTypeRefForName("a.X")
        .getBinCIType();

    BinPackage newPackage = project.createPackageForName("b", true);

    MoveType mover = new MoveType(new NullContext(project), testType);
    mover.setTargetPackage(newPackage);

    boolean result = (mover.apply())
        .isOk();

    assertTrue("Moved X a->b", result);

    Project expected = Utils.createTestRbProject(Utils.getTestProjects()
        .getProject("MoveType_extract_expected"));

    RwRefactoringTestUtils.assertSameSources("after moving of X a->b",
        expected, project);

    cat.info("SUCCESS");
  }

  /**
   * Tests export of a type from shared source file to the same package.
   */
  public void testExportToSamePackage() throws Exception {
    cat
        .info("Testing export of a type from shared source file to the same package.");

    final String projectName = "MoveType_extract_same_initial";
    Project project = loadMutableProject(projectName);

    BinCIType testType = project.getTypeRefForName("a.Z")
        .getBinCIType();

    BinPackage newPackage = testType.getPackage();

    MoveType mover = new MoveType(new NullContext(project), testType);
    mover.setTargetPackage(newPackage);

    RefactoringStatus result =
      mover.apply();

    assertTrue("Extracted Z to the same package. (Messages: "
        + result.getAllMessages() + ")", result.isOk());

    Project expected = Utils.createTestRbProject(Utils.getTestProjects()
        .getProject("MoveType_extract_same_expected"));

    RwRefactoringTestUtils.assertSameSources(
        "after extracting of Z to the same package", expected, project);

    cat.info("SUCCESS");
  }

  /**
   * Tests bug1161: move to default package.
   */
  public void testBug1161() throws Exception {
    cat.info("Testing bug #1161 - move to default package.");

    final String projectName = "bug1161_initial";
    Project project = loadMutableProject(projectName);

    BinCIType testType = project.getTypeRefForName("a.Test")
        .getBinCIType();

    BinPackage newPackage = project.createPackageForName("", true);

    MoveType mover = new MoveType(new NullContext(project), testType);
    mover.setTargetPackage(newPackage);
    RefactoringStatus status = mover.apply();
    boolean result = status.isOk();

    assertTrue("Moved Test to default package", result);

    Project expected = Utils.createTestRbProject(Utils.getTestProjects()
        .getProject("bug1161_expected"));

    RwRefactoringTestUtils.assertSameSources(
        "after moving of Test to default package", expected, project);

    cat.info("SUCCESS");
  }

  /**
   * Tests bug1415: move from default package and importing exception in throws.
   */
  public void testBug1415() throws Exception {
    cat
        .info("Testing bug #1415 - move from default package and importing exception.");

    Project project = getMutableProject();
    BinPackage newPackage = project.createPackageForName("zzz", true);

    BinCIType testType = project.getTypeRefForName("Class1")
        .getBinCIType();
    MoveType mover = new MoveType(new NullContext(project), testType);
    mover.setTargetPackage(newPackage);

    RefactoringStatus status =
      mover.apply();
    assertTrue("Moved Class1 to new package: " + status.getAllMessages(),
        status.isOk());

    testType = project.getTypeRefForName("Class2")
        .getBinCIType();
    mover = new MoveType(new NullContext(project), testType);
    mover.setTargetPackage(newPackage);

    status = mover.apply();
    assertTrue("Moved Class2 to new package: " + status.getAllMessages(),
        status.isOk());

    RwRefactoringTestUtils
        .assertSameSources("after moving of classes to new package",
            getExpectedProject(), project);

    cat.info("SUCCESS");
  }

  public void testbug1496() throws Exception {
    if (!RefactorItConstants.runNotImplementedTests) {
      return;
    }

    cat.info("Testing bug #1496");
    Project project = getMutableProject();
    BinPackage newPackage = project.createPackageForName("b", true);
    final BinTypeRef testTypeRef = project
        .getTypeRefForName("a.Subclass");
    assertNotNull("Found class a.Subclass", testTypeRef);

    BinCIType testType = testTypeRef.getBinCIType();
    MoveType mover = new MoveType(new NullContext(project), testType);
    mover.setTargetPackage(newPackage);
    TransformationManager manager = new TransformationManager(mover);
    manager.add(mover.checkAndExecute());
    RefactoringStatus status = manager.performTransformations();
    assertTrue("Moved class a.Subclass to package b: "
        + status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources(
        "after moving of class a.Subclass to package b", getExpectedProject(),
        project);

    cat.info("SUCCESS");
  }

  /**
   * Tests bug1475: erasing redundant imports.
   */
  public void testBug1475() throws Exception {
    cat.info("Testing bug #1475 - erasing redundant imports.");

    Project project = getMutableProject();

    BinPackage newPackage = project.getPackageForName("b");
    final BinTypeRef testTypeRef = project
        .getTypeRefForName("a.A");
    assertNotNull("Found class a.A", testTypeRef);
    BinCIType testType = testTypeRef.getBinCIType();

    MoveType mover = new MoveType(new NullContext(project), testType);
    mover.setTargetPackage(newPackage);
    RefactoringStatus status =
      mover.apply();
    boolean result = status.isOk();
    assertTrue("Moved class a.A to package b", result);

    RwRefactoringTestUtils
        .assertSameSources("after moving of class a.A to package b",
            getExpectedProject(), project);

    cat.info("SUCCESS");
  }

  /** 1518 -- removing "static" keyword when extracting an inner class */
  public void testBug1518() throws Exception {
    cat
        .info("Testing bug #1518 -- removing 'static' keyword when extracting an inner class");

    Project project = getMutableProject();

    BinPackage newPackage = project.getPackageForName("b");
    final BinTypeRef testTypeRef = project
        .getTypeRefForName("a.A$I");
    assertNotNull("Found class a.A$I", testTypeRef);
    BinCIType testType = testTypeRef.getBinCIType();

    MoveType mover = new MoveType(new NullContext(project), testType);
    mover.setTargetPackage(newPackage);
    mover.setTargetSource(project.getPaths().getSourcePath().getRootSources()[0]);
    RefactoringStatus status =
      mover.apply();
    boolean result = status.isOk();
    assertTrue("Moved class a.A$I to package b", result);

    RwRefactoringTestUtils.assertSameSources(
        "after moving of class a.A$I to package b", getExpectedProject(),
        project);

    cat.info("SUCCESS");
  }

  public void testBug1519() throws Exception {
    cat
        .info("Testing bug #1519 -- import statements not refactored when moving internal interface");

    Project project = getMutableProject();

    final BinPackage newPackage = project.getPackageForName("a");
    final BinTypeRef testTypeRef = project
        .getTypeRefForName("a.X$Inner");
    assertNotNull("Found class a.X$Inner", testTypeRef);
    BinCIType testType = testTypeRef.getBinCIType();

    MoveType mover = new MoveType(new NullContext(project), testType);
    mover.setTargetPackage(newPackage);
    mover.setTargetSource(project.getPaths().getSourcePath().getRootSources()[0]);
    RefactoringStatus status =
      mover.apply();
    boolean result = status.isOk();
    assertTrue("Moved class a.X$Inner outside from it's outer class", result);

    RwRefactoringTestUtils.assertSameSources(
        "After moving class a.X$Inner outside from it's outer class",
        getExpectedProject(), project);

    cat.info("SUCCESS");
  }

  public void testBug1753() throws Exception {
    cat
        .info("Testing bug #1753 -- SourceEditor fails on constructor invocation");

    Project project = getMutableProject();

    final BinPackage newPackage = project.createPackageForName("r", true);
    final BinTypeRef testTypeRef = project
        .getTypeRefForName("p.Test");
    assertNotNull("Found class p.Test", testTypeRef);
    BinCIType testType = testTypeRef.getBinCIType();

    MoveType mover = new MoveType(new NullContext(project), testType);
    mover.setTargetPackage(newPackage);
    mover.setTargetSource(project.getPaths().getSourcePath().getRootSources()[0]);

    RefactoringStatus status =
      mover.apply();
    assertTrue("Moved class p.Test to package r: " + status.getAllMessages(),
        status.isOk());

    RwRefactoringTestUtils
        .assertSameSources("After moving class p.Test to package r",
            getExpectedProject(), project);

    cat.info("SUCCESS");
  }

  /**
   * Sourcepath specifies a/b, moving from a.b.c to a.b.d,
   * should create a/b/d, since file is still within sourcepath.
   *
   * @throws Exception
   */
  public void testRelocate1() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    Project project = loadMutableProject("MoveType_" + getStrippedTestName()
        + "_in");

    final BinTypeRef testTypeRef = project
        .getTypeRefForName("a.b.c.Test");
    assertNotNull("Found class a.b.c.Test", testTypeRef);
    BinCIType testType = testTypeRef.getBinCIType();

    MoveType mover = new MoveType(new NullContext(project), testType);
    mover.setTargetPackage(project.createPackageForName("a.b.d", true));
    Source[] dirs = project.getPaths().getSourcePath().getRootSources();
    for (int i = 0; i < dirs.length; i++) {
      if (dirs[i].getAbsolutePath().endsWith("a" + File.separatorChar + "b")) {
        mover.setTargetSource(dirs[i]);
        break;
      }
    }
    assertNotNull("Found target source", mover.getTargetSource());

    RefactoringStatus status =
      mover.apply();
    assertTrue("Moved class a.b.c.Test to a.b.d: " + status.getAllMessages(),
        status.isOk());

    RwRefactoringTestUtils.assertSameSources(
        "After moving class a.b.c.Test to a.b.d", Utils
            .createTestRbProjectFromXml("MoveType_" + getStrippedTestName()
                + "_out"), project);

    cat.info("SUCCESS");
  }

  /**
   * Sourcepath specifies a/b/c, moving from a.b.c to a.b.d,
   * should leave in a/b/c, or otherwise file will get out of sourcepath scope.
   *
   * @throws Exception
   */
  public void testRelocate2() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    Project project = loadMutableProject("MoveType_" + getStrippedTestName()
        + "_in");

    final BinTypeRef testTypeRef = project
        .getTypeRefForName("a.b.c.Test");
    assertNotNull("Found class a.b.c.Test", testTypeRef);
    BinCIType testType = testTypeRef.getBinCIType();

    MoveType mover = new MoveType(new NullContext(project), testType);
    mover.setTargetPackage(project.createPackageForName("a.b.d", true));
    Source[] dirs = project.getPaths().getSourcePath().getRootSources();
    for (int i = 0; i < dirs.length; i++) {
      if (dirs[i].getAbsolutePath().endsWith(
          "a" + File.separatorChar + "b" + File.separatorChar + "c")) {
        mover.setTargetSource(dirs[i]);
        break;
      }
    }
    assertNotNull("Found target source", mover.getTargetSource());

    RefactoringStatus status =
      mover.apply();
    assertTrue("Not moved class a.b.c.Test to a.b.d: "
        + status.getAllMessages(), !status.isErrorOrFatal()
        && status.getAllMessages().indexOf("Unable to relocate") >= 0);

    RwRefactoringTestUtils.assertSameSources(
        "After moving class a.b.c.Test to a.b.d", Utils
            .createTestRbProjectFromXml("MoveType_" + getStrippedTestName()
                + "_out"), project);

    cat.info("SUCCESS");
  }

  /**
   * Sourcepath specifies a/b/c & /a/b/d, moving from a.b.c to a.b.d,
   * setting target source as a/b/d, so should move to a/b/d.
   *
   * @throws Exception
   */
  public void testRelocate3() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    Project project = loadMutableProject("MoveType_" + getStrippedTestName()
        + "_in");

    final BinTypeRef testTypeRef = project
        .getTypeRefForName("a.b.c.Test");
    assertNotNull("Found class a.b.c.Test", testTypeRef);
    BinCIType testType = testTypeRef.getBinCIType();

    MoveType mover = new MoveType(new NullContext(project), testType);
    mover.setTargetPackage(project.createPackageForName("a.b.d", true));
    Source[] dirs = project.getPaths().getSourcePath().getRootSources();
    for (int i = 0; i < dirs.length; i++) {
      if (dirs[i].getAbsolutePath().endsWith(
          "a" + File.separatorChar + "b" + File.separatorChar + "d")) {
        mover.setTargetSource(dirs[i]);
        break;
      }
    }
    assertNotNull("Found target source", mover.getTargetSource());

    RefactoringStatus status =
      mover.apply();
    assertTrue("Moved class a.b.c.Test to a.b.d: " + status.getAllMessages(),
        status.isOk());

    RwRefactoringTestUtils.assertSameSources(
        "After moving class a.b.c.Test to a.b.d", Utils
            .createTestRbProjectFromXml("MoveType_" + getStrippedTestName()
                + "_out"), project);

    cat.info("SUCCESS");
  }

  /**
   * Sourcepath specifies a/b/c & /a/b/d, moving from a.b.c to a.b.d,
   * setting target source as a/b/c, so should leave to a/b/c.
   *
   * @throws Exception
   */
  public void testRelocate4() throws Exception {
    cat.info("Testing " + getStrippedTestName());

    Project project = loadMutableProject("MoveType_" + getStrippedTestName()
        + "_in");

    final BinTypeRef testTypeRef = project
        .getTypeRefForName("a.b.c.Test");
    assertNotNull("Found class a.b.c.Test", testTypeRef);
    BinCIType testType = testTypeRef.getBinCIType();

    MoveType mover = new MoveType(new NullContext(project), testType);
    mover.setTargetPackage(project.createPackageForName("a.b.d", true));
    Source[] dirs = project.getPaths().getSourcePath().getRootSources();
    for (int i = 0; i < dirs.length; i++) {
      if (dirs[i].getAbsolutePath().endsWith(
          "a" + File.separatorChar + "b" + File.separatorChar + "c")) {
        mover.setTargetSource(dirs[i]);
        break;
      }
    }
    assertNotNull("Found target source", mover.getTargetSource());

    RefactoringStatus status =
      mover.apply();
    assertTrue("Not moved class a.b.c.Test to a.b.d: "
        + status.getAllMessages(), !status.isErrorOrFatal()
        && status.getAllMessages().indexOf("Unable to relocate") >= 0);

    RwRefactoringTestUtils.assertSameSources(
        "After moving class a.b.c.Test to a.b.d", Utils
            .createTestRbProjectFromXml("MoveType_" + getStrippedTestName()
                + "_out"), project);

    cat.info("SUCCESS");
  }

  public void testManyMovesInARow() throws Exception {
    Utils.TempCompilationUnit[] sources = new Utils.TempCompilationUnit[5];
    BinCIType[] types = new BinCIType[sources.length];

    for (int i = 0; i < sources.length; i++) {
      sources[i] = new Utils.TempCompilationUnit("package a;\n"
          + "public class C" + i + " {\n" + "  C" + i + " randomUsage; \n" + // No reason for this line it seems
          "  public void someMethod() {System.err.println(8);} \n" + "}\n", "C"
          + i + ".java", "a");
    }

    Project before = Utils.createTestRbProjectFromString(sources);
    Project after = Utils.createTestRbProjectFromString(sources);

    for (int i = 0; i < 20; i++) {
      for (int j = 0; j < types.length; j++) {
        types[j] = before.getTypeRefForName("a.C" + j).getBinCIType();
      }

      MoveType mover = new MoveType(new NullContext(before), types);
      mover.setTargetPackage(before.createPackageForName("b", true));
      RefactoringStatus status
          = mover.apply();
      assertTrue(status.getAllMessages(), status.isOk());

      before.getProjectLoader().build(null, false);
      assertTrue(getUserFriendlyErrorsAsString(before),
          !(before.getProjectLoader().getErrorCollector()).hasUserFriendlyErrors());

      for (int j = 0; j < types.length; j++) {
        types[j] = before.getTypeRefForName("b.C" + j).getBinCIType();
      }

      mover = new MoveType(new NullContext(before), types);
      mover.setTargetPackage(before.createPackageForName("a", true));
      status = mover.apply();
      assertTrue(status.getAllMessages(), status.isOk());

      before.getProjectLoader().build(null, false);
      assertTrue(getUserFriendlyErrorsAsString(before),
          !(before.getProjectLoader().getErrorCollector()).hasUserFriendlyErrors());
    }

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public String getUserFriendlyErrorsAsString(Project p) {
    String result = "";

    for (java.util.Iterator i = (p.getProjectLoader().getErrorCollector()).getUserFriendlyErrors(); i.hasNext();) {
      result = result + i.next() + System.getProperty("line.separator");
    }

    return result;
  }

  public void testAccessConflictWithProtectedMembers() throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b; public class X {"
                + "  protected X() {}" + "}", "X.java", "b"),
            new Utils.TempCompilationUnit("package b; public class Y {"
                + "  X x = new X(); " + // Protected constructor access -- should disable moving X
                "}", "Y.java", "b"),
            new Utils.TempCompilationUnit(
                "package c; public class RandomClassInAnotherPackage {}",
                "RandomClassInAnotherPackage.java", "c") });

    MoveType moveType = new MoveType(new NullContext(project), project
        .getTypeRefForName("b.X").getBinType());
    moveType.setTargetPackage(project.getPackageForName("c"));
    RefactoringStatus status = RenameTestUtil
        .canBeSuccessfullyChanged(moveType);
    assertTrue(status != null && !status.isOk());
  }

  public void testAccessConflictWithProtectedMembers_noConflict()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b; public class X {"
                + "  protected X() {}" + "}", "X.java", "b"),
            new Utils.TempCompilationUnit(
                "package b; public class Y extends X {" + "  X x = new X(); " + // Protected constructor access, OK because it's _inherited_
                    "}", "Y.java", "b"),
            new Utils.TempCompilationUnit(
                "package c; public class RandomClassInAnotherPackage {}",
                "RandomClassInAnotherPackage.java", "c") });

    MoveType moveType = new MoveType(new NullContext(project), project
        .getTypeRefForName("b.X").getBinType());
    moveType.setTargetPackage(project.getPackageForName("c"));
    RefactoringStatus result = moveType.apply();
    assertTrue(result.getAllMessages(), result.isOk() || result.isInfo());
  }

  public void testDirectlyImportedClassGetsMovedToSamePackageWhereItsImported()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "package apackagewithlongername;import anotherpackage.Y;\n"
                    + "\n" + "public class X {}", "X.java",
                "apackagewithlongername"),
            new Utils.TempCompilationUnit(
                "package anotherpackage;public class Y {}\n", "Y.java",
                "anotherpackage") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package apackagewithlongername;\n"
                + "\n" + "public class X {}", "X.java",
                "apackagewithlongername"),
            new Utils.TempCompilationUnit(
                "package apackagewithlongername;public class Y {}\n", "Y.java",
                "apackagewithlongername") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("anotherpackage.Y").getBinType());
    moveType.setTargetPackage(before
        .getPackageForName("apackagewithlongername"));
    RefactoringStatus result =
      moveType.apply();
    assertTrue(result.getAllMessages(), result.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testImportCreationWhenMovingToDefaultPackage() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;public class X {}",
                "X.java", "a"),
            new Utils.TempCompilationUnit("package a;public class Y {\n"
                + "  X x;\n" + "}", "Y.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X {}", "X.java", null),
            new Utils.TempCompilationUnit("package a;public class Y {\n"
                + "  X x;\n" + "}", "Y.java", "a") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.X").getBinType());
    moveType.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testImportCreationWhenMovingToDefaultPackage_extractType()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;public class X {\n"
                + "  public static class I{}\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit("package a;\n" + "public class Y {\n"
                + "  X.I i;\n" + "}", "Y.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("\npublic class I{}\n", "I.java",
                null),
            new Utils.TempCompilationUnit("package a;public class X {\n" + "}",
                "X.java", "a"),
            new Utils.TempCompilationUnit("package a;\n" + "public class Y {\n"
                + "  I i;\n" + "}", "Y.java", "a") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.X$I").getBinType());
    moveType.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testRemovingImportStatementsWhenMovingToDefaultPackage()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;public class X {}",
                "X.java", "a"),
            new Utils.TempCompilationUnit("package b;import a.X;\n\n"
                + "public class Y {\n" + "  X x;\n" + "}", "Y.java", "b") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X {}", "X.java", null),
            new Utils.TempCompilationUnit("package b;\n\npublic class Y {\n"
                + "  X x;\n" + "}", "Y.java", "b") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.X").getBinType());
    moveType.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testNotCreatingImportStatementsWhenMovingAwayFromDefaultPackage()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X { Other o; }\n",
                "X.java", null),
            new Utils.TempCompilationUnit("public class Other {}",
                "Other.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n"
                + "public class X { Other o; }\n", "X.java", "a"),
            new Utils.TempCompilationUnit("public class Other {}",
                "Other.java", null) });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("X").getBinType());
    moveType.setTargetPackage(before.createPackageForName("a", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testSuperConstructorCallInMovedType_superTypeIsInSourcePackage()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n"
                + "public class X extends Super{\n" + "  public X() {\n"
                + "    super();\n" + "  }\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit("package a; public class Super{}",
                "Super.java", "a"),
            new Utils.TempCompilationUnit("package b; public class Random{}",
                "Random.java", "b") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a; public class Super{}",
                "Super.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "import a.Super;\n" + "\n" + "\n"
                + "public class X extends Super{\n" + "  public X() {\n"
                + "    super();\n" + "  }\n" + "}", "X.java", "b"),
            new Utils.TempCompilationUnit("package b; public class Random{}",
                "Random.java", "b") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.X").getBinType());
    moveType.setTargetPackage(before.getPackageForName("b"));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testThisReferencing() throws Exception {
    Project p = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package a;\n" + "public class X {\n" + "  private int field;\n"
                + "  public X() {\n" + "    super();\n"
                + "    System.out.println( this.toString() );\n"
                + "    System.err.println( super.toString() );\n"
                + "    this.field = 0;\n" + "  }\n" + "}", "X.java", "a") });

    MoveType moveType = new MoveType(new NullContext(p), p
        .getTypeRefForName("a.X").getBinType());
    moveType.setTargetPackage(p.createPackageForName("b", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());
  }

  public void testAddingFqnImportsForInnerTypes() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "  Y.Inner i;\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit("package a;public class Y {\n"
                + "  public class Inner {}\n" + "}", "Y.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("\n" + "import a.Y;\n" + "\n" + "\n"
                + "public class X {\n" + "  Y.Inner i;\n" + "}", "X.java", null),
            new Utils.TempCompilationUnit("package a;public class Y {\n"
                + "  public class Inner {}\n" + "}", "Y.java", "a") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.X").getBinType());
    moveType.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status = moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testAddingFqnImportsForInnerTypes_twoLevelsDeep()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "  Y.OuterInner.Inner i;\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit("package a;public class Y {\n"
                + "  public class OuterInner {\n"
                + "    public class Inner {}\n" + "  }\n" + "}", "Y.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("\n" + "import a.Y;\n" + "\n" + "\n"
                + "public class X {\n" + "  Y.OuterInner.Inner i;\n" + "}",
                "X.java", null),
            new Utils.TempCompilationUnit("package a;public class Y {\n"
                + "  public class OuterInner {\n"
                + "    public class Inner {}\n" + "  }\n" + "}", "Y.java", "a") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.X").getBinType());
    moveType.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status = moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testAddingFqnImportsForInnerTypes_importClauseReferencesInnerType()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package a; \n" + "public class A {\n" + "  public class B {\n"
                + "  }\n" + "\n" + "  public static class C {\n"
                + "    B b = null;\n" + "  }\n" + "\n" + "}", "A.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a; \n"
                + "public class A {\n" + "  public class B {\n" + "  }\n"
                + "\n" + "}", "A.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "import a.A.B;\n" + "\n" + "\n" + "public class C {\n"
                + "  B b = null;\n" + "}\n", "C.java", "b") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.A$C").getBinType());
    moveType.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status = moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testReadingFqnImports() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a; \n"
                + "public class A {\n" + "  public class B {}\n" + "}",
                "A.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "import a.A.B;\n" + "\n" + "\n" + "public class C {\n"
                + "  B b = null;\n" + "}\n", "C.java", "b") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a; \n"
                + "public class A {\n" + "  public class B {}\n" + "}",
                "A.java", "a"),
            new Utils.TempCompilationUnit("package c;\n" + "\n"
                + "import a.A.B;\n" + "\n" + "\n" + "public class C {\n"
                + "  B b = null;\n" + "}\n", "C.java", "c") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.C").getBinType());
    moveType.setTargetPackage(before.createPackageForName("c", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testReadingFqnImports_2() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "package a; \n" + "public class A {\n" + "  public class B {\n"
                    + "  }\n" + "}", "A.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "import a.A;\n" + "\n" + "\n" + "public class C {\n"
                + "  A.B b = null;\n" + "}\n", "C.java", "b") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "package a; \n" + "public class A {\n" + "  public class B {\n"
                    + "  }\n" + "}", "A.java", "a"),
            new Utils.TempCompilationUnit("package c;\n" + "\n"
                + "import a.A;\n" + "\n" + "\n" + "public class C {\n"
                + "  A.B b = null;\n" + "}\n", "C.java", "c") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.C").getBinType());
    moveType.setTargetPackage(before.createPackageForName("c", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testReadingFqnImports_array() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a; \n"
                + "public class A {\n" + "  public class B {}\n" + "}",
                "A.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "import a.A.B;\n" + "\n" + "\n" + "public class C {\n"
                + "  B[] b = new B[0];\n" + "}\n", "C.java", "b") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a; \n"
                + "public class A {\n" + "  public class B {}\n" + "}",
                "A.java", "a"),
            new Utils.TempCompilationUnit("package c;\n" + "\n"
                + "import a.A.B;\n" + "\n" + "\n" + "public class C {\n"
                + "  B[] b = new B[0];\n" + "}\n", "C.java", "c") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.C").getBinType());
    moveType.setTargetPackage(before.createPackageForName("c", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testReadingFqnImports_importedFromSourcePackage()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a; \n"
                + "public class A {\n" + "  public class B {}\n" + "}",
                "A.java", "a"),
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "import a.A.B;\n" + "\n" + "\n" + "public class C {\n"
                + "  B b = null;\n" + "}\n", "C.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a; \n"
                + "public class A {\n" + "  public class B {}\n" + "}",
                "A.java", "a"),
            new Utils.TempCompilationUnit("package c;\n" + "\n"
                + "import a.A.B;\n" + "\n" + "\n" + "public class C {\n"
                + "  B b = null;\n" + "}\n", "C.java", "c") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.C").getBinType());
    moveType.setTargetPackage(before.createPackageForName("c", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testReadingFqnImports_2_importedFromSourcePackage()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "package a; \n" + "public class A {\n" + "  public class B {\n"
                    + "  }\n" + "}", "A.java", "a"),
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "import a.A;\n" + "\n" + "\n" + "public class C {\n"
                + "  A.B b = null;\n" + "}\n", "C.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "package a; \n" + "public class A {\n" + "  public class B {\n"
                    + "  }\n" + "}", "A.java", "a"),
            new Utils.TempCompilationUnit("package c;\n" + "\n"
                + "import a.A;\n" + "\n" + "\n" + "public class C {\n"
                + "  A.B b = null;\n" + "}\n", "C.java", "c") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.C").getBinType());
    moveType.setTargetPackage(before.createPackageForName("c", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testReadingFqnImports_array_importedFromSourcePackage()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a; \n"
                + "public class A {\n" + "  public class B {}\n" + "}",
                "A.java", "a"),
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "import a.A.B;\n" + "\n" + "\n" + "public class C {\n"
                + "  B[] b = new B[0];\n" + "}\n", "C.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a; \n"
                + "public class A {\n" + "  public class B {}\n" + "}",
                "A.java", "a"),
            new Utils.TempCompilationUnit("package c;\n" + "\n"
                + "import a.A.B;\n" + "\n" + "\n" + "public class C {\n"
                + "  B[] b = new B[0];\n" + "}\n", "C.java", "c") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.C").getBinType());
    moveType.setTargetPackage(before.createPackageForName("c", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testRemovingFqnImportsOfInnerTypes_importInMovedFile()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "package a; \n" + "public class A {\n" + "  public class B {\n"
                    + "  }\n" + "}", "A.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "import a.A.B;\n" + "\n" + "\n" + "public class C {\n"
                + "  B b = null;\n" + "}\n", "C.java", "b") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "package a; \n" + "public class A {\n" + "  public class B {\n"
                    + "  }\n" + "}", "A.java", "a"),
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "import a.A.B;\n" + "\n" + "\n" + "public class C {\n"
                + "  B b = null;\n" + "}\n", "C.java", "a") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.C").getBinType());
    moveType.setTargetPackage(before.createPackageForName("a", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testRemovingFqnImportsOfInnerTypes_importInTargetPackage()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class A {\n"
                + "  public class B {\n" + "  }\n" + "}", "A.java", "a"),
            new Utils.TempCompilationUnit("package a;public class Random{}",
                "Random.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "import a.A.B;\n" + "\n" + "\n" + "public class C {\n"
                + "  B b = null;\n" + "}\n", "C.java", "b") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;public class Random{}",
                "Random.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "public class A {\n"
                + "  public class B {\n" + "  }\n" + "}", "A.java", "b"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "import b.A.B;\n" + "\n" + "\n" + "public class C {\n"
                + "  B b = null;\n" + "}\n", "C.java", "b") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.A").getBinType());
    moveType.setTargetPackage(before.getPackageForName("b"));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testNoNewlineAfterPackageStatement() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "package a;public class X { Y y; }\n", "X.java", "a"),
            new Utils.TempCompilationUnit("package a;public class Y {}",
                "Y.java", "a"), });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "import a.Y;\n" + "\n" + "\n" + "public class X { Y y; }\n",
                "X.java", "b"),
            new Utils.TempCompilationUnit("package a;public class Y {}",
                "Y.java", "a") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.X").getBinType());
    moveType.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testNoNewlineAfterPackageStatement_spacesBeforeSemicolon()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "package a  ;public class X { Y y; }\n", // Spaces in package declaration
                "X.java", "a"),
            new Utils.TempCompilationUnit("package a;public class Y {}",
                "Y.java", "a"), });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b  ;\n" + "\n"
                + "import a.Y;\n" + "\n" + "\n" + "public class X { Y y; }\n",
                "X.java", "b"),
            new Utils.TempCompilationUnit("package a;public class Y {}",
                "Y.java", "a") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.X").getBinType());
    moveType.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testNoNewlineAfterPackageStatement_commentsBeforeSemicolon()
      throws Exception {
    Project p = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package a/**/;public class X {}\n", // Comments in package declaration -- would mess up source code
            "X.java", "a"), });

    MoveType moveType = new MoveType(new NullContext(p), p
        .getTypeRefForName("a.X").getBinType());
    moveType.setTargetPackage(p.createPackageForName("b", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());
  }

  public void testNoNewlineAfterPackageStatement_newlineBeforeSemicolon()
      throws Exception {
    Project p = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package a\n" + // Newline before semicolon
                "; public class X {}\n", "X.java", "a"), });

    MoveType moveType = new MoveType(new NullContext(p), p
        .getTypeRefForName("a.X").getBinType());
    moveType.setTargetPackage(p.getPackageForName(""));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());
  }

  public void testNoNewlineAfterPackageStatement_spaceAndNewlineBeforeSemicolon()
      throws Exception {
    Project p = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package a \n" + // A space and a newline before semicolon
                "; public class X {}\n", "X.java", "a"), });

    MoveType moveType = new MoveType(new NullContext(p), p
        .getTypeRefForName("a.X").getBinType());
    moveType.setTargetPackage(p.getPackageForName(""));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());
  }

  public void testNoNewlineAfterPackageStatement_spaceAndNewlineBeforeSemicolon_2()
      throws Exception {
    Project p = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package a /*;*/\n" + // Semicolon commented out
                "; public class X {}\n", "X.java", "a"), });

    MoveType moveType = new MoveType(new NullContext(p), p
        .getTypeRefForName("a.X").getBinType());
    moveType.setTargetPackage(p.getPackageForName(""));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());
  }

  public void testSearchingCommonPath() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package a;public class X {\n" + "  public static class I {\n"
                + "    private void x() {}\n" + "  }\n" + "}", "X.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;public class X {\n" + "}",
                "X.java", "a"),
            new Utils.TempCompilationUnit("package asd;\n\n\n"
                + "public class I {\n" + "  private void x() {}\n" + "}\n",
                "I.java", "asd" /* *not* "sd", but "asd" */
            ) });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.X$I").getBinType());
    mover.setTargetPackage(before.createPackageForName("asd", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testSuperInvocation() throws Exception {
    Project p = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n\n"
                + "public class X extends Super{\n"
                + "  public String toString() { return super.toString(); }\n"
                + "}\n", "X.java", "a"),
            new Utils.TempCompilationUnit("package a;\n\n"
                + "public class Super{}", "Super.java", "a") });

    MoveType moveType = new MoveType(new NullContext(p), p
        .getTypeRefForName("a.X").getBinType());
    moveType.setTargetPackage(p.getPackageForName(""));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());
  }

  //------------------------------ Import conflicts in moved sources

  public void testImportConfclitsInMovedSources_noConflict_fqnImportWillBeCreatedLater()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "public class X {\n" + "  Conflicting c;\n" + "}", "X.java",
                "a"),
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "public class Conflicting {}", "Conflicting.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "public class Conflicting {}", "Conflicting.java", "b") });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.X").getBinType());
    mover.setTargetPackage(project.getPackageForName("b"));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isOk());
  }

  public void testImportConfclitsInMovedSources_noConflict_fqnUsage()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "public class X {\n" + "  a.Conflicting c;\n" + "}",
                "X.java", "a"),
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "public class Conflicting {}", "Conflicting.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "public class Conflicting {}", "Conflicting.java", "b") });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.X").getBinType());
    mover.setTargetPackage(project.getPackageForName("b"));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isOk());
  }

  public void testImportConfclitsInMovedSources_noConflict_implicitJavaLangObjectExtend()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "public class X {}\n", "X.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "public class Object {}", "Object.java", "b") });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.X").getBinType());
    mover.setTargetPackage(project.getPackageForName("b"));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isOk());
  }

  public void testImportConfclitsInMovedSources_conflict_explicitNonFqnJavaLangObjectExtend()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "public class X extends Object {}\n", "X.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "public class Object {}", "Object.java", "b") });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.X").getBinType());
    mover.setTargetPackage(project.getPackageForName("b"));
    final RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), !status.isOk());
  }

  public void testImportConfclitsInMovedSources_noConflict_explicitFqnJavaLangObjectExtend()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "public class X extends java.lang.Object {}\n", "X.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "public class Object {}", "Object.java", "b") });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.X").getBinType());
    mover.setTargetPackage(project.getPackageForName("b"));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isOk());
  }

  public void testImportConfclitsInMovedSources_conflict_nonFqnJavaLangObjectUsage()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "public class X { public Object o; }\n", "X.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "public class Object {}", "Object.java", "b") });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.X").getBinType());
    mover.setTargetPackage(project.getPackageForName("b"));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), !status.isOk());
  }

  public void testImportConfclitsInMovedSources_noConflict_fqnImport()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n"
                + "import c.Conflicting;\n" + "\n" + "public class X {\n"
                + "  Conflicting ccc;\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit("package c;\n" + "\n"
                + "public class Conflicting {}", "Conflicting.java", "c"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "public class Conflicting {}", "Conflicting.java", "b") });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.X").getBinType());
    mover.setTargetPackage(project.getPackageForName("b"));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isOk());
  }

  public void testImportConfclitsInMovedSources_conflict_nonFqnImport()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "import c.*;\n"
                + "\n" + "public class X {\n" + "  Conflicting ccc;\n" + "}",
                "X.java", "a"),
            new Utils.TempCompilationUnit("package c;\n" + "\n"
                + "public class Conflicting {}", "Conflicting.java", "c"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "public class Conflicting {}", "Conflicting.java", "b") });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.X").getBinType());
    mover.setTargetPackage(project.getPackageForName("b"));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), !status.isOk());
  }

  public void testImportConfclitsInMovedSources_noConflict_extractToSamePackageWhileImportingOwnPackageOnDemand()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package a;\n" + "\n" + "import a.*;\n" + // Imports own package
                "\n" + "public class X {\n"
                + "  public static class I { X x; }\n" + "}", "X.java", "a") });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.X$I").getBinType());
    mover.setTargetPackage(project.getPackageForName("a"));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isOk());
  }

  public void testImportConflictsInMovedSources_conflictOverInnerClassWithSameNameInTargetPackage()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "import b.*;\n"
                + "public class X{ B.I inner; }", "X.java", "a"),
            new Utils.TempCompilationUnit(
                "package b;      public class B{ public class I{} }", "B.java",
                "b"),
            new Utils.TempCompilationUnit(
                "package target; public class B{ public class I{} }", "B.java",
                "target"), });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.X").getBinType());
    mover.setTargetPackage(project.getPackageForName("target"));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), !status.isOk());
  }

  //------------------------------ Import conflicts in other sources (incl target package)

  public void testOtherFilesImportConflicts_conflictOverInnerClassWithSameNameBeingMoved()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "package a;\n public class B{ public class I{} }", "B.java",
                "a"),
            new Utils.TempCompilationUnit(
                "package b;\n public class B{ public class I{} }", "B.java",
                "b"),
            new Utils.TempCompilationUnit("package c;\n" + "import target.*;\n"
                + "import b.*;\n" + "public class Caller{ B.I inner; }",
                "Caller.java", "c"),
            new Utils.TempCompilationUnit(
                "package target; public class Random{}", "Random.java",
                "target") });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.B").getBinType());
    mover.setTargetPackage(project.getPackageForName("target"));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), !status.isOk());
  }

  public void testOtherFilesImportConflicts_conflictOverClassWhoseInnerClassIsUsed()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n public class B{}",
                "B.java", "a"),
            new Utils.TempCompilationUnit(
                "package b;\n public class B{ public class I{} }", "B.java",
                "b"),
            new Utils.TempCompilationUnit("package c;\n" + "import target.*;\n"
                + "import b.*;\n" + "public class Caller{ B.I inner; }", // The other B does not have an inner "I", but still a conflict.
                "Caller.java", "c"),
            new Utils.TempCompilationUnit(
                "package target; public class Random{}", "Random.java",
                "target") });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.B").getBinType());
    mover.setTargetPackage(project.getPackageForName("target"));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), !status.isOk());
  }

  public void testOtherFilesImportConflicts_noConflict_oneClassIsInnerClass()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n public class I{}",
                "I.java", "a"),
            new Utils.TempCompilationUnit(
                "package b;\n public class B{ public class I{} }", "B.java",
                "b"),
            new Utils.TempCompilationUnit("package c;\n" + "import target.*;\n"
                + "import b.*;\n" + "public class Caller{ B.I inner; }", // The other B does not have an inner "I", but still a conflict.
                "Caller.java", "c"),
            new Utils.TempCompilationUnit(
                "package target; public class Random{}", "Random.java",
                "target") });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.I").getBinType());
    mover.setTargetPackage(project.getPackageForName("target"));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isOk());
  }

  public void testOtherFilesImportConflicts_noConflict_fqnUsageFromTragetPackage()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "public class X { public Object o; }\n", "X.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "public class X {}", "Object.java", "b"),
            new Utils.TempCompilationUnit("package target;\n" + "\n"
                + "public class User {\n" + "  public b.X x;\n" + // FQN -- no conflict
                "}", "User.java", "target") });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.X").getBinType());
    mover.setTargetPackage(project.getPackageForName("target"));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isOk());
  }

  public void testOtherFilesImportConflicts_noConflict_fqnImportFromTargetPackage()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "public class X { public Object o; }\n", "X.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "public class X {}", "Object.java", "b"),
            new Utils.TempCompilationUnit("package target;\n" + "import b.X;\n"
                + // FQN import masks classes in the target package
                "public class User {\n" + "  public X x;\n" + "}", "User.java",
                "target") });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.X").getBinType());
    mover.setTargetPackage(project.getPackageForName("target"));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isOk());
  }

  public void testOtherFilesImportConflicts_noConflict_onlyUserIsClassItself()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "public class X { public Object o; }\n", "X.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "public class X {\n" + "  public X x;\n" + // Self usage and in another package
                "}", "Object.java", "b"), });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.X").getBinType());
    mover.setTargetPackage(project.createPackageForName("target", false));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isOk());
  }

  public void testOtherFilesImportConflicts_noConflict_importOnDemandFromTargetPackage()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "public class X { public Object o; }\n", "X.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "public class X {}", "Object.java", "b"),
            new Utils.TempCompilationUnit("package nontarget;\n"
                + "import b.X;\n" + "import target.*;\n"
                + "public class User {\n" + "  public X x;\n" + // OK because import-on-demand does not shadow anything
                "}", "User.java", "nontarget") });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.X").getBinType());
    mover.setTargetPackage(project.createPackageForName("target"));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isOk());
  }

  public void testOtherFilesImportConflicts_noConflict_fqnImportFromNonTargetPackage_alsoImportSomethingFromTargetPackage()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "public class X { public Object o; }\n", "X.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "public class X {}", "Object.java", "b"),
            new Utils.TempCompilationUnit("package nontarget;\n"
                + "import b.X;\n" + "import target.Random;\n"
                + "public class User {\n" + "  public X x;\n" + "}",
                "User.java", "nontarget"),
            new Utils.TempCompilationUnit("package target;\n"
                + "public class Random{}", "Random.java", "target") });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.X").getBinType());
    mover.setTargetPackage(project.getPackageForName("target"));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isOk());
  }

  public void testOtherFilesImportConflicts_noConflict_fqnImportFromNonTargetPackage()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "public class X { public Object o; }\n", "X.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "public class X {}", "X.java", "b"),
            new Utils.TempCompilationUnit("package nontarget;\n"
                + "import b.X;\n" + "public class User {\n" + "  public X x;\n"
                + "}", "User.java", "nontarget"),
            new Utils.TempCompilationUnit("package target;\n"
                + "public class Random{}", "Random.java", "target") });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.X").getBinType());
    mover.setTargetPackage(project.getPackageForName("target"));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isOk());
  }

  public void testOtherFilesImportConflicts_conflictInTargetPackage()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "public class X { public Object o; }\n", "X.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "public class X {}", "X.java", "b"),
            new Utils.TempCompilationUnit("package target;\n" + "import b.*;\n"
                + "public class User {\n" + "  public X x;\n" + "}",
                "User.java", "target"), });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.X").getBinType());
    mover.setTargetPackage(project.getPackageForName("target"));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), !status.isOk());
  }

  public void testOtherFilesImportConflicts_conflictInNonTargetPackage()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "public class X {}\n", "X.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "public class X {}", "X.java", "b"),
            new Utils.TempCompilationUnit("package nontarget;\n"
                + "import b.*;\n" + "import target.*;\n"
                + "public class User {\n" + "  public X x;\n" + "}",
                "User.java", "nontarget"), });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.X").getBinType());
    mover.setTargetPackage(project.createPackageForName("target", true));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), !status.isOk());
  }

  public void testOtherFilesImportConflicts_conflictOverExplicitNonFqnJavaLangObjectReference_nonTargetPackage()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "public class Object {}\n", "Object.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "import target.*;\n"
                + "public class R extends Object{}", "R.java", "b") });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.Object").getBinType());
    mover.setTargetPackage(project.createPackageForName("target", true));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), !status.isOk());
  }

  public void testOtherFilesImportConflicts_conflictOverExplicitNonFqnJavaLangObjectReference_targetPackage()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "public class Object {}\n", "Object.java", "a"),
            new Utils.TempCompilationUnit("package target;\n"
                + "public class R extends Object{}", "R.java", "target") });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.Object").getBinType());
    mover.setTargetPackage(project.getPackageForName("target"));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), !status.isOk());
  }

  public void testOtherFilesImportConflicts_noConflictOverExplicitNonFqnJavaLangObjectReference_targetPackage()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "public class Object {}\n", "Object.java", "a"),
            new Utils.TempCompilationUnit("package target;\n"
                + "public class R { java.lang.Object x; }", "R.java", "target"), });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.Object").getBinType());
    mover.setTargetPackage(project.getPackageForName("target"));
    final RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isOk());
  }

  public void testOtherFilesImportConflicts_noConflictOverExplicitNonFqnJavaLangObjectReference_targetPackage_2()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "public class Object {}\n", "Object.java", "a"),
            new Utils.TempCompilationUnit("package target;\n"
                + "import java.lang.*;\n" + "public class R {}", "R.java",
                "target") });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.Object").getBinType());
    mover.setTargetPackage(project.getPackageForName("target"));
    final RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isOk());
  }

  public void testOtherFilesImportConflicts_conflict_typeExtractedToSamePackage()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "public class I {\n" + "  public class X {}\n" + "}\n",
                "I.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "public class X {}", "I.java", "b"),
            new Utils.TempCompilationUnit("package someother;\n"
                + "import b.*;\n" + // The imports will conflict after move
                "import a.*;\n" + "public class User { X x; }", // X used here
                "User.java", "someother") });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.I$X").getBinType());
    mover.setTargetPackage(project.getPackageForName("a"));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), !status.isOk());
  }

  /** Same test as above, except that some class in target package has the problem */
  public void testOtherFilesImportConflicts_conflict_typeExtractedToSamePackage_2()
      throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "public class I {\n" + "  public class X {}\n" + "}\n",
                "I.java", "a"),
            new Utils.TempCompilationUnit("package a;\n" + "import b.*;\n" + // Extracted class X will shadow the b.X...
                "public class User { X x; }", // X used here
                "User.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "public class X {}", "I.java", "b") });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.I$X").getBinType());
    mover.setTargetPackage(project.getPackageForName("a"));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), !status.isOk());
  }

  //------------------------------ Tests for loosing access when extracting

  public void testInnerClassLoosesAccessToPrivateMembers() throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package a;\n" + "\n" + "public class X {\n" + "  private int i;\n"
                + "\n" + "  public class I {\n"
                + "    public int getI() { return i; }\n" + "  }\n" + "}\n",
            "X.java", "a"), });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.X$I").getBinType());
    mover.setTargetPackage(project.getPackageForName("a"));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), !status.isOk());
  }

  public void testInnerClassLoosesAccessOnlyToPrivateMembers() throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package a;\n" + "\n" + "public class X {\n" + "  int i;\n" + "\n"
                + "  public static class I {\n"
                + "    public int getI() { return i; }\n" + "  }\n" + "}\n",
            "X.java", "a"), });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.X$I").getBinType());
    mover.setTargetPackage(project.getPackageForName("a"));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isOk());
  }

  //------------------------------

  public void testIsFqnUsageDoesNotThrowExceptions() throws Exception {
    Project project = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "public class X {\n" + "  public void m() {\n"
                + "    new Object().toString();\n"
                + "    ((AnotherType)getAnotherType()).toString();" + "  }\n"
                + "\n" + "  public Object getAnotherType() {\n"
                + "    return new AnotherType();\n" + "  }\n" + "}\n",
                "X.java", "a"),
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "public class AnotherType{}", "AnotherType.java", "a") });

    MoveType mover = new MoveType(new NullContext(project), project
        .getTypeRefForName("a.X").getBinType());
    mover.setTargetPackage(project.createPackageForName("b", false));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isOk());
  }

  public void testArrayTypeGetsImportedFromOldPackage() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "public class X {\n" + "  Object o = new Used[0];\n" + "}\n",
                "X.java", "a"),
            new Utils.TempCompilationUnit("package a;public class Used {}\n",
                "Used.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "import a.Used;\n" + "\n" + "\n" + "public class X {\n"
                + "  Object o = new Used[0];\n" + "}\n", "X.java", "b"),
            new Utils.TempCompilationUnit("package a;public class Used {}\n",
                "Used.java", "a") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.X").getBinType());
    moveType.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status = moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testIllegalOneTypeImportFromDefaultPackageDoesNotCauseExceptions()
      throws Exception {
    Project p = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X {\n" + "}\n",
                "X.java", null),
            new Utils.TempCompilationUnit("package a;\n" + "import X;\n" + // Illegal import
                "public class User{}\n", "User.java", "a") });

    MoveType moveType = new MoveType(new NullContext(p), p
        .getTypeRefForName("X").getBinType());
    moveType.setTargetPackage(p.createPackageForName("b", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), !status.isOk());
  }

  public void testIllegalDefaultPackageImport() throws Exception {
    Project p = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package a;\n" + "import *;\n" + // Illegal import
                "public class X{}\n", "X.java", "a") });

    assertTrue((p.getProjectLoader().getErrorCollector()).hasErrors());
  }

  public void testInsertingAndErasingImportsAtTheSameLine() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package aa;\n" + "\n"
                + "import b.Y;\n" + // import deleted when it moves to b;
                "\n" + // also, a new import is needed -- for class U.
                "\n" + "public class X { U u; }", "X.java", "aa"),
            new Utils.TempCompilationUnit("package aa;\n" + "\n"
                + "public class U{}", "U.java", "aa"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "public class Y {}\n", "Y.java", "b") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "import aa.U;\n" + "\n" + "\n" + "public class X { U u; }",
                "X.java", "b"),
            new Utils.TempCompilationUnit("package aa;\n" + "\n"
                + "public class U{}", "U.java", "aa"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "public class Y {}\n", "Y.java", "b") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("aa.X").getBinType());
    moveType.setTargetPackage(before.getPackageForName("b"));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testInsertingAndErasingImportsAtTheSameLine_2() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "import a.U;\n" + "\n" + "\n" + "public class X {\n"
                + "  U s;\n" + "\n" + "  public void method(bbbbbb.U u) {\n"
                + "    u.doSomething();\n" + "  }\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "public class U{}", "U.java", "a"),
            new Utils.TempCompilationUnit("package a;public class Random{}",
                "Random.java", "a"),
            new Utils.TempCompilationUnit("package bbbbbb;\n" + "\n"
                + "public class U{\n" + "  public void doSomething() {}\n"
                + "}", "U.java", "bbbbbb"), });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("\n" + "\n" + "\n"
                + "public class X {\n" + "  U s;\n" + "\n"
                + "  public void method(bbbbbb.U u) {\n"
                + "    u.doSomething();\n" + "  }\n" + "}", "X.java", null),
            new Utils.TempCompilationUnit("\n" + "public class U{}", "U.java",
                null),
            new Utils.TempCompilationUnit("package a;public class Random{}",
                "Random.java", "a"),
            new Utils.TempCompilationUnit("package bbbbbb;\n" + "\n"
                + "public class U{\n" + "  public void doSomething() {}\n"
                + "}", "U.java", "bbbbbb"), });

    MoveType moveType = new MoveType(new NullContext(before),
        new Object[] { before.getTypeRefForName("a.X").getBinType(),
            before.getTypeRefForName("a.U").getBinType() });
    moveType.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testNotImportingClassesFromSourcePackageThatAreAlsoMoved()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "  Y y;\n" + "  Y[] y2 = new Y[0];\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit("package a;\n" + "public class Y {}",
                "Y.java", "a"),
            new Utils.TempCompilationUnit("package a;public class Random{}",
                "Random.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X {\n" + "  Y y;\n"
                + "  Y[] y2 = new Y[0];\n" + "}", "X.java", null),
            new Utils.TempCompilationUnit("public class Y {}", "Y.java", null),
            new Utils.TempCompilationUnit("package a;public class Random{}",
                "Random.java", "a") });

    MoveType moveType = new MoveType(new NullContext(before),
        new Object[] { before.getTypeRefForName("a.X").getBinType(),
            before.getTypeRefForName("a.Y").getBinType() });
    moveType.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testNotImportingClassesFromSourcePackageThatAreAlsoMoved_innerTypes()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "  Y y;\n" + "  Y.I[] y2 = new Y.I[0];\n" + "}", "X.java",
                "a"),
            new Utils.TempCompilationUnit("package a;\n"
                + "public class Y { public static class I{} }", "Y.java", "a"),
            new Utils.TempCompilationUnit("package a;public class Random{}",
                "Random.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X {\n" + "  Y y;\n"
                + "  Y.I[] y2 = new Y.I[0];\n" + "}", "X.java", null),
            new Utils.TempCompilationUnit(
                "public class Y { public static class I{} }", "Y.java", null),
            new Utils.TempCompilationUnit("package a;public class Random{}",
                "Random.java", "a") });

    MoveType moveType = new MoveType(new NullContext(before),
        new Object[] { before.getTypeRefForName("a.X").getBinType(),
            before.getTypeRefForName("a.Y").getBinType() });
    moveType.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testNotImportingClassesFromSourcePackageThatAreAlsoMoved_2()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "  {getY().toString();}\n" + // indirect usage
                "  public static Y getY() { return new Y(); }\n" + "}",
                "X.java", "a"),
            new Utils.TempCompilationUnit("package a;\n" + "public class Y {}",
                "Y.java", "a"),
            new Utils.TempCompilationUnit("package a;public class Random{}",
                "Random.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X {\n"
                + "  {getY().toString();}\n"
                + "  public static Y getY() { return new Y(); }\n" + "}",
                "X.java", null),
            new Utils.TempCompilationUnit("public class Y {}", "Y.java", null),
            new Utils.TempCompilationUnit("package a;public class Random{}",
                "Random.java", "a") });

    MoveType moveType = new MoveType(new NullContext(before),
        new Object[] { before.getTypeRefForName("a.X").getBinType(),
            before.getTypeRefForName("a.Y").getBinType() });
    moveType.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testRemoveWholePackageImportWhenNoClassesLeftInPackage()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {}",
                "X.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "import a.*;\n"
                + "import c.*;\n" + // This must *not* be removed
                "public class Y {}", "Y.java", "b"),
            new Utils.TempCompilationUnit("package c;public class Random{}",
                "Random.java", "c") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X {}", "X.java", null),
            new Utils.TempCompilationUnit("package b;\n" + "import c.*;\n"
                + "public class Y {}", "Y.java", "b"),
            new Utils.TempCompilationUnit("package c;public class Random{}",
                "Random.java", "c") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.X").getBinType());
    moveType.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testRemoveWholePackageImportWhenNoClassesLeftInPackage_twoImportsInOneFile()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {}",
                "X.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "import a.*;\n"
                + "import a.*;\n" + "import c.*;\n" + // This must *not* be removed
                "public class Y {}", "Y.java", "b"),
            new Utils.TempCompilationUnit("package c;public class Random{}",
                "Random.java", "c") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X {}", "X.java", null),
            new Utils.TempCompilationUnit("package b;\n" + "import c.*;\n"
                + "public class Y {}", "Y.java", "b"),
            new Utils.TempCompilationUnit("package c;public class Random{}",
                "Random.java", "c") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.X").getBinType());
    moveType.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testRemoveWholePackageImportWhenNoClassesLeftInPackage_multipleFiles()
      throws Exception {
    // NOTE: All files in wrong folder -- otherwise an empty folder "a" would be left
    // and test would fail because of that empty package not being present in the
    // "after" project.

    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {}",
                "X.java", null),
            new Utils.TempCompilationUnit(
                "package a;\n" + "public class X2 {}", "X2.java", null),
            new Utils.TempCompilationUnit("package b;\n" + "import a.*;\n"
                + "public class Y {}", "Y.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X {}", "X.java", null),
            new Utils.TempCompilationUnit("public class X2 {}", "X2.java", null),
            new Utils.TempCompilationUnit("package b;\n" + "public class Y {}",
                "Y.java", null) });

    MoveType moveType = new MoveType(new NullContext(before),
        new Object[] { before.getTypeRefForName("a.X").getBinType(),
            before.getTypeRefForName("a.X2").getBinType() });
    moveType.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isInfoOrWarning());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testWholePackageImport_reckognizingFqnImportWithMultiLevelPackageNames()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "package net.sf.refactorit.loader;\n"
                    + "import net.sf.refactorit.classmodel.LocationAware;\n"
                    + "public class Comment implements LocationAware {}",
                "Comment.java", null),
            new Utils.TempCompilationUnit(
                "package net.sf.refactorit.classmodel;\n"
                    + "public interface LocationAware {}",
                "LocationAware.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "package net.sf.refactorit.loader;\n"
                    + "import net.sf.refactorit.LocationAware;\n"
                    + "public class Comment implements LocationAware {}",
                "Comment.java", null),
            new Utils.TempCompilationUnit("package net.sf.refactorit;\n"
                + "public interface LocationAware {}", "LocationAware.java",
                null) });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("net.sf.refactorit.classmodel.LocationAware")
        .getBinType());
    moveType.setTargetPackage(before.createPackageForName("net.sf.refactorit", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isInfoOrWarning());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testExtractWithEmptyLines() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "public class X {\n" + "  public static class I{\n" + "\n" + // Empty line, does not have the normal 2-space ident
                "  }\n" + "}", "X.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X {\n" + "}", "X.java",
                null),
            new Utils.TempCompilationUnit("\n" + "public class I{\n" + "\n"
                + "}\n", "I.java", null) });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("X$I").getBinType());
    moveType.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testMovingTwoFilesToDefaultPackageFirstFileIsAlreadyThere()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "\n" + "\n"
                + "public class X {}\n", "X.java", "a"),
            new Utils.TempCompilationUnit("public class Y{}", "Y.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("\n" + "\n" + "public class X {}\n",
                "X.java", null),
            new Utils.TempCompilationUnit("public class Y{}", "Y.java", null) });

    MoveType moveType = new MoveType(new NullContext(before),
        new Object[] { before.getTypeRefForName("a.X").getBinType(),
            before.getTypeRefForName("Y").getBinType() });
    moveType.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testTabSizeEffectOnExtract() throws Exception {
    GlobalOptions.setOption("source.tab-size", "4");

    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "public class X{\n" + "  public static class Inner{\n"
                + "\tprivate int i;\n" + "  }\n" + "}", "X.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X{\n" + "}", "X.java",
                null),
            new Utils.TempCompilationUnit("\n" + "public class Inner{\n"
                + "private int i;\n" + "}\n", "Inner.java", null) });

    MoveType moveType = new MoveType(new NullContext(before),
        before.getTypeRefForName("X$Inner").getBinType());
    moveType.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testTabSizeEffectOnExtract_tabSize2() throws Exception {
    GlobalOptions.setOption("source.tab-size", "2");

    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "public class X{\n" + "\tpublic static class Inner{\n"
                + "\t\tprivate int i;\n" + "  }\n" + "}", "X.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X{\n" + "}", "X.java",
                null),
            new Utils.TempCompilationUnit("\n" + "public class Inner{\n"
                + "\tprivate int i;\n" + "}\n", "Inner.java", null) });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("X$Inner").getBinType());
    moveType.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testUpdateForLineResizeInRenameEditor() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "import a.Y;\n" + // Same package import -- gets renamed; also a.Z import will be generated here.
                "\n" + "\n" + "public class X { Z z; }", // Z usage
                "X.java", "a"),
            new Utils.TempCompilationUnit("package a;\npublic class Y{}",
                "Y.java", "a"),
            new Utils.TempCompilationUnit("package a;\npublic class Z{}",
                "Z.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "import a.Z;\n" + "import b.Y;\n" + "\n" + "\n"
                + "public class X { Z z; }", // Z usage
                "X.java", "b"),
            new Utils.TempCompilationUnit("package b;\npublic class Y{}",
                "Y.java", "b"),
            new Utils.TempCompilationUnit("package a;\npublic class Z{}",
                "Z.java", "a") });

    MoveType moveType = new MoveType(new NullContext(before),
        new Object[] { before.getTypeRefForName("a.X").getBinType(),
            before.getTypeRefForName("a.Y").getBinType() });
    moveType.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testNotInsertingImportsForIndirectUsages_methodAndFieldInvocation()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "  public static class Y{\n" + "    public void mat() {\n"
                + "      System.out.println();\n" + // PrintStream should not be imported now
                "      System.out.println(A.getNotImportedObject().field);\n" + // NotImportedObject should not be imported
                "      System.out.println(A.getNotImportedObject().method());\n"
                + // NotImportedObject should not be imported
                "    }\n" + "  }\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit("package a;\n" + "public class A{\n"
                + "  public NotImportedObject getNotImportedObject(){\n"
                + "    return new NotImportedObject();\n" + "  }\n" + "}",
                "A.java", "a"),
            new Utils.TempCompilationUnit(
                "package a;\npublic class NotImportedObject{ public NotImportedObject field; public NotImportedObject method(){return null;}}",
                "NotImportedObject.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "}", "X.java", "a"),
            new Utils.TempCompilationUnit(
                "import a.A;\n"
                    + "\n"
                    + "\n"
                    + "public class Y{\n"
                    + "  public void mat() {\n"
                    + "    System.out.println();\n"
                    + "    System.out.println(A.getNotImportedObject().field);\n"
                    + "    System.out.println(A.getNotImportedObject().method());\n"
                    + "  }\n" + "}\n", "Y.java", null),
            new Utils.TempCompilationUnit("package a;\n" + "public class A{\n"
                + "  public NotImportedObject getNotImportedObject(){\n"
                + "    return new NotImportedObject();\n" + "  }\n" + "}",
                "A.java", "a"),
            new Utils.TempCompilationUnit(
                "package a;\npublic class NotImportedObject{ public NotImportedObject field; public NotImportedObject method(){return null;}}",
                "NotImportedObject.java", "a") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.X$Y").getBinType());
    moveType.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testAddingImportToOldPackageTypes_innerTypeArray()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n"
                + "public class X{{ new B.I[0]; }}\n", "X.java", "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class B{ public static class I{} }",
                "B.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package target;\n" + "\n"
                + "import a.B;\n" + "\n" + "\n"
                + "public class X{{ new B.I[0]; }}\n", "X.java", "target"),
            new Utils.TempCompilationUnit(
                "package a;\n public class B{ public static class I{} }",
                "B.java", "a") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.X").getBinType());
    moveType.setTargetPackage(before.createPackageForName("target"));
    RefactoringStatus status = moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  /**
   * This bug prevented RefactorIT from moving RefactorItActions in netbeans package.
   * (There was a StackOverflowError caused by BinCIType.getAccessibleInners(BinCIType))
   */
  public void testImplementingInnerInterfaceThatExtendsItsOuterInterface()
      throws Exception {
    Project p = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public interface X {\n"
                + "  public interface I extends X{}\n" + "}", "X.java", null),
            new Utils.TempCompilationUnit("public class A implements X.I {}",
                "A.java", null) });

    MoveType mover = new MoveType(new NullContext(p), p
        .getTypeRefForName("A").getBinType());
    mover.setTargetPackage(p.createPackageForName("a", false));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isOk());
  }

  public void testMemberVisibilityEditorAndStaticModifierEraserUpdateEeachothersNodes()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "public class X{\n" + "  protected static class I{}\n" + "}",
            "X.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X{\n" + "}", "X.java",
                null),
            new Utils.TempCompilationUnit("\n" + "public class I{}\n",
                "I.java", null) });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("X$I").getBinType());
    mover.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testImportingExtractedInnerTypeInOuterType() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "import java.util.List;\n" + "public class X{\n" + "  I i;\n" + // Inner type usage
                "  protected static class I{ List l; }\n" + // This generates an import -- this exposed a bug
                "}", "X.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("import java.util.List;\n"
                + "import target.I;\n" + "\n" + "\n" + "public class X{\n"
                + "  I i;\n" + "}", "X.java", null),
            new Utils.TempCompilationUnit("package target;\n" + "\n"
                + "import java.util.List;\n" + "\n" + "\n"
                + "public class I{ List l; }\n", "I.java", "target") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("X$I").getBinType());
    mover.setTargetPackage(before.createPackageForName("target", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testImportingExtractedInnerTypeInOuterType_constructorCall()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "public class X{\n" + "  Object o = new I();\n" + // Inner type usage
                "  protected static class I{}\n" + // Because class will be public, will constructor be too.
                "}", "X.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("import target.I;\n" + "\n" + "\n"
                + "public class X{\n" + "  Object o = new I();\n" + "}",
                "X.java", null),
            new Utils.TempCompilationUnit("package target;\n" + "\n" + "\n"
                + "public class I{}\n", "I.java", "target") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("X$I").getBinType());
    mover.setTargetPackage(before.createPackageForName("target", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testImportingExtractedInnerTypeInOuterType_protectedConstructorCall()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "public class X{\n" + "  Object o = new I();\n" + // Inner type usage
                "  protected static class I{ protected I() {} }\n" + // protected constructor -- conflict
                "}", "X.java", null) });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("X$I").getBinType());
    mover.setTargetPackage(before.createPackageForName("target", false));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), !status.isOk());
  }

  public void testImportingExtractedInnerTypeInOuterType_publicConstructorCall()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "public class X{\n" + "  Object o = new I();\n" + // Inner type usage
                "  protected static class I{ public I() {} }\n" + // public constructor -- OK
                "}", "X.java", null) });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("X$I").getBinType());
    mover.setTargetPackage(before.createPackageForName("target", false));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isOk());
  }

  public void testImportingExtractedInnerTypeInOuterType_array()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "public class X{\n" + "  Object o = new I[0];\n" + // Inner type usage
                "  protected static class I{}\n" + "}", "X.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("import target.I;\n" + "\n" + "\n"
                + "public class X{\n" + "  Object o = new I[0];\n" + "}",
                "X.java", null),
            new Utils.TempCompilationUnit("package target;\n" + "\n" + "\n"
                + "public class I{}\n", "I.java", "target") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("X$I").getBinType());
    mover.setTargetPackage(before.createPackageForName("target", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testImportingExtractedInnerTypeInOuterType_fqn_noImport()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package a;\n" + "public class X{\n"
                + "  Object o = new a.X.I();\n" + // Inner type usage
                "  protected static class I{}\n" + "}", "X.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X{\n"
                + "  Object o = new target.I();\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit("package target;\n" + "\n" + "\n"
                + "public class I{}\n", "I.java", "target") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.X$I").getBinType());
    mover.setTargetPackage(before.createPackageForName("target", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testExtractedTypeUsesFieldsFromOuterClass() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "public class X{\n" + "  public static int i;\n"
                + "  protected static class I{\n" + "    public I() {\n"
                + "      System.out.println(i);\n" + "    }\n" + "  }\n" + "}",
            "X.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X{\n"
                + "  public static int i;\n" + "}", "X.java", null),
            new Utils.TempCompilationUnit("\n" + "public class I{\n"
                + "  public I() {\n" + "    System.out.println(X.i);\n"
                + "  }\n" + "}\n", "I.java", null) });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("X$I").getBinType());
    mover.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testExtractedTypeUsesFieldsFromAnotherClassInSameFile()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "public class X{\n" + "  public static class I2{\n"
                + "    public static int i;\n" + "  }\n"
                + "  protected static class I{\n" + "    public I() {\n"
                + "      System.out.println(I2.i);\n"
                + "      System.out.println(new I2().i);\n"
                + "      System.out.println(X.I2.i);\n"
                + "      System.out.println(new X.I2().i);\n" + "    }\n"
                + "  }\n" + "}", "X.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X{\n"
                + "  public static class I2{\n" + "    public static int i;\n"
                + "  }\n" + "}", "X.java", null),
            new Utils.TempCompilationUnit("import X.I2;\n\n\n" + "public class I{\n"
                + "  public I() {\n" + "    System.out.println(I2.i);\n"
                + "    System.out.println(new I2().i);\n"
                + "    System.out.println(X.I2.i);\n"
                + "    System.out.println(new X.I2().i);\n" + "  }\n" + "}\n",
                "I.java", null) });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("X$I").getBinType());
    mover.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testExtractedTypeUsesFieldsFromAnotherClassInSameFile_bothTypesAreMoving()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "public class X{\n" + "  public static class I2{\n"
                + "    public static int i;\n" + "  }\n"
                + "  protected static class I{\n" + "    public I() {\n"
                + "      System.out.println(I2.i);\n" + "    }\n" + "  }\n"
                + "}", "X.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X{\n" + "\n" + "}",
                "X.java", null),
            new Utils.TempCompilationUnit("\n" + "public class I2{\n"
                + "  public static int i;\n" + "}\n", "I2.java", null),
            new Utils.TempCompilationUnit("\n" + "public class I{\n"
                + "  public I() {\n" + "    System.out.println(I2.i);\n"
                + "  }\n" + "}\n", "I.java", null) });

    MoveType mover = new MoveType(new NullContext(before), new Object[] {
        before.getTypeRefForName("X$I").getBinType(),
        before.getTypeRefForName("X$I2").getBinType() });
    mover.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  /** This also tests ordering of editors -- makes sure that LocationAwareMover is the last editor in a file */
  public void testExtractedTypeUsesFieldsFromAnotherClassInSameFile_bothTypesAreMoving_deepLevelInner()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "public class X{\n" + "  public static class I2{\n"
                + "    public static class Inn { public static int i; }\n"
                + "  }\n" + "  protected static class I{\n"
                + "    public I() {\n"
                + "      System.out.println(I2.Inn.i);\n" + "    }\n" + "  }\n"
                + "}", "X.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X{\n"
                + "  public static class I2{\n" + "  }\n" + "}", "X.java", null),
            new Utils.TempCompilationUnit("\n" + "public class I{\n"
                + "  public I() {\n" + "    System.out.println(Inn.i);\n"
                + "  }\n" + "}\n", "I.java", null),
            new Utils.TempCompilationUnit("\n"
                + "public class Inn { public static int i; }\n", "Inn.java",
                null) });

    MoveType mover = new MoveType(new NullContext(before), new Object[] {
        before.getTypeRefForName("X$I").getBinType(),
        before.getTypeRefForName("X$I2$Inn").getBinType() });
    mover.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testExtractedTypeUsesFieldsFromAnotherClassInSameFile_bothTypesAreMoving_deepLevelInner_ownerMoves()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "public class X{\n" + "  public static class I2{\n"
                + "    public static class Inn { public static int i; }\n"
                + "  }\n" + "  protected static class I{\n"
                + "    public I() {\n"
                + "      System.out.println(I2.Inn.i);\n" + "    }\n" + "  }\n"
                + "}", "X.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X{\n" + "\n" + "}",
                "X.java", null),
            new Utils.TempCompilationUnit("\n" + "public class I{\n"
                + "  public I() {\n" + "    System.out.println(I2.Inn.i);\n"
                + "  }\n" + "}\n", "I.java", null),
            new Utils.TempCompilationUnit("\n" + "public class I2{\n"
                + "  public static class Inn { public static int i; }\n"
                + "}\n", "I2.java", null) });

    MoveType mover = new MoveType(new NullContext(before), new Object[] {
        before.getTypeRefForName("X$I").getBinType(),
        before.getTypeRefForName("X$I2").getBinType() });
    mover.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testExtractedTypeUsesFieldsFromAnotherClassInSameFile_bothTypesAreMoving_prefixDependsOnWhichOwnerMoves()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "public class X{\n" + "  public static class I2{\n"
                + "    public static class Inn { public static int i; }\n"
                + "  }\n" + "  protected static class I{\n"
                + "    public I() {\n"
                + "      System.out.println(I2.Inn.i);\n" + "    }\n" + "  }\n"
                + "}", "X.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X{\n" + "\n" + "}",
                "X.java", null),
            new Utils.TempCompilationUnit("\n" + "public class I{\n"
                + "  public I() {\n" + "    System.out.println(I2.Inn.i);\n"
                + "  }\n" + "}\n", "I.java", null),
            new Utils.TempCompilationUnit("\n" + "public class I2{\n"
                + "  public static class Inn { public static int i; }\n"
                + "}\n", "I2.java", null) });

    MoveType mover = new MoveType(new NullContext(before), new Object[] {
        before.getTypeRefForName("X$I").getBinType(),
        before.getTypeRefForName("X$I2").getBinType() });
    mover.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testExtractedTypeUsesMethodsFromOuterClass() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "public class X{\n" + "  public static void m(){}\n"
                + "  protected static class I{\n" + "    public I() {\n"
                + "      m();\n" + // Change -- outer class method
                "      new Object().toString();\n" + // No change
                "      selfMethod();\n" + // No Change
                "    }\n" + "    public void selfMethod(){}\n" + "  }\n" + "}",
            "X.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X{\n"
                + "  public static void m(){}\n" + "}", "X.java", null),
            new Utils.TempCompilationUnit("\n" + "public class I{\n"
                + "  public I() {\n" + "    X.m();\n"
                + "    new Object().toString();\n" + "    selfMethod();\n"
                + "  }\n" + "  public void selfMethod(){}\n" + "}\n", "I.java",
                null) });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("X$I").getBinType());
    mover.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testExtractedTypeUsesMethodsFromOuterClass_prefixAlreadyPresent()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "public class X{\n" + "  public static void m(){}\n"
                + "  protected static class I{\n" + "    public I() {\n"
                + "      X.m();\n" + // No change
                "    }\n" + "  }\n" + "}", "X.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X{\n"
                + "  public static void m(){}\n" + "}", "X.java", null),
            new Utils.TempCompilationUnit("\n" + "public class I{\n"
                + "  public I() {\n" + "    X.m();\n" + "  }\n" + "}\n",
                "I.java", null) });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("X$I").getBinType());
    mover.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testExtractedTypeUsesMethodsFromOuterClass_moveToOtherPackage()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package a;\n" + "public class X{\n"
                + "  public static void m(){}\n"
                + "  protected static class I{{ m(); }}\n" + "}", "X.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X{\n"
                + "  public static void m(){}\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "import a.X;\n" + "\n" + "\n"
                + "public class I{{ X.m(); }}\n", "I.java", "b") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.X$I").getBinType());
    mover.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testExtractedTypeUsesMethodsFromOuterClass_nonStaticMethod()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "public class X{\n" + "  public void m(){}\n"
                + "  protected class I{{ m(); }}\n" + "}", "X.java", null) });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("X$I").getBinType());
    mover.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), !status.isOk());
  }

  public void testExtractedTypeUsesMethodsFromSuperTypeOfOuterClass()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X extends Super{\n"
                + "  protected static class I{\n" + "    public I() {\n"
                + "      m();\n" + // Superclass's method invocation
                "    }\n" + "  }\n" + "}", "X.java", null),
            new Utils.TempCompilationUnit("public class Super{\n"
                + "  public static void m(){}\n" + "}", "Super.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X extends Super{\n"
                + "}", "X.java", null),
            new Utils.TempCompilationUnit("public class Super{\n"
                + "  public static void m(){}\n" + "}", "Super.java", null),
            new Utils.TempCompilationUnit("\n" + "public class I{\n"
                + "  public I() {\n" + "    Super.m();\n" + "  }\n" + "}\n",
                "I.java", null) });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("X$I").getBinType());
    mover.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testExtractedTypeUsesMethodsFromSuperTypeOfOuterClass_superMoving()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X extends Super{\n"
                + "  protected static class I{\n" + "    public I() {\n"
                + "      m();\n" + // Superclass's method invocation
                "    }\n" + "  }\n" + "}", "X.java", null),
            new Utils.TempCompilationUnit("public class Super{\n"
                + "  public static void m(){}\n" + "}", "Super.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("import a.Super;\n" + "\n" + "\n"
                + "public class X extends Super{\n" + "}", "X.java", null),
            new Utils.TempCompilationUnit("package a;\n"
                + "public class Super{\n" + "  public static void m(){}\n"
                + "}", "Super.java", "a"),
            new Utils.TempCompilationUnit("package a;\n" + "\n" + "\n"
                + "public class I{\n" + "  public I() {\n" + "    Super.m();\n"
                + "  }\n" + "}\n", "I.java", "a") });

    MoveType mover = new MoveType(new NullContext(before), new Object[] {
        before.getTypeRefForName("X$I").getBinType(),
        before.getTypeRefForName("Super").getBinType() });
    mover.setTargetPackage(before.createPackageForName("a"));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testImportingExtractedInnerTypeInOtherTypes() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X{\n"
                + "  protected static class I{}\n" + // Also tests whether protected & used outside classes
                "}", // can be moved at all
                "X.java", null),
            new Utils.TempCompilationUnit("class User{ X.I f; }", "User.java",
                null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X{\n" + "}", "X.java",
                null),
            new Utils.TempCompilationUnit("class User{ target.I f; }",
                "User.java", null),
            new Utils.TempCompilationUnit("package target;\n" + "\n" + "\n"
                + "public class I{}\n", "I.java", "target") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("X$I").getBinType());
    mover.setTargetPackage(before.createPackageForName("target", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testImportingExtractedInnerTypeInOtherTypes_fqn()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package o;\n" + "public class X{\n"
                + "  protected static class I{}\n" + "}", "X.java", "o"),
            new Utils.TempCompilationUnit("class User{ o.X.I f; }",
                "User.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package o;\n" + "public class X{\n"
                + "}", "X.java", "o"),
            new Utils.TempCompilationUnit("class User{ target.I f; }",
                "User.java", null),
            new Utils.TempCompilationUnit("package target;\n" + "\n" + "\n"
                + "public class I{}\n", "I.java", "target") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("o.X$I").getBinType());
    mover.setTargetPackage(before.createPackageForName("target", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testImportingExtractedInnerTypeInOtherTypes_nonDefaultPackage()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package o;\n" + "public class X{\n"
                + "  protected static class I{}\n" + "}", "X.java", "o"),
            new Utils.TempCompilationUnit("package o; class User{ X.I f; }\n",
                "User.java", "o") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package o;\n" + "public class X{\n"
                + "}", "X.java", "o"),
            new Utils.TempCompilationUnit(
                "package o; class User{ target.I f; }\n", "User.java", "o"),
            new Utils.TempCompilationUnit("package target;\n" + "\n" + "\n"
                + "public class I{}\n", "I.java", "target") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("o.X$I").getBinType());
    mover.setTargetPackage(before.createPackageForName("target", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testImportingExtractedInnerTypeInOtherTypes_changingImportStatement_defaultPackage()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X{\n"
                + "  protected static class I{}\n" + "}", "X.java", null),
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "import X.I;\n" + "\n" + "\n" + "class User{ I f; }",
                "User.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X{\n" + "}", "X.java",
                null),
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "import target.I;\n" + "\n" + "\n" + "class User{ I f; }",
                "User.java", "a"),
            new Utils.TempCompilationUnit("package target;\n" + "\n" + "\n"
                + "public class I{}\n", "I.java", "target") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("X$I").getBinType());
    mover.setTargetPackage(before.createPackageForName("target", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testImportingExtractedInnerTypeInOtherTypes_changingImportStatement()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package o;\n" + "public class X{\n"
                + "  protected static class I{}\n" + "}", "X.java", "o"),
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "import o.X.I;\n" + "\n" + "\n" + "class User{ I f; }",
                "User.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package o;\n" + "public class X{\n"
                + "}", "X.java", "o"),
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "import target.I;\n" + "\n" + "\n" + "class User{ I f; }",
                "User.java", "a"),
            new Utils.TempCompilationUnit("package target;\n" + "\n" + "\n"
                + "public class I{}\n", "I.java", "target") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("o.X$I").getBinType());
    mover.setTargetPackage(before.createPackageForName("target", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testFqnUsageOnExtract() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package o;\n" + "public class X{\n"
                + "  protected static class I{}\n" + "}", "X.java", "o"),
            new Utils.TempCompilationUnit("class User{ o.X.I f; }",
                "User.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package o;\n" + "public class X{\n"
                + "}", "X.java", "o"),
            new Utils.TempCompilationUnit("package target;\n" + "\n" + "\n"
                + "public class I{}\n", "I.java", "target"),
            new Utils.TempCompilationUnit("class User{ target.I f; }",
                "User.java", null) });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("o.X$I").getBinType());
    mover.setTargetPackage(before.createPackageForName("target", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testImportsInOuterIfTwoInnersAreMoving() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package a;\n" + "public class X{\n" + "  I1 f1;\n" + "  I2 f2;\n"
                + "\n" + "  protected static class I1{}\n"
                + "  public static class I2{}\n" + "}", "X.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "import b.I1;\n" + "import b.I2;\n" + "\n" + "\n"
                + "public class X{\n" + "  I1 f1;\n" + "  I2 f2;\n" + "\n"
                + "}", "X.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n" + "\n"
                + "public class I1{}\n", "I1.java", "b"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "public class I2{}\n", "I2.java", "b") });

    MoveType mover = new MoveType(new NullContext(before), new Object[] {
        before.getTypeRefForName("a.X$I1").getBinType(),
        before.getTypeRefForName("a.X$I2").getBinType() });
    mover.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testLinesAreNotShiftedBeforeLocationAwareMover() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X{\n"
                + "  Another usage;\n" + "\n"
                + "  public static class Inner{}\n" + "}", "X.java", null),
            new Utils.TempCompilationUnit("public class Another {}\n",
                "Another.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("import a.Another;\n" + "\n" + "\n"
                + "public class X{\n" + "  Another usage;\n" + "}", "X.java",
                null),
            new Utils.TempCompilationUnit("package a;\n" + "\n" + "\n"
                + "public class Inner{}\n", "Inner.java", "a"),
            new Utils.TempCompilationUnit("package a;\n"
                + "public class Another {}\n", "Another.java", "a") });

    MoveType mover = new MoveType(new NullContext(before), new Object[] {
        before.getTypeRefForName("X$Inner").getBinType(),
        before.getTypeRefForName("Another").getBinType() });
    mover.setTargetPackage(before.createPackageForName("a", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testNonStaticInnerClassExtract() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "public class X{\n"
                + "  public static int staticField;\n"
                + "\n"
                + "  public static void staticMethod(int i){}\n"
                + "\n"
                + "  public class NonStaticInner{{ staticMethod(staticField); }}\n"
                + "}", "X.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X{\n"
                + "  public static int staticField;\n" + "\n"
                + "  public static void staticMethod(int i){}\n" + "}",
                "X.java", null),
            new Utils.TempCompilationUnit(
                "\n"
                    + "public class NonStaticInner{{ X.staticMethod(X.staticField); }}\n",
                "NonStaticInner.java", null) });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("X$NonStaticInner").getBinType());
    mover.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testNonStaticInnerClassExtract_usesNonStaticMembers()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "public class X{\n"
                + "  public int nonStaticField;\n"
                + "\n"
                + "  public void nonStaticMethod(int i){}\n"
                + "\n"
                + "  public class NonStaticInner{{ nonStaticMethod(nonStaticField); }}\n"
                + "}", "X.java", null) });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("X$NonStaticInner").getBinType());
    mover.setTargetPackage(before.createPackageForName("a", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), !status.isOk());
  }

  public void testNonStaticInnerClassExtract_usesNonStaticMembers_2()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "public class X{\n"
                + "  public int nonStaticField;\n"
                + "\n"
                + "  public class NonStaticInner{{ System.err.println(X.this.nonStaticField); }}\n"
                + "}", "X.java", null) });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("X$NonStaticInner").getBinType());
    mover.setTargetPackage(before.createPackageForName("a", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), !status.isOk());
  }

  public void testNonStaticInnerClassExtract_usesNonStaticMembersThroughSelfCreatedReference()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "public class X{\n" + "  public static int staticField;\n" + "\n"
                + "  public static void staticMethod(int i){}\n" + "\n"
                + "  public void nonStaticMethod(){}\n" + "\n"
                + "  public class NonStaticInner{\n"
                + "    public void method(X x) {\n"
                + "      staticMethod(staticField);\n"
                + "      new X().nonStaticMethod();\n"
                + "      x.nonStaticMethod();\n"
                + "      System.out.println(toString()+super.toString());\n"
                + "    }\n" + "  }\n" + "}", "X.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("public class X{\n"
                + "  public static int staticField;\n" + "\n"
                + "  public static void staticMethod(int i){}\n" + "\n"
                + "  public void nonStaticMethod(){}\n" + "}", "X.java", null),
            new Utils.TempCompilationUnit("\n"
                + "public class NonStaticInner{\n"
                + "  public void method(X x) {\n"
                + "    X.staticMethod(X.staticField);\n"
                + "    new X().nonStaticMethod();\n"
                + "    x.nonStaticMethod();\n"
                + "    System.out.println(toString()+super.toString());\n"
                + "  }\n" + "}\n", "NonStaticInner.java", null) });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("X$NonStaticInner").getBinType());
    mover.setTargetPackage(before.getPackageForName(""));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  /** This is a fix for a bug that appeared in MoveTypeAnalyzer */
  public void testCalledItemWillBeInaccessible_calledItemNotMoving()
      throws Exception {
    Project p = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package a;\n" + "public class X {\n"
                + "  public AnotherClass usage;\n" + "}\n" + "\n"
                + "class AnotherClass {}", "X.java", "a") });

    MoveType mover = new MoveType(new NullContext(p), p
        .getTypeRefForName("a.X").getBinType());
    mover.setTargetPackage(p.createPackageForName("b", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), !status.isOk());
  }

  public void testDeletingMultipleImportsOnTheSameLine() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n"
                + "import b.A;import b.B;\n" + // These imports must be deleted properly by MoveType
                "public class X{}", "X.java", "a"),
            new Utils.TempCompilationUnit("package b;public class A{}",
                "A.java", "b"),
            new Utils.TempCompilationUnit("package b;public class B{}",
                "B.java", "b") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "public class X{}", "X.java", "b"),
            new Utils.TempCompilationUnit("package b;public class A{}",
                "A.java", "b"),
            new Utils.TempCompilationUnit("package b;public class B{}",
                "B.java", "b") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.X").getBinType());
    mover.setTargetPackage(before.getPackageForName("b"));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testRenameEditorNotifiersOtherEditorsOnSameLine()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\npublic class Random{}",
                "Random.java", "a"),
            new Utils.TempCompilationUnit("package a;\n"
                + "import a.A; import target.B;\n" + // first import is renamed, second is removed
                "public class X{}", "X.java", "a"),
            new Utils.TempCompilationUnit("package a;\npublic class A{}",
                "A.java", "a"),
            new Utils.TempCompilationUnit("package target;\npublic class B{}",
                "B.java", "target"), });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\npublic class Random{}",
                "Random.java", "a"),
            new Utils.TempCompilationUnit("package target;\n"
                + "import target.A;\n" + "public class X{}", "X.java", "target"),
            new Utils.TempCompilationUnit("package target;\npublic class A{}",
                "A.java", "target"),
            new Utils.TempCompilationUnit("package target;\npublic class B{}",
                "B.java", "target"), });

    MoveType mover = new MoveType(new NullContext(before), new Object[] {
        before.getTypeRefForName("a.A").getBinType(),
        before.getTypeRefForName("a.X").getBinType() });
    mover.setTargetPackage(before.createPackageForName("target", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testRenameEditorNotifiersOtherEditorsOnSameLine_multipleRenameEditorsOnOneLine()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\npublic class Random{}",
                "Random.java", "a"),
            new Utils.TempCompilationUnit("package a;\n"
                + "import a.A; import a.A; import target.B;\n" + // first import is renamed, second is removed
                "public class X{}", "X.java", "a"),
            new Utils.TempCompilationUnit("package a;\npublic class A{}",
                "A.java", "a"),
            new Utils.TempCompilationUnit("package target;\npublic class B{}",
                "B.java", "target"), });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\npublic class Random{}",
                "Random.java", "a"),
            new Utils.TempCompilationUnit("package target;\n"
                + "import target.A; import target.A;\n" + "public class X{}",
                "X.java", "target"),
            new Utils.TempCompilationUnit("package target;\npublic class A{}",
                "A.java", "target"),
            new Utils.TempCompilationUnit("package target;\npublic class B{}",
                "B.java", "target"), });

    MoveType mover = new MoveType(new NullContext(before), new Object[] {
        before.getTypeRefForName("a.A").getBinType(),
        before.getTypeRefForName("a.X").getBinType() });
    mover.setTargetPackage(before.createPackageForName("target", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testNewlineBetweenTwoImportStatements() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package c;\n" + "\n" + // It's important to have 2 empty lines in here -- that confused the old code into inserting no new lines at all,
                "\n" + // which resulted in inserting multiple import statements on the same line.
                "import b.*;\n" + "public class X{ A aUsage; B bUsage; }",
                "X.java", "c"),
            new Utils.TempCompilationUnit("package b;\npublic class A{}",
                "A.java", "b"),
            new Utils.TempCompilationUnit("package b;\npublic class B{}",
                "B.java", "b"),
            new Utils.TempCompilationUnit("package b;\npublic class Random{}",
                "Random.java", "b") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package c;\n" + "\n"
                + "import a.A;\n" + "import a.B;\n" + "import b.*;\n" + // Would be better if this import statement was removed, but OK.
                "public class X{ A aUsage; B bUsage; }", "X.java", "c"),
            new Utils.TempCompilationUnit("package a;\npublic class A{}",
                "A.java", "a"),
            new Utils.TempCompilationUnit("package a;\npublic class B{}",
                "B.java", "a"),
            new Utils.TempCompilationUnit("package b;\npublic class Random{}",
                "Random.java", "b") });

    MoveType mover = new MoveType(new NullContext(before), new Object[] {
        before.getTypeRefForName("b.A").getBinType(),
        before.getTypeRefForName("b.B").getBinType() });
    mover.setTargetPackage(before.createPackageForName("a", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testMovingMultipleTypesWithSameName() throws Exception {
    Project p = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n public class X{}",
                "X.java", "a"),
            new Utils.TempCompilationUnit("package b;\n public class X{}",
                "X.java", "b"), });

    MoveType mover = new MoveType(new NullContext(p), new Object[] {
        p.getTypeRefForName("a.X").getBinType(),
        p.getTypeRefForName("b.X").getBinType() });
    mover.setTargetPackage(p.getPackageForName(""));
    final RefactoringStatus status = mover.checkPreconditions();
    assertTrue(status.getAllMessages(), !status.isOk());
  }

  public void testMemberVisibilityEditorNotifiesOtherEditors() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "  private static class X{a.Y.Y usage;}\n" + "}", "X.java",
                "a"),
            new Utils.TempCompilationUnit("package a;\n" + "public class Y {\n"
                + "  private class Y{}\n" + "}", "Y.java", "a"),
            new Utils.TempCompilationUnit("package a;\n public class Random{}",
                "Random.java", "a"), });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "}", "X.java", "a"),
            new Utils.TempCompilationUnit("package a;\n" + "public class Y {\n"
                + "}", "Y.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n" + "\n"
                + "public class X{b.Y usage;}\n", "X.java", "b"),
            new Utils.TempCompilationUnit("package b;\n" + "\n" + "\n"
                + "public class Y{}\n", "Y.java", "b"),
            new Utils.TempCompilationUnit("package a;\n public class Random{}",
                "Random.java", "a"), });

    MoveType mover = new MoveType(new NullContext(before), new Object[] {
        before.getTypeRefForName("a.X$X").getBinType(),
        before.getTypeRefForName("a.Y$Y").getBinType() });
    mover.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testMemberVisibilityEditorNotifiesOtherEditors_defaultVisibility()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "  static class X{a.Y.Y usage;}\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit("package a;\n" + "public class Y {\n"
                + "  class Y{}\n" + "}", "Y.java", "a"),
            new Utils.TempCompilationUnit("package a;\n public class Random{}",
                "Random.java", "a"), });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "}", "X.java", "a"),
            new Utils.TempCompilationUnit("package a;\n" + "public class Y {\n"
                + "}", "Y.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n" + "\n"
                + "public class X{b.Y usage;}\n", "X.java", "b"),
            new Utils.TempCompilationUnit("package b;\n" + "\n" + "\n"
                + "public class Y{}\n", "Y.java", "b"),
            new Utils.TempCompilationUnit("package a;\n public class Random{}",
                "Random.java", "a"), });

    MoveType mover = new MoveType(new NullContext(before), new Object[] {
        before.getTypeRefForName("a.X$X").getBinType(),
        before.getTypeRefForName("a.Y$Y").getBinType() });
    mover.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testImportingInnerClassesOnDemand_asteriskMustNotBeCountedAsPartOfTypeName()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n"
                + "import a.Container.*;\n" + "public class X {\n"
                + "  Inner usage;\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit("package a;\n"
                + "public class Container {\n" + "  class Inner{}\n" + "}",
                "Container.java", "a"),
            new Utils.TempCompilationUnit("package a;\n public class Random{}",
                "Random.java", "a"), });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b;\n"
                + "import b.Container.*;\n" + "public class X {\n"
                + "  Inner usage;\n" + "}", "X.java", "b"),
            new Utils.TempCompilationUnit("package b;\n"
                + "public class Container {\n" + "  class Inner{}\n" + "}",
                "Container.java", "b"),
            new Utils.TempCompilationUnit("package a;\n public class Random{}",
                "Random.java", "a"), });

    MoveType mover = new MoveType(new NullContext(before), new Object[] {
        before.getTypeRefForName("a.X").getBinType(),
        before.getTypeRefForName("a.Container").getBinType() });
    mover.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testImportingInnerClassesOnDemand_asteriskImportMustNotBeDeletedLater()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n"
                + "import a.Container.*;\n" + "public class X {\n"
                + "  Inner usage;\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit("package a;\n"
                + "public class Container {\n" + "  class Inner{}\n" + "}",
                "Container.java", "a"),
            new Utils.TempCompilationUnit(
                "package b;\n public class Moveable{}", "Moveable.java", "b"),
            new Utils.TempCompilationUnit("package a;\n public class Random{}",
                "Random.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n"
                + "import a.Container.*;\n" + "public class X {\n"
                + "  Inner usage;\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit("package a;\n"
                + "public class Container {\n" + "  class Inner{}\n" + "}",
                "Container.java", "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Moveable{}", "Moveable.java", "a"),
            new Utils.TempCompilationUnit("package a;\n public class Random{}",
                "Random.java", "a") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.Moveable").getBinType());
    mover.setTargetPackage(before.getPackageForName("a"));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testImportingInnerClassesOnDemand_outerTypeNeedsImportingToo()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n"
                + "import a.Container.*;\n" + "public class X {\n"
                + "  Inner usage;\n" + "  Container outerUsage;\n" + "}",
                "X.java", "a"),
            new Utils.TempCompilationUnit("package a;\n"
                + "public class Container {\n" + "  public class Inner{}\n"
                + "}", "Container.java", "a"), });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "import b.Container;\n" + "import b.Container.*;\n"
                + "public class X {\n" + "  Inner usage;\n"
                + "  Container outerUsage;\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit("package b;\n"
                + "public class Container {\n" + "  public class Inner{}\n"
                + "}", "Container.java", "b") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.Container").getBinType());
    mover.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testLoosingAccessToUsedFieldOfMovingSuperType() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "  int usage = Y.superField;\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Y extends Movable {}\n", "Y.java",
                "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Movable { static int superField; }\n",
                "Movable.java", "a") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.Movable").getBinType());
    mover.setTargetPackage(before.createPackageForName("b", false));
    final RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isErrorOrFatal());
  }

  public void testLoosingAccessToUsedMethodOfMovingSuperType() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "  int usage = Y.method();\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Y extends Movable {}\n", "Y.java",
                "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Movable { static int method(){return 0;}; }\n",
                "Movable.java", "a") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.Movable").getBinType());
    mover.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isErrorOrFatal());
  }

  public void testLoosingAccessToUsedInnerOfMovingSuperType() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "  Object o = new Y.Inner();\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Y extends Movable {}\n", "Y.java",
                "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Movable { class Inner{}; }\n",
                "Movable.java", "a") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.Movable").getBinType());
    mover.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isErrorOrFatal());
  }

  public void testLoosingAccessToUsedInnerMemberOfMovingSuperType()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "  int i = Y.Inner.x;\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Y extends Movable {}\n", "Y.java",
                "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Movable { public static class Inner{int x;}; }\n",
                "Movable.java", "a") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.Movable").getBinType());
    mover.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isErrorOrFatal());
  }

  public void testLoosingAccessToUsedInnerOfMovingSuperType_BinTypeExpression()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "  Object o = Y.Inner.class;\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Y extends Movable {}\n", "Y.java",
                "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Movable { static class Inner{}; }\n",
                "Movable.java", "a") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.Movable").getBinType());
    mover.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isErrorOrFatal());
  }

  public void testLoosingAccessToUsedInnerOfMovingSuperType_BinCastExpression()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "  public void m(Object o) {\n" + "    (Y.Inner) o;\n"
                + "  }\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Y extends Movable {}\n", "Y.java",
                "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Movable { static class Inner{}; }\n",
                "Movable.java", "a") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.Movable").getBinType());
    mover.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isErrorOrFatal());
  }

  public void testLoosingAccessToUsedInnerOfMovingSuperType_throwsClause()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "  public void m() throws Y.Inner {}\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Y extends Movable {}\n", "Y.java",
                "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Movable { static class Inner extends Exception{}; }\n",
                "Movable.java", "a") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.Movable").getBinType());
    mover.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isErrorOrFatal());
  }

  public void testLoosingAccessToUsedInnerOfMovingSuperType_throwClause()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "  public void m() { throw new Y.Inner(); }\n" + "}",
                "X.java", "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Y extends Movable {}\n", "Y.java",
                "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Movable { static class Inner extends RuntimeException{}; }\n",
                "Movable.java", "a") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.Movable").getBinType());
    mover.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isErrorOrFatal());
  }

  public void testLoosingAccessToUsedInnerOfMovingSuperType_catchClause()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "package a;\n"
                    + "public class X {\n"
                    + "  public void m() { try{System.exit(-1);} catch( Y.Inner e ) {} }\n"
                    + "}", "X.java", "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Y extends Movable {}\n", "Y.java",
                "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Movable { static class Inner extends RuntimeException{}; }\n",
                "Movable.java", "a") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.Movable").getBinType());
    mover.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isErrorOrFatal());
  }

  public void testLoosingAccessToUsedInnerOfMovingSuperType_methodParameter()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "  public void m( Y.Inner param ) {}\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Y extends Movable {}\n", "Y.java",
                "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Movable { static class Inner extends RuntimeException{}; }\n",
                "Movable.java", "a") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.Movable").getBinType());
    mover.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isErrorOrFatal());
  }

  public void testLoosingAccessToUsedInnerOfMovingSuperType_extendsClause()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n"
                + "public class X extends Y.Inner{}\n", "X.java", "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Y extends Movable {}\n", "Y.java",
                "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Movable { static class Inner extends RuntimeException{}; }\n",
                "Movable.java", "a") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.Movable").getBinType());
    mover.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isErrorOrFatal());
  }

  public void testLoosingAccessToUsedInnerOfMovingSuperType_implementsClause()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n"
                + "public class X implements Y.Inner{}\n", "X.java", "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Y extends Movable {}\n", "Y.java",
                "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Movable { interface Inner {} }\n",
                "Movable.java", "a") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.Movable").getBinType());
    mover.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isErrorOrFatal());
  }

  public void testLoosingAccessToUsedInnerOfMovingSuperType_arrayOfInner()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n"
                + "public class X { Y.Inner[] x; }\n", "X.java", "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Y extends Movable {}\n", "Y.java",
                "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Movable { interface Inner {} }\n",
                "Movable.java", "a") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.Movable").getBinType());
    mover.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isErrorOrFatal());
  }

  public void testLoosingAccessToUsedInnerOfMovingSuperType_arrayCreation()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n"
                + "public class X { Object o = new Y.Inner[0]; }\n", "X.java",
                "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Y extends Movable {}\n", "Y.java",
                "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Movable { interface Inner{} }\n",
                "Movable.java", "a") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.Movable").getBinType());
    mover.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isErrorOrFatal());
  }

  /** This tests the case of "protected" access modifier */
  public void testLoosingAccessToUsedInnerOfMovingSuperType_usageInSubclass()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package j;\n"
                + "public class X extends a.Y { int j = i; }\n", "X.java", "j"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Y extends Movable {}\n", "Y.java",
                "a"),
            new Utils.TempCompilationUnit(
                "package a;\n public class Movable { protected int i; }\n",
                "Movable.java", "a") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.Movable").getBinType());
    mover.setTargetPackage(before.createPackageForName("b", false));
    final RefactoringStatus status = mover.checkUserInput();
    assertTrue(status.getAllMessages(), status.isOk() || status.isInfo());
  }

  public void testImportingImplementedInterfacesFromSourcePackage()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "package a;\npublic class X extends java.util.ArrayList implements I{}",
                "X.java", "a"),
            new Utils.TempCompilationUnit(
                "package a;\npublic interface I {}\n", "I.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "import a.I;\n" + "\n" + "\n"
                + "public class X extends java.util.ArrayList implements I{}",
                "X.java", "b"),
            new Utils.TempCompilationUnit(
                "package a;\npublic interface I {}\n", "I.java", "a") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.X").getBinType());
    mover.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testFqnUsageOfMembers_field() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "  int i = a.Y.field;\n" + // FQN member usage
                "}", "X.java", "a"),
            new Utils.TempCompilationUnit("package a;\n" + "public class Y {\n"
                + "  public static int field;\n" + "}", "Y.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "  int i = b.Y.field;\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "public class Y {\n"
                + "  public static int field;\n" + "}", "Y.java", "b") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.Y").getBinType());
    mover.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testFqnUsageOfMembers_method() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "  int i = a.Y.method();\n" + // FQN member usage
                "}", "X.java", "a"),
            new Utils.TempCompilationUnit("package a;\n" + "public class Y {\n"
                + "  public static int method() {return 0;}\n" + "}", "Y.java",
                "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "  int i = b.Y.method();\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "public class Y {\n"
                + "  public static int method() {return 0;}\n" + "}", "Y.java",
                "b") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.Y").getBinType());
    mover.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testFqnUsageOfMembers_method_inherited() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "  int i = a.Y.method();\n" + // FQN member usage
                "}", "X.java", "a"),
            new Utils.TempCompilationUnit("package a;\n"
                + "public class Y extends Super {}\n", "Y.java", "a"),
            new Utils.TempCompilationUnit("package a;\n"
                + "public class Super {\n"
                + "  public static int method() {return 0;}\n" + "}",
                "Super.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "  int i = b.Y.method();\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "import a.Super;\n" + "\n" + "\n"
                + "public class Y extends Super {}\n", "Y.java", "b"),
            new Utils.TempCompilationUnit("package a;\n"
                + "public class Super {\n"
                + "  public static int method() {return 0;}\n" + "}",
                "Super.java", "a") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.Y").getBinType());
    mover.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testFqnUsageOfMembers_innerClass() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "  Object o = new a.Y.Inner();\n" + // FQN member usage
                "}", "X.java", "a"),
            new Utils.TempCompilationUnit("package a;\n" + "public class Y{\n"
                + "  public static class Inner{}\n" + "}", "Y.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "  Object o = new b.Y.Inner();\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit("package b;\n" + "public class Y{\n"
                + "  public static class Inner{}\n" + "}", "Y.java", "b") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.Y").getBinType());
    mover.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testFqnUsageOfMembers_innerClassMember() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "  int i = a.Y.Inner.i;\n" + // FQN member usage
                "}", "X.java", "a"),
            new Utils.TempCompilationUnit(
                "package a;\n" + "public class Y{\n"
                    + "  public static class Inner{ public static int i; }\n"
                    + "}", "Y.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class X {\n"
                + "  int i = b.Y.Inner.i;\n" + "}", "X.java", "a"),
            new Utils.TempCompilationUnit(
                "package b;\n" + "public class Y{\n"
                    + "  public static class Inner{ public static int i; }\n"
                    + "}", "Y.java", "b") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.Y").getBinType());
    mover.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testNotGeneratingFqnImportsForMovedTypesInDestinationPackageIfTheTypesWerePreviouslyImportedOnDemand()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n"
                + "public class X {}\n", "X.java", "a"),
            new Utils.TempCompilationUnit("package a;\n public class Random{}",
                "Random.java", "a"),
            new Utils.TempCompilationUnit("package target;\n" + "import a.*;\n"
                + "public class T {\n" + "  X usage;\n" + "}", "T.java",
                "target") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n public class Random{}",
                "Random.java", "a"),
            new Utils.TempCompilationUnit("package target;\n"
                + "public class X {}\n", "X.java", "target"),
            new Utils.TempCompilationUnit("package target;\n" + "import a.*;\n"
                + "public class T {\n" + "  X usage;\n" + "}", "T.java",
                "target") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.X").getBinType());
    mover.setTargetPackage(before.getPackageForName("target"));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testNotGeneratingFqnImportsForMovedTypesIfTheTypesWillBeImportedOnDemand()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n"
                + "public class X {}\n", "X.java", "a"),
            new Utils.TempCompilationUnit("package a;\n" + "import target.*;\n"
                + "public class T {\n" + "  X usage;\n" + "}", "T.java", "a"),
            new Utils.TempCompilationUnit(
                "package target;\n public class Random{}", "Random.java",
                "target") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package target;\n"
                + "public class X {}\n", "X.java", "target"),
            new Utils.TempCompilationUnit("package a;\n" + "import target.*;\n"
                + "public class T {\n" + "  X usage;\n" + "}", "T.java", "a"),
            new Utils.TempCompilationUnit(
                "package target;\n public class Random{}", "Random.java",
                "target") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.X").getBinType());
    mover.setTargetPackage(before.getPackageForName("target"));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testUsingProtectedMembersInSubclassesDoesNotCauseConflictsIfSuperclassIsMoved()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n"
                + "public class X extends Super{{ System.out.println(x); }}\n",
                "X.java", "a"),
            new Utils.TempCompilationUnit("package a;\n"
                + "public class Super {\n" + "  protected int x;\n" + "}",
                "Super.java", "a") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.Super").getBinType());
    mover.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk() || status.isInfo());
  }

  public void testRelocationOfTwoClassesInSameFile() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "public class A{}\n"
                + "class B{}\n", "X.java", "a"),
            new Utils.TempCompilationUnit("package a;public class Random{}",
                "Random.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b;\n" + "public class A{}\n"
                + "class B{}\n", "X.java", "b"),
            new Utils.TempCompilationUnit("package a;public class Random{}",
                "Random.java", "a") });

    MoveType mover = new MoveType(new NullContext(before), new Object[] {
        before.getTypeRefForName("a.A").getBinType(),
        before.getTypeRefForName("a.B").getBinType() });
    mover.setTargetPackage(before.createPackageForName("b", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testImportingArraysOnExtract() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package a;\n" + "import java.util.List;\n" + "public class X{\n"
                + "  public static class Inner{ Object o = new List[0]; }\n"
                + "}", "X.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n"
                + "import java.util.List;\n" + "public class X{\n" + "}",
                "X.java", "a"),
            new Utils.TempCompilationUnit("package a;\n" + "\n"
                + "import java.util.List;\n" + "\n" + "\n"
                + "public class Inner{ Object o = new List[0]; }\n",
                "Inner.java", "a") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.X$Inner").getBinType());
    mover.setTargetPackage(before.createPackageForName("a", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testImportingAnonymousClassSupers_extract() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package a;\n"
                + "import java.util.ArrayList;\n"
                + "public class X{\n"
                + "  public static class Inner{ Object o = new ArrayList() { public String toString() { return null; } }; }\n"
                + "}", "X.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n"
                + "import java.util.ArrayList;\n" + "public class X{\n" + "}",
                "X.java", "a"),
            new Utils.TempCompilationUnit(
                "package a;\n"
                    + "\n"
                    + "import java.util.ArrayList;\n"
                    + "\n"
                    + "\n"
                    + "public class Inner{ Object o = new ArrayList() { public String toString() { return null; } }; }\n",
                "Inner.java", "a") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.X$Inner").getBinType());
    mover.setTargetPackage(before.createPackageForName("a", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testImportingAnonymousClassSupers_extract_2() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package a;\n"
                + "import java.util.*;\n"
                + "public class X{\n"
                + "  public static class Inner{\n"
                + "    public void m() {\n"
                + "      Collections.sort( new ArrayList(), new Comparator() {\n"
                + "        public int compare(Object a, Object b) { return 0; } } );\n"
                + "    }\n" + "  }\n" + "}", "X.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n"
                + "import java.util.*;\n" + "public class X{\n" + "}",
                "X.java", "a"),
            new Utils.TempCompilationUnit(
                "package a;\n"
                    + "\n"
                    + "import java.util.ArrayList;\n"
                    + "import java.util.Collections;\n"
                    + "import java.util.Comparator;\n"
                    + "\n"
                    + "\n"
                    + "public class Inner{\n"
                    + "  public void m() {\n"
                    + "    Collections.sort( new ArrayList(), new Comparator() {\n"
                    + "      public int compare(Object a, Object b) { return 0; } } );\n"
                    + "  }\n" + "}\n", "Inner.java", "a") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.X$Inner").getBinType());
    mover.setTargetPackage(before.createPackageForName("a", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testImportingAnonymousClassSupers_extract_2_class()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package a;\n"
                + "import java.util.*;\n"
                + "public class X{\n"
                + "  public static class Inner{\n"
                + "    public void m() {\n"
                + "      Collections.sort( new ArrayList() {\n"
                + "        public int compare(Object a, Object b) { return 0; } } );\n"
                + "    }\n" + "  }\n" + "}", "X.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n"
                + "import java.util.*;\n" + "public class X{\n" + "}",
                "X.java", "a"),
            new Utils.TempCompilationUnit(
                "package a;\n"
                    + "\n"
                    + "import java.util.ArrayList;\n"
                    + "import java.util.Collections;\n"
                    + "\n"
                    + "\n"
                    + "public class Inner{\n"
                    + "  public void m() {\n"
                    + "    Collections.sort( new ArrayList() {\n"
                    + "      public int compare(Object a, Object b) { return 0; } } );\n"
                    + "  }\n" + "}\n", "Inner.java", "a") });

    MoveType mover = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.X$Inner").getBinType());
    mover.setTargetPackage(before.createPackageForName("a", false));
    RefactoringStatus status =
      mover.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  //---------------------------------------- Tests for changing access modifiers of members --------------

  /**
   * This test (and some others here) also tests adding newlines after package statements in those files
   * that are not moved, but have just import statements inserted in them.
   */
  public void testModifierChange_accessConflictWithProtectedMembers()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b; public class X {\n"
                + "  protected X() {}\n" + "}", "X.java", "b"),
            new Utils.TempCompilationUnit("package b; public class Y {\n"
                + "  X x = new X();\n" + // Protected constructor access -- makes constructor public
                "}", "Y.java", "b") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package c; public class X {\n"
                + "  public X() {}\n" + "}", "X.java", "c"),
            new Utils.TempCompilationUnit("package b; \n" + "\n"
                + "import c.X;\n" + "\n" + "\n" + "public class Y {\n"
                + "  X x = new X();\n" + "}", "Y.java", "b") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.X").getBinType(), true);
    moveType.setTargetPackage(before.createPackageForName("c", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testModifierChange_accessConflictWithProtectedMembers_protectedNeeded()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b; public class X {\n"
                + "  X() {}\n" + "}", "X.java", "b"),
            new Utils.TempCompilationUnit(
                "package b; public class Y extends X {\n"
                    + "  X x = new X();\n" + "}", "Y.java", "b") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package c; public class X {\n"
                + "  protected X() {}\n" + "}", "X.java", "c"),
            new Utils.TempCompilationUnit("package b; \n" + "\n"
                + "import c.X;\n" + "\n" + "\n"
                + "public class Y extends X {\n" + "  X x = new X();\n" + "}",
                "Y.java", "b") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.X").getBinType(), true);
    moveType.setTargetPackage(before.createPackageForName("c", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk() || status.isInfo());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testModifierChange_usesOtherPackageAccessMembers()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b; public class X {\n"
                + "  int x = Y.field;\n" + // package private field access
                "}", "X.java", "b"),
            new Utils.TempCompilationUnit("package b; public class Y {\n"
                + "  static int field;\n" + "}", "Y.java", "b") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package c; \n" + "\n"
                + "import b.Y;\n" + "\n" + "\n" + "public class X {\n"
                + "  int x = Y.field;\n" + "}", "X.java", "c"),
            new Utils.TempCompilationUnit("package b; public class Y {\n"
                + "  public static int field;\n" + "}", "Y.java", "b") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.X").getBinType(), true);
    moveType.setTargetPackage(before.createPackageForName("c", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testModifierChange_usesOtherPackageAccessMembers_protectedNeeded()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "package b; public class X extends Y {\n"
                    + "  int usage = Y.field;\n" + "}", "X.java", "b"),
            new Utils.TempCompilationUnit("package b; public class Y {\n"
                + "  static int field;\n" + "}", "Y.java", "b") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package c; \n" + "\n"
                + "import b.Y;\n" + "\n" + "\n"
                + "public class X extends Y {\n" + "  int usage = Y.field;\n"
                + "}", "X.java", "c"),
            new Utils.TempCompilationUnit("package b; public class Y {\n"
                + "  protected static int field;\n" + "}", "Y.java", "b") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.X").getBinType(), true);
    moveType.setTargetPackage(before.createPackageForName("c", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk() || status.isInfo());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testModifierChange_privateMembersInToplevelEnclosingClass_usedByMovedType()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package b; public class X {\n" + "  private static int field;\n"
                + "  public class Inner{ int usage = field; }\n" + "}",
            "X.java", "b") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b; public class X {\n"
                + "  public static int field;\n" + "}", "X.java", "b"),
            new Utils.TempCompilationUnit("package c;\n" + "\n"
                + "import b.X;\n" + "\n" + "\n"
                + "public class Inner{ int usage = X.field; }\n", "Inner.java",
                "c") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.X$Inner").getBinType(), true);
    moveType.setTargetPackage(before.createPackageForName("c", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testModifierChange_privateMembersInToplevelEnclosingClass_usedByMovedType_samePackage()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package b; public class X {\n" + "  private static int field;\n"
                + "  public class Inner{ int usage = field; }\n" + "}",
            "X.java", "b") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b; public class X {\n"
                + "  static int field;\n" + "}", "X.java", "b"),
            new Utils.TempCompilationUnit("package b;\n" + "\n" + "\n"
                + "public class Inner{ int usage = X.field; }\n", "Inner.java",
                "b") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.X$Inner").getBinType(), true);
    moveType.setTargetPackage(before.getPackageForName("b"));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testModifierChange_privateMembersInToplevelEnclosingClass_usedByMovedType_needProtectedAccess()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package b; public class X {\n" + "  private static int field;\n"
                + "  public class Inner extends X{ int usage = field; }\n"
                + "}", "X.java", "b") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b; public class X {\n"
                + "  protected static int field;\n" + "}", "X.java", "b"),
            new Utils.TempCompilationUnit("package c;\n" + "\n"
                + "import b.X;\n" + "\n" + "\n"
                + "public class Inner extends X{ int usage = X.field; }\n",
                "Inner.java", "c") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.X$Inner").getBinType(), true);
    moveType.setTargetPackage(before.createPackageForName("c", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testModifierChange_privateMembersInToplevelEnclosingClass_usedByEnclosingClass()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package b; public class X {\n"
                + "  private int usage = Inner.field;\n"
                + "  public class Inner{ private static int field; }\n" + "}",
            "X.java", "b") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b; \n" + "\n"
                + "import c.Inner;\n" + "\n" + "\n" + "public class X {\n"
                + "  private int usage = Inner.field;\n" + "}", "X.java", "b"),
            new Utils.TempCompilationUnit("package c;\n" + "\n" + "\n"
                + "public class Inner{ public static int field; }\n",
                "Inner.java", "c") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.X$Inner").getBinType(), true);
    moveType.setTargetPackage(before.createPackageForName("c", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  /**
   * Also tests newline generation for extracted classes in case of no generated imports
   * (makes sure that if the source contains UNIX-style newlines then all of the extracted
   * source must not use Windows-style newlines at all).
   */
  public void testModifierChange_privateMembersInToplevelEnclosingClass_usedByEnclosingClass_samePackage()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package b; public class X {\n"
                + "  private int usage = Inner.field;\n"
                + "  public class Inner{ private static int field; }\n" + "}",
            "X.java", "b") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b; public class X {\n"
                + "  private int usage = Inner.field;\n" + "}", "X.java", "b"),
            new Utils.TempCompilationUnit("package b;\n" + "\n" + "\n"
                + "public class Inner{ static int field; }\n", "Inner.java",
                "b") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.X$Inner").getBinType(), true);
    moveType.setTargetPackage(before.getPackageForName("b"));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testModifierChange_privateMembersInToplevelEnclosingClass_usedByEnclosingClass_alreadyPackagePrivate()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package b; public class X {\n"
                + "  private int usage = Inner.field;\n"
                + "  public class Inner{ static int field; }\n" + "}",
            "X.java", "b") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b; \n" + "\n"
                + "import c.Inner;\n" + "\n" + "\n" + "public class X {\n"
                + "  private int usage = Inner.field;\n" + "}", "X.java", "b"),
            new Utils.TempCompilationUnit("package c;\n" + "\n" + "\n"
                + "public class Inner{ public static int field; }\n",
                "Inner.java", "c") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.X$Inner").getBinType(), true);
    moveType.setTargetPackage(before.createPackageForName("c", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testModifierChange_privateMembersInToplevelEnclosingClass_usedByEnclosingClass_alreadyPackagePrivate_samePackage()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package b; public class X {\n"
                + "  private int usage = Inner.field;\n"
                + "  public class Inner{ static int field; }\n" + "}",
            "X.java", "b") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b; public class X {\n"
                + "  private int usage = Inner.field;\n" + "}", "X.java", "b"),
            new Utils.TempCompilationUnit("package b;\n" + "\n" + "\n"
                + "public class Inner{ static int field; }\n", "Inner.java",
                "b") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.X$Inner").getBinType(), true);
    moveType.setTargetPackage(before.getPackageForName("b"));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testModifierChange_packageAccessClassTurnsToPublic()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b;\n" + "class X {}", // Package private, turning to public. That causesd an NPE in BinMember...
                "X.java", "b" // ...because the class has absolutely no modifier nodes.
            ),
            new Utils.TempCompilationUnit("package b;\n"
                + "class User { X x = new X(); }", "User.java", "b") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package c;\n" + "public class X {}",
                "X.java", "c"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "import c.X;\n" + "\n" + "\n"
                + "class User { X x = new X(); }", "User.java", "b") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.X").getBinType(), true);
    moveType.setTargetPackage(before.createPackageForName("c", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk() || status.isInfo());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testModifierChange_notMovedPackageAccessClassTurnsToPublic()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b;\n"
                + "class X { public void m(Object o){ Y y = (Y) o; } }",
                "X.java", "b"),
            new Utils.TempCompilationUnit("package b;\n"
                + "protected interface Y {}", "Y.java", "b") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package c;\n" + "\n"
                + "import b.Y;\n" + "\n" + "\n"
                + "class X { public void m(Object o){ Y y = (Y) o; } }",
                "X.java", "c"),
            new Utils.TempCompilationUnit("package b;\n"
                + "public  interface Y {}", // HACK: extra space added to make tests pass
                "Y.java", "b") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.X").getBinType(), true);
    moveType.setTargetPackage(before.createPackageForName("c", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk() || status.isInfo());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testModifierChange_packageAccessClassTurnsToPublic_usedAsArray()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b;\n" + "class X {}",
                "X.java", "b"),
            new Utils.TempCompilationUnit("package b;\n"
                + "class User { Object o = new X[0]; }", "User.java", "b") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package c;\n" + "public class X {}",
                "X.java", "c"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "import c.X;\n" + "\n" + "\n"
                + "class User { Object o = new X[0]; }", "User.java", "b") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.X").getBinType(), true);
    moveType.setTargetPackage(before.createPackageForName("c", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk() || status.isInfo());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testModifierChange_packageAccessClassTurnsToPublic_usedAsArray_2()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b;\n" + "class X {}",
                "X.java", "b"),
            new Utils.TempCompilationUnit("package b;\n"
                + "class User { X[] usage; }", "User.java", "b") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package c;\n" + "public class X {}",
                "X.java", "c"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "import c.X;\n" + "\n" + "\n" + "class User { X[] usage; }",
                "User.java", "b") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.X").getBinType(), true);
    moveType.setTargetPackage(before.createPackageForName("c", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk() || status.isInfo());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testModifierChange_packageAccessClassTurnsToPublic_usedAsArray_3()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b;\n" + "class X {}",
                "X.java", "b"),
            new Utils.TempCompilationUnit("package b;\n"
                + "class User { public void m(X[] usage) {} }", "User.java",
                "b") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package c;\n" + "public class X {}",
                "X.java", "c"),
            new Utils.TempCompilationUnit("package b;\n" + "\n"
                + "import c.X;\n" + "\n" + "\n"
                + "class User { public void m(X[] usage) {} }", "User.java",
                "b") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.X").getBinType(), true);
    moveType.setTargetPackage(before.createPackageForName("c", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk() || status.isInfo());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testConflict_privateMembersInToplevelEnclosingClass_usedByEnclosingClass()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package b; public class X {\n"
                + "  private int x = Inner.field;\n"
                + "  public class Inner{ private static int field; }\n" + "}",
            "X.java", "b") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.X$Inner").getBinType(), false);
    moveType.setTargetPackage(before.createPackageForName("c", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isErrorOrFatal());
  }

  public void testNoConflict_extractedTypeUsesPrivateFieldInsideItsInnerClass()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package a;\n"
                + "public class X {\n"
                + "  public class Inner{\n"
                + "    int usage = InnerInner.field;\n"
                + "    public static class InnerInner{ private static int field; }\n"
                + "  }\n" + "}", "X.java", "a") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("a.X$Inner").getBinType(), false);
    moveType.setTargetPackage(before.createPackageForName("c", false));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());
  }

  //--------

  public void testDifferentFqnsReferingToSameType() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "package a;\n"
                    + "import b.Super;\n"
                    + "import b.Subclass;\n"
                    + "\n"
                    + "public class User { Super.Inner usage; Subclass.Inner sameInnerUsage; }",
                "User.java", "a"),
            new Utils.TempCompilationUnit("package b;\n"
                + "public class Super {\n" + "  public static class Inner{}\n"
                + "}", "Super.java", "b"),
            new Utils.TempCompilationUnit("package b;\n"
                + "public class Subclass extends Super{}", "Subclass.java", "b") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "package a;\n"
                    + "import b.Super;\n"
                    + "import b.Subclass;\n"
                    + "\n"
                    + "public class User { b.Inner usage; b.Inner sameInnerUsage; }",
                "User.java", "a"),
            new Utils.TempCompilationUnit("package b;\n"
                + "public class Super {\n" + "}", "Super.java", "b"),
            new Utils.TempCompilationUnit("package b;\n"
                + "public class Subclass extends Super{}", "Subclass.java", "b"),
            new Utils.TempCompilationUnit("package b;\n" + "\n" + "\n"
                + "public class Inner{}\n", "Inner.java", "b") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.Super$Inner").getBinType(), true);
    moveType.setTargetPackage(before.getPackageForName("b"));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk() || status.isInfo());

    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testConflictingReference_supertypesNotMentioned()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "package a;\n"
                    + "import c.*;\n"
                    + "public class User { public void m(X usage){ usage.toString(); } }", // the toString() on java.lang.Object should not be reported
                "User.java", "a" // as conflicting usage of "jaga.lang.Object"
            ),
            new Utils.TempCompilationUnit("package b;\n public class X{}",
                "X.java", "b"),
            new Utils.TempCompilationUnit("package c;\n public class X{}",
                "X.java", "c") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.X").getBinType(), true);
    moveType.setTargetPackage(before.getPackageForName("a"));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isErrorOrFatal());
    assertTrue(status.getAllMessages(), status.getAllMessages().indexOf(
        "Object") < 0);
    assertEquals(status.getAllMessages(), 1, status.getEntries().size());
  }

  public void testConflictingReference_supertypesNotMentioned_2()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(
                "package a;\n"
                    + "import c.*;\n"
                    + "public class User { public void m(X usage){ usage.superField; } }", // the superField usage should not generate a warning
                "User.java", "a" // that would say "conflicting reference of Super".
            ),
            new Utils.TempCompilationUnit(
                "package b;\n public class X extends d.Super{}", "X.java", "b"),
            new Utils.TempCompilationUnit(
                "package c;\n public class X extends d.Super{}", "X.java", "c"),
            new Utils.TempCompilationUnit(
                "package d;\n public class Super{ public int superField; }",
                "Super.java", "d") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.X").getBinType(), true);
    moveType.setTargetPackage(before.getPackageForName("a"));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isErrorOrFatal());
    assertTrue(status.getAllMessages(), status.getAllMessages()
        .indexOf("Super") < 0);
    assertEquals(status.getAllMessages(), 1, status.getEntries().size());
  }

  public void testConflictingReference_noConflictIfNameNotExplicitlyMentioned()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "import c.*;\n"
                + "public class User {\n"
                + "  public void user(){ getX().superField; }\n" + // no conflict here -- getX() gives a FQN reference.
                "  public c.X getX() { return new c.X(); }\n" + // FQN
                "}", "User.java", "a"),
            new Utils.TempCompilationUnit(
                "package b;\n public class X extends d.Super{}", "X.java", "b"),
            new Utils.TempCompilationUnit(
                "package c;\n public class X extends d.Super{}", "X.java", "c"),
            new Utils.TempCompilationUnit(
                "package d;\n public class Super{ public int superField; }",
                "Super.java", "d") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.X").getBinType(), true);
    moveType.setTargetPackage(before.getPackageForName("a"));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isOk());
  }

  public void testConflictingReference_conflictIfNameExplicitlyMentioned()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "import c.*;\n"
                + "public class User {\n"
                + "  public void user(){ getX().superField; }\n" + // no conflict here
                "  public X getX() { return new X(); }\n" + // 2 conflicts on this line (reason: not a FQN name)
                "}", "User.java", "a"),
            new Utils.TempCompilationUnit(
                "package b;\n public class X extends d.Super{}", "X.java", "b"),
            new Utils.TempCompilationUnit(
                "package c;\n public class X extends d.Super{}", "X.java", "c"),
            new Utils.TempCompilationUnit(
                "package d;\n public class Super{ public int superField; }",
                "Super.java", "d") });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.X").getBinType(), true);
    moveType.setTargetPackage(before.getPackageForName("a"));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isErrorOrFatal());
    assertEquals(status.getAllMessages(), 2, status.getEntries().size());
  }

  public void testConflictingReference_conflictIfNameExplicitlyMentioned_2()
      throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;\n" + "import c.*;\n"
                + "public class User {\n"
                + "  public void user(){ getX().m(); }\n" + // no conflict here
                "  public X getX() { return new X(); }\n" + // 2 conflicts on this line (reason: not a FQN name)
                "}", "User.java", "a"),
            new Utils.TempCompilationUnit(
                "package b;\n public class X{ public void m(){} }", "X.java",
                "b"),
            new Utils.TempCompilationUnit(
                "package c;\n public class X{ public void m(){} }", "X.java",
                "c"), });

    MoveType moveType = new MoveType(new NullContext(before), before
        .getTypeRefForName("b.X").getBinType(), true);
    moveType.setTargetPackage(before.getPackageForName("a"));
    RefactoringStatus status =
      moveType.apply();
    assertTrue(status.getAllMessages(), status.isErrorOrFatal());
    assertEquals(status.getAllMessages(), 2, status.getEntries().size());
  }

  public void testCannotMoveNonSourcepathItems() throws Exception {
    Project p = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package a; public class X{}", "X.java", "a") });

    MoveType moveType = new MoveType(new NullContext(p), p
        .getTypeRefForName("java.lang.System").getBinType());
    moveType.setTargetPackage(p.getPackageForName("a"));
    RefactoringStatus status =
      moveType.apply();

    assertTrue(status.getAllMessages(), status.isErrorOrFatal());
  }

  public void testNotDeletingRootFolderAfterAllFilesRemoved() throws Exception {
    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "public class X{}\r\n", "X.java", null) });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package a;\r\npublic class X{}\r\n", "X.java", "a") });

    MoveType moveType = new MoveType(new NullContext(before),
        before.getTypeRefForName("X").getBinCIType());
    moveType.setTargetPackage(before.createPackageForName("a"));
    RefactoringStatus status =
      moveType.apply();

    assertTrue(status.getAllMessages(), status.isOk());
    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testDeletingEmptyFolderAfterMovingFilesIfUserSaysYes()
      throws Exception {
    KeyStoringDialogManager dialogManager = new KeyStoringDialogManager() {
      public int showYesNoQuestion(
          IdeWindowContext context,
          String key, String message,
          int defaultSelectedButton
      ) {
        lastMessageKey = key;
        return DialogManager.YES_BUTTON;
      }
    };
    DialogManager.setInstance(dialogManager);

    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package a;public class X{}\r\n", "X.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package b;public class X{}\r\n", "X.java", "b") });

    MoveType moveType = new MoveType(new NullContext(before),
        before.getTypeRefForName("a.X").getBinCIType());
    moveType.setTargetPackage(before.createPackageForName("b"));
    RefactoringStatus status =
      moveType.apply();

    assertTrue(status.getAllMessages(), status.isOk());
    RwRefactoringTestUtils.assertSameSources("", after, before);
    assertTrue(
        "MoveType has to ask user about wheter do delete empty the folder",
        dialogManager.getLastMessageKey() != null);
  }

  public void testDeletingEmptyFolderAfterMovingFilesIfUserSaysYes_folderContainsClassFile()
      throws Exception {
    KeyStoringDialogManager dialogManager = new KeyStoringDialogManager() {
      public int showYesNoQuestion(
          IdeWindowContext context,
          String key, String message,
          int defaultSelectedButton
      ) {
        lastMessageKey = key;
        return DialogManager.YES_BUTTON;
      }
    };
    DialogManager.setInstance(dialogManager);

    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;public class X{}\r\n",
                "X.java", "a"),
            new Utils.TempCompilationUnit("someclassfilecontents", "X.class",
                "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package b;public class X{}\r\n", "X.java", "b") });

    MoveType moveType = new MoveType(new NullContext(before),
        before.getTypeRefForName("a.X").getBinCIType());
    moveType.setTargetPackage(before.createPackageForName("b"));
    RefactoringStatus status =
      moveType.apply();

    assertTrue(status.getAllMessages(), status.isOk());
    RwRefactoringTestUtils.assertSameSources("", after, before);
    assertTrue(
        "MoveType has to ask user about wheter do delete empty the folder",
        dialogManager.getLastMessageKey() != null);
  }

  public void testNotDeletingEmptyFolderAfterMovingFilesIfUserSaysNo()
      throws Exception {
    KeyStoringDialogManager dialogManager = new KeyStoringDialogManager() {
      public int showYesNoQuestion(
          IdeWindowContext context,
          String key, String message,
          int defaultSelectedButton
      ) {
        lastMessageKey = key;
        return DialogManager.NO_BUTTON;
      }
    };
    DialogManager.setInstance(dialogManager);

    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] { new Utils.TempCompilationUnit(
            "package a;public class X{}\r\n", "X.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b;public class X{}\r\n",
                "X.java", "b"), new Utils.TempSourceEmptyPackage("a") });

    MoveType moveType = new MoveType(new NullContext(before),
        before.getTypeRefForName("a.X").getBinCIType());
    moveType.setTargetPackage(before.createPackageForName("b"));
    RefactoringStatus status =
      moveType.apply();

    assertTrue(status.getAllMessages(), status.isOk());
    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testDoNotAskToDeleteANonEmptyFolder() throws Exception {
    KeyStoringDialogManager dialogManager = new KeyStoringDialogManager() {
      public int showYesNoQuestion(
          IdeWindowContext context,
          String key, String message,
          int defaultSelectedButton
      ) {
        lastMessageKey = key;
        return DialogManager.YES_BUTTON;
      }
    };
    DialogManager.setInstance(dialogManager);

    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;public class X{}\r\n",
                "X.java", "a"),
            new Utils.TempCompilationUnit("package a;public class Y{}\r\n",
                "Y.java", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;public class Y{}\r\n",
                "Y.java", "a"),
            new Utils.TempCompilationUnit("package b;public class X{}\r\n",
                "X.java", "b") });

    MoveType moveType = new MoveType(new NullContext(before),
        before.getTypeRefForName("a.X").getBinCIType());
    moveType.setTargetPackage(before.createPackageForName("b"));
    RefactoringStatus status =
      moveType.apply();

    assertTrue(status.getAllMessages(), status.isOk());
    RwRefactoringTestUtils.assertSameSources("", after, before);
    assertTrue(
        "MoveType must NOT ask user about wheter do delete the non-empty folder",
        dialogManager.getLastMessageKey() == null);
  }

  public void testDoNotAskToDeleteANonEmptyFolder_folderHasOnlyNonSourcepathChildren()
      throws Exception {
    KeyStoringDialogManager dialogManager = new KeyStoringDialogManager() {
      public int showYesNoQuestion(
          IdeWindowContext context,
          String key, String message,
          int defaultSelectedButton
      ) {
        lastMessageKey = key;
        return DialogManager.YES_BUTTON;
      }
    };
    DialogManager.setInstance(dialogManager);

    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;public class X{}\r\n",
                "X.java", "a"),
            new Utils.TempCompilationUnit("some file", "somefile.txt", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("some file", "somefile.txt", "a"),
            new Utils.TempCompilationUnit("package b;public class X{}\r\n",
                "X.java", "b") });

    MoveType moveType = new MoveType(new NullContext(before),
        before.getTypeRefForName("a.X").getBinCIType());
    moveType.setTargetPackage(before.createPackageForName("b"));
    RefactoringStatus status =
      moveType.apply();

    assertTrue(status.getAllMessages(), status.isOk());
    RwRefactoringTestUtils.assertSameSources("", after, before);
    assertTrue(
        "MoveType must NOT ask user about wheter do delete the non-empty folder",
        dialogManager.getLastMessageKey() == null);
  }

  public void testDoNotAskToDeleteANonEmptyFolder_folderHasOnlyHiddenChildren()
      throws Exception {
    KeyStoringDialogManager dialogManager = new KeyStoringDialogManager() {
      public int showYesNoQuestion(
          IdeWindowContext context,
          String key, String message,
          int defaultSelectedButton
      ) {
        lastMessageKey = key;
        return DialogManager.YES_BUTTON;
      }
    };
    DialogManager.setInstance(dialogManager);

    Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;public class X{}\r\n",
                "X.java", "a"),
            new Utils.TempCompilationUnit(".hiddenFile", ".hiddenFile", "a") });

    Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit(".hiddenFile", ".hiddenFile", "a"),
            new Utils.TempCompilationUnit("package b;public class X{}\r\n",
                "X.java", "b") });

    MoveType moveType = new MoveType(new NullContext(before),
        before.getTypeRefForName("a.X").getBinCIType());
    moveType.setTargetPackage(before.createPackageForName("b"));
    RefactoringStatus status =
      moveType.apply();

    assertTrue(status.getAllMessages(), status.isOk());
    RwRefactoringTestUtils.assertSameSources("", after, before);
    assertTrue(
        "MoveType must NOT ask user about wheter do delete the non-empty folder",
        dialogManager.getLastMessageKey() == null);
  }

  public void testMovingBakFiles() throws Exception {
    final Project before = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package a;public class X{}\r\n",
                "X.java", "a"),
            new Utils.TempCompilationUnit("package a;public class X{}\r\n",
                "X.bak", "a") });

    final Project after = Utils
        .createTestRbProjectFromString(new Utils.TempCompilationUnit[] {
            new Utils.TempCompilationUnit("package b;public class X{}\r\n",
                "X.java", "b"),
            new Utils.TempCompilationUnit("package a;public class X{}\r\n",
                "X.bak", "b") });

    MoveType
        .setFilesToMoveWithJavaCompilationUnits(new MoveType.FilesToMoveWithJavaCompilationUnits() {

          public Source[] getFilesToMoveWith(Source javaCompilationUnit) {
            Source root = before.getPaths().getSourcePath().getRootSources()[0];
            Source result = root.getChild("a").getChild("X.bak");
            return new Source[] { result };
          }
        });

    MoveType moveType = new MoveType(new NullContext(before),
        before.getTypeRefForName("a.X").getBinCIType());
    moveType.setTargetPackage(before.createPackageForName("b"));
    RefactoringStatus status =
      moveType.apply();

    assertTrue(status.getAllMessages(), status.isOk());
    RwRefactoringTestUtils.assertSameSources("", after, before);
  }

  public void testExtractMainTypeButWrongDir() throws Exception {
    runTest("MoveType_extract_main_wrong_dir", "a.Test", "c");
  }

  public void testExtractWrongSourcepathRoot() throws Exception {
    runTest("MoveType_extract_wrong_sourcepath_root", "com.X", "com.test");
  }

  public void testStaticImports() throws Exception {
    runTest("MoveType_StaticImports", "p1.A", "p2");
  }

  public void testIssue251() throws Exception {
    runTest("MoveType_Issue251", "p1.X$Inner", "p2");
  }

  public void testIssue245() throws Exception {
    runTest("MoveType_Issue245", "p1.A$Inner2", "p1");
  }

  public void testPathsInNonJava() throws Exception {
    runTest("MoveType_paths_in_non_java", "com.test.main.Test", "org.test.simple", true);
  }

  public void testExtractWithJavadoc() throws Exception {
    if (RefactorItConstants.runNotImplementedTests) {
      runTest("MoveType_extract_with_javadoc", "X;Y;Z", "a");
    }
    else {
      cat
          .info("testExtractWithJavadoc temporarily disabled - to be fixed together with MoveEditor");
    }
  }

  private void runTest(final String projectBaseName, final String classNames,
      final String packageName) throws Exception {
    runTest(projectBaseName, classNames, packageName, false);
  }

  private void runTest(final String projectBaseName, final String classNames,
      final String packageName, boolean changeInNonJava) throws Exception {
    cat.info(projectBaseName + "_expected.");

    final String projectName = projectBaseName + "_initial";

    Project project = loadMutableProject(projectName);

    StringTokenizer classes = new StringTokenizer(classNames, ";");

    BinPackage newPackage = project.createPackageForName(packageName);
    assertNotNull(newPackage);

    MoveType mover = new MoveType(new NullContext(project), null);
    mover.setTargetPackage(newPackage);
    mover.setChangeInNonJavaFiles(changeInNonJava);
    List types = new ArrayList();

    while (classes.hasMoreTokens()) {
      final String className = classes.nextToken();

      BinCIType testType = project.getTypeRefForName(className)
          .getBinCIType();

      types.add(testType);
    }
    mover.setTypes(types);

    RefactoringStatus status = mover.apply();

    assertTrue(projectBaseName + "_expected: " + status.getAllMessages(),
        status.isOk());

    Project expected = Utils.createTestRbProject(Utils.getTestProjects()
        .getProject(projectBaseName + "_expected"));

    RwRefactoringTestUtils.assertSameSources(projectBaseName + "_expected",
        expected, project);

    cat.info("SUCCESS");
  }

  /**
   * For debugging; a sample line that could be run in this method:
   * new MoveTypeTest("").testExtractWithJavadoc();
   */
  public static void main(String args[]) throws Exception {
    System.err.println("sleeping 5 sec -- attach debugger now");
    Thread.sleep(5000);

    DialogManager.setInstance(new NullDialogManager());

    new MoveTypeTest("").testExtractWithJavadoc();
  }
}
