/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.standalone;


import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.ActionProxy;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.RefactorItActionUtils;

import javax.swing.JOptionPane;

import java.awt.Point;
import java.awt.event.ActionEvent;


public abstract class AbstractStandaloneAction implements StandaloneAction {

//  private RefactorItAction action;
//  private Object object;
//  private Point clickPoint;
//
//  /**
//   * @param action
//   * @param anItem
//   * @param clickPoint
//   */
//  public AbstractStandaloneAction(RefactorItAction action,Object anItem,
//                          Point clickPoint) {
//    this.action=action;
//    this.object=anItem;
//    this.clickPoint=clickPoint;
//  }

//  public void actionPerformed(ActionEvent event) {
//
//    StandaloneController instance=(StandaloneController)IDEController.
//        getInstance();
//    // show the console, if it is not open.
//    // this fixes the Bug 1137
//    // Also this functions seems to be duplicate of
//    // {@link JBrowserPanel.createPopup(..)}
//
//    JBrowserPanel browser=instance.getBrowser();
//    browser.showConsoleArea();
//
//    final Project project=instance.getActiveProject();
//
//    BrowserContext context
//        = new BrowserContext(project,instance.getBrowser());
//    context.setPoint(clickPoint);
//
//    if (project.isParsingCanceledLastTime()) {
//      if (!instance.ensureProject()) {
//        return;
//      }
//    }
//
//    if(project.hasCriticalUserErrors()) {
//
//      DialogManager.getInstance().showCriticalError(DialogManager.
//          getDialogParent(),browser.getProject());
//
//      if(project.someErrorsCausedByAssertMode()) {
//        DialogManager.getInstance().showAssertModeWarning(DialogManager.
//            getDialogParent());
//      }
//
//      return;
//
//    } else {
//
//      if(object instanceof BinItemReference) {
//        object=((BinItemReference)object).findBinObject(project);
//        if(object==null) {
//          JOptionPane.showMessageDialog(DialogManager.getDialogParent(),
//              "There is no object in BinItemReference repository",
//              "No or Error BinItemReference",
//              JOptionPane.ERROR_MESSAGE);
//          return;
//        }
//      }
//
//      if(object==null) {
//        JOptionPane.showMessageDialog(DialogManager.getDialogParent(),
//            "No units were found to perform refactoring operation for",
//            "No or Error selection Unit",
//            JOptionPane.ERROR_MESSAGE);
//        return;
//      }
//
//      if(RefactorItActionUtils.run(
//          action,context,instance.getIDEMainWindow(),object)) {
//        action.updateEnvironment(instance.getIDEMainWindow(),context);
//          //browser.getTree().rebuild();
//      } else {
//        action.raiseResultsPane(instance.getIDEMainWindow(),context);
//      }
//    }
//
//    System.setOut(browser.getConsol().allocateTab(
//        JRefactorItFrame.resLocalizedStrings.getString("tab.console"),
//        browser.getConsolListener()));
//
//  }
//
//  public String getName() {
//    return action.getName();
//  }

//  public ActionProxy getAction() {
//    return action;
//  }

  public static AbstractStandaloneAction create(
      final RefactorItAction action, final Object object, final Point clickPoint
  ) {
    return new AbstractStandaloneAction() {
      public void actionPerformed(ActionEvent event) {
        Object target = object;

        StandaloneController instance
            = (StandaloneController) IDEController.getInstance();
        // show the console, if it is not open.
        // this fixes the Bug 1137
        // Also this functions seems to be duplicate of
        // {@link JBrowserPanel.createPopup(..)}

        JBrowserPanel browser = instance.getBrowser();
        browser.showConsoleArea();

        final Project project = instance.getActiveProject();

        BrowserContext context
            = new BrowserContext(project, instance.getBrowser());
        context.setPoint(clickPoint);

        if (project.getProjectLoader().isParsingCanceledLastTime()) {
          if (!instance.ensureProject()) {
            return;
          }
        }

        if ((project.getProjectLoader().getErrorCollector()).hasCriticalUserErrors()) {
          DialogManager.getInstance().showCriticalError(
              IDEController.getInstance().createProjectContext(),
              browser.getProject());

          if ((project.getProjectLoader().getErrorCollector()).hasErrorsCausedByWrongJavaVersion()) {
            DialogManager.getInstance().showJavaVersionWarning(
                IDEController.getInstance().createProjectContext());
          }

          return;
        } else {
          if (target instanceof BinItemReference) {
            target = ((BinItemReference) target).restore(project);
            if (target == null) {
              RitDialog.showMessageDialog(
                  IDEController.getInstance().createProjectContext(),
                  "There is no object in BinItemReference repository",
                  "No or Error BinItemReference", JOptionPane.ERROR_MESSAGE);
              return;
            }
          }

          if (target == null) {
            RitDialog.showMessageDialog(
                IDEController.getInstance().createProjectContext(),
                "No units were found to perform refactoring operation for",
                "No or Error selection Unit", JOptionPane.ERROR_MESSAGE);
            return;
          }

          if (RefactorItActionUtils.run(action, context, target)) {
            action.updateEnvironment(context);
          } else {
            action.raiseResultsPane(context);
          }
        }

        System.setOut(browser.getConsol().allocateTab(
            JRefactorItFrame.resLocalizedStrings.getString("tab.console"),
            browser.getConsolListener()));
      }

      public String getName() {
        return action.getName();
      }

//
//  public void setTarget(final Object object) {
//    this.object = object;
//  }

      public ActionProxy getAction() {
        return action;
      }
    };
  }
}
