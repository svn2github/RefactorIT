/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module;

import net.sf.refactorit.audit.ReconcileActionDecorator;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinSelection;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.commonIDE.IdeAction;
import net.sf.refactorit.loader.ProjectLoader;
import net.sf.refactorit.refactorings.undo.IUndoManager;
import net.sf.refactorit.refactorings.undo.IUndoableTransaction;
import net.sf.refactorit.refactorings.undo.RitUndoManager;
import net.sf.refactorit.reports.Statistics;
import net.sf.refactorit.source.MethodNotFoundError;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.errors.ErrorsTab;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class RefactorItActionUtils {
  public static Object unwrapTarget(Object object) {
    if (object instanceof BinConstructor) {
      // Warning, it returns BinClass, not BinConstructor!!
      return ((BinConstructor) object).getOwner().getBinCIType();
    } else {
      return unwrapTargetIfNotConstructor(object);
    }
  }

  public static Object unwrapTargetIfNotConstructor(Object object) {
    if (object instanceof Object[]) {
      Object[] current = (Object[]) object;
      List result = new ArrayList(current.length);
      for (int i = 0; i < current.length; i++) {
//        Object[] result = new Object[current.length];
        Object unwrapped = unwrapTarget(current[i]);
        if(unwrapped != null) {
          result.add(unwrapped);
        }
      }
      return result.toArray();
    }

    if (object instanceof BinMethod.Throws) {
      return ((BinMethod.Throws) object).getException().getBinType();
    }

    if (object instanceof BinThrowStatement) {
      return ((BinThrowStatement) object).getExpression()
          .getReturnType().getBinType();
    }

    if (object instanceof BinMemberInvocationExpression) {
      return ((BinMemberInvocationExpression) object).getMember();
    }

    if (object instanceof BinVariableUseExpression) {
      return ((BinVariableUseExpression) object).getVariable();
    }

    return object;
  }

  /**
   *  If action doesn't support BinSelection it filters out BinSelection elements.
   *  Otherwise just returns target.
   *  If it supports BinSelection, first of all return BinSelection
   * FIXME: remove need for this, action should be able to handle case by itself
   */
  public static final Object separateSelectionItems(
      final Object target, final RefactorItAction action) {

    Object targetArray[] = null;

    if (!(target instanceof Object[])) {
//      targetArray = new Object[] {target};
      return target;
    } else {
      targetArray = (Object[]) target;
    }

    if (targetArray.length == 1) {
      return targetArray[0];
    }

    final boolean actionSupportsSelection
        = action.isAvailableForType(BinSelection.class);

    if (actionSupportsSelection) {
      for (int i = 0, max = targetArray.length; i < max; i++) {
        if (targetArray[i] instanceof BinSelection) {
          return targetArray[i];
        }
      }

      return target;
    }

    ArrayList result = new ArrayList(targetArray.length);

    for (int i = 0, max = targetArray.length; i < max; i++) {
      boolean isSelection = targetArray[i] instanceof BinSelection;
      if (!isSelection) {
        result.add(targetArray[i]);
      }
    }

    if (result.size() == 1) {
      return result.get(0);
    }

    if (result.size() > 0) {
      return result.toArray(new Object[result.size()]);
    }

    return null;
  }

  public static final boolean run(IdeAction action) {
    IDEController controller = IDEController.getInstance();

    if (action.needsEnsureProject()) {
      controller.ensureProjectWithoutParsing();
    }

    RefactorItContext context = controller.createProjectContext();

    if (action.run(context)) {
      context.rebuildAndUpdateEnvironment();
      return true;
    }

    return false;
  }

  public static final boolean run(
      final RefactorItAction action,
      final RefactorItContext context,
      final Object object
  ) {
    final Object target = separateSelectionItems(object, action);

//    if (Assert.enabled) {
      Assert.must(target != null && !target.getClass().equals(Object.class),
          "Received old-way target signifying project: " + target);
//    }

    String objectName;
    try {
      if (target instanceof Project) {
        objectName = "project " + ((Project) target).getName();
      } else if (target instanceof BinSelection) {
        objectName = "";
      } else if (target instanceof Object[]) {
        objectName = Arrays.asList(
            JProgressDialog.getDetails((Object[]) target)).toString();
      } else if (target instanceof BinItem) {
        objectName = JProgressDialog.getDetails((BinItem) target);
      } else if (target instanceof MethodNotFoundError) {
        objectName = ((MethodNotFoundError) target).getMethodName() + "( )";
      } else {
        objectName = target.toString();
      }
    } catch (IllegalArgumentException e) {
      // happens when getDetails doesn't know what to do
      objectName = target.toString();
    }
  if(action.getClass() != ReconcileActionDecorator.class)
    Statistics.getInstance().addUsage(Statistics.CATEGORY_COMMON, action.getKey(), action.getName(), null);
    //System.out.println("Started \'" + action.getName() + "\' on " + objectName + " " + action.getKey());*/
    boolean creatingUndo = UndoModule.UNDO_ENABLED && !action.isReadonly() &&
        !(action instanceof UndoAction || action instanceof RedoAction ||
        action instanceof UndoMilestoneAction);

    IUndoManager undoManager = RitUndoManager.getInstance(context.getProject());

    IUndoableTransaction transaction = null;
    boolean finishedTransactionCorrectly = false;

    final boolean oldMemoryMode = ProjectLoader.isLowMemoryMode();
    if (!action.isReadonly()) {
      ProjectLoader.setLowMemoryMode(false); // during analysis it may traverse the model several times
    }

    try {
      if (creatingUndo) {
        transaction = undoManager
            .createTransaction(action.getName(), objectName);
      }

      boolean result = action.run(context, target);

      ErrorsTab.showAddToIgnoreDialog(context, ErrorsTab.getModel());
      finishedTransactionCorrectly = true;

//      GlobalOptions.logUsage("Finished \'" + action.getName() + "\' on " + objectName + " - " + result);

      return result;
    } catch (RuntimeException e) {
      AppRegistry.getExceptionLogger().error(e,
          "Crashed \'" + action.getName() + "\' on \'" + objectName + "\'",
          RefactorItActionUtils.class);

      throw e;
    } finally {
      if (creatingUndo && transaction != null) {
        Assert.must(transaction == RitUndoManager.getCurrentTransaction());
        if (finishedTransactionCorrectly) {
          undoManager.commitTransaction();
        } else {
          undoManager.rollbackTransaction();
        }
      }

      ProjectLoader.setLowMemoryMode(oldMemoryMode);
    }
  }
}
