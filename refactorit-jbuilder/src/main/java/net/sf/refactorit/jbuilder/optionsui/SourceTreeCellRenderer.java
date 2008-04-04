/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder.optionsui;


import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.sf.refactorit.vfs.Source;

import java.awt.Component;


/**
 * <code>TreeCellRenderer</code> implementation for rendering
 * <code>Source</code> objects in a <code>TreeChooser</code>. Is passed to
 * <code>JIgnoredPathChooser</code> to render sourcepath elements in it's
 * "add" dialog.
 *
 * @author juri
 */
public class SourceTreeCellRenderer extends DefaultTreeCellRenderer {
  private IgnorablePathsTreeModel sourcepathModel;
  public SourceTreeCellRenderer(IgnorablePathsTreeModel sourcepathModel) {
    this.sourcepathModel = sourcepathModel;
  }

  public Component getTreeCellRendererComponent(JTree tree, Object value,
      boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    //System.out.println("RECEIVING value=" + value);
    Component component = super.getTreeCellRendererComponent(
        tree, value, sel, expanded, leaf, row, hasFocus);

    if ((value instanceof String)) {
      return component;
    }

    Source src = (Source) value;

    if (sourcepathModel.contains(src)) {
      setText(src.getAbsolutePath());
    } else {
      setText(src.getName());
    }
    return component;
  }
}
