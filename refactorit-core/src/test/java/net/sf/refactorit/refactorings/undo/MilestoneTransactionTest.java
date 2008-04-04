/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.undo;



import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.FileCopier;
import net.sf.refactorit.test.RwRefactoringTestUtils;
import net.sf.refactorit.vfs.Source;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 *
 * @author Tonis Vaga
 */
public class MilestoneTransactionTest extends UndoTestCase {
  public static Test suite() {
    return new TestSuite(MilestoneTransactionTest.class);
  }

  public void test() throws Exception {
    Project project = RwRefactoringTestUtils.getMutableProject("simple");
    project.getProjectLoader().build();

    saveProjectState(project);

    IMilestoneManager manager = MilestoneManager.getInstance(project);
    manager.createMilestone();

    modifyProject(project);

    manager.undo();

    checkProjectState(project);

    modifyProject(project);

    saveProjectState(project);

    manager.createMilestone();

    manager.undo();

    manager.redo();

    checkProjectState(project);
  }

  public void testMilestoneManagerDir() throws Exception {
    Project project = RwRefactoringTestUtils.getMutableProject("simple");
    project.getProjectLoader().build();

    String dir = MilestoneManager.getMilestoneDir(project);

    assertTrue("dir == null", dir != null);

    String dir2 = MilestoneManager.getMilestoneDir(project);

    assertTrue("should be equal but were " + dir + "!=" + dir2, dir.equals(dir2));
  }

  public void testMilestoneManager() throws Exception {
    Project project = RwRefactoringTestUtils.getMutableProject("simple");
    project.getProjectLoader().build();

    saveProjectState(project);

    IMilestoneManager instance = MilestoneManager.getInstance(project);

    instance.createMilestone();

    modifyProject(project);

    instance.undo();

    checkProjectState(project);

    instance.redo();

    //
    MilestoneManager.clear();

    instance = MilestoneManager.getInstance(project);

    assertTrue("canUndo should be true!!", instance.canUndo());

    modifyProject(project);

    instance.undo();

    checkProjectState(project);
  }

  private void modifyProject(Project project) {
    List sources = CompilationUnit
        .extractSourcesFromCompilationUnits(project.getCompilationUnits());

    Iterator it = sources.iterator();

    while (it.hasNext()) {
      Source src = (Source) it.next();
      File file = src.getFileOrNull();
      String content = FileCopier.readFileToString(file);

      FileCopier.writeStringToFile(file, "/*** */" + content);
    }
  }
}
