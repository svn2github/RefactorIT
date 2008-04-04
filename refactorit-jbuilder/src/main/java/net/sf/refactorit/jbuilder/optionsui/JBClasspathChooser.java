/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder.optionsui;


import com.borland.primetime.vfs.Url;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import net.sf.refactorit.common.util.FileExtensionFilter;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.options.JClasspathChooser;
import net.sf.refactorit.ui.options.TreeChooser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.HashSet;


/**
 * Extension of JClasspathChooser with overriden <code>fillAddButtonPanel</code>
 * method, that is JBuilder specific in it's "add from project path" usecase.
 *
 * @author juri
 */
public class JBClasspathChooser extends JClasspathChooser {
  static File lastDirectory; // for file open dialog

  public JBClasspathChooser() {
    super();
  }

  protected void fillAddButtonPanel(JPanel panel) {
    JButton addButton = new JButton(resLocalizedStrings.getString(
        "pathchooser.add"));
    addButton.setMnemonic(KeyEvent.VK_A);
    addButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        FileFilter zipFilter = new FileExtensionFilter(
            new String[] {".jar", ".zip"}, "Jar and Zip archives");
        chooser.setFileFilter(zipFilter);

        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        int choice = RitDialog.showFileDialog(
            IDEController.getInstance().createProjectContext(), chooser);
        if (choice == JFileChooser.APPROVE_OPTION) {
          File[] selection = chooser.getSelectedFiles();
          for (int i = 0; i < selection.length; i++) {
            addPathItem(selection[i].getAbsolutePath());
          }
        }
      }
    });

    panel.add(addButton);
    JButton addFromFilesystems = new JButton("Add From Project Paths");
    addFromFilesystems.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JbUrlsTreeModel jbUrlsModel = new JbUrlsTreeModel();
        Object[] urls = TreeChooser.getNewDataObjectReferences(
            IDEController.getInstance().createProjectContext(),
            "Add from project paths", jbUrlsModel, new JbUrlTreeCellRenderer(
            jbUrlsModel));

        if (urls != null) {
          for (int i = 0; i < urls.length; i++) {
            String pathStr = ((Url) urls[i]).getFileObject().getAbsolutePath();
            addPathItem(pathStr);
          }
        }
      }
    });
    panel.add(addFromFilesystems);
    JButton addAllJarsButton = new JButton("Add All Jars...");
    panel.add(addAllJarsButton);
    addAllJarsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setCurrentDirectory(lastDirectory);

        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        int rc = RitDialog.showFileDialog(
            IDEController.getInstance().createProjectContext(), chooser);
        if (rc == JFileChooser.CANCEL_OPTION) {
          return;
        }

        lastDirectory = chooser.getCurrentDirectory();

        File f = chooser.getSelectedFile();

        addJarsFrom(f, new HashSet());
      }

      private void addJarsFrom(final File dir, final HashSet added) {
        added.add(dir);
        File[] allList = dir.listFiles();
        for (int i = 0; i < allList.length; ++i) {
          File cur = allList[i];
          if (cur.isDirectory()) {
            if (added.contains(cur)) {
              continue;
            } else {
              addJarsFrom(cur, added);
            }
          }

          String curName = cur.getName().toLowerCase();
          if (!curName.endsWith(".jar") && !curName.endsWith(".zip")) {
            continue;
          }

          addPathItem(cur.getAbsolutePath());
        }
        // added.remove(dir); - extra safety - don't remove, there are not many
        // dirs anyway
      }
    });
  }
}
