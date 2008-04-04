/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.dependencies;


import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.graph.BinNode;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.panel.BinPanel;
import net.sf.refactorit.ui.panel.ResultArea;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


/**
 */
public class DependencyLoopsAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.DependencyLoopsAction";
  public static final String NAME = "Show Dependency Cycles";

  private static final ResourceBundle resLocalizedStrings
      = ResourceUtil.getBundle(DependencyLoopsAction.class);

  public boolean isAvailableForType(Class type) {
    return (type!=null) &&
    (BinNode.class.isAssignableFrom(type) || type.equals(Integer.class) ||
        type.equals(ArrayList.class));
  }

  public boolean isAvailableForTarget(Object[] target) {
    if(target!=null && target.length == 2
        && target[0] instanceof List
        && (target[1] instanceof BinNode || target[1] instanceof Integer)) {
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
  public boolean run(final RefactorItContext context, final Object object) {
    // Catch incorrect parameters

    final List loops = unwrapLoops(object);
    final BinNode target = unwrapTarget(object);

    Assert.must(context != null,
        "Attempt to pass NULL context into DependencyLoopsAction.run()");
    Assert.must(loops != null,
        "Attempt to pass unsuitable loop list into DependencyLoopsAction.run()");



    try {
      final DependencyLoopsModel[] model = new DependencyLoopsModel[1];
      JProgressDialog.run(context, new Runnable() {
        public void run() {
          model[0] = new DependencyLoopsModel(context.getProject(), loops, target);
        }
      }, target, true);


      BinTreeTable table = createTable(model[0], context);
      String moduleName = resLocalizedStrings.getString("action.cycles_name");
      ResultArea results = ResultArea.create(table, context, DependencyLoopsAction.this);
      results.setTargetBinObject(target);
      BinPanel panel = BinPanel.getPanel(context, moduleName, results);
      panel.getCurrentPane().setRerunEnabled(false);
      table.smartExpand();
      // Register default help for panel's current toolbar
      //panel.setDefaultHelp("refact.dependency_loops");

    } catch (SearchingInterruptedException ex) {
    }
    return false;
  }

  private List unwrapLoops(Object object) {
    if(object instanceof Object[]){
      Object[] arr = (Object[])object;
      if(arr.length >0){
        if(arr[0] instanceof List){
          return (List)arr[0];
        }
      }
    }
    return null;
  }

  private BinNode unwrapTarget(Object object) {
    if(object instanceof Object[]){
      Object[] arr = (Object[])object;
      if(arr.length > 1){
        if(arr[1] instanceof BinNode){
          return (BinNode)arr[1];
        }
      }
    }
    return null;
  }

  private BinTreeTable createTable(BinTreeTableModel model,
      final RefactorItContext context) {

    final BinTreeTable table = new BinTreeTable(model, context);
    table.smartExpand();
    table.setTableHeader(null);
    return table;
  }
}
