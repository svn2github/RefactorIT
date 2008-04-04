/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.projectoptions.ui;


import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.netbeans.common.projectoptions.NBFileUtil;
import net.sf.refactorit.netbeans.common.projectoptions.PathItemReference;
import net.sf.refactorit.netbeans.common.projectoptions.PathUtil;


/**
 * All folders on sourcepath, including the ignored ones. <br><br>
 * 
 * Caches contents and initializes lazily.
 */
public class NBSourcepathModel implements TreeModel {
  List sourcepathRoots = new ArrayList();
  
  public NBSourcepathModel() {
    this(PathUtil.getInstance().getSourcepath(
        IDEController.getInstance().getActiveProjectFromIDE(), true));
  }

  public NBSourcepathModel(final PathItemReference[] sourcePathRoots) {
    for (int i = 0; i < sourcePathRoots.length; i++) {
      if(sourcePathRoots[i].isValid()) {
        if (NBFileUtil.sourceAcceptedIfNotIgnored(sourcePathRoots[i].getSource())) {
          sourcepathRoots.add(new PathItemReferenceWrapper(sourcePathRoots[i]));
        }
      }
    }
  }
  
  public List getRootDataObjects() {
    return sourcepathRoots;
  }

  public List getChildDataObjects(Object parentDataObject) {
    return ((PathItemReferenceWrapper) parentDataObject).getChildren();
  }

  /* (non-Javadoc)
   * @see javax.swing.tree.TreeModel#getRoot()
   */
  public Object getRoot() {
    return "RefactorIT Sourcepath";
  }

  /* (non-Javadoc)
   * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
   */
  public int getChildCount(Object parent) {
    if (parent instanceof String) { //if parent is the title node
      return getRootDataObjects().size();
    }
    return getChildDataObjects(parent).size();
  }

  /* (non-Javadoc)
   * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
   */
  public boolean isLeaf(Object node) {
    if (node instanceof String) { //if this parent is title node
      return false;
    }
    return!((PathItemReferenceWrapper) node).isFolder();
  }

  /* (non-Javadoc)
   * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
   */
  public void addTreeModelListener(TreeModelListener l) {}

  /* (non-Javadoc)
   * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
   */
  public void removeTreeModelListener(TreeModelListener l) {}

  /* (non-Javadoc)
   * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
   */
  public Object getChild(Object parent, int index) {
    if (parent instanceof String) { //if parent is the title node
      return getRootDataObjects().get(index);
    }
    return getChildDataObjects(parent).get(index);
  }

  /* (non-Javadoc)
   * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
   */
  public int getIndexOfChild(Object parent, Object child) {
    if (parent instanceof String) { //if parent is the title node
      return getRootDataObjects().indexOf(child);
    }
    return getChildDataObjects(parent).indexOf(child);
  }

  /* (non-Javadoc)
   * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
   */
  public void valueForPathChanged(TreePath path, Object newValue) {}
}
