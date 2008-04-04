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
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;


/**
 * @author Juri Reinsalu
 *
 */
public class JSourcepathChooser extends JPathChooser {

  /**
   *
   */
  public JSourcepathChooser() {
    super(SOURCEPATH);
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
        chooser.addChoosableFileFilter(new FileExtensionFilter(
            (String[]) null, "Directories/Folders only"));

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

  }

  public Path getPath() {
    if (data.isEmpty()) {
      return SourcePath.EMPTY;
    }

    StringBuffer buf = new StringBuffer();

    Enumeration e = data.elements();
    buf.append(e.nextElement());

    while (e.hasMoreElements()) {
      buf.append(File.pathSeparator).append(e.nextElement());
    }

    String path = buf.toString();

    return new SourcePath(path);
  }


  public void setPath(Path path) {
    data.removeAllElements();
    mode = SOURCEPATH;

    StringTokenizer t = new StringTokenizer(
        path.toString(), File.pathSeparator);
    Set redundancyFilter = new HashSet(t.countTokens());
    while (t.hasMoreTokens()) {
      redundancyFilter.add(t.nextToken());
    }
    Iterator i = redundancyFilter.iterator();
    while (i.hasNext()) {
      data.addElement(i.next());
    }

    if (data.getSize() > 0) {
      list.setSelectedIndex(0);
    }
  }


}
