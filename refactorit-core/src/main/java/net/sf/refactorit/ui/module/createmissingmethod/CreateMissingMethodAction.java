/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.createmissingmethod;

import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.MissingBinMember;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.query.AbstractIndexer;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.createmissing.CreateMethodContext;
import net.sf.refactorit.refactorings.createmissing.CreateMissingMethodRefactoring;
import net.sf.refactorit.source.MethodNotFoundError;
import net.sf.refactorit.source.UserFriendlyError;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactoringStatusViewer;
import net.sf.refactorit.ui.module.RefactorItContext;

import javax.swing.JOptionPane;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;


/**
 *
 * @author  tanel
 */
public class CreateMissingMethodAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.CreateNewMethod";
  public static final String NAME = "Create Missing Method";

  private ResourceBundle bundle
      = ResourceUtil.getBundle(CreateMissingMethodAction.class);

  /** Creates a new instance of CreateNewMethodAction */
  public CreateMissingMethodAction() {
  }

  public boolean isAvailableForType(Class type) {
    if (MissingBinMember.class.isAssignableFrom(type) ||
        MethodNotFoundError.class.isAssignableFrom(type)
        || BinMethod.class.equals(type) ||
        BinMethodInvocationExpression.class.equals(type)) {
      return true;
    }
    return false;
  }

  /** Module execution.
   *
   * It executes the RefactorItAction on target BinXXX object.
   * If you want to make a clean, first time execution of this RefactorItAction,
   * then you MUST provide a new Context() object into run(...) method,
   * otherwise, if you just want to ReRun the action on specified target,
   * then you provide the old context object into run(...) method it was
   * executed previously.
   *
   * Some module actions check the context object for old data they have
   * put into it. For example Metrics should check whether it should display
   * a Dialog full of metric selections to the user or not, it depends whether
   * it finds old data from Context or not.
   *
   * @param context  some native class (For native implemetation of modules)
   * @param parent  any visible component on the screen
   * @param object  Bin object to operate
   * @return  false if nothing changed, true otherwise
   *
   */
  public boolean run(RefactorItContext context, final Object object) {
    final Object[] target = unwrapTarget(object);

    // let's update the project - the bug could have already gone
    // NOTE: When action invoked from result panel, project is not updated automatically,
    // that's why we must do it here.
    if (!(object instanceof MissingBinMember)) {
      try {
        refreshProject(context, target);
      } catch (SearchingInterruptedException ex) {
        return false;
      }
    }

    final Set bugs = new HashSet(
        CollectionUtil.toList(context.getProject().getProjectLoader().getErrorCollector().getUserFriendlyErrors()));

    final List nodesList = new ArrayList();
    boolean wasError = false;
    for (int i = 0; i < target.length; i++) {
      if (!(target[i] instanceof MethodNotFoundError)) {
        continue; // selection contained some crap
      }
      if (bugs.contains(target[i])) {
        if (isFixable((MethodNotFoundError) target[i])) {
          // FIXME: what it does when where are 2 errors about the same method?
          // Creates 2 methods?
          nodesList.add(new CreateMethodContext((MethodNotFoundError) target[i]));
        } else {
          wasError = true;
        }
      }
    }

    if (nodesList.size() > 0) {
      if (wasError) {
        RitDialog.showMessageDialog(context,
            bundle.getString("error.some_classes_not_on_sourcepath"),
            "Warning", JOptionPane.WARNING_MESSAGE);
      }
      final CreateMissingMethodDialog dialog
          = new CreateMissingMethodDialog(context,
          (CreateMethodContext[]) nodesList.toArray(
          new CreateMethodContext[nodesList.size()]));
      dialog.show();
      if (dialog.isOkPressed()) {
        final CreateMissingMethodRefactoring refactoring
            = new CreateMissingMethodRefactoring(context, dialog.getNodes());

        final RefactoringStatus status = refactoring.apply();/*TransformationManager
            .performTransformationFor(refactoring);*/

        if (!status.isOk() && !status.isCancel()) {
          RefactoringStatusViewer statusViewer = new RefactoringStatusViewer(
              context, "<conflicts>", "refact.createmissingmethod");
          statusViewer.display(status);
        }

        return status.isOk() || status.isErrorOrFatal();
      }
    } else {
      DialogManager.getInstance().showInformation(context,
          "error.missingmethod.notfound");
      //this may also mean that bugs have already gone
      //JOptionPane.showMessageDialog(parent,
      //    bundle.getString("error.classes_not_on_sourcepath"),
      //    "Error", JOptionPane.ERROR_MESSAGE);
    }

    return false;
  }

  private void refreshProject(
      final RefactorItContext context, final Object[] target
      ) throws SearchingInterruptedException {
    final List paths = new ArrayList();
    final List types = new ArrayList();
    for (int i = 0; i < target.length; i++) {
      if (target[i] instanceof UserFriendlyError) {
        CollectionUtil.addNew(paths,
            ((UserFriendlyError) target[i]).getCompilationUnit().getSource().
            getRelativePath());
      }
      if (target[i] instanceof MethodNotFoundError) {
        final BinTypeRef invokedIn
            = ((MethodNotFoundError) target[i]).getInvokedIn();
        if (invokedIn != null) {
          CollectionUtil.addNew(types, invokedIn.getQualifiedName());
        }
        if (((MethodNotFoundError) target[i]).getOwner() != null) {
          CollectionUtil.addNew(types,
              ((MethodNotFoundError) target[i]).getOwner().getQualifiedName());
        }

        final BinTypeRef returnType
            = ((MethodNotFoundError) target[i]).getReturnType();
        if (returnType != null && returnType.getNonArrayType().isReferenceType()) {
          CollectionUtil.addNew(types, returnType.getNonArrayType().getQualifiedName());
        }
      }
    }

    JProgressDialog.run(context, new Runnable() {
      public void run() {
        updateEnvironment(context);

        final AbstractIndexer visitor = new AbstractIndexer();

        for (int i = 0; i < paths.size(); i++) {
          String path = (String) paths.get(i);
          CompilationUnit newSource = context.getProject().
              getCompilationUnitForName(path);
          if (newSource != null) {
            visitor.visit(newSource);
          }
        }

        for (int i = 0; i < types.size(); i++) {
          String name = (String) types.get(i);
          BinTypeRef newType = context.getProject().getTypeRefForName(name);
          if (newType != null && newType.isReferenceType()) {
            visitor.visit(newType.getBinCIType());
          }
        }

// This takes too long to complete :(
//        visitor.visit(context.getProject());
      }
    }


    , "Refreshing project", true);
  }

  private Object[] unwrapTarget(final Object object) {
    final Object[] target;
    if (!(object instanceof Object[])) {
      target = new Object[] {object};
    } else {
      target = (Object[]) object;
    }
    for (int i = 0; i < target.length; i++) {
      if (target[i] instanceof MissingBinMember) {
        target[i] = ((MissingBinMember) target[i]).error;
      }
    }

    return target;
  }

  private boolean isFixable(MethodNotFoundError error) {
    try {
      return error.getOwner().getBinCIType().isFromCompilationUnit();
    } catch (NullPointerException e) {
      return false;
    }
  }

  /** Determines whether this action can operate with
   * multiple selected objects.
   *
   * @return  true if action can operate with multiple targets.
   *
   */
  public boolean isMultiTargetsSupported() {
    return true;
  }

  /** Name of action (shown in menus).
   *
   * @return  name
   *
   */
  public String getName() {
    return NAME;
  }

  /** Returns unique key for this action.
   *
   * @return  key
   *
   */
  public String getKey() {
    return KEY;
  }

  public boolean isReadonly() {return false;
  }

  public char getMnemonic() {
    return 'T';
  }
}
