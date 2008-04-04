/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.test.loader;

import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.FileCopier;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.LoadingProperties;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.test.TestProject;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.utils.ClasspathUtil;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.local.LocalClassPath;
import net.sf.refactorit.vfs.local.LocalSourcePath;

import java.io.File;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * @author risto
 */
public class RebuildTestWithCustomProjects extends TestCase {
  public static Test suite() {
    return new TestSuite(RebuildTestWithCustomProjects.class);
  }

  public void testClasspathUpdate() throws Exception {
    Project project = Utils.createTestRbProjectFromString(
        "public class X extends LibraryClass {}",
        "X.java",
        null);

    List errors = ensureAndGetErrors(project);
    assertEquals(2, errors.size());
    assertTrue(errors.get(0).toString().matches(".*Could not resolve superclass LibraryClass.*"));
    assertTrue(errors.get(1).toString().matches(".*Class not found: LibraryClass.*"));

    ((LocalClassPath)project.getPaths().getClassPath()).addPath(Utils.getSomeJarFile().getAbsolutePath());

    // FIXME: small hack for current architecture - this way we force cleaner rebuild
    // project.getPaths().setClassPath(project.getPaths().getClassPath());
    project.clean();
    project.getProjectLoader().build();
    
    errors = ensureAndGetErrors(project);
    assertTrue(errors.toString(), errors.isEmpty());
  }

  public void testRebuildAfterCriticalErrors() throws Exception {
    Project newProject = Utils.createTestRbProject(
        Utils.getTestProjects().getProject("bug1628"));

    Project p = RwRefactoringTestUtils.createMutableProject(newProject);
    p.getProjectLoader().build(null, false);
    assertTrue(p.getProjectLoader().isLoadingCompleted());

    Source source = ((CompilationUnit) p.getCompilationUnits().get(0)).getSource();
    File file = source.getFileOrNull();
    String content = FileCopier.readFileToString(file);

    FileCopier.writeStringToFile(file,
        "!! (class) \u01BE\u01BE,%this produces" +
        content + ",,%this produces critical errors");

    long newTime = source.lastModified()+2*Source.MAX_TIMESTAMP_ERROR;
    source.setLastModified(newTime);

    assertEquals ( newTime,source.lastModified());
    p.getProjectLoader().build(null, false);
    assertTrue((p.getProjectLoader().getErrorCollector()).hasErrors());

    FileCopier.writeStringToFile(file, content);
    final List compilationUnitList = p.getCompilationUnits();
    for (int i = 0; i < compilationUnitList.size(); i++) {
      source = ((CompilationUnit) compilationUnitList.get(0)).getSource();
      if (source.getName().equals(file.getName())) {
        break;
      }
    }

    source.setLastModified(1);
    final Source finalSource = source;
  //   p.addProjectChangedListener( new ProjectChangedListener() {
  //     public void rebuildPerformed(Project project) {}
  //     public void rebuildStarted(Project project) {
    assertTrue(p.getProjectLoader().getAstTreeCache().checkCacheFor(finalSource) == null);
  //     }
  //   });

    p.getProjectLoader().build(null, false);
    assertTrue(p.getProjectLoader().isLoadingCompleted());
    assertTrue(!(p.getProjectLoader().getErrorCollector()).hasErrors());
  }

  public void testClasspathReleaseOnRebuild() throws Exception {
    final StringBuffer log = new StringBuffer();

    Project p = Utils.createTestRbProjectFromMethodBody("");
    p.getPaths().setClassPath(new LocalClassPath(ClasspathUtil.getDefaultClasspath()) {
      public void release() {
        log.append("released");
      }
    });
    p.getProjectLoader().build(null, false);

    assertEquals("released", log.toString());
  }

  public void testDiscoverAllUsedTypesReleasesClasspath() throws Exception {
    Project p = Utils.createNewProjectFrom(
        new TestProject("", new LocalSourcePath(""), new LocalClassPath(
            ClasspathUtil.getDefaultClasspath()), null));
    IDEController.getInstance().setActiveProject(p);

    p.getProjectLoader().build(null, false);
    assertFalse(((LocalClassPath) p.getPaths().getClassPath()).isReleased());

    p.discoverAllUsedTypes();
    assertTrue(((LocalClassPath) p.getPaths().getClassPath()).isReleased());
  }
  
  // Util methods

  private static List ensureAndGetErrors(Project p) {
    IDEController.getInstance().setActiveProject(p);

    IDEController.getInstance().ensureProject(new LoadingProperties(false));
    return CollectionUtil.toList((p.getProjectLoader().getErrorCollector()).getUserFriendlyErrors());
  }

}
