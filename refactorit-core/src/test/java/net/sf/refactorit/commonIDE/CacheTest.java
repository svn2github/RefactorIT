/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.commonIDE;

import net.sf.refactorit.RitTestCase;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.loader.ASTTreeCache;
import net.sf.refactorit.test.ProjectMetadata;
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.test.commonIDE.MockIDEController;
import net.sf.refactorit.vfs.Source;

import java.util.Iterator;
import java.util.List;


/**
 *
 *
 * @author Tonis Vaga
 */
public class CacheTest extends RitTestCase {
  IDEController oldInstance;
  IDEController controller;

  final Project projects[] = new Project[2];

  public void setUp() throws Exception {
    oldInstance = IDEController.getInstance();

    List allProjects = Utils.getTestProjects().getProjects();
    final Iterator i = allProjects.iterator();

    for (int index = 0; i.hasNext() && index < projects.length; index++) {
      final ProjectMetadata data = (ProjectMetadata) i.next();

      projects[index] = Utils.createTestRbProject(data);
    }

    controller = getController();

    IDEController.setInstance(controller);
  }

  protected IDEController getController() {
    return new MockIDEController();
  }

  public void tearDown() {
    if (oldInstance != null) {
      IDEController.setInstance(oldInstance);
    }
  }

  public void testEnsureProject() {
    Project pr1 = projects[0];
//    Project pr2 = projects[1];
//    ProgressListener listener=ProgressListener.SILENT_LISTENER;

    pr1.clean();

    controller.setActiveProject(pr1);

    assertTrue(controller.ensureProject(
        new LoadingProperties(false, true, false)));

    assertTrue(pr1.getProjectLoader().isLoadingCompleted() &&
        !pr1.getProjectLoader().getErrorCollector().hasErrors());
  }

  /** tests cache serializing and deserializing
   * @throws Exception*/
  public void testSerializeCache() throws Exception {
    Project pr1 = projects[0];

    controller.setActiveProject(pr1);

    Project activeProject = controller.getActiveProject();

    activeProject.setCachePath(IDEController.generateNewCacheFileName("."));
    activeProject.getProjectLoader().build();

    assertCacheExists(activeProject);

    assertTrue(controller.serializeProjectCache(activeProject, false));

    activeProject.getProjectLoader().getAstTreeCache().cleanAll();

    controller.deserializeCache(false);

    Thread.sleep(100);

    assertCacheExists(activeProject);
  }

  private void assertCacheExists(Project activeProject) {
    final ASTTreeCache astTreeCache =
      activeProject.getProjectLoader().getAstTreeCache();

    final List sources =
      activeProject.getPaths().getSourcePath().getAllSources();

    assertNotNull(astTreeCache.checkCacheFor((Source) sources.get(0)));
  }

  public void testSetActiveProject() {
    Project project = projects[0];
    controller.setActiveProject(project);
    assertEquals(project,controller.getActiveProject());

    Project project2 = projects[1];
    controller.setActiveProject(project2);
    assertEquals(project2,controller.getActiveProject());
  }
}
