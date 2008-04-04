/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.notused;

import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinInterface;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.query.notused.ExcludeFilterRule;
import net.sf.refactorit.query.notused.NotUsedTreeTableModel;
import net.sf.refactorit.reports.ReportGenerator;
import net.sf.refactorit.reports.ReportGeneratorFactory;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.errors.ErrorsTab;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.panel.BinPanel;
import net.sf.refactorit.ui.panel.ResultArea;
import net.sf.refactorit.ui.treetable.BinTreeTable;
import net.sf.refactorit.ui.treetable.BinTreeTableModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;



/**
 * @author Anton Safonov
 */
public class NotUsedAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.NotUsedAction";
  public static final String NAME = "Not Used";

  static ResourceBundle resLocalizedStrings
      = ResourceUtil.getBundle(NotUsedAction.class);

//  private static final ImageIcon iconExcludes =
//    ResourceUtil.getIcon(NotUsedAction.class, "Excludes.gif");

  public boolean isMultiTargetsSupported() {
    return false;
  }

  public String getKey() {
    return KEY;
  }

  public boolean isReadonly() {
    return true;
  }

  public String getName() {
    return resLocalizedStrings.getString("action.name");
  }

  public char getMnemonic() {
    return 'N';
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
    {
      Assert.must(context != null,
          "Attempt to pass NULL context into NotUsedAction.run()");
      Assert.must(object != null,
          "Attempt to pass NULL object into NotUsedAction.run()");
    }

//    context.getProject().accept(new AbstractIndexer() {});
//    if (true) {
//      return false;
//    }

    ExcludeFilterRule[] rules = (ExcludeFilterRule[]) context.getState();

    if (rules == null) {
      rules = askForOptions(context, object);

      if (rules == null) {
        return false; // cancel
      }
    }

    Object target = RefactorItActionUtils.unwrapTarget(object);

    new ActionInstance().run(context, target);

    // we never change anything
    return false;
  }

  ExcludeFilterRule[] askForOptions(
      final IdeWindowContext context, final Object object
  ) {
    ExcludeFilterRule[] selected = NotUsedDialog.show(new FilterPanel());

    if (selected != null) {
      // just to be sure
      context.setState(selected);
    }

    return selected;
  }

  public boolean isAvailableForType(Class type) {
    return Project.class.equals(type)
        || BinPackage.class.equals(type)
        || BinClass.class.equals(type)
        || BinInterface.class.equals(type)
        || BinConstructor.class.equals(type)
        || BinMethod.Throws.class.equals(type)
        || BinThrowStatement.class.equals(type);
  }

  /**
   * Represents one 'Not Used' instance. Holds a list of excluded words.
   */
  private class ActionInstance {
    public void run(final RefactorItContext context, final Object target) {
      Object s = context.getState();
      if ((s == null) || !(s instanceof ExcludeFilterRule[])) {
        return;
      }

      final ExcludeFilterRule[] filterRules = (ExcludeFilterRule[]) s;

      final NotUsedTreeTableModel[] model = new NotUsedTreeTableModel[1];
      try {
        JProgressDialog.run(context, new Runnable() {
          public void run() {
            model[0] = new NotUsedTreeTableModel(
                context.getProject(), target, filterRules);
          }
        }, true);
      } catch (SearchingInterruptedException ex) {
        return;
      }

      if (model[0] == null) {
        return; // search crashed, nothing to show anyway
      }

      BinTreeTable table = createTable(model[0], context);

      String moduleName = resLocalizedStrings.getString("module.name");
      ResultArea results = ResultArea.create(table, context, NotUsedAction.this);
      results.setTargetBinObject(target);
      BinPanel panel = BinPanel.getPanel(context, moduleName, results);
      table.smartExpand();

      initFilterButtons(model, panel, context, target);
      ReportGenerator gen = ReportGeneratorFactory.getNotUsedReportGenerator();
      panel.addToolbarButton(gen.getReportButton(model, context, target));
      
      panel.setDefaultHelp("refact.notUsed");
    }

    BinTreeTable createTable(BinTreeTableModel model,
        final RefactorItContext context) {
      final BinTreeTable table = new BinTreeTable(model, context);

      // open types branch
      table.getTree().expandRow(1);

      // Line number has usually small width
      table.getColumnModel().getColumn(1).setMinWidth(5);
      table.getColumnModel().getColumn(1).setPreferredWidth(50);
      table.getColumnModel().getColumn(1).setMaxWidth(100);

      return table;
    }

    private void initFilterButtons(final NotUsedTreeTableModel[] model, final BinPanel panel,
        final RefactorItContext context, Object anObject) {
      final BinItemReference objectReference = BinItemReference.create(
          anObject);

      ActionListener listener = new ActionListener() {
        public void actionPerformed(ActionEvent event) {
          Object object = objectReference.restore(context.getProject());

          if (object == null) {
            DialogManager.getInstance().showCustomError(
                context, "Info", "Cannot rediscover the target.");
            return;
          }

          ExcludeFilterRule[] rules = askForOptions(context, object);

          if (rules != null) {
            reload(model, panel, context, object, rules);
          }
        }
      };

      panel.setFilterActionListener(listener);
    }

    void reload(
        final NotUsedTreeTableModel[] model, final BinPanel panel, final RefactorItContext context,
        final Object object, final ExcludeFilterRule[] rules
    ) {
      try {
        //final NotUsedTreeTableModel[] model = new NotUsedTreeTableModel[1];
        JProgressDialog.run(context, new Runnable() {
          public void run() {
            model[0] = new NotUsedTreeTableModel(context.getProject(), object,
                rules);
          }
        }, object, true);

        final BinTreeTable table = createTable(model[0], context);

        ErrorsTab.addNew(context);
        context.showTab(panel.getIDEComponent());

        panel.reload(table);
        table.smartExpand();
      } catch (SearchingInterruptedException ex) {
      }
    }
  }
}
