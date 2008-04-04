/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common.action;


import net.sf.refactorit.common.util.ClassUtil;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.netbeans.common.BinItemNotFoundException;
import net.sf.refactorit.netbeans.common.ElementInfo;
import net.sf.refactorit.netbeans.common.FileNotFoundReason;
import net.sf.refactorit.netbeans.common.NBContext;
import net.sf.refactorit.netbeans.common.NBShortcuts;
import net.sf.refactorit.netbeans.common.ProjectNotFoundException;
import net.sf.refactorit.netbeans.common.RefactorItActions;
import net.sf.refactorit.netbeans.common.standalone.ErrorManager;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.RefactorITLock;
import net.sf.refactorit.ui.dialog.AWTContext;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.type.JavadocAction;
import net.sf.refactorit.utils.SwingUtil;

import org.apache.log4j.Logger;
import org.netbeans.modules.java.JavaDataObject;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataFolder;
import org.openide.nodes.Node;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import java.awt.Container;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.text.StyledDocument;


public abstract class HighLevelMenuAction extends CookieAction implements
    NBShortcuts.ActionKeyProvider {

  private static Logger log = Logger.getLogger(HighLevelMenuAction.class);

  public HighLevelMenuAction() {
    super();
  }

  public boolean asynchronous() {
    return false;
  }

  protected void initialize() {
    super.initialize();
    RefactorItActions.staticInit();
  }

  public void performAction(Node[] activatedNodes) {
    if (!RefactorITLock.lock()) {
      return;
    }
    try {
      Container selectedElement = TopComponent.getRegistry().getActivated();
      if (selectedElement == null) {
        selectedElement = WindowManager.getDefault().getMainWindow();
      }
      try {
        performActionWork(activatedNodes, selectedElement);
      } catch (ProjectNotFoundException e) {
        JOptionPane.showMessageDialog(
            WindowManager.getDefault().getMainWindow(),
            "No open projects.\n" +
            "(Select one in the \"Projects\" window and try again.)",
            GlobalOptions.REFACTORIT_NAME,
            JOptionPane.INFORMATION_MESSAGE);
      } catch (Exception e) {
        ErrorManager.showAndLogInternalError(e);
      }
    } catch (Exception e) {
      // don't let exceptions fall out of RIT
      log.warn(e.getMessage(), e);
    } finally {
      RefactorITLock.unlock();
    }
  }

  protected boolean enable(Node[] activatedNodes) {

    return true;
  }

  protected void performActionWork(
      Node[] activatedNodes, Container selectedElement
  ) {
    if (activatedNodes.length == 0) {
      // we are in RefactorIT result panel of Output Window
      RITAction.actionPerformedFromResultPanel(selectedElement, getActionKey());
      return;
    }

    ElementInfo[] elements = ElementInfo.getElementsFromNodes(activatedNodes);

    int caret = SwingUtil.getCaretPosition(selectedElement);
    int line = -1;
    int column = -1;

    boolean inSourceEditor = (caret != -1);
    Object binItem = null;
    // This must come _after_ the caret info has been collected -- parsing sometimes changes focus
    if (!IDEController.getInstance().ensureProject()) {
      return;
    }

    if (inSourceEditor) {
      //checking whether we have selected some text
      EditorCookie cookie = (EditorCookie) activatedNodes[0].getCookie(
          EditorCookie.class);

      if (cookie != null) {
        JEditorPane[] openedPanes = cookie.getOpenedPanes();

        if (openedPanes != null) {
          StyledDocument doc = cookie.getDocument();

          String selectedText = openedPanes[0].getSelectedText();
          if (selectedText != null) {
            int start = openedPanes[0].getSelectionStart();

            int pos = 0;
            while (Character.isWhitespace(selectedText.charAt(pos))) {
              ++pos;
              ++start;
            }

            line = NbDocument.findLineNumber(doc, start) + 1;
            column = NbDocument.findLineColumn(doc, start) + 1;
          } else {
            line = NbDocument.findLineNumber(doc, caret) + 1;
            column = NbDocument.findLineColumn(doc, caret) + 1;
          }
        }
      } else {
        // Reported S1S bug
        log.warn("RefactorIT: caught a bug: activated node does not have an EditorCookie");
      }
      binItem = elements[0].getBinItemFromCompilationUnit(line, column, getActionKey());
    } else {
      try {
        if (elements.length > 1) {
          Object[] binItemArray = new Object[elements.length];
          for (int i = 0; i < elements.length; i++) {
            binItemArray[i] = elements[i].getBinItem();
          }
          binItem = binItemArray;
        } else {
          binItem = elements[0].getBinItem();
        }
      } catch (BinItemNotFoundException e) {
        binItem = null;
      }
    }

    if (someProjectwide(elements)) {
      DialogManager.getInstance().showWarning(
          IDEController.getInstance().createProjectContext(),
          "warning.action.unit.error",
          "This command does not work on entire project");
      return;
    }

    if (binItem == null) {
      FileNotFoundReason.showMessageDialogOnWhyBinItemNotFound(
          IDEController.getInstance().createProjectContext(),
          elements, inSourceEditor);
      return;
    }

    RefactorItAction action;
    if (elements.length > 1) {
      action = ModuleManager.getAction(ClassUtil.getClassesArray((Object[])
          binItem), getActionKey());
    } else {
      action = ModuleManager.getAction(binItem.getClass(), getActionKey());
    }

    if (action == null
        && getActionKey().equals(net.sf.refactorit.ui.module.type.TypeAction.
        KEY)) {
      //lets call JavaDoc then
      action = ModuleManager.getAction(binItem.getClass(), JavadocAction.KEY);
    }

    Class[] classes;
    if (elements.length > 1) {
      classes = ClassUtil.getClassesArray((Object[]) binItem);
    } else {
      classes = new Class[] {binItem.getClass()};
    }

    if (ElementInfo.isJsp(elements)) {
      List actions = new ArrayList(1);
      actions.add(action);
      ModuleManager.filterJspSupportedActions(actions, classes);
      if (actions.isEmpty()) {
        return;
      }
    }

    if (action != null) {
      NBContext context = new NBContext(
          IDEController.getInstance().getActiveProject());

      Window window = ((AWTContext) context).getWindow();
      context.setPoint(SwingUtil.positionToClickPoint(
          selectedElement, caret, window));

      if (RefactorItActionUtils.run(action, context, binItem)) {
        action.updateEnvironment(context);
      } else {
        action.raiseResultsPane(context);
      }
      BackAction.notifyWillRun(action, elements[0], line);

    }
    // else: Silent ignore
  }

  private boolean someProjectwide(ElementInfo[] elements) {
    for (int i = 0; i < elements.length; i++) {
      if (elements[i].isProjectwide()) {
        return true;
      }
    }

    return false;
  }

  protected final Class[] cookieClasses() {
    return new Class[] {DataFolder.class, JavaDataObject.class};
  }

  protected int mode() {
    return MODE_EXACTLY_ONE; // All must be DataFolders or JavaDataObjects
  }

  public HelpCtx getHelpCtx() {
    return HelpCtx.DEFAULT_HELP;
  }

  public abstract String getActionKey();

  /**
   * Convinience function that returns a path to the icon. To be used
   * in ancestor classes.
   **/
  public static String getIconResource(String iconFileName) {
    return StringUtil.replace(net.sf.refactorit.ui.UIResources.class.getPackage().
        getName(), '.', '/') +
        "/images/" + iconFileName;
  }

}
