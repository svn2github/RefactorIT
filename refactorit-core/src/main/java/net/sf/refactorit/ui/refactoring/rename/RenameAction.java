/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.refactoring.rename;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinInterface;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.classmodel.statements.BinLabeledStatement;
import net.sf.refactorit.classmodel.statements.BinThrowStatement;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.ResourceUtil;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.ConfirmationTreeTableModel;
import net.sf.refactorit.refactorings.rename.RenameField;
import net.sf.refactorit.refactorings.rename.RenameLabel;
import net.sf.refactorit.refactorings.rename.RenameMultiLocal;
import net.sf.refactorit.refactorings.rename.RenamePackage;
import net.sf.refactorit.refactorings.rename.RenameRefactoring;
import net.sf.refactorit.refactorings.rename.RenameType;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.JProgressDialog;
import net.sf.refactorit.ui.SearchingInterruptedException;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.errors.ErrorsTab;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactorItActionUtils;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.module.RefactoringStatusViewer;
import net.sf.refactorit.utils.GetterSetterUtils;

import javax.swing.JOptionPane;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


/**
 * @author Anton Safonov
 */
public class RenameAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.RenameAction";
  public static final String NAME = "Rename";
  public String newName = "";

  private static ResourceBundle resLocalizedStrings =
      ResourceUtil.getBundle(RenameAction.class);

  public boolean isAvailableForType(Class type) {
	return BinMethod.class.equals(type)
        || BinConstructor.class.equals(type)
        || BinVariable.class.isAssignableFrom(type)
        || BinMemberInvocationExpression.class.isAssignableFrom(type)
        || BinPackage.class.equals(type)
        || BinCIType.class.isAssignableFrom(type)
        || BinMethod.Throws.class.equals(type)
        || BinThrowStatement.class.equals(type)
        || BinLabeledStatement.class.equals(type);
  }

  public boolean isMultiTargetsSupported() {
    return false;
  }

  public String getName() {
    return NAME;
  }

  public char getMnemonic() {
    return 'R';
  }

  public String getKey() {
    return KEY;
  }

  public boolean isReadonly() {
    return false;
  }

  public String getNewName(){
    return newName;
  }

  /*
  public boolean run(final RefactorItContext context, final Object object) {
    final net.sf.refactorit.classmodel.Project project = context.getProject();

    DialogManager realOne = DialogManager.getInstance();
    DialogManager.setInstance( new NullDialogManager() );

    JProgressDialog.run(parent, new Runnable() {
      public void run() {
        for( int i = 0; i < 20; i++ ) {
          // rename from ASd to ASd2
          RenameType renamer = new RenameType(context, parent,
            project.getTypeRefForName("rtyu.ASd").getBinCIType());
          renamer.setNewName("ASd2");
          renamer.performChange();
          updateEnvironment(parent, context);
          try {Thread.sleep(2000);} catch(Exception e) {e.printStackTrace();}

          // rename back from ASd2 to ASd
          renamer = new RenameType(context, parent,
            project.getTypeRefForName("rtyu.ASd2").getBinCIType());
          renamer.setNewName("ASd");
          renamer.performChange();

          updateEnvironment(parent, context);
          try {Thread.sleep(2000);} catch(Exception e) {e.printStackTrace();}
        }
      }
    } );

    DialogManager.setInstance(realOne);

    return true;
  }
  */

  /**
   * Module execution.
   *
   * @param context if return value of context.getState() is a String, then
   *      this string will be suggested as new name in user-dialog. Use
   *      context.setState("newName") to suggest a name to user.
   * @param parent  any visible component on the screen
   * @param object  Bin object to operate
   * @return  false if nothing changed, true otherwise
   */



  public boolean run(final RefactorItContext context, final Object object) {
  Object target = RefactorItActionUtils.unwrapTarget(object);
  target = suggestBetterRefactoring(context, (BinItem)target);
  final RenameRefactoring refactoring = RenameRefactoring.getRefactoring(context, (BinItem) target);

  if(!prepareRefactoring(context, refactoring, target)){
    return false;
  }

  return performRefactoringTransformation(context, refactoring);

}

  private Object suggestBetterRefactoring(final RefactorItContext context, BinItem item) {
    if(item instanceof BinMethod) {
      BinMethod method = ((BinMethod) item);

      BinField field = GetterSetterUtils.getAccessibleProperty(method, true);
      if(field != null) {
        int result = RitDialog.showConfirmDialog(context,
            "RefactorIT has detected that you are trying to rename field's setter or getter. \n" +
            "Would you like to rename this field instead?",
            "Better refactoring was found",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
          GlobalOptions.setOption("rename.getters_and_setters", "true");
          return field;
        } else {
          return method;
        }

      }
    }
    return item;
  }

  /**
   * Transformation list filling method.
   * It DOES NOT execute the RefactorIt action, byt fills the list, given
   * as a parameter with appropriate refactoring transformations.
   *  @param context context context on which refactoring was run,
   *                includes: project, point, state
   * @param object object Bin object to operate
   * @param transList Transformation list, that will be updated
   *           with selected refactoring transformations
   * @return false if nothing changed, true otherwise. Return value should be
   * used to decide whether the environment must be updated.
   */

  public boolean run(final RefactorItContext context, final Object object, final TransformationList transList){
    Object target = RefactorItActionUtils.unwrapTarget(object);
    final RenameRefactoring refactoring = RenameRefactoring.getRefactoring(context, (BinItem) target);

    if(!prepareRefactoring(context, refactoring, target)){
        return false;
    }

        transList.merge(refactoring.performChange());
    return true;
  }

  /**
   *  Prepares the rename refactoring using GUI rename dialog.
   */

  private boolean prepareRefactoring(final RefactorItContext context,
      final RenameRefactoring refactoring, Object target){

    final RefactoringStatus[] result = new RefactoringStatus[] {null};

    try {
        JProgressDialog.run(context, new Runnable() {
          public void run() {
            result[0] = refactoring.checkPreconditions();
          }
        }, true);
      } catch (SearchingInterruptedException ex) {
        //context.getProject().cancelParsing();
        return false;
      }

      ErrorsTab.addNew(context);

      RefactoringStatus status = result[0];
      if (!status.isOk()) {
        if (status.getAllMessages().length() > 0) {
          RitDialog.showMessageDialog(context,
              status.getAllMessages(), "Problems with rename",
              status.getJOptionMessageType());

//    TODO BinTreeTable of the viewer works badly with multiline status messages
//          RefactoringStatusViewer statusViewer
//              = new RefactoringStatusViewer(context, parent,
//                  "Problems with rename", "refact.rename");
//          statusViewer.display(status);
    //
//          if (!statusViewer.isOkPressed()) {
//            return false;
//          }
        }
      }

      if (status.isErrorOrFatal() || status.isCancel()) {
        return false;
      }

      AbstractRenameDialog dialog = getDialog(context, (BinItem) target);
      if (context.getState() instanceof String){
        dialog.setNewName((String) context.getState());
      }

      do {
        dialog.show();

        if (!dialog.isOkPressed()) {
          return false; // cancelled
        }

        forwardUserInput(dialog, refactoring);

        status = refactoring.checkUserInput();
        if (status.isOk() || status.isCancel()) {
          break;
        }

        if (status.isQuestion()) {
          int res = RitDialog.showConfirmDialog(context,
              status.getAllMessages(), "Conflicts found",
              JOptionPane.OK_CANCEL_OPTION, status.getJOptionMessageType());
          if (res != JOptionPane.CANCEL_OPTION) {
            break;
          }
          status.addEntry("", RefactoringStatus.CANCEL);
        } else {
          if (status.getEntriesNum() <= 1) {
            RitDialog.showMessageDialog(context,
                status.getAllMessages(), "Problems with rename",
                status.getJOptionMessageType());
          } else {
            RefactoringStatusViewer statusViewer
                = new RefactoringStatusViewer(context,
                "Some conflicts arose during rename.", "refact.rename");
            statusViewer.display(status);
//            JOptionPane.showMessageDialog(DialogManager.findOwnerWindow(parent),
//                status.getAllMessages(),
//                "Conflicts found",
//                status.getJOptionMessageType());
            if (!statusViewer.isOkPressed()) {
              status.addEntry("", RefactoringStatus.CANCEL);
            }
          }
        }

        if (status.isInfoOrWarning() || status.isCancel()) {
          break;
        }
      } while (true);

      newName = dialog.getNewName();


//      if(refactoring instanceof RenameType) {
//        RenameType renameType = (RenameType)refactoring;
//        if((renameType.isSemanticRename())) {
//          RenameType.ItemUsages usages = renameType.getSemanticRenameItemUsages();
//          List invocationDatas = showSemanticRenameItemsToUser(usages, context);
//          if(invocationDatas.size() > 0) {
//            renameType.setAdditionalItems(InvocationData.getInvocationObjects(invocationDatas));
//          } else {
//            return false;
//          }
//        }
//      }


      if (status.isCancel()) {
        return false;
      }


      return true;
  }

  private List showSemanticRenameItemsToUser(RenameType.ItemUsages usages,
      final RefactorItContext context) {

    ConfirmationTreeTableModel model =
        new ConfirmationTreeTableModel("", usages.getAllUsages(), usages.getNonCheckedUsages());

    model = (ConfirmationTreeTableModel) DialogManager
        .getInstance().showSettings("Semantic rename", context, model,
        "RefactorIT detected the following semantically coupled items: ", "refact.semantic_rename");

    if (model == null) {
      // null - when user pressed cancle button, so return empty list to remove
      return new ArrayList();
    }

    return model.getCheckedUsages();
  }


  /**
   *  Executes the refactoring given as a parameter wrapped in GUI
   *
   */
  private boolean performRefactoringTransformation(final RefactorItContext context,
      final RenameRefactoring refactoring){
    final RefactoringStatus[] result = new RefactoringStatus[] {null};
    if (!IDEController.runningJDev()) {
      try {
        JProgressDialog.run(context, new Runnable() {
          public void run() {
            result[0] = refactoring.apply();
          }
        }, false);
      } catch (SearchingInterruptedException ex) {
        Assert.must(false);
      }
    } else {
      result[0] = refactoring.apply();
    }

    RefactoringStatus status = result[0];
    if (status == null || status.isCancel()) {
      return false;
    }

    if (!status.isErrorOrFatal()) {
      if (!status.isOk()) {
        if (status.getAllMessages().length() > 0) {
          RitDialog.showMessageDialog(context,
              status.getAllMessages(), "Problems during rename",
              status.getJOptionMessageType());
        }
      }
      return true;
    } else {
      RitDialog.showMessageDialog(context,
          status.getAllMessages(), "Failed to rename",
          status.getJOptionMessageType());
    }

    return false;
  }


  private void forwardUserInput(AbstractRenameDialog dialog,
      RenameRefactoring refactoring) {
    if (refactoring instanceof RenamePackage) {
      RenamePackage renamePackage = (RenamePackage) refactoring;

      renamePackage.setNewName(((JRenamePackageDialog) dialog).getNewName());
      renamePackage.setRenameInJavadocs(((JRenamePackageDialog) dialog)
          .isRenameInJavadocs());
      renamePackage.setRenamePrefix(((JRenamePackageDialog) dialog)
          .isRenamePrefix());
      renamePackage.setPrefix(((JRenamePackageDialog) dialog).getPrefix());
      renamePackage.setRenameInNonJavaFiles(((JRenamePackageDialog) dialog)
          .isRenameInNonJavaFiles());
    } else if(refactoring instanceof RenameLabel) {
      RenameLabel renameLabel = (RenameLabel) refactoring;
      renameLabel.setNewName(((JRenameLabelDialog)dialog).getNewName());
    } else {
      refactoring.setNewName(((JRenameMemberDialog) dialog).getNewName());
      refactoring.setRenameInJavadocs(((JRenameMemberDialog) dialog)
          .isRenameInJavadocs());
      if (refactoring instanceof RenameType) {
        ((RenameType) refactoring)
            .setRenameInNonJavaFiles(((JRenameMemberDialog) dialog)
            .isRenameInNonJavaFiles());
        ((RenameType) refactoring)
        .setSemanticRename(((JRenameMemberDialog) dialog).isSemanticRename());
      }
      if (refactoring instanceof RenameField) {
        ((RenameField) refactoring)
            .setRenameGettersAndSetters(((JRenameMemberDialog) dialog)
            .isRenameGettersAndSetters());
      }
      if (refactoring instanceof RenameMultiLocal) {
        ((RenameMultiLocal) refactoring).setIncludeOverridedMethods(
             ((JRenameMemberDialog) dialog)
            .isRenameInHierarchy());

      }
    }
  }

  private AbstractRenameDialog getDialog(
      RefactorItContext context, BinItem item
  ) {
    if (item instanceof BinPackage) {
      return new JRenamePackageDialog(context, (BinPackage) item);
    } else if(item instanceof BinLabeledStatement) {
      return new JRenameLabelDialog(context, (BinLabeledStatement)item);
    }

    String title;
    String helpContextId;

    if (item instanceof BinMethod) {
      title = "title.method";
      helpContextId = "refact.rename.method";
    } else if (item instanceof BinField) {
      title = "title.field";
      helpContextId = "refact.rename.field";
    } else if (item instanceof BinCIType) {
      if (item instanceof BinClass) {
        title = "title.class";
      } else if (item instanceof BinInterface) {
        title = "title.interface";
      } else {
        title = "title.type";
      }
      helpContextId = "refact.rename.type";
    } else if (item instanceof BinLocalVariable) {
      title = "title.local";
      helpContextId = "refact.rename.local";
    } else {
      title = "title.item";
      helpContextId = "refact.rename";
    }

    return new JRenameMemberDialog(context,
        resLocalizedStrings.getString(title), (BinMember) item, helpContextId);
  }

  public void updateEnvironment(final RefactorItContext context) {
    super.updateEnvironment(context);

    context.getProject().cleanEmptyPackages();
  }
}
