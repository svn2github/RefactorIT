/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.apidiff;


import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.ui.UIResources;
import net.sf.refactorit.ui.tree.NodeIcons;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import java.awt.Component;
import java.awt.Graphics;


// TODO: BinTreeCellRenderer?
public class ApiDiffCellRenderer extends JLabel implements TreeCellRenderer {
  private static final ImageIcon added
      = ResourceUtil.getIcon(UIResources.class, "ItemAdded.gif");
  private static final ImageIcon removed
      = ResourceUtil.getIcon(UIResources.class, "ItemRemoved.gif");

  public Component getTreeCellRendererComponent(
      JTree tree, Object value, boolean selected, boolean expanded,
      boolean leaf, int row, boolean hasFocus) {
    Icon icon1 = getIcon(value);
    ApiDiffModel.DiffNode node = (ApiDiffModel.DiffNode) value;
    Icon icon2 = NodeIcons.getBinIcon(node.getType(), node.getBin(), false);

    setIcon(new SplittedIcon(icon1, icon2));
    setText(value.toString());
    setHorizontalAlignment(JLabel.LEFT);

    return this;
  }

  private Icon getIcon(Object value) {
    ApiDiffModel.DiffNode node = (ApiDiffModel.DiffNode) value;

    if (node.isAddedNode()) {
      return added;
    }

    if (node.isRemovedNode()) {
      return removed;
    }

    return null;
  }

  class SplittedIcon implements Icon {
    private int w = 0;
    private int h = 0;

    private Icon icon1;
    private Icon icon2;

    public SplittedIcon(Icon icon1, Icon icon2) {
      this.icon1 = icon1;
      this.icon2 = icon2;

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

    public int getIconHeight() {
      return h;
    }

    public int getIconWidth() {
      return w;
    }

    public void paintIcon(Component c, Graphics g, int x, int y) {
      if (icon1 != null) {
        icon1.paintIcon(c, g, x, y);
      }

      if (icon2 != null) {
        if (icon1 != null) {
          icon2.paintIcon(c, g, x + icon1.getIconWidth() + 4, y - 2);
        } else {
          icon2.paintIcon(c, g, x, y - 8);
        }
      }
    }
  }
}
