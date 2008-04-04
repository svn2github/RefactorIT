/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.projectoptions.ui;


import net.sf.refactorit.common.util.ChainableRuntimeException;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import java.awt.Component;


/**
 * @author juri
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class NBFileObjectsTreeCellRenderer extends DefaultTreeCellRenderer {
  public Component getTreeCellRendererComponent(JTree tree, Object value,
      boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    Component component = super.getTreeCellRendererComponent(
        tree, value, sel, expanded, leaf, row, hasFocus);
    if ((value instanceof String)) {
      return component;
    }
    FileObject fileObject = ((FileObject) value);
    if (fileObject.isRoot()) {
      try {
        setText(fileObject.getFileSystem().getDisplayName());
      } catch (FileStateInvalidException e) {
        throw new ChainableRuntimeException("Invalid filesystem!", e);
      }
    } else {
      setText(fileObject.getNameExt());
    }
    return component;
  }

}
