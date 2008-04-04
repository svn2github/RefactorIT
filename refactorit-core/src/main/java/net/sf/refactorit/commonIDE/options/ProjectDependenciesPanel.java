/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.commonIDE.options;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.common.util.BidirectionalMap;
import net.sf.refactorit.commonIDE.DataCheckBox;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.Workspace;
import net.sf.refactorit.commonIDE.WorkspaceManager;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class ProjectDependenciesPanel extends JPanel {

  private final Dimension projectDependenciesScrollpaneSize
      = new Dimension(500, 200);
  private ArrayList projectCheckBoxes = new ArrayList();

  public ProjectDependenciesPanel() {
    WorkspaceManager manager = IDEController.getInstance().getWorkspaceManager();
    Workspace space = manager.getWorkspace();
    Project activeProject = space.getActiveProject();
    if(activeProject != null) {
      Object activeProjectKey = manager.getProjectIdentifier(activeProject);
      List dependsOnProjects
          = manager.getDependsOnProjects(activeProjectKey);

      BidirectionalMap projectsMap
          = (BidirectionalMap) space.getProjects().clone();
      this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

      if(projectsMap.getKeyByValue(activeProject) != null) {
        projectsMap.removeByValue(activeProject);
      }

      JPanel panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
      panel.setBackground(Color.WHITE);

	    Set keySet = projectsMap.getKeySetCopy();
	    for(Iterator it = keySet.iterator(); it.hasNext(); ) {
	      Object projectId = it.next();
	      Object obj = projectsMap.getValueByKey(projectId);
	      Project project = (Project)obj;
	      String projectName;
		      if(project == null) {
		      // FIXME: shall never be. closed project shall only have a flag, that it is closed, but not a null
		      projectName = "Some closed project";
		    } else {
		      projectName = project.getName();
		    }
	      DataCheckBox box = new DataCheckBox(projectId, projectName);
	      if(dependsOnProjects.contains(project)) {
	        box.setSelected(true);
	      }
	      box.setBorder(BorderFactory.createEmptyBorder());
	      box.setContentAreaFilled(false);
	      projectCheckBoxes.add(box);
	      panel.add(box);
	    }

	  JScrollPane pane = new JScrollPane(panel);
      pane.setPreferredSize(projectDependenciesScrollpaneSize);
      pane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
      this.add(pane);
    }
  }

  public PathItem[] getSelectedProjects() {
    Iterator i = projectCheckBoxes.iterator();
    ArrayList pathItems = new ArrayList();
    while(i.hasNext()) {
      DataCheckBox box = (DataCheckBox)i.next();
      if(box.isSelected()) {
        // TODO: toString shall be implemented for each key
        pathItems.add(new PathItem(box.getData().toString()));
      }
    }
    return (PathItem[])pathItems.toArray(new PathItem[pathItems.size()]);
  }
}
