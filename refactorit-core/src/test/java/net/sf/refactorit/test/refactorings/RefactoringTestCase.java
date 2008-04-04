/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.refactorings;


import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.refactorings.rename.RenamePackage;
import net.sf.refactorit.source.SourceParsingException;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.NullDialogManager;

import java.io.File;
import java.util.List;

import junit.framework.TestCase;


/**
 * @author Anton Safonov
 */
public abstract class RefactoringTestCase extends TestCase {
  static {
    // TODO find better way to initialize
    Utils.setUpTestingEnvironment();
  }
  public RefactoringTestCase(final String name) {
    super(name);
  }

  /**
   * Examples:<pre>
     ExtractSuper/&lt;stripped_test_name&gt;/&lt;in_out&gt;
     RenameField/imported/RenamePrivateField/&lt;test_name&gt;/&lt;in_out&gt;
     InlineTemp/canInline/A_&lt;test_name&gt;_&lt;in_out&gt;.java
     ExtractMethod/Imported/&lt;extra_name&gt;_&lt;in_out&gt;/A_&lt;test_name&gt;.java
   </pre>
   * &lt;extra_name&gt; is replaced with parameter of
   * {@link #getInitialProject(String)} or {@link #getExpectedProject(String)}<br>
   * &lt;stripped_test_name&gt; is replaced with value of {@link #getStrippedTestName()}<br>
   * &lt;test_name&gt; is replaced with value of {@link #getTestName()}<br>
   * &lt;in_out&gt; is replaced with either 'in' or 'out' depending on either asked
   * for initial or expected project.<br>
   * Either of them can be missing.
   * @return every test suite defines it's own template how its test projects
   * are identified and located
   */
  public abstract String getTemplate();

  /**
   * Repesented in template as "&lt;test_name&gt;".
   * @return the name of the currect single test; can't be <code>null</code>
   */
  public final String getTestName() {
    return getName();
  }

  /**
   * Repesented in template as "&lt;stripped_test_name&gt;".
   * @return the name of the currect single test word without "test" in the
   * beginning; can't be <code>null</code>
   */
  public final String getStrippedTestName() {
    final String testName = getTestName();
    if (testName.startsWith("test")) {
      return testName.substring("test".length());
    }

    return testName;
  }

  /**
   * @param extraName some test suites can have tests groups,
   *        see e.g. {@link ExtractMethodTest}; can be null
   * @param initial true when asked for initial project, false - for expected
   * @return fully resolved path to the test project
   */
  public final String resolveTestName(final String extraName,
      final boolean initial) {
    String result = getTemplate();
    result = StringUtil.replace(result, "<extra_name>",
        extraName == null ? "" : extraName);
    result = StringUtil.replace(result, "<stripped_test_name>",
        getStrippedTestName());
    result = StringUtil.replace(result, "<test_name>", getTestName());
    result = StringUtil.replace(result, "<in_out>", initial ? "in" : "out");
    result = result.replace('/', File.separatorChar);

    return result;
  }

  /**
   * @return initial project, not mutable. If need mutable,
   *         use {@link #getMutableProject()}
   * @throws Exception when failed to get the project
   */
  public Project getInitialProject() throws Exception {
    return getInitialProject(null);
  }

  /**
   * @param extraName the name of the test group, see {@link ExtractMethodTest}
   * @return initial project, not mutable. If need mutable,
   *         use {@link #getMutableProject()}
   * @throws Exception when failed to get the project
   */
  public Project getInitialProject(final String extraName) throws Exception {
    return Utils.createTestRbProject(resolveTestName(extraName, true));
  }

  /**
   * @return expected project to compare with
   * @throws Exception when failed to get the project
   */
  public Project getExpectedProject() throws Exception {
    return getExpectedProject(null);
  }

  /**
   * @param extraName the name of the test group, see {@link ExtractMethodTest}
   * @return expected project to compare with
   * @throws Exception when failed to get the project
   */
  public Project getExpectedProject(final String extraName) throws Exception {
    return Utils.createTestRbProject(resolveTestName(extraName, false));
  }

  /**
   * @return mutable project out of initial project
   * @throws Exception
   */
  public Project getMutableProject() throws Exception {
    return getMutableProject((String)null);
  }

  /**
   * @param extraName the name of the test group, see {@link ExtractMethodTest}
   * @return mutable project out of initial project
   * @throws Exception
   */
  public Project getMutableProject(final String extraName) throws Exception {
    return getMutableProject(getInitialProject(extraName));
  }

  /**
   * @param nonMutableProject converts this project into mutable one and loads
   * @return a mutable project
   * @throws java.lang.Exception
   */
  public Project getMutableProject(final Project nonMutableProject) throws
      Exception {
    Project mutableProject = null;
    try {
      mutableProject
          = RwRefactoringTestUtils.createMutableProject(nonMutableProject);
      mutableProject.getProjectLoader().build();

//      assertTrue("Project has defined types",
//          mutableProject.getDefinedTypes().size() > 0);
    } catch (SourceParsingException e) {
      System.err.println(e.getMessage());
      e.printStackTrace(System.err);
    }

    return mutableProject;
  }

  public static void renameToOut(final Project project) throws Exception {
    final List definedTypes = project.getDefinedTypes();
    if (definedTypes == null || definedTypes.size() == 0) {
      return;
    }

    final BinPackage oldPackage = ((BinTypeRef) definedTypes.get(0))
        .getPackage();
    String name = oldPackage.getQualifiedName();
    if (name.endsWith("_in")) {
      name = name.substring(0, name.length() - 3) + "_out";
      RenamePackage renamePackage
          = new RenamePackage(new NullContext(project), oldPackage);
      renamePackage.setNewName(name);
            renamePackage.apply();
      project.getProjectLoader().build(null, false);
      ((NullDialogManager) DialogManager.getInstance()).customErrorString = "";
    }
  }
}
