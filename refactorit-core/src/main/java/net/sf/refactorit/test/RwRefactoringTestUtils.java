/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.ChainableRuntimeException;
import net.sf.refactorit.common.util.FileCopier;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.refactorings.AbstractRefactoring;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.utils.FileUtil;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePath;
import net.sf.refactorit.vfs.local.LocalClassPath;
import net.sf.refactorit.vfs.local.LocalSourcePath;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Utilities to be used in testing of refactorings which modify original
 * source files.
 * <p>
 * Testing:
 * <ol>
 *   <li>Load/create your test project {@link Project}</li>
 *   <li>Create a mutable copy of the project by calling
 *       {@link #createMutableProject(Project, boolean) createMutableProject}</li>
 *   <li>Make changes to the mutable copy, e.g. run Rename on some item</li>
 *   <li>Load/create another project that is what you expect modified project to
 *       be.
 *   <li>Compare sources of modified and expected projects using
 *       {@link #assertSameSources(String, Project, Project)
 *              assertSameSources}. For example,
 *       <code><pre>
 *assertSameSources(
 *  "After renaming Test.a to Test.b",
 *  expected,
 *  modified);
 *</pre></code>
 * </ol>
 *
 * @author Anton Safonov
 * @author Alexander Klyubin
 */
public class RwRefactoringTestUtils {
  static {
    Utils.setUpTestingEnvironment();
  }

  /**
   * Creates mutable copy of the project. Changes to the copy will not affect
   * the project in any way. Each call to this method returns a new project.
   * Changing mutable copy doesn't affect other mutable copies or projects.
   * <p>
   * There is no need to delete mutable copy, since it is temporary and will be
   * deleted by JVM or the OS.
   * </p>
   *
   * NOTE: source project must be a on LocalSourcePath; destination might not be
   * (unless the "copySource" attr is used).
   * @param project project to create mutable copy of.
   *
   * @return mutable copy. Never returns <code>null</code>.
   *
   * @throws NullPointerException if <code>project</code> is <code>null</code>.
   * @throws UnsupportedOperationException in case creating mutable
   *         project from the <code>project</code> is not supported.
   */
  public static Project createMutableProject(Project project) {
    if (project == null) {
      throw new NullPointerException("project is null");
    }

    IDEController controller=IDEController.getInstance();


    final SourcePath sourcePath = project.getPaths().getSourcePath();
    final Source[] rootSources = sourcePath.getRootSources();
    String projectName = project.getName() + "_mutable";
    if (rootSources.length == 0) {
      // No sources

      Project result = Utils.createNewProjectFrom(
          new TestProject(projectName,sourcePath, project.getPaths().getClassPath(), null));
      controller.setActiveProject(result);
      return result;

    }

    try {

      String mutablePath = "";
      Source mutableSource = null;
      final SourcePath newSourcePath;

      Source copySource = null;
      if (sourcePath instanceof LocalSourcePath) {
        copySource = ((LocalSourcePath) sourcePath).getCopySource();
      }

      if (copySource == null) {
        if (rootSources.length > 1) {
          // Two or more source root directories
          throw new UnsupportedOperationException(
              "Projects with several root"
              +
              " source directories not supported without specified \"copypath\":"
              + " Project = " + project);
        }
        final Source sourceRoot = rootSources[0];

        if (sourceRoot.isFile()) { // Single file
          mutableSource = TempFileCreator.getInstance().createRootFile();
        } else { // Directory
          mutableSource = TempFileCreator.getInstance().createRootDirectory();
        }

        FileUtil.copy(sourceRoot, mutableSource);
        newSourcePath = TempFileCreator.getInstance().createSourcePath(
            mutableSource);
      } else {
        if (!copySource.isDirectory()) {
          throw new UnsupportedOperationException(
              "\"copypath\" should be directory: " + copySource);
        }

        mutableSource = new LocalTempFileCreator().createRootDirectory(); // CopyPath only supported for local sources
        FileUtil.copy(copySource, mutableSource);

        for (int i = 0; i < rootSources.length; ++i) {
          final Source sourceRoot = rootSources[i];

          if (!(sourceRoot.getAbsolutePath() + File.separator)
              .startsWith(copySource.getAbsolutePath() + File.separator)) {
            throw new UnsupportedOperationException(
                "\"sourcepath\" should lie below \"copypath\": "
                + sourceRoot.getAbsolutePath() + " -- " + copySource.getAbsolutePath());
          }

          String newPath = StringUtil.replace(
              sourceRoot.getAbsolutePath(),
              copySource.getAbsolutePath(),
              mutableSource.getAbsolutePath());

          if (mutablePath.length() > 0) {
            mutablePath += File.pathSeparatorChar;
          }
          mutablePath += newPath;
        }

        newSourcePath = new LocalSourcePath(mutablePath);
      }

      return Utils.createNewProjectFrom(
          new TestProject(projectName, newSourcePath, project.getPaths().getClassPath(), null));

    } catch (IOException e) {
      throw new ChainableRuntimeException(
          "Failed to create mutable project from " + project, e);
    }
  }

  /**
   * Asserts that source files of two projects are same.
   *
   * @param message message to provide in case assertion fails.
   * @param expected expected project.
   * @param actual actual project.
   *
   * @throws NullPointerException if <code>expected</code> or
   *         <code>actual</code> project  is <code>null</code>.
   * @throws AssertionFailedError in case source files of two projects
   *         differ.
   * @throws UnsupportedOperationException in case comparing the two
   *         paricular projects not supported.
   */
  public static void assertSameSources(String message,
      Project expected,
      Project actual) {
    if (expected == null) {
      throw new NullPointerException("expected is null");
    }

    if (actual == null) {
      throw new NullPointerException("actual is null");
    }

    final Source[] expectedRootSources
        = expected.getPaths().getSourcePath().getRootSources();
    Source[] actualRootSources;
    final SourcePath actualSourcePath = actual.getPaths().getSourcePath();

    if (actualSourcePath instanceof LocalSourcePath &&
        ((LocalSourcePath) actualSourcePath).getCopySource() != null) {
      actualRootSources = new Source[] {((LocalSourcePath) actualSourcePath).
          getCopySource()};
    } else {
      actualRootSources = actualSourcePath.getRootSources();
    }

    final File[] expectedRoots = getFilesOrNulls(expectedRootSources);
    final File[] actualRoots = getFilesOrNulls(actualRootSources);

    compareWithDiff(message, expectedRoots, actualRoots);
  }

  private static File[] getFilesOrNulls(Source[] sources) {
    File[] result = new File[sources.length];
    for (int i = 0; i < sources.length; i++) {
      result[i] = sources[i].getFileOrNull();
    }

    return result;
  }

  public static void compareWithDiff(String message, File[] expectedRoots,
      File[] actualRoots) {
    if (expectedRoots.length == 0 || actualRoots.length == 0) {
      throw new UnsupportedOperationException("Projects has no source roots");
    } else if (expectedRoots.length != actualRoots.length) {
      throw new UnsupportedOperationException(
          "Projects must have have the same number of roots " +
          "(expected: " + Arrays.asList(expectedRoots) + ", actual: "
          + Arrays.asList(actualRoots) + ")");
    }

    for (int i = 0; i < expectedRoots.length; i++) {
      compareWithDiff(message, expectedRoots[i], actualRoots[i]);
    }
  }

  public static void compareWithDiff(String message, File expectedRoot,
      File actualRoot) throws AssertionFailedError {

    // Start diff on expected and actual projects' source roots.
    if (!expectedRoot.exists()) {
      throw new RuntimeException(expectedRoot.getAbsolutePath()
          + " does not exist");
    }
    if (!actualRoot.exists()) {
      throw new RuntimeException(actualRoot.getAbsolutePath()
          + " does not exist");
    }

    String diffExecutable = System.getProperty("diff.command");
    if (diffExecutable == null) {
      diffExecutable = "diff";
    }

    final String[] diffCommand =
        new String[] {
        diffExecutable,
        "-ru",
        "--binary",
        //"--strip-trailing-cr", // does not work on karkass
        //                     "-w", // don't check whitespace
        "-x", "CVS", // exclude CVS directories
        "-x", "*~", // exclude backup files (generated by NetBeans, for instance)
        "-x", ".nbattrs", // exclude files generated by NetBeans
        "-x", ".#*.java*", // exclude files generated by NetBeans CVS
        "-x", ".#*.txt*", // exclude files generated by NetBeans CVS

        expectedRoot.getPath(),
        actualRoot.getPath()};

    // Differences, if any,  that will be reported by diff
    final ByteArrayOutputStream differences = new ByteArrayOutputStream();
    try {
      final Process diff = Runtime.getRuntime().exec(diffCommand);
      try {
        // Read differences
        InputStream in = null;
        try {
          in = new BufferedInputStream(diff.getInputStream());
          FileCopier.pump(in, differences, 8192, false);
          differences.flush();
        } finally {
          if (in != null) {
            in.close();
            in = null;
          }
        }

        // Read differences
        InputStream in2 = null;
        try {
          in2 = new BufferedInputStream(diff.getErrorStream());
          FileCopier.pump(in2, differences, 8192, false);
          differences.flush();
        } finally {
          if (in2 != null) {
            in2.close();
            in2 = null;
          }
        }

        final int errorCode = diff.waitFor();

        if (errorCode == 0) {
          // No differences
          return;
        }
      } catch (InterruptedException e) {
        throw new ChainableRuntimeException(
            "Failed while waiting for diff to complete",
            e);
      }

      final String lineSeparator = System.getProperty("line.separator");

      throw new AssertionFailedError(
          "Sources of projects differ"
          + ((message == null) ? "" : (": " + message))
          + lineSeparator
          + "Differences between " + expectedRoot + " and " + actualRoot + ":"
          + lineSeparator
          + new String(differences.toByteArray()));
    } catch (IOException e) {
      throw new ChainableRuntimeException(
          "Failed to compare two source roots: " + expectedRoot + " and "
          + actualRoot,
          e);
    }
  }

  public static void assertRefactoring(AbstractRefactoring refactoring,
      Project before, Project after) {
    TransformationManager manager = new TransformationManager(refactoring);
    manager.add(refactoring.checkAndExecute());
    RefactoringStatus status = manager.performTransformations();

    Assert.assertTrue("refactoring status was not OK: " +
        status.getAllMessages(),
        status.isOk() || status.isInfoOrWarning());

    assertSameSources("performed rename", before, after);
  }

  public static Project getMutableProject(final String projectName) {
    Project tempProject = Utils.createTestRbProject(Utils.getTestProjects().
        getProject(projectName));
    Project project = RwRefactoringTestUtils.createMutableProject(tempProject);

    return project;
  }

  /**
   * Test driver for {@link RwRefactoringTestUtils}.
   */
  public static final class TestDriver extends TestCase {
    public TestDriver(String name) {
      super(name);
    }

    public static Test suite() {
      final TestSuite suite = new TestSuite(TestDriver.class);
      suite.setName("RwRefactoringTestUtils tests");
      return suite;
    }



    /**
     * Tests creating mutable project from single source file.
     * @throws Exception
     */
    public void testMutableSingleCompilationUnit() throws Exception {
      final File compilationUnit = File.createTempFile("source", ".java");
      compilationUnit.deleteOnExit();
      final byte[] compilationUnitContents =
          "Hello, World!\n\nTesting...".getBytes();
      writeBytes(compilationUnit, compilationUnitContents);


      final Project project = Utils.createNewProjectFrom(new TestProject("",
          new LocalSourcePath(compilationUnit.getAbsolutePath()), new LocalClassPath(""), null));
      IDEController.getInstance().setActiveProject(project);

      final Project mutableProject = createMutableProject(project);
      assertTrue("mutableProject != null", (mutableProject != null));
      assertEquals("class path of mutableProject",
          project.getPaths().getClassPath(),
          mutableProject.getPaths().getClassPath());
      assertTrue("source path of mutableProject different",
          !project.getPaths().getSourcePath().equals(
              mutableProject.getPaths().getSourcePath()));

      final SourcePath mutableSourcePath = mutableProject.getPaths().getSourcePath();
      assertEquals("Number of roots in mutableSourcePath",
          1,
          mutableSourcePath.getRootSources().length);

      final File mutableCompilationUnit = mutableSourcePath.getRootSources()[0].
          getFileOrNull();

      assertEquals("Mutable source file contents",
          new String(compilationUnitContents),
          new String(readBytes(mutableCompilationUnit)));

      assertSameSources("after becoming mutable", project, mutableProject);
    }

    /**
     * Tests creating mutable project from a directory with sources.
     * @throws Exception
     */
    public void testMutableDirectory() throws Exception {
      final File sourceDirectory = TempFileCreator.getInstance().
          createRootDirectory().getFileOrNull();
      sourceDirectory.deleteOnExit();
      final File sourceA = new File(sourceDirectory, "a.java");

      sourceA.deleteOnExit();
      final File sourceSubdirectory = new File(sourceDirectory, "com");
      if (!sourceSubdirectory.mkdir()) {
        throw new RuntimeException("Failed to create source subdirectory "
            + sourceSubdirectory);
      }

      sourceSubdirectory.deleteOnExit();

      final File sourceB = new File(sourceSubdirectory, "b.java");

      sourceB.deleteOnExit();

      final byte[] sourceAContents = "nclass a{}".getBytes();
      final byte[] sourceBContents = "package com;\n\nclass b{}".getBytes();
      writeBytes(sourceA, sourceAContents);
      writeBytes(sourceB, sourceBContents);

      final Project project = Utils.createNewProjectFrom(
        new TestProject("",
          new LocalSourcePath(sourceDirectory.getAbsolutePath()), new LocalClassPath(""), null));
      IDEController.getInstance().setActiveProject(project);

      final Project mutableProject = createMutableProject(project);
      assertTrue("mutableProject != null", (mutableProject != null));
      assertEquals("class path of mutableProject",
          project.getPaths().getClassPath(),
          mutableProject.getPaths().getClassPath());
      assertTrue("source path of mutableProject different",
          !project.getPaths().getSourcePath().equals(
              mutableProject.getPaths().getSourcePath()));

      final SourcePath mutableSourcePath = mutableProject.getPaths().getSourcePath();
      assertEquals("Number of roots in mutableSourcePath",
          1,
          mutableSourcePath.getRootSources().length);

      final Source mutableRoot = mutableSourcePath.getRootSources()[0];
      final File mutableSourceDirectory = mutableRoot.getFileOrNull();
      mutableSourceDirectory.deleteOnExit();

      final File mutableSourceA = new File(mutableSourceDirectory, "a.java");

      mutableSourceA.deleteOnExit();
      final File mutableSourceSubdirectory =
          new File(mutableSourceDirectory, "com");

      mutableSourceSubdirectory.deleteOnExit();
      final File mutableSourceB = new File(mutableSourceSubdirectory, "b.java");

      mutableSourceB.deleteOnExit();

      assertTrue("Mutable source directory exists",
          mutableSourceDirectory.isDirectory());
      assertTrue("Mutable source subdirectory exists",
          mutableSourceSubdirectory.isDirectory());
      assertTrue("mutable source a.java exists",
          mutableSourceA.isFile());
      assertTrue("mutable source com/b.java exists",
          mutableSourceB.isFile());

      assertEquals("mutable a.java contents",
          new String(sourceAContents),
          new String(readBytes(mutableSourceA)));

      assertEquals("mutable com/b.java contents",
          new String(sourceBContents),
          new String(readBytes(mutableSourceB)));

      assertSameSources("after becoming mutable", project, mutableProject);
    }

    /**
     * Tests {@link RwRefactoringTestUtils#assertSameSources(String, Project, Project)} for two projects
     * consisting of a single file that differs between the projects.
     * @throws Exception
     */
    public void testAssertSameSourcesSingleModifiedFile() throws Exception {
      final File expectedCompilationUnit = File.createTempFile("source", ".java");
      expectedCompilationUnit.deleteOnExit();
      writeBytes(expectedCompilationUnit, "Hello\n\n\nTesting...".getBytes());

      final File actualCompilationUnit = File.createTempFile("source", ".java");
      actualCompilationUnit.deleteOnExit();
      writeBytes(actualCompilationUnit, "Hello2\n\n\nTesting...".getBytes());

      final Project expected = Utils.createNewProjectFrom(
        new TestProject("",
          new LocalSourcePath(expectedCompilationUnit.getAbsolutePath()), new LocalClassPath(""), null));

      final Project actual = Utils.createNewProjectFrom(
        new TestProject("",
          new LocalSourcePath(actualCompilationUnit.getAbsolutePath()), new LocalClassPath(""), null));
      IDEController.getInstance().setActiveProject(actual);

      try {
        assertSameSources("shouldn't differ",
            expected,
            actual);
        // Note: Cannot use fail() here!

      } catch (AssertionFailedError e) {
        // Expected
        return;
      }

      fail("AssertionFailedError should have been thrown");
    }

    /**
     * Tests {@link RwRefactoringTestUtils#assertSameSources(String, Project, Project)} for two projects
     * where some files differ in some directories.
     * @throws Exception
     */
    public void testAssertSameSourcesModifiedFilesInSubdirectories() throws
        Exception {

      final File expectedSourceRoot = TempFileCreator.getInstance().
          createRootDirectory().getFileOrNull();
      expectedSourceRoot.deleteOnExit();
      final File expectedSourceSubdirectory =
          new File(expectedSourceRoot, "com");

      expectedSourceSubdirectory.deleteOnExit();
      assertTrue("com subdirectory created in expected project",
          expectedSourceSubdirectory.mkdir());
      final File expectedCompilationUnit =
          new File(expectedSourceSubdirectory, "a.java");

      expectedCompilationUnit.deleteOnExit();
      writeBytes(expectedCompilationUnit, "Hello\n\n\nTesting...".getBytes());

      final File actualSourceRoot = TempFileCreator.getInstance().
          createRootDirectory().getFileOrNull();
      actualSourceRoot.deleteOnExit();
      final File actualSourceSubdirectory =
          new File(actualSourceRoot, "com");

      actualSourceSubdirectory.deleteOnExit();
      assertTrue("com subdirectory created in actual project",
          actualSourceSubdirectory.mkdir());
      final File actualCompilationUnit =
          new File(actualSourceSubdirectory, "a.java");

      actualCompilationUnit.deleteOnExit();
      writeBytes(actualCompilationUnit, "Hello\n\n\nTesting2...".getBytes());

      final Project expected = Utils.createNewProjectFrom(
        new TestProject("",
          new LocalSourcePath(expectedSourceRoot.getAbsolutePath()), new LocalClassPath(""), null));

      final Project actual = Utils.createNewProjectFrom(
        new TestProject("",
          new LocalSourcePath(actualSourceRoot.getAbsolutePath()), new LocalClassPath(""), null));
      IDEController.getInstance().setActiveProject(actual);

      try {
        assertSameSources("shouldn't differ",
            expected,
            actual);
        // Note: Cannot use fail() here!
      } catch (AssertionFailedError e) {
        // Expected
        return;
      }

      fail("AssertionFailedError should have been thrown");
    }

    /**
     * Tests when a directory
     * is missing in actual project.
     * @throws Exception
     */
    public void testAssertSameSourcesMissingDirectory() throws Exception {

      final File expectedSourceRoot = TempFileCreator.getInstance().
          createRootDirectory().getFileOrNull();
      expectedSourceRoot.deleteOnExit();
      final File expectedSourceSubdirectory =
          new File(expectedSourceRoot, "com");

      expectedSourceSubdirectory.deleteOnExit();
      assertTrue("com subdirectory created in expected project",
          expectedSourceSubdirectory.mkdir());

      final File actualSourceRoot = TempFileCreator.getInstance().
          createRootDirectory().getFileOrNull();
      actualSourceRoot.deleteOnExit();

      final Project expected = Utils.createNewProjectFrom(
        new TestProject("",
          new LocalSourcePath(expectedSourceRoot.getAbsolutePath()), new LocalClassPath(""), null));

      final Project actual = Utils.createNewProjectFrom(
        new TestProject("",
          new LocalSourcePath(actualSourceRoot.getAbsolutePath()), new LocalClassPath(""), null));
      IDEController.getInstance().setActiveProject(actual);

      try {
        assertSameSources("shouldn't differ",
            expected,
            actual);
        // Note: Cannot use fail() here!
      } catch (AssertionFailedError e) {
        // Expected
        return;
      }

      fail("AssertionFailedError should have been thrown");
    }

    /**
     * Tests {@link RwRefactoringTestUtils#assertSameSources(String, Project, Project) } when a file
     * is missing in actual project.
     * @throws Exception
     */
    public void testAssertSameSourcesMissingFile() throws Exception {

      final File expectedSourceRoot = TempFileCreator.getInstance().
          createRootDirectory().getFileOrNull();
      expectedSourceRoot.deleteOnExit();
      final File expectedSourceSubdirectory =
          new File(expectedSourceRoot, "com");

      expectedSourceSubdirectory.deleteOnExit();
      assertTrue("com subdirectory created in expected project",
          expectedSourceSubdirectory.mkdir());
      final File expectedCompilationUnit =
          new File(expectedSourceSubdirectory, "a.java");

      expectedCompilationUnit.deleteOnExit();
      writeBytes(expectedCompilationUnit, "Hello\n\n\nTesting...".getBytes());

      final File actualSourceRoot = TempFileCreator.getInstance().
          createRootDirectory().getFileOrNull();
      actualSourceRoot.deleteOnExit();
      final File actualSourceSubdirectory =
          new File(actualSourceRoot, "com");

      actualSourceSubdirectory.deleteOnExit();
      assertTrue("com subdirectory created in actual project",
          actualSourceSubdirectory.mkdir());

      final Project expected = Utils.createNewProjectFrom(
        new TestProject("",
          new LocalSourcePath(expectedSourceRoot.getAbsolutePath()), new LocalClassPath(""), null));

      final Project actual = Utils.createNewProjectFrom(
        new TestProject("",
          new LocalSourcePath(actualSourceRoot.getAbsolutePath()), new LocalClassPath(""), null));
      IDEController.getInstance().setActiveProject(actual);

      try {
        assertSameSources("shouldn't differ",
            expected,
            actual);
        // Note: Cannot use fail() here!
      } catch (AssertionFailedError e) {
        // Expected
        return;
      }

      fail("AssertionFailedError should have been thrown");
    }

    /**
     * Writes the given byte array into the specified file.
     * If the file exists already, its contents is overwritten.
     *
     * @param file file to write to.
     * @param bytes new contents of the file.
     *
     * @throws IOException if I/O exception occurs.
     */
    private static void writeBytes(File file, byte[] bytes) throws IOException {

      FileOutputStream output = null;
      try {
        output = new FileOutputStream(file);
        output.write(bytes);
        output.flush();
      } finally {
        if (output != null) {
          output.close();
        }
      }
    }

    /**
     * Returns the content of the specified file as a byte array.
     *
     * @param file file to read from.
     * @return the contents of the file.
     *
     * @throws IOException if I/O exception occurs.
     */
    private static byte[] readBytes(File file) throws IOException {
      FileInputStream input = null;
      try {
        input = new FileInputStream(file);
        final int len = (int) file.length();
        byte[] bytes = new byte[len];
        input.read(bytes);
        return bytes;
      } finally {
        if (input != null) {
          input.close();
        }
      }
    }
  }
}
