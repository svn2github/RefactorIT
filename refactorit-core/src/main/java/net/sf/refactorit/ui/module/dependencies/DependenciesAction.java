/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.dependencies;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.query.dependency.DependenciesModel;
import net.sf.refactorit.query.dependency.ResultFilter;
import net.sf.refactorit.ui.FilterDialog;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.panel.BinPanel;
import net.sf.refactorit.ui.panel.ResultArea;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import javax.swing.JCheckBox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


/**
 * @author Anton Safonov
 */
public class DependenciesAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.DependenciesAction";
  public static final String NAME = "Show Dependencies";

  static final ResourceBundle resLocalizedStrings
      = ResourceUtil.getBundle(DependenciesAction.class);

  public boolean isMultiTargetsSupported() {
    return true;
  }

  public boolean isPreprocessedSourcesSupported(Class cl) {
    // generates some exceptions in netbeans
//    if( BinCIType.class.isAssignableFrom(cl) ) {
//      return true;
//    }
//
    return true;
  }


  public boolean isAvailableForType(Class type) {
    if (Project.class.equals(type)
        || BinMethod.class.isAssignableFrom(type)
        || BinMethodInvocationExpression.class.isAssignableFrom(type)
        || BinField.class.isAssignableFrom(type)
        || BinFieldInvocationExpression.class.isAssignableFrom(type)
        || BinCIType.class.isAssignableFrom(type)
        || BinPackage.class.equals(type)
        || BinMethod.Throws.class.equals(type)
        || BinThrowStatement.class.equals(type)
        ) {
      return true;
    }
    return false;

  }
  public boolean isReadonly() {
    return true;
  }

  public String getName() {
    return NAME;
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
    Assert.must(context != null,
        "Attempt to pass NULL context into DependenciesAction.run()");
    Assert.must(object != null,
        "Attempt to pass NULL object into DependenciesAction.run()");

    final Object target = unwrapTarget(object, context.getProject());
    final ResultFilter resultFilter = extractResultFilter(object);

    try {
      final DependenciesModel[] model = new DependenciesModel[1];
      JProgressDialog.run(context, new Runnable() {
        public void run() {
          model[0] = new DependenciesModel(context.getProject(), target,
              resultFilter);
        }
      }, target, true);

      // cancelled
      if (((BinTreeTableNode) model[0].getRoot()).isHidden()) {
        return false;
      }
      BinTreeTable table = createTable(model[0], context);
      String moduleName = resLocalizedStrings.getString("action.name");
      ResultArea results
          = ResultArea.create(table, context, DependenciesAction.this);
      results.setTargetBinObject(target);
      BinPanel panel = BinPanel.getPanel(context, moduleName, results);
      table.smartExpand();

      createFilterDialog(context, panel);

      // Register default help for panel's current toolbar
      panel.setDefaultHelp("refact.show_dependencies");
    } catch (SearchingInterruptedException ex) {
    }

    // we never change anything
    return false;
  }

  protected Object unwrapTarget(final Object object, final Project project) {
    Object target = RefactorItActionUtils.unwrapTarget(object);

    // skip ResultFilter
    if (target instanceof Object[]) {
      List result = new ArrayList(((Object[]) target).length);
      for (int i = 0, max = ((Object[]) target).length; i < max; i++) {
        if (((Object[]) target)[i] == null) {
          continue;
        }
        if (!(((Object[]) target)[i] instanceof ResultFilter)) {
          result.add(((Object[]) target)[i]);
        }
      }
      target = result.toArray();
    }

    return target;
  }

  private ResultFilter extractResultFilter(final Object target) {
    if (target instanceof Object[]) {
      for (int i = 0, max = ((Object[]) target).length; i < max; i++) {
        if (((Object[]) target)[i] instanceof ResultFilter) {
          return (ResultFilter) ((Object[]) target)[i];
        }
      }
    }

    return null;
  }

  private void createFilterDialog(
      final IdeWindowContext context, final BinPanel panel) {
    final JCheckBox ignoreJdk = new JCheckBox(resLocalizedStrings.getString(
        "ignore-jdk-packages"));
    ignoreJdk.setToolTipText(resLocalizedStrings.getString(
        "ignore-jdk-packages"));
    ignoreJdk.setSelected(GlobalOptions.getOptionAsBoolean("dependencies-ignore-jdk-packages", true));
    panel.setFilterActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!FilterDialog.showDialog(context, new JCheckBox[] {ignoreJdk},
            resLocalizedStrings.getString("show_dependencies.filter.title"))) {
          return;
        }

        GlobalOptions.setOption("dependencies-ignore-jdk-packages", "" + ignoreJdk.isSelected());
        GlobalOptions.save();
        panel.invokeReRun();
      }
    });
  }

  private BinTreeTable createTable(BinTreeTableModel model,
      final RefactorItContext context) {

    final BinTreeTable table = new BinTreeTable(model, context);

    // open all branches
//    table.expandAll(); // there is sometimes too much

//    table.setTableHeader(null);

    table.getColumnModel().getColumn(1).setMinWidth(5);
    table.getColumnModel().getColumn(1).setPreferredWidth(50);
    table.getColumnModel().getColumn(1).setMaxWidth(100);

    return table;
  }
}
