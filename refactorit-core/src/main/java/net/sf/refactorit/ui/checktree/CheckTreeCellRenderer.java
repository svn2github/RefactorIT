/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.checktree;


import net.sf.refactorit.ui.tree.BinTree;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;


/**
 * A cell editor for @link{JCheckTree}. Doesn't display tree icons by default.
 * Based on <a href="http://www.fawcette.com/archives/premier/mgznarch/javapro/2001/01jan01/vc0101/vc0101.asp">
 * code</a> by Claude Duguay.
 */
public class CheckTreeCellRenderer extends JPanel implements TreeCellRenderer {
  protected TreeCellRenderer renderer;
  protected JCheckBox check;

  public CheckTreeCellRenderer(JTree tree) {
    this(tree, new DefaultTreeCellRenderer());

    DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer)this.renderer;

    renderer.setOpenIcon(null);
    renderer.setClosedIcon(null);
    renderer.setLeafIcon(null);
  }

  public CheckTreeCellRenderer(JTree tree, TreeCellRenderer renderer) {
    super(new BorderLayout());

    this.renderer = renderer;

    setOpaque(false);

    Component comp = renderer
        .getTreeCellRendererComponent(tree, "", true, true, true, 0, true);
    add(BorderLayout.CENTER, comp);

    check = new JCheckBox();
    check.setMargin(new Insets(0, 0, 0, 0));
    check.setBorderPaintedFlat(true);
    check.setOpaque(true);
    add(BorderLayout.WEST, check);
  }

  public Component getTreeCellRendererComponent(
      JTree tree, Object value,
      boolean selected, boolean expanded, boolean leaf,
      int row, boolean hasFocus) {
    final BinTree binTree = (BinTree) tree;
    final Color background;
    final Color foreground;
    final Font font = binTree.getFont();
    if (selected) {
      background = binTree.getSelectedBackground();
      foreground = binTree.getSelectedForeground();
    } else {
      background = binTree.getBackground();
      foreground = binTree.getForeground();
    }

    if (value instanceof CheckTreeNode) {
      CheckTreeNode node = (CheckTreeNode) value;
      check.setSelected(node.isSelected());
      check.setVisible(node.isShowCheckBox());
			check.setBackground(background);
			if(!node.isFullySelected())
				check.setEnabled(false);
			else
				check.setEnabled(true);
      value = node.getUserObject();
    }

    Component comp = renderer.getTreeCellRendererComponent(
        tree, value, selected, expanded, leaf, row, hasFocus);
    
    DefaultTreeCellRenderer defaultRenderer = (DefaultTreeCellRenderer)
        renderer;
    defaultRenderer.setBackground(background);
    defaultRenderer.setBackgroundNonSelectionColor(background);
    defaultRenderer.setBackgroundSelectionColor(background);
    defaultRenderer.setForeground(foreground);
    defaultRenderer.setFont(font);

    setBackground(background);

    return this;
  }
}
