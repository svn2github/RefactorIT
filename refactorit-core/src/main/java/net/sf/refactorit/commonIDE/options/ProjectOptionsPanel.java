/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.commonIDE.options;


import net.sf.refactorit.ui.projectoptions.CommonOptionsPanel;
import net.sf.refactorit.ui.projectoptions.ProjectOptions;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import java.awt.BorderLayout;


/**
 * ProjectOptionsPanel panel containing all project options
 *
 * @author <a href="mailto:tonis.vaga@aqris.com>Tonis Vaga</a>
 * @version $Revision: 1.7 $ $Date: 2004/12/10 11:08:26 $
 */
public class ProjectOptionsPanel extends JPanel {
  private PathSettingsPanel pathsPanel;
  private ProjectOptions options;

  public ProjectOptionsPanel(ProjectOptions options) {
    super(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(4, 4, 0, 4));

    this.options = options;

    pathsPanel = new PathSettingsPanel(options);

    CommonOptionsPanel commonOptionsPane =
        options.getCommonPropertiesInOnePanel();

    commonOptionsPane.setBorder(
        BorderFactory.createTitledBorder("Global Options"));

    add(pathsPanel);
    add(commonOptionsPane, BorderLayout.SOUTH);
  }

  public ProjectOptions getProjectOptions() {
    return options;
  }

  public void updateSettings() {
    pathsPanel.updateSettings();
  }
}
