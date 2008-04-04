/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.commonIDE.options;


import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.projectoptions.ProjectOptionsDialog;
import net.sf.refactorit.utils.SwingUtil;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;


public class AbstractPathEditingPanel extends JPanel {
  
  class AddDirAction implements ActionListener {
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
      

      File files[] = chooser.getSelectedFiles();
      
      for (int i = 0; i < files.length; i++) {
        addPathItem(new PathItem(files[i]));
      }
    }
  }
  
  class AddUrlAction implements ActionListener {

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
      addPathItem(new PathItem(url.toExternalForm()));
    }
    
  }


  private boolean initialized;

  public void addNotify() {
    // intializing before
    if ( !initialized ) {
      initialized=true;
      init();
    }
    
    super.addNotify();
  }
  public AbstractPathEditingPanel(
  ) {
    setLayout(new BorderLayout());

  }
  /**
   * must get called before using but not in constructor because contains calls
   * to overridden methods 
   *
   */
  private void init() {
    JScrollPane scrollpane = new JScrollPane(list);

    scrollpane.setMaximumSize(scrollpaneSize);
    scrollpane.setMinimumSize(scrollpaneSize);
    scrollpane.setPreferredSize(scrollpaneSize);
    scrollpane.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

    add(scrollpane, BorderLayout.CENTER);
    add(createButtonsPanel(), BorderLayout.EAST) ;
  }
  
  public boolean containsPathItem(PathItem item) {
    return listModel.contains(item);
  }


  DefaultListModel listModel = new DefaultListModel();
  JList list = new JList(listModel);

  protected JButton addButton = new JButton("Add");
  protected JButton addUrlButton = new JButton("Add URL");
  protected JButton removeButton = new JButton("Remove");

  protected JButton upButton = new ProjectOptionsDialog.UpButton(list, listModel);
  protected JButton downButton = new ProjectOptionsDialog.DownButton(list,
      listModel);

  protected static final Dimension scrollpaneSize = new Dimension(300, 200);

  public PathItem[] getPathItems() {
    Enumeration elements = listModel.elements();

    List elList = CollectionUtil.toList(elements);
    return (PathItem[]) elList.toArray(new PathItem[elList.size()]);
  }
  /**
   * Sets enabled status for panel
   */
  public void setEnabled(boolean b) {
    super.setEnabled(b);

    this.addButton.setEnabled(b);
    this.addUrlButton.setEnabled(b);
    this.removeButton.setEnabled(b);
    this.upButton.setEnabled(b);
    this.downButton.setEnabled(b);
    this.list.setEnabled(b);
  }


  public void setContents(List pathItemList) {
    listModel.clear();
    for (int i = 0; i < pathItemList.size(); i++) {
      addPathItem((PathItem) pathItemList.get(i));
    }
  }


  private boolean autodetect;

  public void setAutoDetect() {
    autodetect = true;
  }

  public boolean isAutodetect() {
    return this.autodetect;
  }
  
  protected JPanel createButtonsPanel() {
    JPanel contents = new JPanel();

    contents.setLayout(new BorderLayout());

    JComponent[] components = getButtons();

    JPanel buttons = SwingUtil.combineInNorth(components);
    contents.add(buttons, BorderLayout.NORTH);


    removeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int[] selection = list.getSelectedIndices();
        for (int i = selection.length - 1; i >= 0; i--) {
          listModel.remove(selection[i]);
        }
      }
    });

    return contents;
  }

  protected JComponent[] getButtons() {
    return new JComponent[] {
        addButton, removeButton, upButton, downButton
    };
  }
  protected void addPathItem(PathItem item) {
    if ( !listModel.contains(item)) {
      listModel.addElement(item);
    }
  }
  
  public void selectNone() {
    list.clearSelection();
  }
  
  public void removeAllElement() {
    this.listModel.removeAllElements(); 
  }
}
