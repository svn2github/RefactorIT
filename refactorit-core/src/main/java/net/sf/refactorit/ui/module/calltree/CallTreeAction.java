/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.calltree;


import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.panel.BinPanel;
import net.sf.refactorit.ui.panel.ResultArea;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;

import java.util.ResourceBundle;


/**
 *
 *
 * @author Anton Safonov
 */
public class CallTreeAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.CallTreeAction";
  public static final String NAME = "Call Tree";

  private static final ResourceBundle resLocalizedStrings
      = ResourceUtil.getBundle(CallTreeAction.class);

  public boolean isAvailableForType(Class type) {
    if (BinMethod.class.isAssignableFrom(type)
        || BinMethodInvocationExpression.class.equals(type)
        || BinField.class.isAssignableFrom(type)
        || BinFieldInvocationExpression.class.equals(type)
        ) {
      return true;
    }
    return false;
  }

  public boolean isMultiTargetsSupported() {
    return false;
  }

  public String getName() {
    return NAME;
  }

  public boolean isReadonly() {
    return true;
  }

  public String getKey() {
    return KEY;
  }

  /**
   * Module execution.
   *
   * @param context context of the refactoring (also provides us current Project)
   * @param parent  any visible parent component
   * @param object  Bin object to operate
   * @return  false if nothing changed, true otherwise
   */
  public boolean run(final RefactorItContext context, Object object) {
    // Catch incorrect parameters
    Assert.must(context != null,
        "Attempt to pass NULL context into CallTreeAction.run()");
    Assert.must(object != null,
        "Attempt to pass NULL object into CallTreeAction.run()");

    try {
      final Object target;
      if (object instanceof BinMemberInvocationExpression) {
        target = ((BinMemberInvocationExpression) object).getMember();
      } else {
        target = object;
      }

      final CallTreeModel[] model = new CallTreeModel[1];
      JProgressDialog.run(context, new Runnable() {
        public void run() {
          model[0] = new CallTreeModel(context.getProject(), (BinItem) target);
        }
      }, object, true);

      BinTreeTable table = createTable(model[0], context);
      String moduleName = resLocalizedStrings.getString("action.name");
      ResultArea results = ResultArea.create(table, context, CallTreeAction.this);
      results.setTargetBinObject(target);
      BinPanel panel = BinPanel.getPanel(context, moduleName, results);
      table.smartExpand();
      // Register default help for panel's current toolbar
      panel.setDefaultHelp("refact.call_tree");
    } catch (SearchingInterruptedException ex) {
    }

    // we never change anything
    return false;
  }

  private BinTreeTable createTable(BinTreeTableModel model,
      final RefactorItContext context) {

    final BinTreeTable table = new BinTreeTable(model, context);

    table.smartExpand();

    table.setTableHeader(null);

    return table;
  }
}
