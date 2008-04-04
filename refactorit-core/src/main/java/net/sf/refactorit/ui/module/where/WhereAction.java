/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.where;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.classmodel.references.BinItemReference;
import net.sf.refactorit.classmodel.statements.BinLabeledStatement;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.query.usage.filters.SearchFilter;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.IdeWindowContext;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.panel.BinPanel;
import net.sf.refactorit.ui.panel.ResultArea;
import net.sf.refactorit.ui.treetable.BinTreeTable;

import javax.swing.JOptionPane;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;



/**
 * It is responsible for executing the where used process. Starting the
 * process until displaying the results on the screen.
 */
public class WhereAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.WhereAction";

  private boolean neededTableLastTime = true;

  static ResourceBundle resLocalizedStrings =
      ResourceUtil.getBundle(WhereAction.class);

  public boolean isMultiTargetsSupported() {
    return true;
  }

  public boolean isReadonly() {
    return true;
  }

  public boolean isAvailableForType(Class type) {
    if (type != null) {
      String h = "";
      if (BinMethod.class.isAssignableFrom(type)
          || BinVariable.class.isAssignableFrom(type)
          || BinCIType.class.isAssignableFrom(type)
          || BinMemberInvocationExpression.class.isAssignableFrom(type)
          || BinMethod.Throws.class.equals(type)
          || BinThrowStatement.class.equals(type)
          || BinPackage.class.equals(type)
          || BinLabeledStatement.class.equals(type)) {
        return true;
      }
    }

    return false;
  }

  public boolean isPreprocessedSourcesSupported(Class cl) {
//    if( BinCIType.class.isAssignableFrom(cl) || !BinItem.class.isAssignableFrom(cl)) {
//      return false;
//    }
    return true;
  }

  public String getKey() {
    return KEY;
  }

  public String getName() {
    return resLocalizedStrings.getString("action.name");
  }

  public char getMnemonic() {
    return 'W';
  }

  /**
   * Module execution.
   *
   * @param context
   * @param object  Bin object to operate

   * @return  false if nothing changed, true otherwise
   */
  public boolean run(final RefactorItContext context, final Object object) {
    // Catch incorrect parameters
    Assert.must(context != null,
        "Attempt to pass NULL context into WhereAction.run()");
    Assert.must(object != null,
        "Attempt to pass NULL object into WhereAction.run()");

    final Object target;

    // the object to be searched for where used. i.e. Class, method
    if (object instanceof BinMethod.Throws) {
      target = ((BinMethod.Throws) object).getException().getBinCIType();
    } else if (object instanceof BinThrowStatement) {
      target = ((BinThrowStatement) object).getExpression().getReturnType().
          getBinType();
    } else {
//      if (object instanceof Object[]) {
//        if (!isObjectsOfSameType((Object[]) object)) {
//          JOptionPane.showMessageDialog(DialogManager.findOwnerWindow(parent),
//              "Cannot perform Where Used on the objects of different types",
//              "Error", JOptionPane.ERROR_MESSAGE);
//          return false;
//        }
//      }
      target = object;
    }

    SearchFilter filter = (SearchFilter) context.getState();

    if (filter == null) {
      filter = askForOptions(context, target);
      if (filter == null) {
        return false; // cancel
      }
    }

    final SearchFilter searchFilter = filter;

    context.postponeShowUntilNotified(); // FIXME: is it still needed? check, refactor and remove

    // Call the JProgressDialog to start the process of WhereUsed.
    try {
      final WhereModel[] model = new WhereModel[1];

      JProgressDialog.run(context, new Runnable() {
        public void run() {
          model[0] = new WhereModel(context, target, searchFilter);
        }
      }, target, true);

      boolean needTable = model[0].getMessage() == null;
      neededTableLastTime = needTable;

      if (needTable) {
        BinTreeTable table = createTable(context, model[0]);

        // Create the ResultArea object to hold the rundata/result for
        //component shown to the user and then provide it to the getPanel(...)
        // function.
        // returns a panel: where WhereAction results are hold.

        ResultArea results = ResultArea.create(table, context,
            WhereAction.this);
        table.smartExpand();

        results.setTargetBinObject(target);
        BinPanel panel = BinPanel.getPanel(
            context, resLocalizedStrings.getString("module.name"), results);
        panel.setDefaultHelp("refact.whereUsed");

        initFilterButtons(panel, context, target);

        context.showPostponedShows(); // FIXME: is it still needed? check, refactor and remove

        table.requestFocusInWindow();

      } else if (model[0].getMessage().length > 0 && model[0].getMessage()[0] != null) {
        RitDialog.showMessageDialog(
            context, model[0].getMessage()[1], model[0].getMessage()[0],
            JOptionPane.INFORMATION_MESSAGE);
      } else {
        context.showPostponedShows();
      }
    } catch (SearchingInterruptedException ex) {
//      Project project=context.getProject();
//      if ( project!=null ) {
//        // marking parsing as canceled because we never know is cache correct or not
//        //project.cancelParsing();
//      }
      return false;
    }

    return false;
  }

  void initFilterButtons(
      final BinPanel panel, final RefactorItContext context, Object anObject
  ) {
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

        SearchFilter newFilter = askForOptions(context, object);

        if (newFilter != null) {
          reload(panel, context, object, newFilter);
        }
      }
    };

    panel.setFilterActionListener(listener);
  }

  SearchFilter askForOptions(IdeWindowContext context, Object object) {
    WhereUsedDialog dialog = new WhereUsedDialog(
        context, object, (SearchFilter) context.getState());

    if (context.getState() != null || !dialog.isRunWithDefaultSettings()) {
      dialog.show();
    } else {
      dialog.forceOkPressed();
    }

    SearchFilter newFilter = null;
    if (dialog.isOkPressed()) {
      newFilter = dialog.getFilter();
      if (newFilter != null) {
        // just to be sure
        context.setState(newFilter);
      }
    }

    return newFilter;
  }

  void reload(
      final BinPanel panel, final RefactorItContext context,
      final Object object, final SearchFilter filter
  ) {
    final Object[] callback = new Object[1];

    try {
      JProgressDialog.run(context, new Runnable() {
        public void run() {
          final WhereModel model = new WhereModel(context, object, filter);
          callback[0] = model.getMessage();

          boolean needTable = callback[0] == null;
          neededTableLastTime = needTable;

          if ( ! needTable) {
            context.removeTab(panel.getIDEComponent());
            return;
          }

          BinTreeTable table = createTable(context, model);
          panel.reload(table);
          table.smartExpand();
          table.requestFocusInWindow();

        }
      }, object, false);
    } catch (SearchingInterruptedException ex) {
      return;
    }

    if (callback[0] != null && ((Object[]) callback[0])[0] != null) {
      RitDialog.showMessageDialog(context,
          ((Object[]) callback[0])[1],
          (String) ((Object[]) callback[0])[0],
          JOptionPane.INFORMATION_MESSAGE);
    }
  }

  BinTreeTable createTable(RefactorItContext context, WhereModel model) {
    BinTreeTable table = new BinTreeTable(model, context);

    table.getColumnModel().getColumn(1).setMinWidth(5);
    table.getColumnModel().getColumn(1).setPreferredWidth(50);
    table.getColumnModel().getColumn(1).setMaxWidth(100);

    return table;
  }

  protected boolean enableRaisingResultsPane() {
    return neededTableLastTime;
  }
}
