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
import net.sf.refactorit.source.edit.PunctuationEraser;
import net.sf.refactorit.transformations.DeleteTransformation;
import net.sf.refactorit.transformations.TransformationList;


public class InitializeInMethod extends FieldInitialization {
  private boolean removesVariableNameFromOriginalDeclaration = false;
  protected void updateAssignmentLocation(BinLocalVariable var, String newName,
      final TransformationList transList, final ImportManager importManager) {
    if (var.getExpression() != null) {
      convertDeclarationToAssignment(var, newName, transList);
      removesVariableNameFromOriginalDeclaration = false;
    } else {
      transList.add(new DeleteTransformation(var, (BinVariableDeclaration) var
          .getWhereDeclared()));
      removesVariableNameFromOriginalDeclaration = true;
    }
  }

  private void convertDeclarationToAssignment(BinLocalVariable var,
      String newName, final TransformationList transList) {
    BinVariableDeclaration declaration = (BinVariableDeclaration) var
        .getWhereDeclared();

    if (declaration.getVariables().length > 1) {
      breakDeclarationApart(var, transList);
    }

    if (declaration.isFirst(var)) {
      transList.add(new DeleteTransformation(var,
          DeleteTransformation.DELETE_TYPE_NODE));
    } else {
      transList.add(new DeleteTransformation(var,
          DeleteTransformation.DELETE_ARRAY_DECLARATION_BRACKETS_AFTER_NAME));
    }
  }

  private void breakDeclarationApart(BinLocalVariable var,
      final TransformationList transList) {
    BinVariableDeclaration declaration = (BinVariableDeclaration) var.
        getWhereDeclared();

    if (!declaration.isLast(var)) {
      breakDeclarationApartAfter(var, transList);
    }

    if (!declaration.isFirst(var)) {
      breakDeclarationApartBefore(var, transList);
    }
  }

  private void breakDeclarationApartBefore(final BinLocalVariable var,
      final TransformationList transList) {
    transList.add(new PunctuationEraser(var.getCompilationUnit(), var
        .getNameStart(), true));
    StatementInserter.breakStatementApart(var.getCompilationUnit(), var
        .getNameStart(), "", transList);
  }

  private void breakDeclarationApartAfter(final BinLocalVariable var,
      final TransformationList transList) {
    transList.add(new PunctuationEraser(var.getCompilationUnit(), var
        .getExpressionEnd(), false));
    StatementInserter.breakStatementApart(var.getCompilationUnit(), var
        .getExpressionEnd(), var.getTypeAndModifiersNodeText() + " ", transList);
  }

  protected boolean hasValueAssignedInDeclaration() {
    return false;
  }

  public boolean supports(BinLocalVariable var) {
    return true;
  }

  public boolean initializesInMethod() {
    return true;
  }

  public String getDisplayName() {
    return "Method";
  }

  public char getMnemonic() {
    return 'M';
  }

  public boolean removesVariableNameFromOriginalDeclaration() {
    // 'true' only when there was LocVarDeclaration without assigment expression,
    // so when it will be removed, no need, to rename daclaration node
    return removesVariableNameFromOriginalDeclaration;
  }
}
