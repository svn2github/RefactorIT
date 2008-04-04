/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.commonIDE.options;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.vfs.Source;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 *
 *
 * @author Tonis Vaga
 * 
 * TODO: don't show excluded(existing) sourcepath items
 */
public class ProjectSourcePathModel implements TreeModel {
  
  /**
   * List of {@link PathItem}'s to exclude
   */
  private List itemsToExclude=new ArrayList();
  
  public ProjectSourcePathModel() {
  }

  public Source[] getRootDataObjects() {
    Project project = IDEController.getInstance().getActiveProject();
    return project.getPaths().getSourcePath().getPossibleRootSources();
  }

  /* (non-Javadoc)
   * @see javax.swing.tree.TreeModel#getRoot()
   */
  public Object getRoot() {
    return "Sourcepath roots";
  }

  /* (non-Javadoc)
   * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
   */
  public int getChildCount(Object parent) {
    if (parent instanceof String) {
      return getRootDataObjects().length;
    }
    return ((Source) parent).getChildren().length;
  }

  /* (non-Javadoc)
   * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
   */
  public boolean isLeaf(Object node) {
    if (node instanceof String) {
      return false;
    }
    return!((Source) node).isDirectory();
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
    if (parent instanceof String) {
      return getRootDataObjects()[index];
    }
    return ((Source) parent).getChildren()[index];
  }

  /* (non-Javadoc)
   * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
   */
  public int getIndexOfChild(Object parent, Object child) {
    if (parent instanceof String) {
      return Arrays.asList(getRootDataObjects()).indexOf(child);
    }
    return Arrays.asList(((Source) parent).getChildren()).indexOf(child);
  }

  /* (non-Javadoc)
   * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
   */
  public void valueForPathChanged(TreePath path, Object newValue) {}

  /**
   * @param items
   */
  public void addExcludeItems(PathItem[] items) {
    
    for (int i = 0; i < items.length; i++) {
      itemsToExclude.add(items[i]);
    }
  }
  public void addExcludeItem(PathItem item) {
    itemsToExclude.add(item);
  }
}
