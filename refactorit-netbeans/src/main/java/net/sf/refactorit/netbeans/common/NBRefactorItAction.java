/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.netbeans.common;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.netbeans.common.action.BackAction;
import net.sf.refactorit.netbeans.common.standalone.ErrorManager;
import net.sf.refactorit.ui.module.ModuleManager;
import net.sf.refactorit.ui.module.RefactorItAction;
import net.sf.refactorit.ui.module.RefactorItActionUtils;

import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;



public class NBRefactorItAction extends NBAction implements Cloneable {
  private ElementInfo element;
  private int line;
  private int column;
//  private RefactorItAction  action;

  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }

  public NBRefactorItAction() {
    super(null);
  }

  public NBRefactorItAction(
      ElementInfo element, int line, int column
  ) {
    super(null);

    this.element = element;
    this.line = line;
    this.column = column;
  }

  public void actionPerformed(ActionEvent e) {
    try {
      doActionPerformed();
    } catch(Exception ex) {
      ErrorManager.showAndLogInternalError(ex);
    }
  }

  private void doActionPerformed() {
    //JURI R
//    bsh.util.JConsole console = new bsh.util.JConsole();
//    bsh.Interpreter bsh=new bsh.Interpreter(console);
//    Thread t= new Thread(bsh);
//    javax.swing.JFrame f=new javax.swing.JFrame(RefactorItActions.openIdeSpecificationAtLeast(RefactorItActions.NB_4_0)?"4.0 beta2":"3.6");
//    t.start();
//    java.awt.Toolkit tkit=f.getToolkit();
//    java.awt.Dimension scrSize=tkit.getScreenSize();
//    f.getContentPane().add(console);
//    f.setSize(400,300);
//    f.setLocation((scrSize.width-f.getWidth())/2,(scrSize.height-f.getHeight())/2);
//    f.setVisible(true);
    //JURI R
    if (!IDEController.getInstance().ensureProject()) {
      return; // Silent ignore
    }

    BinItem item = element.getBinItemFromCompilationUnit(line, column,
        getAction().getKey());
    if (item == null) {
      FileNotFoundReason.showMessageDialogOnWhyBinItemNotFound(
          IDEController.getInstance().createProjectContext(),
          new ElementInfo[] {element}
          , true);
      return;
    }

    RefactorItAction action = (RefactorItAction) getAction();

    if (!actionAvailableForSubclass(action, item.getClass())) {
      return; // Silent ignore
    }

    NBContext ctx = new NBContext(IDEController.getInstance().getActiveProject());
    Container topComp = TopComponent.getRegistry().getActivated();
    if (topComp == null) {
      topComp = WindowManager.getDefault().getMainWindow();
    }

    if (RefactorItActions.isSourceWindow(topComp)) {
      ctx.setPoint(RefactorItActions.clickInfo.point);
    }

    BackAction.notifyWillRun(action, element, line);

    if (RefactorItActionUtils.run(action, ctx, item)) {
      action.updateEnvironment(ctx);
    } else {
      action.raiseResultsPane(ctx);
    }
  }

  private boolean actionAvailableForSubclass(
      final RefactorItAction action, final Class itemClass) {
    List actions = ModuleManager.getActions(itemClass);
    for (Iterator i = actions.iterator(); i.hasNext(); ) {
      Class actionClass = i.next().getClass();
      if (actionClass == action.getClass()) {
        return true;
      }
    }

    return false;
  }
}
