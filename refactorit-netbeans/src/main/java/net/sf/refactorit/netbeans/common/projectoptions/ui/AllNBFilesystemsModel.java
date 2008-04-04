/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.projectoptions.ui;



import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.netbeans.common.VersionSpecific;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;


public class AllNBFilesystemsModel implements TreeModel {

  public List getRootDataObjects() {
    List result = new ArrayList();
    Object ideProject = IDEController.getInstance().getActiveProjectFromIDE();
    if(ideProject == null) {
      return Collections.EMPTY_LIST;
    }
    
    FileObject[] roots = VersionSpecific.getInstance().getIdeSourcepath(ideProject);
    for (int i = 0; i < roots.length; i++) {
      try {
        if (roots[i].isValid() 
            && roots[i].canWrite() 
            && (!roots[i].getFileSystem().isDefault())
            && roots[i].isValid()) {
          result.add(roots[i]);
        }
      } catch (FileStateInvalidException e) {
        AppRegistry.getExceptionLogger().error(e,this);
      }
    }
    return result;
  }

  public List getChildDataObjects(Object parentDataObject) {
    List result = new ArrayList();
    for (Enumeration en = ((FileObject) parentDataObject).getFolders(false);
        en.hasMoreElements(); ) {
      result.add(en.nextElement());
    }

    Collections.sort(result, new Comparator() {
      public int compare(Object a, Object b) {
        return (((FileObject) a).getNameExt().compareTo(((FileObject) b).
            getNameExt()));
      }
    });

    return result;
  }

  /* (non-Javadoc)
   * @see javax.swing.tree.TreeModel#getRoot()
   */
  public Object getRoot() {
    return "Filesystems";
  }

  /* (non-Javadoc)
   * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
   */
  public int getChildCount(Object parent) {
    if (parent instanceof String) { //if this parent is title node
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
    return!((FileObject) node).isFolder();
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
      return getRootDataObjects().get(index);
    }
    return getChildDataObjects(parent).get(index);
  }

  /* (non-Javadoc)
   * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object, java.lang.Object)
   */
  public int getIndexOfChild(Object parent, Object child) {
    if (parent instanceof String) {
      return getRootDataObjects().indexOf(child);
    }
    return getChildDataObjects(parent).indexOf(child);
  }

  /* (non-Javadoc)
   * @see javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath, java.lang.Object)
   */
  public void valueForPathChanged(TreePath path, Object newValue) {}

}
