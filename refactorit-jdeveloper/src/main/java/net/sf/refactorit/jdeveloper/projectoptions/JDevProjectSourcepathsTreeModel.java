/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jdeveloper.projectoptions;


import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.jdeveloper.vfs.JDevSource;
import net.sf.refactorit.jdeveloper.vfs.JDevSourcePath;
import net.sf.refactorit.vfs.Source;
import net.sf.refactorit.vfs.SourcePath;

import java.util.Arrays;

/**
 * Provides a <code>TreeModel</code> for
 * <code>JIgnoredSourcepathChooser</code>. Is used for configuration of
 * <code>JIgnoredSourcepathChooser</code> and it's
 * <code>TreeChooserSourceRenderer</code>
 * 
 * @author juri
 */
public class JDevProjectSourcepathsTreeModel implements TreeModel {

  /**
   * @see javax.swing.tree.TreeModel#getRoot()
   */

  public Object getRoot() {
    return "all source paths";
  }

  
  /**
   * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
   */
  public int getChildCount(Object parent) {
    return getChildren(parent).length;
  }
  
  /**
   * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
   */
  public boolean isLeaf(Object node) {
    if(node instanceof String) {// title node
      return false;
    }
    return node instanceof JDevSource;
  }

  /** method stub
   * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
   */
  public void addTreeModelListener(TreeModelListener l) {}

  /** method stub
   * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
   */
  public void removeTreeModelListener(TreeModelListener l) {}

  /**
   * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
   */
  public Object getChild(Object parent, int index) {
    return getChildren(parent)[index];
  }

  /**
   * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
   */
  public int getIndexOfChild(Object parent, Object child) {
    return Arrays.asList(getChildren(parent)).indexOf(child);
  }

  /** method stub
   * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
   */
  public void valueForPathChanged(TreePath path, Object newValue) {}
  
  private static Source[] getChildren(Object parent) {
    if (parent instanceof String) {// title node
      return getRootSources();
    }
    Source source=(Source)parent;
    return source.getChildren();
  }
  
  private static Source[] getRootSources() {
    IDEController jDevController=(IDEController)IDEController.getInstance();
    Project activeProject=jDevController.getActiveProject();
    SourcePath sourcePath = activeProject.getPaths().getSourcePath();
    JDevSourcePath jdevSourcePath = (JDevSourcePath) sourcePath;
    return jdevSourcePath.getAutodetectedRootSources();
  }
  
}
