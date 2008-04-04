/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.options;


import net.sf.refactorit.vfs.Source;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;

import java.awt.Component;

/**
 * Is used to render Source objects in a TreeChooser such that root nodes are
 * shown in full absolute path, while for all other nodes only the last path
 * element is used
 * 
 * @author juri
 */
public class TreeChooserSourceRenderer extends DefaultTreeCellRenderer {

  public TreeChooserSourceRenderer(TreeModel dataModel) {
    this.dataModel=dataModel;
  }
  /**
   * Is used to check if the node is root (as root nodes are shown in full
   * absolute path, while for all other nodes only the last path element is
   * used)
   */ 
  public TreeModel dataModel;
  
  public Component getTreeCellRendererComponent(JTree tree, Object value,
      boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
        row, hasFocus);
    if ((value instanceof String)) {
      return this;
    }    
    Source source = (Source)value;
    if(isRoot(source)) {
      setText(source.getAbsolutePath());
    }
    else {
      setText(source.getName());
    }
    return this;
  }
  
  private boolean isRoot(Source source) {
    Object superRoot=dataModel.getRoot();
    int rootCount=dataModel.getChildCount(superRoot);
    for (int i = 0; i < rootCount; i++) {
      Object root=dataModel.getChild(superRoot,i);
      if(source.equals(root)) {
        return true;
      }
    }
    return false;
  }
  
}
