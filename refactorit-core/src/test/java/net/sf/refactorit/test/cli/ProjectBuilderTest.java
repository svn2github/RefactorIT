/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.cli;

import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.cli.ProjectBuilder;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.utils.ClasspathUtil;

import junit.framework.TestCase;


/**
 * @author RISTO A
 */
public class ProjectBuilderTest extends TestCase {
  public void testDefaultClasspath() {
    assertEquals("somejar.jar" + StringUtil.PATH_SEPARATOR +
        ClasspathUtil.getDefaultClasspath(),
        ProjectBuilder.getClasspathWithDefaultEntries("somejar.jar"));
  }

  public void testProjectBuilderUsesDefaultClasspath() throws Exception {
    Project project = Utils.createTestRbProjectFromArray(new String[] {
        "class X {}", ""});
    CompilationUnit source = (CompilationUnit) project.getCompilationUnits().get(0);

    ProjectBuilder b =
        new ProjectBuilder(source.getSource().getAbsolutePath(), ".");

    assertEquals("", b.getErrorsAsString());
    assertNotNull(b.getProject());

    assertTrue(b.getProject().getPaths().getClassPath().getStringForm().indexOf(
        "." + StringUtil.PATH_SEPARATOR) >= 0);
    assertTrue(b.getProject().getPaths().getClassPath().getStringForm().indexOf("rt.jar")
        >= 0);
  }
}
