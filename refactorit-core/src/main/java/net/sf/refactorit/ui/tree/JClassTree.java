/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.tree;


import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.common.util.FastStack;
import net.sf.refactorit.commonIDE.IdeAction;
import net.sf.refactorit.commonIDE.ShortcutAction;
import net.sf.refactorit.standalone.BrowserContext;
import net.sf.refactorit.ui.Shortcuts;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.RefactorItContext;

import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


/**
 * Tree presenting classes.
 *
 * @author Igor Malinin
 * @author Vladislav Vislogubov
 * @author Anton Safonov
 */
public final class JClassTree extends BinTree {
  // context is neccessary for the RefactorItAction.run in KeyListener
  RefactorItContext context;

  private JPackageTree packageTree;

  public JClassTree(TreeModel model, RefactorItContext context) {
    super(model);

    this.context = context;

    init();
  }

  private void init() {
    setRootVisible(false);
    setShowsRootHandles(true);

    setCellRenderer(new BinTreeCellRenderer());

    addTreeWillExpandListener(new TreeWillExpandListener() {
      public void treeWillCollapse(TreeExpansionEvent evt) throws
          ExpandVetoException {
        if (evt.getPath().getPathCount() == 1) {
          // never collapse root node
          throw new ExpandVetoException(evt);
        }
      }

      public void treeWillExpand(TreeExpansionEvent evt) {}
    });

    addKeyListener(new TypeAheadSelector(this));

    ToolTipManager.sharedInstance().registerComponent(this);

    this.addKeyListener(new KeyAdapter() {
      public void keyPressed(KeyEvent ke) {
        TreePath[] paths = JClassTree.this.getSelectionPaths();
        if (paths == null || paths.length == 0) {
          return;
        }

        KeyStroke ks = KeyStroke.getKeyStroke(ke.getKeyCode(),
            ke.getModifiers());
        ShortcutAction shortcutAction = Shortcuts.getAction(ks);

        if (shortcutAction instanceof IdeAction) {
          RefactorItActionUtils.run((IdeAction) shortcutAction);
          return;
        }

        RefactorItAction act = (RefactorItAction) shortcutAction;
        //System.out.println( "act: " + act );
        if (act == null) {
          return;
        }

        ke.consume();

        Object obj;
        if (paths.length == 1) {
          TreePath path = paths[0];

          UITreeNode node = (UITreeNode) path.getLastPathComponent();
          obj = node.getBin();
        } else {
          int size = paths.length;
          Object[] bins = new Object[size];
          for (int i = 0; i < size; i++) {
            bins[i] = ((UITreeNode) paths[i].getLastPathComponent()).getBin();
          }
          obj = bins;
        }

        List actions = null;
        if (obj instanceof Object[]) {
          actions = ModuleManager.getActions((Object[]) obj);
        } else {
          actions = ModuleManager.getActions(obj);
        }

        if (actions == null || actions.size() == 0) {
          return;
        }

        if (!actions.contains(act)) {
          return;
        }

        //System.out.println( "obj: " + obj );
        //System.out.println( "class: " + obj.getClass() );
        if (context instanceof BrowserContext) {
          // FIXME what is this for???
          context = new BrowserContext(
              ((BrowserContext) context).getBrowser().getProject(),
              ((BrowserContext) context).getBrowser());
        }

        if (RefactorItActionUtils.run(act, context,obj)) {
          act.updateEnvironment(context);
        } else {
          act.raiseResultsPane(context);
        }
      }
    });

    optionsChanged(); // load default colors and font
  }

  public final JToolTip createToolTip() {
    JToolTip tip = new JToolTip();

    tip.setBackground(getBackground());
    tip.setForeground(getForeground());
    tip.setFont(getFont());

    tip.setComponent(this);

    return tip;
  }

  public final Point getToolTipLocation(MouseEvent e) {
    Point p = e.getPoint();

    int row = -1;
    try {
      row = getRowForLocation(p.x, p.y);
    } catch (NullPointerException ex) {
    }

    if (row < 0) {
      return null;
    }

    Rectangle rect = getRowBounds(row);
    if (!getVisibleRect().contains(rect)) {
      // move off the screen
      p.move( -30, -30);
      return p;
    }

    p = rect.getLocation();
    // IconWidth+1, -2
    p.translate(17, -2);
    return p;
  }

  public final String getToolTipText(MouseEvent e) {
    Point p = e.getPoint();

    int row = -1;
    try {
      row = getRowForLocation(p.x, p.y);
    } catch (NullPointerException ex) {
    }
    if (row < 0) {
      return null;
    }

    Rectangle rect = getRowBounds(row);
    if (!getVisibleRect().contains(rect)) {
      return null;
    }

    TreePath path = getPathForRow(row);
    UITreeNode node = (UITreeNode) path.getLastPathComponent();

    Object bin = node.getBin();

    String toolTipText = null;
    if (bin instanceof BinMember) {
      toolTipText = ((BinMember) bin).getQualifiedName();
    } else if (bin instanceof BinPackage) {
      toolTipText = ((BinPackage) bin).getQualifiedName();
    }

    return toolTipText;
  }

  public final void rebuild() {
    rebuild(getModel());
  }

  public final void rebuild(TreeModel updated) {
    TreeModel oldModel = getModel();
    List expanded = null;
    TreePath[] selected = null;

    if (oldModel != null && updated instanceof PackageTreeModel) {
      // save expansion state & selection path
      expanded = new ArrayList();

      Enumeration e = getExpandedDescendants(
          new TreePath(oldModel.getRoot()));

      while (e != null && e.hasMoreElements()) {
        TreePath current = (TreePath) e.nextElement();

        //System.err.println("Storing: " + current);

        expanded.add(current);
      }

      selected = getSelectionPaths();
      getSelectionModel().clearSelection();
    } else {
      expanded = null;
      selected = null;
    }

    // Rebuild old model
    if (oldModel != null && oldModel == updated) {
      ((PackageTreeModel) updated).rebuild();
    }

    setModel(updated);

    if (expanded != null) {
      for (int i = 0, max = expanded.size(); i < max; i++) {
        TreePath path = (TreePath) expanded.get(i);
        path = transformPath(path, (BranchNode) updated.getRoot());

//if (Assert.enabled)
//System.err.println("Expanding: " + (path.toString()));

        setExpandedState(path, true);
      }
    }

    requestFocus();

    if (selected == null) {
      setSelectionRow(0);
    } else {
      for (int i = 0; i < selected.length; i++) {
        TreePath path = selected[i];
//System.err.println("sel before: " + path);
        path = transformPath(path, (BranchNode) updated.getRoot());

//if (Assert.enabled)
//System.err.println("Selecting: " + path);

        setSelectionPath(path);
      }

      // clear old path to avoid memory leak
//      int[] selection = getSelectionRows();
//      selected = null;
//      getSelectionModel().clearSelection();
//      setSelectionRows(selection);
    }

//    SwingUtilities.invokeLater(
//        new Runnable() {
//      public void run() {
//        clearToggledPaths(); // avoid memory leak on saved expanded states
//      }
//    });

    if (packageTree != null) {
      packageTree.refreshTree();
    }
  }

  public final void setPackageTree(JPackageTree packageTree) {
    this.packageTree = packageTree;
  }

  private TreePath transformPath(TreePath path, BranchNode newRoot) {
    List newNodes = new ArrayList();

    FastStack pathItems = new FastStack();
    pathItems.push(path.getLastPathComponent());
    while (path.getParentPath() != null) {
      path = path.getParentPath();
      pathItems.push(path.getLastPathComponent());
    }

    UITreeNode curNode = newRoot;
    newNodes.add(curNode);
    for (int i = pathItems.size() - 2; i >= 0; i--) {
      UITreeNode oldNode = (UITreeNode) pathItems.get(i);
      UITreeNode newNode = null;
      try {
        newNode = ((BranchNode) curNode).getChild(oldNode.getDisplayName());
      } catch (Exception e) {
        // ignore - means bin was already broken, e.g. method has no more return
      }
      if (newNode != null) {
        newNodes.add(newNode);
        if (newNode instanceof BranchNode) {
          curNode = newNode;
        } else {
          break;
        }
      } else {
        break;
      }
    }

    return new TreePath(newNodes.toArray());
  }

}


final class TypeAheadSelector extends KeyAdapter implements MouseListener {
  String myString = "";
  final JClassTree myTree;

  TypeAheadSelector(JClassTree aTree) {
    this.myTree = aTree;
  }

  long lastHit = 0;

  /**
   * Here we try to check if something is typed and move this into an active position at tree
   */
  public final void keyTyped(KeyEvent ke) {
    if (ke.isAltDown()) {
      return;
    }
    char kc = ke.getKeyChar();

    long now = System.currentTimeMillis();
    if (lastHit == 0) {
      lastHit = now;

    }
    if ((now - lastHit) > 1000) {
      myString = "";
    }
    lastHit = now;

    if (isBreakerKey(kc)) {
      myString = "";
      return;
    } else {
      myString += kc;
    }

    int leadSelect = myTree.getLeadSelectionRow();
    // If there is no selection, equals to -1
    int lastRow = myTree.getRowCount();

    for (; leadSelect != -1 && leadSelect < lastRow; leadSelect++) {

      // Cast-safe implementation (avoids ClassCastExcaptions when TreeNodes are not of type FastNavigateable)
      Object component =
          myTree.getPathForRow(leadSelect).getLastPathComponent();

      // Check, Check, Check
      if (component != null && (component instanceof FastNavigateable)) {
        if (((FastNavigateable) component).matchesFor(myString)) {
          myTree.setSelectionRow(leadSelect);
          break; // Gotcha!
        }
      }
    }
  }

  private boolean isBreakerKey(char kc) {
    switch (kc) {
      case KeyEvent.VK_ENTER:
      case KeyEvent.VK_LEFT:
      case KeyEvent.VK_RIGHT:
      case KeyEvent.VK_UP:
      case KeyEvent.VK_DOWN:
        return true;
    }

    return false;
  }

  public final void mouseClicked(MouseEvent e) {
    myString = "";
  }

  public final void mouseEntered(MouseEvent e) {
  }

  public final void mouseExited(MouseEvent e) {
  }

  public final void mousePressed(MouseEvent e) {
  }

  public final void mouseReleased(MouseEvent e) {
  }
}
