/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.standalone;


import net.sf.refactorit.ui.tree.JClassTree;
import net.sf.refactorit.ui.tree.SourceNode;
import net.sf.refactorit.ui.tree.TypeNode;
import net.sf.refactorit.ui.tree.UITreeNode;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;


class BrowserTreeKeyListener extends KeyAdapter {
  private JClassTree tree;
  private JBrowserPanel panel;

  public BrowserTreeKeyListener(JClassTree tree, JBrowserPanel panel) {
    this.tree = tree;
    this.panel = panel;
  }

  public void keyReleased(KeyEvent ke) {
    int kc = ke.getKeyCode();
    switch (kc) {
      case KeyEvent.VK_M: {
        if (ke.isControlDown()) {
          TreePath[] paths = tree.getSelectionPaths();
          if (paths == null || paths.length == 0) {
            return;
          }

          int row = tree.getRowForPath(paths[paths.length - 1]);
          Rectangle rect = tree.getRowBounds(row);
          //if ( !packages.getVisibleRect().contains( rect ) )
          //rect = packages.getVisibleRect();

          Point point = SwingUtilities.convertPoint(
              tree, (int) rect.getCenterX(), (int) rect.getCenterY(),
              SwingUtilities.getWindowAncestor(panel));

          JPopupMenu menu = null;
          if (paths.length == 1) {
            Object node = paths[0].getLastPathComponent();

            menu = panel.popupManager.getPopupMenu(
                ((UITreeNode) node).getBin(), point);
          } else {
            int size = paths.length;
            Object[] bins = new Object[size];
            for (int i = 0; i < size; i++) {
              bins[i] = ((UITreeNode) paths[i].getLastPathComponent()).getBin();
            }
            menu = panel.popupManager.getPopupMenu(bins, point);
          }

          // Do not show empty PopupMenu
          if (menu != null) {
            int y = (int) rect.getCenterY();
            int popupH = (int) menu.getPreferredSize().getHeight() + 30; // undo menuitem

            int availablePopupY = panel.getHeight() - point.y;

            if (availablePopupY < popupH) {
              // popup is coming out of JBrowserPanel
              int availableScreenH = (int) panel.getToolkit().getScreenSize().
                  getHeight() - (panel.getLocationOnScreen().y
                  + panel.getHeight());
              if (availablePopupY + availableScreenH < popupH) {
                //popup is out of screen
                int offset = popupH - (availablePopupY + availableScreenH);
                y -= offset;
                //if ( y < 0 ) y = 0;
              }
            }

            menu.show(tree, (int) rect.getCenterX(), y);
            menu.requestFocus();
          }
        }
        break;
      }

      case KeyEvent.VK_ENTER: {

        TreePath path = tree.getSelectionPath();
        if (path == null) {
          return;
        }

        Object object = path.getLastPathComponent();
        if (object instanceof SourceNode) {
          // Cast to SourceNode
          SourceNode node = (SourceNode) object;
          int line = 1;
          if (node.getStart() == null) {
            path = path.getParentPath();
            if (path.getLastPathComponent() instanceof SourceNode) {
              SourceNode newNode = (SourceNode) path.getLastPathComponent();
              if (newNode.getStart() != null) {
                line = newNode.getStart().getLine();
              }
            }
          } else {
            line = node.getStart().getLine();
          }

          panel.show(node.getCompilationUnit(), line,
              (node instanceof TypeNode.Member));
        }
      }
    }
  }
}
