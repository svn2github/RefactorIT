/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.commonIDE.options;


import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.FileExtensionFilter;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.dialog.RitDialog;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;

/**
 * ClassPathEditingPanel
 *
 * @author <a href="mailto:tonis.vaga@aqris.com>Tõnis Vaga</a>
 * @version $Revision: 1.9 $ $Date: 2005/12/09 12:02:59 $
 */
public class ClassPathEditingPanel extends AbstractPathEditingPanel {
  private static final String[] FILTER_SUFFIXES = {".jar", ".zip"};
  private static final String FILTER_DESC = "Jar and Zip archives";

  FileFilter zipFilter = new FileExtensionFilter(FILTER_SUFFIXES, FILTER_DESC) {
    public boolean accept(File f) {
      return super.accept(f) && ! containsPathItem(new PathItem(f));
    }
  };

  protected JComponent[] getButtons() {
    return new JComponent[] {
        addButton, addAllJars, removeButton, upButton, downButton
    };
  }

  public ClassPathEditingPanel() {
    super();

    initButtons();
  }

  private void initButtons() {
    addButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

        chooser.setCurrentDirectory(GlobalOptions.getLastDirectory());

        chooser.setFileFilter(zipFilter);

        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        int choice = RitDialog.showFileDialog(
            IDEController.getInstance().createProjectContext(), chooser);

        if (choice == JFileChooser.APPROVE_OPTION) {
          File[] selection = chooser.getSelectedFiles();
          for (int i = 0; i < selection.length; i++) {
            addPathItem(new PathItem(selection[i].getAbsolutePath()));
          }

          GlobalOptions.setLastDirectory(chooser.getCurrentDirectory());
        }
      }
    });

    addAllJars.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setCurrentDirectory(GlobalOptions.getLastDirectory());

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

      /** Adds jars recursively */
      private void addJarsFrom(final File dir, final HashSet added) {
        Assert.must( dir != null && dir.exists());
        added.add(dir);
        File[] allList = dir.listFiles();
        for (int i = 0; i < allList.length; i++) {
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

            addPathItem(new PathItem(cur.getAbsolutePath()));
          }
        }
        // added.remove(dir); - extra safety - don't remove, there are not many dirs anyway
      }
    });
  }

  private JButton addAllJars = new JButton("Add All Jars...");

  public void setEnabled(boolean b) {
    super.setEnabled(b);

    this.addAllJars.setEnabled(b);
  }
}
