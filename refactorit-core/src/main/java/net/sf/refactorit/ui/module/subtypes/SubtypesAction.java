/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.subtypes;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.panel.BinPanel;
import net.sf.refactorit.ui.panel.ResultArea;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;

import java.util.ResourceBundle;


/**
 * @author Anton Safonov
 */
public class SubtypesAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.SubtypesAction";
  public static final String NAME = "Subtypes";

  private static ResourceBundle resLocalizedStrings =
      ResourceUtil.getBundle(SubtypesAction.class);

  public boolean isMultiTargetsSupported() {
    return false;
  }

  public String getName() {
    return resLocalizedStrings.getString("action.name");
  }

  public String getKey() {
    return KEY;
  }

  /**
   * Module execution.
   *
   * @param context
   * @param parent  any visible component on the screen
   * @param object  Bin object to operate
   * @return  false if nothing changed, true otherwise
   */
  public boolean run(final RefactorItContext context, Object object) {
    // Catch incorrect parameters
    {
      Assert.must(context != null,
          "Attempt to pass NULL context into SubtypesAction.run()");
      Assert.must(object != null,
          "Attempt to pass NULL object into SubtypesAction.run()");
    }

    final Object target = RefactorItActionUtils.unwrapTarget(object);

    try {
      final SubtypesTreeTableModel[] model = new SubtypesTreeTableModel[1];
      JProgressDialog.run(context, new Runnable() {
        public void run() {
          model[0] = new SubtypesTreeTableModel(context.getProject(), target);
        }
      }, true);

      BinTreeTable table = createTable(model[0], context);

      String moduleName = resLocalizedStrings.getString("action.name");
      ResultArea results = ResultArea.create(table, context, SubtypesAction.this);
      results.setTargetBinObject(object);
      BinPanel panel = BinPanel.getPanel(context, moduleName, results);
      table.smartExpand();

      // Register default help for panel's current toolbar
      panel.setDefaultHelp("refact.subtypes");
    } catch (SearchingInterruptedException ex) {
    }

    // we never change anything
    return false;
  }

  private BinTreeTable createTable(BinTreeTableModel model,
      final RefactorItContext context) {

    final BinTreeTable table = new BinTreeTable(model, context);

    // open all branches
    table.expandAll();

    table.setTableHeader(null);

    return table;
  }

  public boolean isReadonly() {
    return true;
  }
  public boolean isAvailableForType(Class type) {
    return BinCIType.class.isAssignableFrom(type)
        || BinConstructor.class.isAssignableFrom(type)
        || BinMethod.Throws.class.equals(type)
        || BinThrowStatement.class.equals(type);
  }
}
