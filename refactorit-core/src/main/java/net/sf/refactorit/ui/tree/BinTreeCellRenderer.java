/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.tree;


import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.ui.module.RefactoringStatusIcons;
import net.sf.refactorit.ui.treetable.ErrorTabNode;
import net.sf.refactorit.ui.treetable.ParentTreeTableNode;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.TreeCellRenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;



/**
 * Basic TreeCellRenderer used by all RefactorIT trees.
 *
 * @author Igor Malinin
 */
public class BinTreeCellRenderer extends JPanel implements TreeCellRenderer {
  // FIXME move to NodeIcons ?
  private static final ImageIcon errorIcon =
      ResourceUtil.getIcon(NodeIcons.class, "error-badge.gif");

  /** Label with an icon and primary text. */
  protected final JLabel primary = new JLabel();

  /** Label with a secondary text. */
  protected final JLabel secondary = new JLabel();

  /** Color to use for the border for selected nodes. */
  protected Color borderSelectionColor;

// TODO drawsFocusBorderAroundIcon
//  /** True if draws focus border around icon as well. */
//  protected boolean drawsFocusBorderAroundIcon;

  // state

  /** Is the value currently selected. */
  protected boolean selected;

  /** True if has focus. */
  protected boolean hasFocus;

  public BinTreeCellRenderer() {
    super(new BorderLayout(4, 0));

    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));

    add(primary, BorderLayout.CENTER);
    add(secondary, BorderLayout.EAST);
  }

  /*
   * @see TreeCellRenderer#getTreeCellRendererComponent(
   *    JTree, Object, boolean, boolean, boolean, int, boolean)
   */
  public final Component getTreeCellRendererComponent(
      JTree tree, Object value,
      boolean selected, boolean expanded, boolean leaf,
      int row, boolean hasFocus
      ) {
    Assert.must((value instanceof UITreeNode), "UITreeNode expected as value!");

    this.selected = selected;
    this.hasFocus = hasFocus;

    setEnabled(tree.isEnabled());
    setComponentOrientation(tree.getComponentOrientation());

    BinTree binTree = (BinTree) tree;

    Color background;
    Color foreground;
    if (selected) {
      background = binTree.getSelectedBackground();
      foreground = binTree.getSelectedForeground();
    } else {
      background = binTree.getBackground();
      foreground = binTree.getForeground();
    }

    Font font = tree.getFont();

    if (value instanceof ParentTreeTableNode) {
      final ParentTreeTableNode node = (ParentTreeTableNode) value;
      if (node.isHidden()) {
        font = font.deriveFont(Font.ITALIC);
      }
    }

    setBackground(background);
    setForeground(foreground);
    setFont(font);

    UITreeNode node = (UITreeNode) value;

    primary.setBackground(background);
    primary.setForeground(foreground);
    primary.setFont(font);

    primary.setText(node.getDisplayName());

    String text = node.getSecondaryText();
    if (text == null) {
      secondary.setVisible(false);
    } else {
      secondary.setVisible(true);

      secondary.setBackground(background);
      secondary.setForeground(getSemitransparentColor(foreground));
      secondary.setFont(font.deriveFont(Font.ITALIC));

      secondary.setText(text);
    }

    primary.setIcon(getIcon(node, expanded));

    return this;
  }

  private Color getSemitransparentColor(Color color) {
    int rgb = (color == null) ? 0 : color.getRGB();
    return new Color((127 << 24) | (rgb & 0x00ffffff), true);
  }

  /**
   * Paints the value.  The background is filled based on selected.
   */
  public final void paint(Graphics g) {
    Color bColor = getBackground();

    int imageOffset = -1;
    if (bColor != null) {
      imageOffset = getLabelStart();
      g.setColor(bColor);

      if (getComponentOrientation().isLeftToRight()) {
        g.fillRect(imageOffset, 0, getWidth() - 1 - imageOffset, getHeight());
      } else {
        g.fillRect(0, 0, getWidth() - 1 - imageOffset, getHeight());
      }
    }

    if (hasFocus) {
      Color bsColor = borderSelectionColor;
      if (bsColor != null) {
        g.setColor(bsColor);

//        if (drawsFocusBorderAroundIcon) {
//          imageOffset = 0;
//        }

        if (imageOffset == -1) {
          imageOffset = getLabelStart();
        }

        if (getComponentOrientation().isLeftToRight()) {
          g.drawRect(imageOffset, 0, getWidth() - 1 - imageOffset,
              getHeight() - 1);
        } else {
          g.drawRect(0, 0, getWidth() - 1 - imageOffset, getHeight() - 1);
        }
      }
    }

    super.paint(g);
  }

  private int getLabelStart() {
    Icon icon = primary.getIcon();
    if (icon != null && primary.getText() != null) {
      return icon.getIconWidth() + Math.max(0, primary.getIconTextGap() - 1);
    }
    return 0;
  }

  protected Icon getIcon(final UITreeNode node, final boolean expanded) {
    if (node instanceof ErrorTabNode) {
      ErrorTabNode enode = (ErrorTabNode) node;
      if (enode.getSource() == null) {
        return errorIcon;
      }

      return NodeIcons.getBinIcon(node.getType(), node.getBin(), expanded);
    }

    Object bin = node.getBin();
    if (bin instanceof RefactoringStatus.Entry) {
      int severity = ((RefactoringStatus.Entry) bin).getSeverity();

      Icon icon = RefactoringStatusIcons.getSeverityIcon(severity);
      if (icon != null) {
        return icon;
      }

      Object obj = ((RefactoringStatus.Entry) bin).getBin();

      return NodeIcons.getBinIcon(node.getType(), obj, expanded);
    }

    return NodeIcons.getBinIcon(node.getType(), bin, expanded);
  }
}
