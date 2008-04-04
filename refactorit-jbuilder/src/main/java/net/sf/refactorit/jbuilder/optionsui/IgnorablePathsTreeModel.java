/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder.optionsui;


import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sf.refactorit.jbuilder.vfs.JBSourcePath;
import net.sf.refactorit.ui.options.JPathChooser;
import net.sf.refactorit.vfs.Source;

import java.util.Arrays;


/**
 * A <code>TreeModel</code> implementation for ignored sourcepath
 * <code>TreeChooser</code>. It relies on a sourcepath provided by
 * sourcepath's ui dialog: <code>JPathChooser</code>. So that the model is up
 * to date even if sourcepath from the dialog hasn't been saved to persistance
 * yet.
 *
 * @author juri
 */
public class IgnorablePathsTreeModel implements TreeModel {

  JPathChooser sourcePathEditorAsModelProvider;

  public IgnorablePathsTreeModel(JPathChooser sourcepathEditor) {
    this.sourcePathEditorAsModelProvider = sourcepathEditor;
  }

  /** (non-Javadoc)
   * @see javax.swing.tree.TreeModel#getRoot()
   */
  public Object getRoot() {
    return "sourcepath";
  }

  boolean contains(Object element) {
    return sourcePathEditorAsModelProvider.getListModel().contains(element);
  }

  /** (non-Javadoc)
   * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
   */
  public int getChildCount(Object parent) {
    //if it's the root (title) node
    if (parent instanceof String) {
      return sourcePathEditorAsModelProvider.getListModel().getSize();
    }
    Source[] children = ((Source) parent).getChildren(new SourcepathFilter());
    return children.length;
  }

  /** (non-Javadoc)
   * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
   */
  public boolean isLeaf(Object node) {
    // if it's the root node
    if (node instanceof String) {
      return false;
    }

    return!((Source) node).isDirectory();
  }

  /**
   * this method is not implemented as the model do not change while the <code>TreeChooser</code>
   * dialog is open
   * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
   */
  public void addTreeModelListener(TreeModelListener l) {}

  /**
   * this method is not implemented as the model do not change while the <code>TreeChooser</code>
   * dialog is open
   * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
   */
  public void removeTreeModelListener(TreeModelListener l) {}

  /**
   * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
   */
  public Object getChild(Object parent, int index) {
    if (parent instanceof String) {
      return sourcePathEditorAsModelProvider.getListModel().getElementAt(index);
    }
    Source[] children = ((Source) parent).getChildren(new SourcepathFilter());
    return children[index];
  }

  /** (non-Javadoc)
   * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
   */
  public int getIndexOfChild(Object parent, Object child) {
    if (parent instanceof String) {
      return sourcePathEditorAsModelProvider.getListModel().indexOf(child);
    }
    Source[] children = ((Source) parent).getChildren(new SourcepathFilter());
    return Arrays.asList(children).indexOf(child);
  }

  /**
   * not implemented as user can't chage the values in this case
   *
   * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
   */
  public void valueForPathChanged(TreePath path, Object newValue) {}

  static class SourcepathFilter implements Source.SourceFilter {
    JBSourcePath jbSourcepath = JBSourcePath.getActiveJBSourcePath();
    /**
     * @see net.sf.refactorit.vfs.Source.SourceFilter#accept(net.sf.refactorit.vfs.Source)
     */
    public boolean accept(Source source) {
      return (source.isDirectory() || jbSourcepath.isValidSource(source.getName()));
    }
  }

}
