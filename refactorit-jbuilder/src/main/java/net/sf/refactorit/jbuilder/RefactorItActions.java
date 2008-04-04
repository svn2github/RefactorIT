/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.jbuilder;


import com.borland.jbuilder.node.java.JavaStructureNode;
import com.borland.primetime.editor.EditorActions;
import com.borland.primetime.editor.EditorPane;
import com.borland.primetime.ide.*;
import com.borland.primetime.node.FileNode;
import com.borland.primetime.node.Node;
import com.borland.primetime.node.TextStructure;
import com.borland.primetime.viewer.TextNodeViewer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.tree.TreePath;

import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ClassUtil;
import net.sf.refactorit.commonIDE.ActionRepository;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.NotFromSrcOrFromIgnoredException;
import net.sf.refactorit.jsp.JspUtil;
import net.sf.refactorit.ui.JErrorDialog;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.gotomodule.actions.GotoAction;
import net.sf.refactorit.ui.module.type.JavadocAction;
import net.sf.refactorit.ui.module.type.TypeAction;
import net.sf.refactorit.ui.panel.BinPanel;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import com.borland.jbuilder.node.java.JavaStructureElement;


/**
 * Contains all available Module Actions.
 * Performs actions for selected nodes or from caret position.
 *
 * @author Vladislav Vislogubov
 */
public class RefactorItActions {
//  public static final Action REFACTORIT_PULLPUSH =
//        new RITAction( net.sf.refactorit.ui.module.pullpush.PullPushAction.NAME,
//                net.sf.refactorit.ui.module.pullpush.PullPushAction.KEY,
//                null );
// ....

  private static Object getBinObjectFromMessageView(Browser browser) throws
      NotFromSrcOrFromIgnoredException {
    MessageView mv = browser.getMessageView();
    if (mv == null) {
      return null;
    }

    if (!mv.hasFocus()) {
      return null;
    }

    MessageCategory mc = mv.getActiveTab();
    if (!(mc instanceof JBContext.RefactorItMessageCategory)) {
      return null;
    }

    JComponent binPanel = ((JBContext.RefactorItMessageCategory) mc).
        getComponent();

    if (!(binPanel instanceof BinPanel)) {
      return null;
    }

    BinPanel p = (BinPanel) binPanel;

    JComponent comp = p.getCurrentPane().getComponent().getUI();
    if (!(comp instanceof BinTreeTable)) {
      return null;
    }

    BinTreeTable table = (BinTreeTable) comp;

    TreePath[] paths = table.getTree().getSelectionPaths();
    if (paths == null || paths.length == 0) {
      return null;
    }

    Object obj;
    if (paths.length == 1) {
      TreePath path = paths[0];

      BinTreeTableNode node = (BinTreeTableNode) path.getLastPathComponent();
      obj = node.getBin();
    } else {
      int size = paths.length;
      Object[] bins = new Object[size];
      for (int j = 0; j < size; j++) {
        bins[j] = ((BinTreeTableNode) paths[j].getLastPathComponent()).getBin();
      }
      obj = bins;
    }

    return obj;
  }

  private static Object getBinObjectFromEditor(EditorPane pane) throws
      RefactorItActionProvider.InterruptRefactorItActionSilently,
      NotFromSrcOrFromIgnoredException  {
    if (pane == null) {
      return null;
    }

    return RefactorItActionProvider.getBinObjectFromEditor(pane,
        IDEController.getInstance().getActiveProject());
  }

  private static Object getBinObjectFromProjectView(Browser browser) throws
      RefactorItActionProvider.InterruptRefactorItActionSilently,
      NotFromSrcOrFromIgnoredException {
    ProjectView pv = browser.getProjectView();
    if (pv == null) {
      return null;
    }

    Browser.getActiveBrowser().dispatchEvent(
        new WindowEvent(Browser.getActiveBrowser(),
        WindowEvent.WINDOW_ACTIVATED));
    Browser.getActiveBrowser().requestFocus();

    Node[] nodes = pv.getSelectedNodes();
    if (nodes == null || nodes.length == 0) {
      nodes = JBRunContext.getNodes();
      if (nodes == null || nodes.length == 0) {
        return null;
      } else {
        JBRunContext.setNodes(null);
      }
    }

    return RefactorItActionProvider.getBinItemFromProjectViewNode(
        IDEController.getInstance().getActiveProject(),
        browser, nodes);

    //return RefactorItActionProvider.getBinObject( RefactorItTool.getProject() );
  }

  public static Object getBinObjectFromStructureView(StructureView sv)
      throws NotFromSrcOrFromIgnoredException {
    if (sv == null) {
      return null;
    }
    JTree tree = getStructureTree(sv);

    if (tree == null) {
      return null;
    }

    JavaStructureNode root = (JavaStructureNode) tree.getModel().getRoot();

    if (!JavaStructureRe.isFromProjectSource(root)) {
      throw new NotFromSrcOrFromIgnoredException();
    }

    JavaStructureNode[] nodes = JavaStructureRe.getJavaStructureNodes(
        tree.getSelectionPaths());
    if (nodes == null) {
      return null;
    }

    int size = nodes.length;
    Object[] bins = new Object[size];

    for (int i = 0; i < size; i++) {
      bins[i] = JavaStructureRe.getBinObject(nodes[i], root);
      if (bins[i] == null) {
        return null;
      }
    }

    return bins;
  }

  public static JTree getStructureTree(StructureView sv) {
    JTree tree = null;

    try {
      int x = (int) sv.getStructureComponent().getBounds().getCenterX();
      int y = (int) sv.getStructureComponent().getBounds().getCenterY();
      Component c = sv.getStructureComponent().getComponentAt(x, y);
      if (c != null && c instanceof JScrollPane
          && ((JScrollPane) c).getViewport().getView() instanceof JTree) {
        tree = (JTree) ((JScrollPane) c).getViewport().getView();
      }
    } catch (Exception e) {
      // ignore, let's try some other way
      // but usually works ok!
    }

    if (tree == null) {
      TextStructure structure = null;

      Component structureComponent = sv.getStructureComponent();

      if (structureComponent == null) {
        return null;
      }

      Method[] methods = structureComponent.getClass().getMethods();
      for (int i = 0; i < methods.length; i++) {
        Method method = methods[i];
        if (TextStructure.class.isAssignableFrom(method.getReturnType())
            && method.getParameterTypes().length == 0) {
          try {
            structure = (TextStructure) method.invoke(sv.getStructureComponent(),
                new Object[0]);
          } catch (InvocationTargetException ex) {
            // ignore
            //System.err.println("Failed to receive TextStructure: "
            //    + ex.getTargetException());
          } catch (Exception ex) {
            // ignore
            //System.err.println("Failed to receive TextStructure: " + ex);
          }
          break;
        }
      }

      if (structure == null) {
        try {
          Browser browser = Browser.getActiveBrowser();
          NodeViewer viewer = browser.getActiveViewer(browser.getActiveNode());
          if (viewer instanceof TextNodeViewer
              && browser.getActiveNode() instanceof FileNode) {
            structure = ((TextNodeViewer) viewer).getStructure(
                (FileNode) browser.getActiveNode());
          }
        } catch (Exception e) {
          // ignore
        }
      }

      if (structure != null) {
        tree = structure.getTree();
      }
    }

    return tree;
  }

  public static void performAction(
      EditorPane pane, StructureView sv, String key
  ) {
    Browser browser = Browser.getActiveBrowser();

//    Window oldParent = DialogManager.getDialogParent();
//    DialogManager.setDialogParent(browser);
    //must be first !!!
    if (!IDEController.getInstance().ensureProject()) {
      return;
    }

      Object obj = null;

    try {
      obj = getBinObjectFromStructureView(sv);
    } catch (NotFromSrcOrFromIgnoredException e) {
      showNotFromSrcOrFromIgnoredSrcDialog();
      return;
    }

    try {
      if (obj == null) {
        // we are in editor view

        try {
          obj = getBinObjectFromEditor(pane);
        } catch (NotFromSrcOrFromIgnoredException e) {
          showNotFromSrcOrFromIgnoredSrcDialog();
          return;
        }

//        if (pane != null) {
//          URL page = pane.getPage();
//          System.err.println("Getting object from editor:" + obj);
//          System.err.println("@@ file="+page.getFile()); // not working, page is null
//          if (page != null && FileUtil.isJspFile(page.getFile())) {
//            isJsp = true;
//          }
//
//        }
      }

      if (obj == null) {
        // we are in message view
        try {
          obj = getBinObjectFromMessageView(browser);
        } catch (NotFromSrcOrFromIgnoredException e) {
          showNotFromSrcOrFromIgnoredSrcDialog();
          return;
        }
      }

      if (obj == null) {
        // we are in project view
        try {
          obj = getBinObjectFromProjectView(browser);
        } catch (NotFromSrcOrFromIgnoredException e) {
          showNotFromSrcOrFromIgnoredSrcDialog();
          return;
        }


      }
    } catch (RefactorItActionProvider.InterruptRefactorItActionSilently e) {
      return;
    }

    if (obj == null) {
      RitDialog.showMessageDialog(
          IDEController.getInstance().createProjectContext(),
          "Can not perform refactorings on item you selected\n",
          "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    // this gets focus back after it was stolen by our Parsing dialog
    // without it EditorPane becomes totally frozen/read-only
    Browser.getActiveBrowser().dispatchEvent(new WindowEvent(
        Browser.getActiveBrowser(), WindowEvent.WINDOW_ACTIVATED));

    RefactorItAction action = null;
    if (obj instanceof Object[]) {
      Object[] objs = (Object[]) obj;

      int size = objs.length;
      if (size > 0) {
//        Class[] cls = new Class[size];
//        for ( int j = 0; j < size; j++ ) {
//                  cls[j] = objs[j].getClass();
//        }
        Class[] cls = ClassUtil.getClassesArray(objs);
        action = ModuleManager.getAction(cls, key);
      }
      if (size == 1) {
        obj = objs[0];
      }
    } else {
      if(obj instanceof JBUsersSelection) {
        if (((JBUsersSelection) obj).getBinItem() != null) {
          action = ModuleManager.getAction(
              ((JBUsersSelection) obj).getBinItem().getClass(), key);
        }

        if (action == null) {
          obj = ((JBUsersSelection) obj).getBinSelection();
          action = ModuleManager.getAction(obj.getClass(), key);
        } else {
          obj = ((JBUsersSelection) obj).getBinItem();
        }
      } else {
        action = ModuleManager.getAction(obj.getClass(), key);
      }
    }

    if (action == null && key.equals(TypeAction.KEY)) {
      if (!(obj instanceof Object[])) {
        //lets call JavaDoc then
        action = ModuleManager.getAction(obj.getClass(), JavadocAction.KEY);
      }
    }

    if (action == null) {
      return;
    }

    // Filter non jsp actions
    Object tmpArr[] = null;
    if (obj instanceof Object[]) {
      tmpArr = (Object[]) obj;
    } else {
      tmpArr = new Object[] {obj};
    }

    if (JspUtil.containsJSPNodes(tmpArr)) {
      if (!ModuleManager.isAllowedForJsp(
          action, ClassUtil.getClassesArray(tmpArr))
          ) {
        action = null;
      }
    }

    if (action == null) {
      return;
    }

    if (pane != null && (key.equals(GotoAction.KEY))
        ) {
      Node n = browser.getActiveNode();
      int line = pane.getLineNumber(pane.getCaretPosition());
      BackAction.addRecord(n, line);
    }

    JBContext context = new JBContext(
        IDEController.getInstance().getActiveProject(), browser);

    try {
      setupPoint(browser, pane, sv, context);

      if (RefactorItActionUtils.run(action, context, obj)) {
        action.updateEnvironment(context);
        browser.getProjectView().refreshTree();
      } else {
        action.raiseResultsPane(context);
      }

//      if (oldParent != null) {
//        DialogManager.setDialogParent(oldParent);
//      }
    } catch (Exception e) {
      AppRegistry.getExceptionLogger().error(e, RefactorItActions.class);
      JErrorDialog err = new JErrorDialog(context, "Error");
      err.setException(e);
      err.show();
      return;
    } finally {
      Browser.getActiveBrowser().dispatchEvent(new WindowEvent(
          Browser.getActiveBrowser(), WindowEvent.WINDOW_ACTIVATED));
    }
  }

  private static void showNotFromSrcOrFromIgnoredSrcDialog() {
      RitDialog.showMessageDialog(
          IDEController.getInstance().createProjectContext(),
          "Selected item is not from project path\nor is from ignored path\n",
          "Error", JOptionPane.ERROR_MESSAGE);
  }

  public static void setupPoint(
      final Browser browser, final EditorPane pane,
      final StructureView sv, final IdeWindowContext context
      ) {
    try {
      if (pane != null) {
        setupPointForEditorPane(browser, pane, context);
      } else if (sv != null) {
        setupPointForTree(browser, getStructureTree(sv), context);
      } else {
        /*Point point = new Point((int) browser.getBounds().getCenterX(),
            (int) browser.getBounds().getCenterY());
                 context.setPoint(point);*/
      }
    } catch (Exception ex) {
      // ignore
      if (Assert.enabled) {
        ex.printStackTrace();
      }
    }
  }

  public static void setupPointForEditorPane(
      final Browser browser, final EditorPane pane, final IdeWindowContext context
      ) throws BadLocationException {
    Rectangle rec = pane.modelToView(pane.getCaretPosition());
    Point point = new Point(
        (int) rec.getLocation().getX(),
        (int) (rec.getLocation().getY() + rec.getHeight() / 2 + 2));
    point = SwingUtilities.convertPoint(pane, point, browser);
    context.setPoint(point);
  }

  public static void setupPointForTree(
      final Browser browser, final JTree tree, final IdeWindowContext context
      ) {
    try {
      TreePath[] selected = tree.getSelectionPaths();

      TreePath lastPathSelected = null;
      if (selected != null && selected.length > 0) {
        lastPathSelected = selected[selected.length - 1];
      }

      if (lastPathSelected == null) {
        lastPathSelected = tree.getAnchorSelectionPath();
      }

      if (lastPathSelected == null) {
        lastPathSelected = tree.getLeadSelectionPath();
      }

      if (lastPathSelected == null) {
        return;
      }

      Rectangle lastRow = tree.getUI().getPathBounds(tree, lastPathSelected);
      if (lastRow == null) {
        return;
      }

      Point point = SwingUtilities.convertPoint(tree,
          (int) lastRow.getLocation().getX() + 20,
          (int) (lastRow.getLocation().getY() + lastRow.getHeight() / 2 + 2),
          browser);
      context.setPoint(point);
    } catch (NullPointerException ex) {
      return;
    }
  }

  public static void initOpenTool(byte major, byte minor) {

    RefactorItTool.ensureControllerInit();

    ActionRepository repository = ActionRepository.getInstance();
    Collection actions = repository.getShortcutActions();
    for (Iterator i = actions.iterator(); i.hasNext(); ) {
      Action item = (Action) i.next();

      EditorActions.addBindableIdeAction(item);
      EditorActions.addBindableEditorAction(item);
    }
//
//    EditorActions.addBindableIdeAction( RefactorItActions.REFACTORIT_GOTO );
//    EditorActions.addBindableEditorAction( RefactorItActions.REFACTORIT_GOTO );

  }

}
