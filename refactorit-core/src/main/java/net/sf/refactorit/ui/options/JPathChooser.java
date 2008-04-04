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
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.ui.UIResources;
import net.sf.refactorit.utils.ClasspathUtil;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

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
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;


/**
 * @author Igor Malinin
 * @author Vladislav Vislogubov //added UP/DOWN buttons and their logic
 */
public abstract class JPathChooser extends JComponent {
  protected static ResourceBundle resLocalizedStrings =
      ResourceUtil.getBundle(JPathChooser.class);

  public static final int CLASSPATH  = 0;
  public static final int SOURCEPATH = 1;
  public static final int JAVADOCPATH = 2;
 
  static final FileFilter zipFilter = new FileExtensionFilter(
      new String[] { ".jar", ".zip" },
      resLocalizedStrings.getString("pathchooser.filter"));

  protected int mode;
  protected DefaultListModel data;

  protected JList list;

//  private JButton addButton;
//  private JButton addAllJarsButton;
//  private JButton addUrlButton;
//  private JButton removeButton;

  private JButton upButton = new JButton(
      ResourceUtil.getIcon(UIResources.class, "arrow_up.gif"));
  private JButton downButton = new JButton(
      ResourceUtil.getIcon(UIResources.class, "arrow_down.gif"));
  
  JPathChooser(int aMode) {
    setMode(aMode);
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

    upButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int selected[] = list.getSelectedIndices();
        if (selected.length != 1 || selected[0] == 0) {
          return;
        }

        Object item = data.set(selected[0] - 1, data.getElementAt(selected[0]));
        data.set(selected[0], item);

        list.setSelectedIndex(selected[0] - 1);
        list.ensureIndexIsVisible(selected[0] - 1);
      }
    });

    downButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int selected[] = list.getSelectedIndices();
        if (selected.length != 1 || selected[0] == data.getSize() - 1) {
          return;
        }

        Object item = data.set(selected[0] + 1, data.getElementAt(selected[0]));
        data.set(selected[0], item);
        list.setSelectedIndex(selected[0] + 1);
        list.ensureIndexIsVisible(selected[0] + 1);
      }
    });
  }
  
  
  public static JPathChooser getJPathChooser(int aMode) {
    switch(aMode){
      case SOURCEPATH: return new JSourcepathChooser();
      case CLASSPATH: return new JClasspathChooser();
      case JAVADOCPATH: return new JJavadocpathChooser();
      default:throw new RuntimeException("mode "+aMode+" is unknown in JPathChooser");
    }
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
          setArrows(false);
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
    
    JPanel upDownPanel = new JPanel(new GridLayout(0, 1, 1, 1));
    upDownPanel.add(upButton);
    upDownPanel.add(downButton);
    upButton.setEnabled(false);

    layer.add(upDownPanel);

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

  protected abstract void fillAddButtonPanel(JPanel panel);

  public void setFile(File f) {
    data.removeAllElements();
    data.addElement(f.getAbsolutePath());
  }

  public void addFile(File f) {
    if(!contained(f)) {
      data.addElement(f.getAbsolutePath());
    }
  }

  public boolean removeFile(File f) {
    for (int i = 0; i < data.getSize(); i++) {
      File f2 = new File(data.getElementAt(i).toString());
      if (f2.equals(f)) {
        data.remove(i);
        return true;
      }
    }

    return false;
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
  
  private boolean contained(File fileWanted) {
    for( int i = 0; i < data.getSize(); i++) {
      File f = new File(data.getElementAt(i).toString());
      if (f.equals(fileWanted)) {
        return true;
      }
    }

    return false;
  }
  
  
  private static ClassPath getDefaultClasspath() {
    return new ClassPath(ClasspathUtil.getDefaultClasspath());
  }

//  public void setButtonsEnabled(boolean enabled) {
//    addButton.setEnabled(enabled);
//    removeButton.setEnabled(enabled);
//
//    if (addAllJarsButton != null) {
//      addAllJarsButton.setEnabled(enabled);
//    }
//
//    if (addUrlButton != null) {
//      addUrlButton.setEnabled(enabled);
//    }
//  }

  public int getMode() {
    return mode;
  }

  public abstract Path getPath();


  /**
   * convenience method; the same as getPath().toString();
   * @return
   */
  public String getPathString(){
    return getPath().toString();
  }

  private void setMode(int mode) {
    this.mode = mode;
  }

  /**
   * returns <code>ListModel</code> of this <code>JPathChooser</code> that 
   * could be used by an adapter for eg. ignored sourcepath dialog (working with 
   * the model is more efficient than using <code>getPath</code> and extracting 
   * it's elements)
   * 
   * (added by juri reinsalu)
   * 
   * @return <code>ListModel</code> of this <code>JPathChooser</code>  
   */
  public DefaultListModel getListModel(){
    return data;
  } 

  /**
   * convenience alternative to <code>setPath</code>
   * 
   * @param pathElements strings representing absolute paths
   */
  public void setContent(List pathElements){
    data.removeAllElements();
    Iterator i=pathElements.iterator();
    while (i.hasNext()) {
      Object pathItem=i.next();
      if(!data.contains(pathItem))
        data.addElement(pathItem);
    }
    if (data.getSize() > 0) {
      list.setSelectedIndex(0);
    }
  }
  
  public abstract void setPath(Path path);

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
  /**
   * convenience method to add path items with a check for redundancy (no 
   * matching items will be added)
   * 
   * @param obj
   */
  protected void addPathItem(Object pathItem) {
    if(data.contains(pathItem))return;
    data.addElement(pathItem);
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
  
  /**
   * sets 
   *
   */
  public void selectNone() {
    list.clearSelection();
  }
  
  /**
	 * removes all path elements that are presently in this chooser model.
	 * convenience method that delegates call to the list data model.
	 */
	public void removeAllElements() {
	  data.removeAllElements();
	}
  
}
