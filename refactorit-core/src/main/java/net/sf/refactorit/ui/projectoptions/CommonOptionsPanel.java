/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.projectoptions;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;


public class CommonOptionsPanel extends JPanel {
  private int rows = 0;

  public CommonOptionsPanel(ProjectProperty[] properties) {
    setLayout(new GridBagLayout());

    for (int i = 0; i < properties.length; i++) {
      addProperty(properties[i].getTitle(), properties[i].getEditor());
    }
  }

  public void addProperty(String title, JComponent editor) {
    add(new JLabel(title), getLabelConstraints(rows));
    add(editor, getFieldConstraints(rows));

    ++rows;
  }

  private GridBagConstraints getLabelConstraints(int y) {
    return new GridBagConstraints(
        0, y, 1, 1, 0, 0,
        GridBagConstraints.WEST,
        GridBagConstraints.HORIZONTAL,
        new Insets(0, 4, 0, 4), 0, 0);
  }

  private GridBagConstraints getFieldConstraints(int y) {
    return new GridBagConstraints(
        1, y, 1, 1, 1, 0,
        GridBagConstraints.WEST,
        GridBagConstraints.HORIZONTAL,
        new Insets(0, 0, 0, 4), 0, 0);
  }
}
