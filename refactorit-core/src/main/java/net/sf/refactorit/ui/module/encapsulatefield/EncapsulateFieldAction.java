/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.ui.module.encapsulatefield;


import net.sf.refactorit.classmodel.BinAnnotation;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.encapsulatefield.EncapsulateField;
import net.sf.refactorit.refactorings.encapsulatefield.EncapsulateFields;
import net.sf.refactorit.refactorings.encapsulatefield.EncapsulateTreeTableModel;
import net.sf.refactorit.ui.DialogManager;
import net.sf.refactorit.ui.dialog.RitDialog;
import net.sf.refactorit.ui.module.AbstractRefactorItAction;
import net.sf.refactorit.ui.module.RefactoringStatusViewer;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.ui.treetable.BinTreeTableNode;

import javax.swing.JOptionPane;

import java.util.ArrayList;
import java.util.List;


public class EncapsulateFieldAction extends AbstractRefactorItAction {
  public static final String KEY = "refactorit.action.EncapsulateFieldAction";
  public static final String NAME = "Encapsulate Field";

  public boolean isMultiTargetsSupported() {
    return true;
  }

  public boolean isReadonly() {
    return false;
  }

  public String getName() {
    return NAME;
  }

  public boolean isAvailableForType(Class type) {
    if ((BinClass.class.isAssignableFrom(type)
        && !BinAnnotation.class.equals(type))
        || BinField.class.isAssignableFrom(type)
        || BinFieldInvocationExpression.class.isAssignableFrom(type)) {
      return true;
    }
    return false;

  }
  public String getKey() {
    return KEY;
  }

  /**
   * Module execution.
   *
   * @param context
   * @param object  Bin object to operate
   * @return  false if nothing changed, true otherwise
   */
  public boolean run(final RefactorItContext context, Object object) {

    RefactoringStatus status;
    EncapsulateFields encapsulateFields = null;
    boolean isAllFieldsEnabled = true;

    //if were seected few classes, take first
    if(object instanceof Object[]) {
      if(((Object[]) object).length > 0) {
        if(((Object[]) object)[0] instanceof BinClass) {
          object = ((Object[]) object)[0];
        }
      }
    }

    if (!(object instanceof Object[])) {
      Object[] temp = new Object[1];
      temp[0] = object;
      object = temp;
    }

    Object[] objectA = (Object[]) object;

    if (objectA[0] instanceof BinAnnotation
        || (objectA[0] instanceof BinMember
        && ((BinMember) objectA[0]).getOwner() != null
        && ((BinMember) objectA[0]).getOwner().getBinType() instanceof BinAnnotation)) {
      RitDialog.showMessageDialog(context,
          "Field encapsulation can not be run on annotations.",
          "Field encapsulation not possible",
          JOptionPane.INFORMATION_MESSAGE);
      return false;
    }

    // if user clicked on class, take all fields in class
    // and mark them as enabled for encapsulation
    if (objectA[0] instanceof BinClass) {
      isAllFieldsEnabled = false;
      objectA = ((BinClass) objectA[0]).getDeclaredFields();
    }

    encapsulateFields = new EncapsulateFields(context, objectA, isAllFieldsEnabled);

    status = encapsulateFields.checkPreconditions();

    if (status.isCancel()) {
      if (status.hasSomethingToShow()) {
        RitDialog.showMessageDialog(context,
            status.getAllMessages(), "Field encapsulation not possible",
            status.getJOptionMessageType());
      }
      return false;
    }

    if (status.isErrorOrFatal()) {
      RitDialog.showMessageDialog(context,
          status.getAllMessages(), "Field encapsulation not possible",
          status.getJOptionMessageType());
      return false;
    } else if (status.isInfoOrWarning()) {
      if (RitDialog.showConfirmDialog(context,
          status.getAllMessages(), "Continue?",
          JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
        return false;
      }
    }

    if (!showEncapsulateDialog(context, encapsulateFields)) {
      return false;
    }

    if (!showUsageDialog(context, encapsulateFields)) {
      return false;
    }

    status = encapsulateFields.apply();//TransformationManager.performTransformationFor(encapsulateFields);

    if (!status.isOk()) {
      if (status.hasSomethingToShow()) {
        RitDialog.showMessageDialog(context,
            status.getAllMessages(), "Encapsulate Field problems",
            status.getJOptionMessageType());
      }
      return !status.isErrorOrFatal();
    }

    return true;
  }

  private boolean showUsageDialog(
      RefactorItContext context, EncapsulateFields encapuslateFields
  ) {
    List usages = new ArrayList();
    List fields = new ArrayList();

    EncapsulateField[] encapsulator = encapuslateFields.getEncapsulateFields();
    for (int x = 0; x < encapsulator.length; x++) {
      usages.add(encapsulator[x].getEncapsulateUsages());
      fields.add(encapsulator[x].getField());
    }

    EncapsulateTreeTableModel model
        = new EncapsulateTreeTableModel(fields, usages);

    if (((BinTreeTableNode) model.getRoot()).getChildCount() > 0)
        model = (EncapsulateTreeTableModel) DialogManager.getInstance()
            .showConfirmations("Encapsulate Field", context, model,
                "Select places to replace references to field ",
                "refact.encapsulate_field");

    if (model == null) {
      // User cancelled rename process
      return false;
    }

    for (int x = 0; x < encapsulator.length; x++) {
      if (fields.get(x) != null)
        encapsulator[x].setUsages(model.getUsages(x));
    }

    return true;
  }

  private boolean showEncapsulateDialog(
      RefactorItContext context, EncapsulateFields encf
  ) {
    EncapsulateDialog dialog = new EncapsulateDialog(context, encf);

    RefactoringStatus status;

    do {
      dialog.show();

      if (!dialog.isOkPressed()) {
        return false;
      }

      encf.setEncapsulateFields(forwardUserInput(dialog,
          encf.getEncapsulateFields()));

      status = new RefactoringStatus();

      status = encf.checkUserInput();

      if (status.isOk() || status.isCancel()) {
        break;
      } else {
        if (status.isQuestion()) {
          int res = RitDialog.showConfirmDialog(context,
              status.getAllMessages(), "Conflicts found",
              JOptionPane.OK_CANCEL_OPTION,
              status.getJOptionMessageType());
          if (res == JOptionPane.CANCEL_OPTION) {
            status.addEntry("", RefactoringStatus.CANCEL);
          } else {
            break;
          }
        } else {
          RefactoringStatusViewer statusViewer = new RefactoringStatusViewer(
              context,
              "Some conflicts arose during encapsulation.",
              "refact.encapsulate_field");
          statusViewer.display(status);
          if (!statusViewer.isOkPressed()) {
            status.addEntry("", RefactoringStatus.CANCEL);
          }
        }
        if (status.isInfoOrWarning() || status.isCancel()) {
          break;
        }
      }
    } while (true);

    dialog.dispose();

    if (status.isCancel()) {
      return false;
    }
    return true;
  }

  private EncapsulateField[] forwardUserInput(final EncapsulateDialog dialog,
      EncapsulateField[] encapsulatorA) {

    for (int x = 0; x < encapsulatorA.length; x++) {

      encapsulatorA[x].setGetterName(dialog.getGetterName(x));
      encapsulatorA[x].setSetterName(dialog.getSetterName(x));

      encapsulatorA[x].setFieldVisibility(translateModifier(
          dialog.getFieldAccessor()));

      encapsulatorA[x].setGetterVisibility(dialog.getGetterAccessor());
      encapsulatorA[x].setSetterVisibility(dialog.getSetterAccessor());

      encapsulatorA[x].setEncapsulateRead(dialog.isGetterEnabled(x));
      encapsulatorA[x].setEncapsulateWrite(dialog.isSetterEnabled(x));

      // if there is no choseen add getter or setter for this field,
      // encapsulator removed
      if (!dialog.isGetterEnabled(x) && !dialog.isSetterEnabled(x)) {
        encapsulatorA[x] = null;
      }

    }

    return this.removeEmptyEncapsulates(encapsulatorA);
  }

  private int translateModifier(int modifier) {
    switch (modifier) {
      case EncapsulateDialog.FIELD_AS_IS:
        return -1;
      case EncapsulateDialog.FIELD_PRIVATE:
        return BinModifier.PRIVATE;
      case EncapsulateDialog.FIELD_PACKAGE:
        return BinModifier.PACKAGE_PRIVATE;
      case EncapsulateDialog.FIELD_PROTECTED:
        return BinModifier.PROTECTED;
      case EncapsulateDialog.FIELD_PUBLIC:
        return BinModifier.PUBLIC;
      default:
        return -1; //shouldn't ever happen
    }
  }

  public char getMnemonic() {
    return 'L';
  }

  public boolean isInList(BinField field, EncapsulateField[] enc) {
    for (int x = 0; x < enc.length; x++) {

      if (field.getName().equals(enc[x].getField().getName()))
        return true;
    }
    return false;
  }

  public EncapsulateField[] removeEmptyEncapsulates(EncapsulateField[] enc) {
    List ar = new ArrayList();

    for (int z = 0; z < enc.length; z++) {

      if (enc[z] != null) {
        ar.add(enc[z]);
      }
    }

    return (EncapsulateField[]) ar.toArray(new EncapsulateField[ar.size()]);

  }

}
