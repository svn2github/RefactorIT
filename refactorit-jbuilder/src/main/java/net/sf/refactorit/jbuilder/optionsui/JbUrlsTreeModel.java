/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder.optionsui;


import com.borland.jbuilder.node.JBProject;
import com.borland.jbuilder.paths.ProjectPathSet;
import com.borland.primetime.util.RegularExpression;
import com.borland.primetime.vfs.Url;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.jbuilder.RefactorItPropGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * A <code>javax.swing.tree.TreeModel</code> implementation for use with a
 * <code>@see net.sf.refactorit.ui.options.TreeChooser</code>. Is used in
 * <code>JBClasspathChooser</code> in it's "add from projects paths" usecase.
 *
 * @author juri
 */
public class JbUrlsTreeModel implements TreeModel {
  private List rootDataObjects;

  public JbUrlsTreeModel() {
    rootDataObjects = new ArrayList();

    JBProject jbProject = RefactorItPropGroup.getActiveProject();
    ProjectPathSet paths = jbProject.getPaths();

    CollectionUtil.addAllNew(rootDataObjects, Arrays.asList(paths.getSourcePath()));
    CollectionUtil
        .addAllNew(rootDataObjects, Arrays.asList(paths.getFullClassPath()));
    CollectionUtil.addAllNew(rootDataObjects, Arrays.asList(paths.getDocPath()));
    CollectionUtil.addNew(rootDataObjects, paths.getBakPath());
    CollectionUtil.addNew(rootDataObjects, paths.getLibPath());
    CollectionUtil.addNew(rootDataObjects, paths.getOutPath());
  }

  public List getRootDataObjects() {
    return rootDataObjects;
  }

  public List getChildDataObjects(Object parentDataObject) {
    List result = new ArrayList();
    Url url = (Url) parentDataObject;
    Url[] children = url.getFilesystem().getChildren(url,
        new RegularExpression[0],
        com.borland.primetime.vfs.Filesystem.TYPE_DIRECTORY);
    for (int i = 0; i < children.length; i++) {
      result.add(children[i]);
    }

    Collections.sort(result, new Comparator() {
      public int compare(Object a, Object b) {
        return ((Url) a).getFullName().compareTo(((Url) b).getFullName());
      }
    });

    return result;
  }

  /**
   * @see javax.swing.tree.TreeModel#getRoot()
   */
  public Object getRoot() {
    return "All filesystems";
  }

  /**
   * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
   */
  public int getChildCount(Object parent) {
    if (parent instanceof String) { // if it's the title node
      return rootDataObjects.size();
    }
    Url url = (Url) parent;
    Url[] children = url.getFilesystem().getChildren(url,
        new RegularExpression[0],
        com.borland.primetime.vfs.Filesystem.TYPE_DIRECTORY);
    return children.length;
  }

  /**
   * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
   */
  public boolean isLeaf(Object node) {
    if (node instanceof String) { // if it's the title node
      return false;
    }
    return!((Url) node).getFileObject().isDirectory();
  }

  /**
   * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.TreeModelListener)
   */
  public void addTreeModelListener(TreeModelListener l) {
  }

  /**
   * @see javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.TreeModelListener)
   */
  public void removeTreeModelListener(TreeModelListener l) {
  }

  /**
   * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
   */
  public Object getChild(Object parent, int index) {
    if (parent instanceof String) { // if it's the title node
      return rootDataObjects.get(index);
    }
    List children = getChildDataObjects(parent);
    return children.get(index);
  }

  /**
   * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object,
   *      java.lang.Object)
   */
  public int getIndexOfChild(Object parent, Object child) {
    if (parent instanceof String) { // if it's the title node
      return rootDataObjects.indexOf(child);
    }
    List children = getChildDataObjects(parent);
    return children.indexOf(child);
  }

  /**
   * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath,
   *      java.lang.Object)
   */
  public void valueForPathChanged(TreePath path, Object newValue) {
  }

}
