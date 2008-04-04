/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.treetable;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.ui.tree.BinTreeCellRenderer;
import net.sf.refactorit.ui.tree.NodeIcons;
import net.sf.refactorit.ui.tree.UITreeNode;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import java.awt.Component;
import java.awt.Graphics;


/**
 * @author Vladislav Vislogubov
 * @author Igor Malinin
 */
public final class CheckBoxCellRenderer extends BinTreeCellRenderer {
  static final ImageIcon iconChecked =
      ResourceUtil.getIcon(NodeIcons.class, "checked.gif");
  static final ImageIcon iconUnchecked =
      ResourceUtil.getIcon(NodeIcons.class, "unchecked.gif");
  static final ImageIcon iconCheckedGray =
      ResourceUtil.getIcon(NodeIcons.class, "checked_gray.gif");

  static final class SplittedIcon implements Icon {
    private int w = 0;
    private int h = 0;

    private final Icon icon1;
    private Icon icon2;

    public SplittedIcon(
        final boolean isCheckBoxNeeded, final boolean gray,
        final boolean isSelected, final Icon icon2) {
      this.icon2 = icon2;
      if (isCheckBoxNeeded) {
        if (isSelected) {
          if (gray) {
            this.icon1 = iconCheckedGray;
          } else {
            this.icon1 = iconChecked;
          }
        } else {
          this.icon1 = iconUnchecked;
        }
      } else {
        this.icon1 = icon2;
        this.icon2 = null;
      }

      if (this.icon1 != null) {
        w += this.icon1.getIconWidth();
      }

      if (this.icon2 != null) {
        w += this.icon2.getIconWidth() + 4;
      }

      if (this.icon1 != null) {
        h = this.icon1.getIconHeight();
      }
    }

    public final int getIconHeight() {
      return h;
    }

    public final int getIconWidth() {
      return w;
    }

    public final void paintIcon(
        final Component c, final Graphics g, final int x, final int y) {
      // g.setColor(c.getBackground());
      // g.fillRect(x, y, w, h);

      if (icon1 != null) {
        icon1.paintIcon(c, g, x, y);
      }

      if (icon2 != null) {
        icon2.paintIcon(c, g, x + icon1.getIconWidth() + 4, y);
      }
    }
  }


  /*
   * @see net.sf.refactorit.ui.tree.BinTreeCellRenderer#getIconFor(net.sf.refactorit.ui.tree.UITreeNode, boolean)
   */
  protected Icon getIcon(UITreeNode node, boolean expanded) {
    if (node instanceof ParentTreeTableNode) {
      final ParentTreeTableNode pnode = (ParentTreeTableNode) node;

      return new SplittedIcon(
          pnode.isCheckBoxNeeded(), pnode.isGreyed(), pnode.isSelected(),
          NodeIcons.getBinIcon(node.getType(), node.getBin(), expanded));
    }

    return super.getIcon(node, expanded);
  }
}
