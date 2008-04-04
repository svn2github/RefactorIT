/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options;


import net.sf.refactorit.common.util.FileExtensionFilter;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.dialog.RitDialog;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.StringTokenizer;


/**
 * @author juri
 *
 */
public class JClasspathChooser extends JPathChooser {
  public JClasspathChooser() {
    super(JPathChooser.CLASSPATH);
  }

  protected void fillAddButtonPanel(JPanel panel) {
    JButton addButton = new JButton(resLocalizedStrings.getString(
        "pathchooser.add"));
    addButton.setMnemonic(KeyEvent.VK_A);

    addButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        if (mode == CLASSPATH || mode == JAVADOCPATH) {
          chooser.setFileFilter(zipFilter);
        } else {
          chooser.addChoosableFileFilter(new FileExtensionFilter(
              (String[]) null, "Directories/Folders only"));
        }

        // FIXME: what about?
        // chooser.setAcceptAllFileFilterUsed(false);

        chooser.setCurrentDirectory(GlobalOptions.getLastDirectory());

        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        int rc = RitDialog.showFileDialog(
            IDEController.getInstance().createProjectContext(), chooser);
        if (rc == JFileChooser.CANCEL_OPTION) {
          return;
        }

        GlobalOptions.setLastDirectory(chooser.getCurrentDirectory());

        File[] files = chooser.getSelectedFiles();
        for (int i = 0; i < files.length; i++) {
          File file = files[i];
          while (!file.exists()) {
            file = file.getParentFile();
          }
          addPathItem(file.getAbsolutePath());
        }

        if (files.length == 0) {
          // !!! bug of SUN JRE 1.2.2 !!!
          File f = chooser.getSelectedFile();
          if (f != null) {
            addPathItem(f.getAbsolutePath());
          }
        }
      }
    });

    panel.add(addButton);

    JButton addAllJarsButton = new JButton("Add All Jars..."); // FIXME: internationalization
    panel.add(addAllJarsButton);

    addAllJarsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setCurrentDirectory(GlobalOptions.getLastDirectory());
        chooser.addChoosableFileFilter(new FileExtensionFilter(
            (String[]) null, "Directories/Folders only"));

        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        int rc = RitDialog.showFileDialog(
            IDEController.getInstance().createProjectContext(), chooser);
        if (rc == JFileChooser.CANCEL_OPTION) {
          return;
        }

        GlobalOptions.setLastDirectory(chooser.getCurrentDirectory());

        File f = chooser.getSelectedFile();
        addJarsFrom(f, new HashSet());
      }

      private void addJarsFrom(final File dir, final HashSet added) {
        added.add(dir);
        File[] allList = dir.listFiles();
        for (int i = 0; i < allList.length; ++i) {
          File cur = allList[i];
          if (cur.isDirectory()) {
            if (!added.contains(cur)) {
              addJarsFrom(cur, added);
            }
          } else {
            String curName = cur.getName().toLowerCase();
            if (!curName.endsWith(".jar") && !curName.endsWith(".zip")) {
              continue;
            }

            addPathItem(cur.getAbsoluteFile());
          }
        }
        // added.remove(dir); - extra safety - don't remove, there are not many dirs anyway
      }
    });
  }

  public Path getPath() {
    if (data.isEmpty()) {
      return ClassPath.EMPTY;
    }

    StringBuffer buf = new StringBuffer();

    Enumeration e = data.elements();
    buf.append(e.nextElement());

    while (e.hasMoreElements()) {
      buf.append(File.pathSeparator).append(e.nextElement());
    }

    String path = buf.toString();

    return new ClassPath(path);
  }

  public void setPath(Path path) {
    data.removeAllElements();
    mode = CLASSPATH;

    StringTokenizer t = new StringTokenizer(
        path.toString(), File.pathSeparator);
    while (t.hasMoreTokens()) {
      Object pathItem = t.nextToken();
      if (!data.contains(pathItem)) {
        data.addElement(pathItem);
      }
    }
    if (data.getSize() > 0) {
      list.setSelectedIndex(0);
    }
  }

}
