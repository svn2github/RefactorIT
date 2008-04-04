/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.vfs.Source;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.ResourceBundle;


/**
 * @author juri
 */
public class JIgnoredPathChooser extends JComponent{

  protected static ResourceBundle resLocalizedStrings =
      ResourceUtil.getBundle(JPathChooser.class);

  DefaultListModel data;

  JList list;

  final TreeModel treeChooserModel;
  final TreeCellRenderer treeChooserCellRenderer;
  

  public JIgnoredPathChooser(TreeModel treeChooserModel, TreeCellRenderer treeChooserCellRenderer) {
    
    this.treeChooserCellRenderer=treeChooserCellRenderer;
    this.treeChooserModel=treeChooserModel;
    
    setBorder(BorderFactory.createEtchedBorder());

    setLayout(new GridBagLayout());

    GridBagConstraints constr = new GridBagConstraints();

    // path components
    data = new DefaultListModel();
    list = new JList( data );
    constr.gridx = 0;
    constr.gridy = 0;
    constr.fill = GridBagConstraints.BOTH;
    constr.weightx = 1.0;
    constr.weighty = 1.0;
    constr.insets = new Insets(5, 5, 5, 5);
    add(new JScrollPane(list), constr);

    createButtonPanel();

  }

  private void createButtonPanel() {
    JPanel layer = new JPanel();
    layer.setLayout(new BoxLayout(layer, BoxLayout.Y_AXIS));

    JPanel addRemovePanel = new JPanel(new GridLayout(0, 1, 1, 1));
    addRemovePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

    fillAddButtonPanel(addRemovePanel);

    JButton removeButton = new JButton(resLocalizedStrings.getString("pathchooser.remove"));
    removeButton.setMnemonic(KeyEvent.VK_R);

    removeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int[] idxs = list.getSelectedIndices();
        if (idxs == null || idxs.length <= 0) {
          return;
        }

        int first = idxs[0];
        
        for (int i = idxs.length-1; i >= 0;i--) {
          int idx = idxs[i];
          data.removeElementAt(idx);
        }

        if (first >= data.getSize()) {
          first = data.getSize() - 1;
        }

        list.setSelectedIndex(first);
      }
    });

    addRemovePanel.add(removeButton);
    
    layer.add(addRemovePanel);
    
    GridBagConstraints constr = new GridBagConstraints();
    constr.gridx = 1;
    constr.gridy = 0;
    constr.fill = GridBagConstraints.HORIZONTAL;
    constr.anchor = GridBagConstraints.CENTER;
    constr.weightx = 0.0;
    constr.weighty = 0.0;
    constr.insets = new Insets(5, 0, 5, 5);

    add(layer, constr);
  }

  protected void fillAddButtonPanel(JPanel panel) {
    JButton addButton = new JButton(resLocalizedStrings.getString("pathchooser.add"));
    addButton.setMnemonic(KeyEvent.VK_A);
    addButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {      
        Object[] selection = TreeChooser.getNewDataObjectReferences(
            IDEController.getInstance().createProjectContext(),
            "Ignored path chooser", treeChooserModel, treeChooserCellRenderer);
        if (selection == null) {
          return;
        }

        for (int i = 0; i < selection.length; i++) {
          if (data.contains(selection[i])) {
            continue;
          }

          data.addElement(selection[i]);
        }

        return;
      }
    });
    panel.add(addButton);
  }



  public void setEnabled(boolean enabled){
    super.setEnabled(enabled);
    setEnabledReq(this,enabled);
    selectNone();
  }
  
  private void setEnabledReq(Container parent, boolean enabled){
    Component[] components=parent.getComponents();
    for (int i = 0; i < components.length; i++) {
      if(components[i] instanceof Container)
        setEnabledReq((Container)components[i], enabled);
      components[i].setEnabled(enabled);
    }
  }
  


  public Path getPath() {
    if (data.isEmpty()) {
        return SourcePath.EMPTY;
    }

    StringBuffer buf = new StringBuffer();

    Enumeration e = data.elements();
    buf.append(((Source)e.nextElement()).getAbsolutePath());

    while (e.hasMoreElements()) {
      buf.append(File.pathSeparator).append(((Source)e.nextElement()).getAbsolutePath());
    }

    String path = buf.toString();

    return new SourcePath(path);
  }
  /**
   * convenience method; the same as <code>getPath().toString()</code>
   *
   */
  public String getPathString(){
    return getPath().toString();
  }
  
  /**
   * Passes given renderer to the internal <code>JList</code> that is used to 
   * render the chosen path elements. Thus <code>JPathChooser</code> gets 
   * abstract enough to render whatever kind of objects representing paths.   
   *
   */
  public void setCellRenderer(ListCellRenderer renderer) {
    list.setCellRenderer(renderer);
  }

  public void setContent(Collection paths) {
    data.removeAllElements();
    Iterator i=paths.iterator();
    while(i.hasNext()) {
      data.addElement(i.next());
    }
  }

//  public void setPath(Path path) {
//    data.removeAllElements();
//
//    StringTokenizer t = new StringTokenizer(
//        path.toString(), File.pathSeparator);
//        Set redundancyFilter=new HashSet(t.countTokens()); 
//        while (t.hasMoreTokens()) {
//          redundancyFilter.add(t.nextToken());
//        }
//        Iterator i=redundancyFilter.iterator();
//        while (i.hasNext()) {
//          data.addElement(JBSource.getSource(new File((String)i.next())));
//        }
//    
//    if (data.getSize() > 0) {
//      list.setSelectedIndex(0);
//    }
//  }

  /**
   * removes all path elements from the model of this chooser
   *
   */
  public void removeAllElements() {
    this.data.removeAllElements(); 
  }
  
  public void selectNone() {
    list.clearSelection();
  }
}
