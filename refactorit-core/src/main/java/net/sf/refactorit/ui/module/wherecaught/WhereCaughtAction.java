/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.wherecaught;


import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
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
 * @author Anton Safonov
 */
public class WhereCaughtAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.WhereCaughtAction";
  public static final String NAME = "Where Caught";

  private static final ResourceBundle resLocalizedStrings =
      ResourceUtil.getBundle(WhereCaughtAction.class);

  public boolean isAvailableForType(Class type) {
    if (BinThrowStatement.class.equals(type)
        || BinMethod.Throws.class.equals(type)) {
      return true;
    }
    return false;
  }

  public String getKey() {
    return KEY;
  }

  public boolean isReadonly() {
    return true;
  }

  public boolean isMultiTargetsSupported() {
    return false;
  }

  public String getName() {
    return resLocalizedStrings.getString("action.name");
  }

  /**
   * Module execution.
   *
   * @param context context of the refactoring (also provides us current Project)
   * @param object  Bin object to operate
   * @return  false if nothing changed, true otherwise
   */
  public boolean run(final RefactorItContext context, final Object object) {
    // Catch incorrect parameters
    Assert.must(context != null,
        "Attempt to pass NULL context into WhereCaughtAction.run()");
    Assert.must(object != null,
        "Attempt to pass NULL object into WhereCaughtAction.run()");

    try {
      final WhereCaughtModel[] model = new WhereCaughtModel[1];
      JProgressDialog.run(context, new Runnable() {
        public void run() {
          model[0] = new WhereCaughtModel(context.getProject(),
              (BinItem) object);
        }
      }, object, true);

      BinTreeTable table = createTable(model[0], context);

      String moduleName = resLocalizedStrings.getString("action.name");
      ResultArea results = ResultArea.create(table, context,
          WhereCaughtAction.this);
      results.setTargetBinObject(object);
      BinPanel panel = BinPanel.getPanel(context, moduleName, results);
      table.smartExpand();

      // Register default help for panel's current toolbar
      panel.setDefaultHelp("refact");
    } catch (SearchingInterruptedException ex) {
    }

    // we never change anything
    return false;
  }

  private BinTreeTable createTable(BinTreeTableModel model,
      final RefactorItContext context) {

    final BinTreeTable table = new BinTreeTable(model, context);

    // open all branches
//    table.expandAll(); // there is sometimes too much

    table.setTableHeader(null);

    return table;
  }

}
