/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.action;


import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.jsp.JspUtil;
import net.sf.refactorit.netbeans.common.BinItemNotFoundException;
import net.sf.refactorit.netbeans.common.ElementInfo;
import net.sf.refactorit.netbeans.common.FileNotFoundReason;
import net.sf.refactorit.netbeans.common.NBContext;
import net.sf.refactorit.netbeans.common.NBShortcuts;
import net.sf.refactorit.netbeans.common.RefactorItActions;
import net.sf.refactorit.netbeans.common.standalone.ErrorManager;
import net.sf.refactorit.ui.RefactorITLock;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.panel.BinPane;
import net.sf.refactorit.ui.panel.BinPanel;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.windows.Workspace;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.text.StyledDocument;
import javax.swing.tree.TreePath;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Base class for all refactoring actions in NB.
 * Performs actions for selected nodes or from caret position.
 *
 * @author Vladislav Vislogubov
 */
public abstract class RITAction extends SystemAction implements NBShortcuts.
    ActionKeyProvider {

  /**
   * @return name
   * @see SystemAction#getName()
   */
  public String getName() {
    return null;
  }

  /**
   * @return help context
   * @see SystemAction#getHelpCtx()
   */
  public HelpCtx getHelpCtx() {
    return HelpCtx.DEFAULT_HELP;
  }

  public abstract String getActionKey();

  /**
   * @param e event
   * @see java.awt.event.ActionListener#actionPerformed(ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    if (!RefactorITLock.lock()) {
      return;
    }
    try {
      doAction(e);
    } catch (Exception ex) {
      // don't let exceptions fall out of RIT
      ErrorManager.showAndLogInternalError(ex);
    } finally {
      RefactorITLock.unlock();
    }

  }

  /**
   * Wrapper
   * @param e event
   */
  private void doAction(ActionEvent e) {
    Node[] nodes = TopComponent.getRegistry().getCurrentNodes();

    Container component = TopComponent.getRegistry().getActivated();
    if (component == null) {
      component = WindowManager.getDefault().getMainWindow();
    }

    if (nodes == null || nodes.length == 0) {
      actionPerformedFromResultPanel(component, getActionKey());
      return;
    }

    ElementInfo[] elements = ElementInfo.getElementsFromNodes(nodes);

    int caret = net.sf.refactorit.utils.SwingUtil.getCaretPosition(component);
    int line = -1;
    int column = -1;

    // This must come _after_ the caret info has been saved -- parsing sometimes changes focus
    if (!IDEController.getInstance().ensureProject()) {
      return;
    }

    BinSelection selection = null;
    if (caret < 0) {
      //we are in Project Explorer
    } else {
      //we are in Source Editor
      EditorCookie cookie = (EditorCookie) nodes[0].getCookie(
          EditorCookie.class);

      JEditorPane[] op = null;
      if (cookie != null) {
        op = cookie.getOpenedPanes();
      }

      if ((op == null) || (op.length < 1)) {
      } else {
        String text = op[0].getSelectedText();
        if (text != null && text.length() > 0) {
          int start = op[0].getSelectionStart();
          int end = op[0].getSelectionEnd();

          StyledDocument doc = cookie.getDocument();

          int startL = NbDocument.findLineNumber(doc, start) + 1;
          int startC = NbDocument.findLineColumn(doc, start) + 1;
          int endL = NbDocument.findLineNumber(doc, end) + 1;
          int endC = NbDocument.findLineColumn(doc, end) + 1;

          selection = new BinSelection(text, startL, startC, endL, endC);
          selection.setCompilationUnit(elements[0].getCompilationUnit());
        } else {
          StyledDocument doc = cookie.getDocument();

          line = NbDocument.findLineNumber(doc, caret) + 1;
          column = NbDocument.findLineColumn(doc, caret) + 1;
        }
      }
    }

    NBContext context = new NBContext(
        IDEController.getInstance().getActiveProject());

    if (selection != null) {
      //checking whether there is RefactorItAction for BinSelection
      RefactorItAction action
          = ModuleManager.getAction(BinSelection.class, getActionKey());

      if (action != null) {
        if (selection.getCompilationUnit() != null) {
          if (RefactorItActionUtils.run(action, context, selection)) {
            action.updateEnvironment(context);
          } else {
            action.raiseResultsPane(context);
          }
        } else {
          FileNotFoundReason.showMessageDialogOnWhyBinItemNotFound(
              context, elements, true);
        }
      }

      return;
    }

    RefactorItAction action = null;
    Object[] bins = null;
    try {
      Class[] classes;
      if (caret < 0) {
        bins = ElementInfo.getBinItems(elements);
        if (bins == null || bins.length == 0 || bins[0] == null) {
          FileNotFoundReason.showMessageDialogOnWhyBinItemNotFound(
              context, elements, false);
          return;
        }

        classes = ElementInfo.getBinItemClasses(elements);
      } else {
        bins = new Object[] {
            elements[0].getBinItemFromCompilationUnit(line, column, getActionKey())};
        if (bins == null || bins.length == 0 || bins[0] == null) {
          FileNotFoundReason.showMessageDialogOnWhyBinItemNotFound(
              context, elements, true);
          return;
        }

        classes = new Class[] {bins[0].getClass()};
      }

      action = ModuleManager.getAction(classes,
          getActionKey());
      List list = new ArrayList();
      list.add(action);

      if (JspUtil.containsJSPNodes(classes)) {
        if (action.isPreprocessedSourcesSupported(classes)) {
          ModuleManager.filterJspSupportedActions(list, classes);
          if (list.isEmpty()) {
            return;
          }
        }
      }

      if (action == null) {
        return;
      }
    } catch (BinItemNotFoundException ignore) {
      FileNotFoundReason.showMessageDialogOnWhyBinItemNotFound(
          context, elements, caret >= 0);

      return;
    }

    boolean res;
    if (bins.length == 1) {
      BackAction.notifyWillRun(action, elements[0], line);

      res = RefactorItActionUtils.run(action, context, bins[0]);
    } else {
      res = RefactorItActionUtils.run(action, context, bins);
    }
    if (res) {
      action.updateEnvironment(context);
    } else {
      action.raiseResultsPane(context);
    }
  }

  /**
   * Performs action if we are in RefactorIT result panel of Output Window
   * @param owner owner
   * @param key key
   */
  public static void actionPerformedFromResultPanel(Component owner, String key) {
    BinTreeTable table = findTreeTable();

    if (table == null) {
      return;
    }

    // we have found table
    TreePath[] paths = table.getTree().getSelectionPaths();
    if (paths == null || paths.length == 0) {
      return;
    }

    if (!IDEController.getInstance().ensureProject()) {
      return;
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

    run(obj, key, owner);
  }

  public static void run(final Object target, final String key,
      final Component owner) {
    NBContext context = new NBContext(IDEController.getInstance().
        getActiveProject());

    RefactorItAction action = null;
    boolean res = false;
    if (target instanceof Object[]) {
      Object[] objs = (Object[]) target;
      int size = objs.length;
      if (size > 0) {
        Class[] cls = new Class[size];
        for (int j = 0; j < size; j++) {
          cls[j] = objs[j].getClass();
        }
        action = ModuleManager.getAction(cls, key);
        if (action == null) {
          return;
        }

        res = RefactorItActionUtils.run(action, context, objs);
      }
    } else {
      action = ModuleManager.getAction(target.getClass(), key);
      if (action == null) {
        return;
      }

      res = RefactorItActionUtils.run(action, context, target);
    }

    if (res) {
      action.updateEnvironment(context);
    } else {
      action.raiseResultsPane(context);
    }
  }

  private static BinTreeTable findTreeTable() {
    Workspace workspace = WindowManager.getDefault().getCurrentWorkspace();

    Mode mode = workspace.findMode("output");
    if (mode == null) {
      return null;
    }
    TopComponent[] tops = mode.getTopComponents();

    for (int i = 0; i < tops.length; i++) {
      if ( !(tops[i].isOpened() && tops[i].isShowing())) {
        continue;
      }
      if (!(tops[i] instanceof NBContext.TabComponent)) {
        continue;
      }

      Component c = tops[i].getComponent(0);
      if (!(c instanceof BinPanel)) {
        continue;
      }
      BinPanel p = (BinPanel) c;


      if(p.getCurrentPane() == null) {
        Iterator iter = p.getAllPanes();
        while (iter.hasNext()) {
          JComponent comp = ((BinPane) iter.next()).getComponent().getUI();
          if (!comp.hasFocus()) {
            continue;
          }
          if (!(comp instanceof BinTreeTable)) {
            continue;
          }

          return (BinTreeTable) comp;
        }
      } else {
        return (BinTreeTable) p.getCurrentPane().getComponent().getUI();
      }

    }

    return null;
  }
}
