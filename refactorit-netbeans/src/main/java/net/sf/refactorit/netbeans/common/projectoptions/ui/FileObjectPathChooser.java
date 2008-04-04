/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.projectoptions.ui;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.netbeans.common.NBControllerVersionState;
import net.sf.refactorit.netbeans.common.RefactorItOptions;
import net.sf.refactorit.netbeans.common.VersionSpecific;
import net.sf.refactorit.netbeans.common.projectoptions.PathItemReference;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.options.TreeChooser;
import net.sf.refactorit.ui.projectoptions.ProjectOptionsDialog;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;


public class FileObjectPathChooser extends JComponent {
  static ResourceBundle resLocalizedStrings =
      ResourceUtil.getBundle(RefactorItOptions.class);

  public static final int CLASSPATH = 0;
  public static final int SOURCEPATH = 1;
  public static final int JAVADOCPATH = 2;
  public static final int IGNORED_ON_SOURCEPATH = 3;

  DefaultListModel data = new DefaultListModel();
  JList list = new JList(data);

  int mode;

  private JButton addButton;
  private JButton addAllJarsButton;
  private JButton removeButton;
  private JButton addFromFileSystemsButton;
  private JButton addUrlButton;

  private JButton upButton = new ProjectOptionsDialog.UpButton(list, data);
  private JButton downButton = new ProjectOptionsDialog.DownButton(list, data);

  static final FileFilter zipFilter = new FileFilter() {
    public boolean accept(File f) {
      return f.isDirectory() ||
          f.getName().endsWith(".jar") ||
          f.getName().endsWith(".zip");
    }

    public String getDescription() {
      return resLocalizedStrings.getString("pathchooser.filter");
    }
  };

  public FileObjectPathChooser(final int aMode) {
    setMode(aMode);

    setBorder(BorderFactory.createEtchedBorder());

    setLayout(new GridBagLayout());

    GridBagConstraints constr = new GridBagConstraints();

    constr.gridx = 0;
    constr.gridy = 0;
    constr.gridwidth = 1;
    constr.gridheight = 2;
    constr.fill = GridBagConstraints.BOTH;
    constr.weightx = 1.0;
    constr.weighty = 1.0;
    constr.insets = new Insets(4, 4, 4, 4);
    add(new JScrollPane(list), constr);

    // add/remove buttons
    constr.gridx = 2;
    constr.gridheight = 1;
    constr.fill = GridBagConstraints.HORIZONTAL;

    addButton = new JButton(resLocalizedStrings.getString("pathchooser.add"));
    addButton.setMnemonic(KeyEvent.VK_A);

    constr.gridy = 0;
    constr.weightx = 0.0;
    constr.weighty = 0.0;
    constr.insets = new Insets(4, 4, 4, 4);

    addFromFileSystemsButton = new JButton(
        mode == IGNORED_ON_SOURCEPATH ?
        resLocalizedStrings.getString("pathchooser.add") :
        resLocalizedStrings.getString("pathchooser.add.from.filesystems")
        );
    addFromFileSystemsButton.setMnemonic(KeyEvent.VK_F);

    constr.gridheight = 2;
    constr.anchor = GridBagConstraints.CENTER;
    constr.weighty = .9;
    constr.gridheight = 2;

    add(createButtonsPanel(), constr);

    constr.gridheight = 1;
    constr.gridx = 1;

    constr.gridy = 0;
    constr.anchor = GridBagConstraints.SOUTH;
    constr.weighty = .9;
    constr.insets = new Insets(4, 4, 2, 4);
    add(upButton, constr);

    constr.gridheight = 1;
    constr.gridy = 1;
    constr.anchor = GridBagConstraints.NORTH;
    constr.weighty = .9;
    constr.insets = new Insets(0, 4, 4, 4);
    add(downButton, constr);

    // actions
    list.getSelectionModel().addListSelectionListener(
        new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
          return;
        }

        onSelect();
      }
    });

    addButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        final JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        // we don't support jar/zip javadocs, right?
        if (mode == CLASSPATH /*|| mode == JAVADOCPATH*/) {
          chooser.setFileFilter(zipFilter);
          chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        } else {
          chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
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
          PathItemReference ref;
          if(mode == JAVADOCPATH) {
            ref = new PathItemReference(files[i].getAbsolutePath());
          } else {
            ref = VersionSpecific.getInstance().getPathItemReference(files[i]);
            //ref = new PathItemReference(files[i]);
          }
          
          data.addElement(ref);
        }
      }
    });

    if (addAllJarsButton != null) {
      addAllJarsButton.addActionListener(new ActionListener() {
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

            PathItemReference ref;
            if(mode == JAVADOCPATH) {
              ref = new PathItemReference(cur.getAbsolutePath());
            } else {
              ref = new PathItemReference(cur);
            }
            data.addElement(ref);
          }
          // added.remove(dir); - extra safety - don't remove, there are not many dirs anyway
        }
      });
    }

    if (addUrlButton != null) {
      addUrlButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String urlString = RitDialog.showInputDialog(
              IDEController.getInstance().createProjectContext(),
              "Enter URL", 
              "Add URL",
              JOptionPane.QUESTION_MESSAGE);
          if (urlString == null || urlString.trim().length() == 0) {
            return;
          }

          URL url;
          try {
            url = new URL(urlString);
          } catch (MalformedURLException ee) {
            RitDialog.showMessageDialog(
                IDEController.getInstance().createProjectContext(),
                "The entered URL is malformed", "Bad URL",
                JOptionPane.ERROR_MESSAGE);
            return;
          }

          data.addElement(new PathItemReference(url.toExternalForm()));
        }
      });
    }

    removeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int[] idxs = list.getSelectedIndices();
        int first;
        if (idxs == null || idxs.length <= 0) {
          setArrows(false);
          return;
        } else {
          first = idxs[0];
        }

        for (int i = idxs.length - 1; i >= 0; ) {
          int idx = idxs[i];
          data.removeElementAt(idx);

          for (int j = --i; j >= 0; --j) {
            if (idx < idxs[j]) {
              --idxs[j];
            }
          }
        }

        if (first >= data.getSize()) {
          first = data.getSize() - 1;
        }

        list.setSelectedIndex(first);
      }
    });

    addFromFileSystemsButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object[] result;
        if (mode == IGNORED_ON_SOURCEPATH) {
          result = TreeChooser.getNewDataObjectReferences(
              IDEController.getInstance().createProjectContext(),
              "Add from sourcepath", 
              new NBSourcepathModel(),
              new PathItemReferenceTreeCellRenderer());
        } else {
          result = TreeChooser.getNewDataObjectReferences(
              IDEController.getInstance().createProjectContext(),
              "Add from filesystems", 
              new AllNBFilesystemsModel(),
              new NBFileObjectsTreeCellRenderer());
        }

        if (result != null) {
          for (int i = 0; i < result.length; i++) {
            Object obj = result[i];
            if (obj instanceof FileObject) {
              obj = new PathItemReference((FileObject) obj);
            } else if (obj instanceof PathItemReferenceWrapper) {
              obj = ((PathItemReferenceWrapper)obj).getReference();
            }
            data.addElement(obj);
          }
        }
      }
    });
  }

  private JPanel createButtonsPanel() {
    JPanel pn = new JPanel();
    pn.setLayout(new GridLayout(0, 1));

    if (mode != IGNORED_ON_SOURCEPATH) {
      pn.add(addButton);
    }

    if (getMode() == FileObjectPathChooser.CLASSPATH
        /*|| getMode() == FileObjectPathChooser.JAVADOCPATH*/) {
      addAllJarsButton = new JButton("Add All Jars...");

      pn.add(addAllJarsButton);
      pn.add(addFromFileSystemsButton);

    } else {
      if(getMode() == FileObjectPathChooser.IGNORED_ON_SOURCEPATH) {
        pn.add(addFromFileSystemsButton);
      }
    }
    
    if (getMode() == FileObjectPathChooser.JAVADOCPATH) {
      addUrlButton = new JButton("Add URL...");
      pn.add(addUrlButton);
    }
    
    removeButton = new JButton(resLocalizedStrings.getString(
        "pathchooser.remove"));
    removeButton.setMnemonic(KeyEvent.VK_R);

    pn.add(removeButton);

    return pn;
  }

  public void setButtonsEnabled(boolean enabled) {
    addButton.setEnabled(enabled);
    removeButton.setEnabled(enabled);
    addFromFileSystemsButton.setEnabled(enabled);
    if (addAllJarsButton != null) {
      addAllJarsButton.setEnabled(enabled);
    }
    if (addUrlButton != null) {
      addUrlButton.setEnabled(enabled);
    }
  }

  public int getMode() {
    return mode;
  }

  public PathItemReference[] getPath() {
    PathItemReference[] result = new PathItemReference[data.size()];
    for (int i = 0; i < data.size(); i++) {
      result[i] = (PathItemReference) data.elementAt(i);
    }

    return result;
  }

  private void setMode(int mode) {
    this.mode = mode;
  }

  public void setPath(PathItemReference[] pathElements) {
    data.removeAllElements();

    for (int i = 0; i < pathElements.length; i++) {
      data.addElement(pathElements[i]);
    }

    if (data.getSize() > 0) {
      list.setSelectedIndex(0);
    }
  }

  void setArrows(boolean enabled) {
    upButton.setEnabled(enabled);
    downButton.setEnabled(enabled);
  }

  void onSelect() {
    int selected[] = list.getSelectedIndices();
    if (selected.length != 1) {
      setArrows(false);
      return;
    }

    setArrows(true);

    if (selected[0] == 0) {
      upButton.setEnabled(false);
    }

    if (selected[0] == (data.getSize() - 1)) {
      downButton.setEnabled(false);
    }
  }

  public void clickAdd() {
    addButton.doClick();
  }
  
  public void clickAddFromFilesystems() {
    addFromFileSystemsButton.doClick();
  }
}
