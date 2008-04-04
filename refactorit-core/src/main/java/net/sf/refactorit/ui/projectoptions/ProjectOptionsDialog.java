/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.projectoptions;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.ui.UIResources;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class ProjectOptionsDialog {
  public static class UpButton extends JButton {
    public UpButton(final JList list, final DefaultListModel listModel) {
      super(ResourceUtil.getIcon(UIResources.class, "arrow_up.gif"));

      addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          int selected[] = list.getSelectedIndices();
          if (selected.length != 1 || selected[0] == 0) {
            return;
          }

          Object item = listModel.set(selected[0] - 1,
              listModel.getElementAt(selected[0]));
          listModel.set(selected[0], item);
          list.setSelectedIndex(selected[0] - 1);
          list.ensureIndexIsVisible(selected[0] - 1);
        }
      });
    }
  }


  public static class DownButton extends JButton {
    public DownButton(final JList list, final DefaultListModel listModel) {
      super(ResourceUtil.getIcon(UIResources.class, "arrow_down.gif"));

      addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          int selected[] = list.getSelectedIndices();
          if (selected.length != 1 || selected[0] == listModel.getSize() - 1) {
            return;
          }

          Object item = listModel.set(selected[0] + 1,
              listModel.getElementAt(selected[0]));
          listModel.set(selected[0], item);
          list.setSelectedIndex(selected[0] + 1);
          list.ensureIndexIsVisible(selected[0] + 1);
        }
      });
    }
  }
}
