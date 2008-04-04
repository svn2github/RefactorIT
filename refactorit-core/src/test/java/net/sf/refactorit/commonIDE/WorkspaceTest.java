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
import net.sf.refactorit.test.Utils;
import net.sf.refactorit.test.commonIDE.NullWorkspaceManager;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * WorkspaceTest
 *
 * @author <a href="mailto:tonis.vaga@aqris.com>Tonis Vaga</a>
 * @version $Revision: 1.10 $ $Date: 2005/02/09 16:15:31 $
 */
public class WorkspaceTest extends RitTestCase {

  public static Test suite() {
    return new TestSuite(WorkspaceTest.class);
  }

  public void testRequiredProjectSolving() throws Exception {
    NullWorkspaceManager manager
        = (NullWorkspaceManager) NullWorkspaceManager.getInstance();
    Workspace workspace = manager.getWorkspace();
    workspace.clear();

    Project bingoPrj = Utils.createTestRbProjectFromXml("bingo");
    Project bookstorePrj = Utils.createTestRbProjectFromXml("bookstore2");

    workspace.addProject(bingoPrj, "bingo");
    workspace.addProject(bookstorePrj, "bookstore2");
    manager.linkProjects(bookstorePrj, bingoPrj);
    assertEquals(1, manager.getDependsOnProjects("bookstore2").size());
    assertEquals(0, manager.getReferencedInProjects("bookstore2").size());
    assertEquals(0, manager.getDependsOnProjects("bingo").size());
    assertEquals(1, manager.getReferencedInProjects("bingo").size());

    String qName = "bingo.game.BINGO";

    IDEController.getInstance().setActiveProject(bingoPrj);
    bingoPrj.getProjectLoader().build();
    assertNotNull(bingoPrj.findTypeRefForName(qName));

    IDEController.getInstance().setActiveProject(bookstorePrj);
    bookstorePrj.getProjectLoader().build();
    assertNotNull(bookstorePrj.getTypeRefForName(qName));
  }

  public void testAddOneProject() {
    NullWorkspaceManager manager
        = (NullWorkspaceManager) NullWorkspaceManager.getInstance();
    Workspace workspace = manager.getWorkspace();
    workspace.clear();

    Project bingoPrj = Utils.createTestRbProjectFromXml("bingo");

    Object projectKey = bingoPrj.getName();
    workspace.addProject(bingoPrj, projectKey);
    Project project = workspace.getProjectByIdentifier(projectKey);

    assertEquals("Projects is saved in workspace", bingoPrj, project);
  }

  public void testXXX() {
    NullWorkspaceManager manager
        = (NullWorkspaceManager) NullWorkspaceManager.getInstance();
    Workspace workspace = manager.getWorkspace();
    workspace.clear();

    Project bingoPrj = Utils.createTestRbProjectFromXml("bingo");
    Project bookstorePrj =Utils.createTestRbProjectFromXml("bookstore2");

    workspace.addProject(bingoPrj, bingoPrj.getName());
    workspace.addProject(bookstorePrj, bookstorePrj.getName());


  }


}
