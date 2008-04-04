/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.tree;


import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.TunableComponent;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;


public class BinTree extends JTree implements TunableComponent {
  private Color selectionBackground;
  private Color selectionForeground;

  public BinTree() {
    setLargeModel(true);
  }

  public BinTree(TreeModel model) {
    super(model);
    setLargeModel(true);
  }

  public BinTree(TreeNode root) {
    super(root);
    setLargeModel(true);
  }

  public final void optionsChanged() {
    setFont(getFontProperty());
    setBackground(getBackgroundProperty());
    setForeground(getForegroundProperty());
    selectionBackground
        = Color.decode(GlobalOptions.getOption("tree.selection.background"));
    selectionForeground
        = Color.decode(GlobalOptions.getOption("tree.selection.foreground"));

    FontMetrics fm = getFontMetrics(getFont());
    int height = fm.getMaxAscent() + fm.getMaxDescent() + fm.getLeading();
    setRowHeight((height < 16) ? 16 : height);
  }

  public static Color getForegroundProperty() {
    try {
      return Color.decode(GlobalOptions.getOption("tree.foreground"));
    } catch (Exception e) {
      return null; // headless configuration
    } catch (NoClassDefFoundError e) {
      return null; // headless configuration
    }
  }

  public static Color getBackgroundProperty() {
    try {
      return Color.decode(GlobalOptions.getOption("tree.background"));
    } catch (Exception e) {
      return null; // headless configuration
    } catch (NoClassDefFoundError e) {
      return null; // headless configuration
    }
  }

  public static Font getFontProperty() {
    try {
      return Font.decode(GlobalOptions.getOption("tree.font"));
    } catch (Exception e) {
      return null; // headless configuration
    } catch (NoClassDefFoundError e) {
      return null; // headless configuration
    }
  }

  public final Color getSelectedBackground() {
    return selectionBackground;
  }

  public final Color getSelectedForeground() {
    return selectionForeground;
  }

  public final void expandAll() {
    expandRecursively(new TreePath(this.getModel().getRoot()));
  }

  private final void expandRecursively(final TreePath path) {
    if (path == null) {
      return;
    }

    expandPath(path);

    if (path.getLastPathComponent() instanceof TreeNode) {
      final TreeNode node = (TreeNode) path.getLastPathComponent();
      for (int i = 0, max = node.getChildCount(); i < max; i++) {
        expandRecursively(path.pathByAddingChild(node.getChildAt(i)));
      }
    } else if (path.getLastPathComponent() instanceof BranchNode) {
      final BranchNode node = (BranchNode) path.getLastPathComponent();
      for (int i = 0, max = node.getChildCount(); i < max; i++) {
        expandRecursively(path.pathByAddingChild(node.getChildAt(i)));
      }
    }
  }

  public final void collapseAll() {
    final Object root = this.getModel().getRoot();
    collapseRecursively(new TreePath(root));
  }

  private final void collapseRecursively(final TreePath path) {
    if (path == null) {
      return;
    }

    if (path.getLastPathComponent() instanceof TreeNode) {
      final TreeNode node = (TreeNode) path.getLastPathComponent();

      for (int i = 0, max = node.getChildCount(); i < max; i++) {
        collapseRecursively(path.pathByAddingChild(node.getChildAt(i)));
      }

      if (node.getParent() != null) {
        collapsePath(path);
      }
    } else if (path.getLastPathComponent() instanceof BranchNode) {
      final BranchNode node = (BranchNode) path.getLastPathComponent();

      for (int i = 0, max = node.getChildCount(); i < max; i++) {
        collapseRecursively(path.pathByAddingChild(node.getChildAt(i)));
      }

      if (node.getParent() != null) {
        collapsePath(path);
      }
    }

  }

  /*
   * Expands tree if there are less than 8 nodes
   */
  public final void smartExpand() {
    final int count = getLeafCount(getModel().getRoot());
    if (count <= 7) {
      expandAll();
    }
  }

  private int getLeafCount(final Object aNode) {
    try {
      if (aNode instanceof TreeNode) {
        TreeNode node = (TreeNode) aNode;
        final int children = node.getChildCount();
        if (children == 0) {
          return 1; // leaf itself
        }

        int count = 0;
        for (int i = 0; i < children; i++) {
          count += getLeafCount(node.getChildAt(i));
          if (count > 7) { // shortcut
            break;
          }
        }

        return count;
      }

      if (aNode instanceof BranchNode) {
        BranchNode node = (BranchNode) aNode;
        final int children = node.getChildCount();
        if (children == 0) {
          return 1; // leaf itself
        }

        int count = 0;
        for (int i = 0; i < children; i++) {
          count += getLeafCount(node.getChildAt(i));
          if (count > 7) { // shortcut
            break;
          }
        }

        return count;
      }
    } catch (Exception e) {
      AppRegistry.getExceptionLogger().error(e, this);
    }

    return 1;
  }
}
