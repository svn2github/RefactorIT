/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.encapsulatefield;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.refactorings.AbstractRefactoring;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.module.RefactorItContext;

import java.util.ArrayList;
import java.util.List;


public class EncapsulateFields extends AbstractRefactoring {
  public static String key = "refactoring.encapsulatefields";

  private EncapsulateField[] encapsulateFields = null;
  private boolean isEnabled = true;

  public EncapsulateFields(RefactorItContext context, Object[] objectA,
      boolean isAllEnabled) {
    super("Encapsulate Field", context);

    this.isEnabled = isAllEnabled;

    Object object = null;
    BinField target = null;
    EncapsulateField encapsulator = null;
    List encapsulatorArray = new ArrayList();

    // add selected fields
    for (int x = 0; x < objectA.length; x++) {
      object = objectA[x];

      if (object instanceof BinField) {
        target = (BinField) object;
      } else if (object instanceof BinFieldInvocationExpression) {
        target = ((BinFieldInvocationExpression) object).getField();
      } else {
        target = null;
      }

      if (target != null) {
        encapsulator = new EncapsulateField(context, target);

        if (isEnabled) {
          encapsulator.setEnabled();
        }
        encapsulatorArray.add(encapsulator);
      }
    }

    // add all lasts fields
    if (encapsulatorArray.size() > 0) {
      BinCIType bct = ((EncapsulateField) encapsulatorArray.get(0)).getField()
          .getOwner().getBinCIType();

      if (bct.isClass()) {
        BinClass binCl = (BinClass) bct;
        BinField[] fieldArr = binCl.getDeclaredFields();

        EncapsulateField[] encField
            = (EncapsulateField[]) encapsulatorArray.toArray(
            new EncapsulateField[encapsulatorArray.size()]);

        for (int x = 0; x < fieldArr.length; x++) {
          if (!this.isInList(fieldArr[x], encField)) {
            encapsulator = new EncapsulateField(context, fieldArr[x]);
            encapsulatorArray.add(encapsulator);
          }
        }
      }
    }
    encapsulateFields = (EncapsulateField[]) encapsulatorArray.toArray(
        new EncapsulateField[encapsulatorArray.size()]);
  }

  public RefactoringStatus checkUserInput() {

    RefactoringStatus status = new RefactoringStatus();

    for (int x = 0; x < encapsulateFields.length; x++) {
      status = encapsulateFields[x].checkUserInput(status);

      // remove encapsulates, that not enabled. (in checkUserImput it
      // can be disabled if there are errors)
      if (!encapsulateFields[x].encR && !encapsulateFields[x].encW) {
        encapsulateFields[x] = null;
      }
    }

    encapsulateFields = this.removeEmptyEncapsulates(encapsulateFields);

    if (!(encapsulateFields.length > 0)) {
      status.addEntry("There is no any field to encapsulate", status.CANCEL);
    }

    return status;
  }

  public TransformationList performChange() {

    TransformationList transList = new TransformationList();

    for (int x = 0; x < encapsulateFields.length; x++) {

      // FIXME: check why there apear "null"
      if (encapsulateFields[x] != null) {
        transList.merge(encapsulateFields[x].performChange());
      }
    }

    return transList;
  }

  public RefactoringStatus checkPreconditions() {

    RefactoringStatus status = new RefactoringStatus();

    for (int x = 0; x < encapsulateFields.length; x++) {
      status.addEntry(encapsulateFields[x].checkPreconditions());
    }

    if (!(encapsulateFields.length > 0)) {
      status.addEntry("There is no any field to encapsulate", status.ERROR);
    }

    return status;
  }

  public void setIsEnabled(final boolean isEnabled) {
    this.isEnabled = isEnabled;
  }

  public boolean isInList(BinField field, EncapsulateField[] enc) {
    for (int x = 0; x < enc.length; x++) {

      if (field.getName().equals(enc[x].getField().getName()))
        return true;
    }
    return false;
  }

  public EncapsulateField[] getEncapsulateFields() {
    return this.encapsulateFields;
  }

  public void setEncapsulateFields(final EncapsulateField[] encapsulateFields) {
    this.encapsulateFields = encapsulateFields;
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

  public String getDescription() {
    StringBuffer buf = new StringBuffer();

    buf.append("Create getters/setters for: ");

    for (int x = 0; x < encapsulateFields.length; x++) {
      buf.append(/*encapsulateFields[x].getField().getTypeRef().getQualifiedName()
          + " " +*/ encapsulateFields[x].getField().getName());
      if (x < encapsulateFields.length - 1) {
        buf.append(", ");
      }
    }
    return new String(buf);
  }

  public String getKey() {
    return key;
  }

}
