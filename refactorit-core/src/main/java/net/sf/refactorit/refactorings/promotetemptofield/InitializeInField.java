/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.promotetemptofield;

import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.statements.BinVariableDeclaration;
import net.sf.refactorit.refactorings.ImportManager;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.transformations.DeleteTransformation;
import net.sf.refactorit.transformations.TransformationList;


public class InitializeInField extends FieldInitialization {
  protected void updateAssignmentLocation(BinLocalVariable var, String newName,
      final TransformationList transList, final ImportManager importManager) {
    transList.add(new DeleteTransformation(var, (BinVariableDeclaration) var
        .getWhereDeclared()));
  }

  protected boolean hasValueAssignedInDeclaration() {
    return true;
  }

  public RefactoringStatus checkUserInput(BinLocalVariable var, String newName) {
    RefactoringStatus result = super.checkUserInput(var, newName);

    DependencyAnalyzer d = new DependencyAnalyzer();
    d.checkUsedItemsAvailableOnClassLevel(var);
    return result.merge(d.getStatus());
  }

  public boolean supports(BinLocalVariable var) {
    return true;
  }

  public boolean initializesInMethod() {
    return false;
  }

  public String getDisplayName() {
    return "Field";
  }

  public char getMnemonic() {
    return 'F';
  }

  public boolean removesVariableNameFromOriginalDeclaration() {
    return true;
  }
}
