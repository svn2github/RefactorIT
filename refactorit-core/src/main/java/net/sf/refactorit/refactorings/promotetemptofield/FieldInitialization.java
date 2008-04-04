/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.promotetemptofield;

import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.refactorings.ImportManager;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.format.BinFieldFormatter;
import net.sf.refactorit.transformations.TransformationList;


public abstract class FieldInitialization {
  public void convertTempToField(
      String newName, BinLocalVariable var, int modifiers,
      final TransformationList transList, ImportManager importManager
      ) {
    createField(var, newName, modifiers, transList, importManager);

    updateAssignmentLocation(var, newName, transList, importManager);
  }

  private void createField(
      BinLocalVariable var, String newName, int modifiers,
      final TransformationList transList, ImportManager importManager
      ) {
    BinField f = new BinField(newName, var.getTypeRef(), modifiers,
        !hasValueAssignedInDeclaration());
    f.setOwner(var.getOwner());
    if (hasValueAssignedInDeclaration()) {
      f.setExpression(var.getExpression());
    }
    BinFieldFormatter formatter = (BinFieldFormatter)f.getFormatter();
    formatter.setFqnTypes(importManager.manageImports(f));
    String fieldDeclaration = formatter.formHeader();
    fieldDeclaration+=formatter.formBody();
    fieldDeclaration+=formatter.formFooter();
    StatementInserter.append(fieldDeclaration, var.getOwner().getBinCIType(),
        transList);
  }

  public abstract boolean supports(BinLocalVariable var);

  public RefactoringStatus checkUserInput(BinLocalVariable var, String newName) {
    if (!var.getParentType().canCreateField(newName)) {
      return new RefactoringStatus("Variable already exists with this name: "
          + newName, RefactoringStatus.ERROR);
    }

    return new RefactoringStatus();
  }

  protected abstract boolean hasValueAssignedInDeclaration();

  protected abstract void updateAssignmentLocation(BinLocalVariable var,
      String newName, final TransformationList transList,
      final ImportManager manager);

  public abstract boolean initializesInMethod();

  public abstract boolean removesVariableNameFromOriginalDeclaration();

  public abstract String getDisplayName();

  public String toString() {
    return getClass().getName().substring(getClass().getName().lastIndexOf(".")
        + 1);
  }

  public abstract char getMnemonic();
}
