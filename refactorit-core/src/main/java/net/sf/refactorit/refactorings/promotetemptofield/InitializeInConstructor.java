/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.promotetemptofield;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.statements.BinVariableDeclaration;
import net.sf.refactorit.query.ItemByNameFinder;
import net.sf.refactorit.refactorings.ImportManager;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.delegate.MethodBodySkeleton;
import net.sf.refactorit.refactorings.delegate.MethodCreator;
import net.sf.refactorit.refactorings.delegate.MethodSkeleton;
import net.sf.refactorit.source.format.BinAssigmentExpressionFormatter;
import net.sf.refactorit.source.format.BinItemFormatter;
import net.sf.refactorit.source.format.FormatSettings;
import net.sf.refactorit.transformations.DeleteTransformation;
import net.sf.refactorit.transformations.TransformationList;

import java.util.List;


public class InitializeInConstructor extends FieldInitialization {

  private void createInitializationInConstructors(BinTypeRef target,
      final BinLocalVariable var, String newName,
      final TransformationList transList, final ImportManager importManager) {
    BinConstructor[] constructors = ((BinClass) target.getBinCIType())
        .getDeclaredConstructors();

    if (constructors.length > 0) {
      createInitializationIn(constructors, var, newName, transList);
    } else {
      createDefaultConstructor(target, var, newName, transList, importManager);
    }
  }

  private void createInitializationIn(BinConstructor[] constructors,
      BinLocalVariable var, String newName, final TransformationList transList) {
    for (int i = 0; i < constructors.length; i++) {
      if (!constructors[i].hasExplicitConstructorInvocationWithThisKeword()) {
        createInitializationIn(constructors[i], var, newName, transList);
      }
    }
  }

  private void createDefaultConstructor(BinTypeRef target,
      BinLocalVariable var, String newName, final TransformationList transList,
      final ImportManager importManager) {
    createDefaultConstructor(target,newName
        + new BinAssigmentExpressionFormatter().print()
        + var.getExprNodeText() + ";", transList, importManager);
  }

  private void createInitializationIn(BinConstructor constructor,
      BinLocalVariable var, String newName, final TransformationList transList) {
    String varName = newName;
    if (containsAnotherVarWithName(constructor, newName, var)) {
      varName = "this." + varName;
    }

    String statement = FormatSettings.getIndentStringForChildrenOf(constructor)
        + varName
        + new BinAssigmentExpressionFormatter().print() + var.getExprNodeText() + ";"
        + FormatSettings.LINEBREAK;
    StatementInserter.append(statement, constructor, transList);
  }

  private boolean containsAnotherVarWithName(BinItemVisitable item,
      String name, BinLocalVariable var) {
    List varsWithSameName = ItemByNameFinder.findVariables(item, name);
    varsWithSameName.remove(var);
    return varsWithSameName.size() > 0;
  }

  private void createDefaultConstructor(BinTypeRef target, String statement,
      final TransformationList transList, final ImportManager importManager) {
    BinConstructor toAdd = new BinConstructor(BinParameter.NO_PARAMS,
        BinModifier.PACKAGE_PRIVATE, BinMethod.Throws.NO_THROWS);
    MethodSkeleton method = new MethodSkeleton(target.getBinCIType(), toAdd,
        new MethodBodySkeleton(statement));

    new MethodCreator(method, transList, importManager).createEdit();
  }

  protected void updateAssignmentLocation(BinLocalVariable var, String newName,
      final TransformationList transList, final ImportManager importManager) {
    if (var.getExpression() != null) {
      createInitializationInConstructors(var.getOwner(), var, newName,
          transList, importManager);
    }

    transList.add(new DeleteTransformation(var, (BinVariableDeclaration) var
        .getWhereDeclared()));
  }

  protected boolean hasValueAssignedInDeclaration() {
    return false;
  }

  public RefactoringStatus checkUserInput(BinLocalVariable var, String newName) {
    RefactoringStatus result = super.checkUserInput(var, newName);

    DependencyAnalyzer d = new DependencyAnalyzer();
    d.checkUsedItemsAvailableOnClassLevel(var);

    // check if contructor in GuardedBlocks
    {
      CompilationUnit cu = var.getCompilationUnit();
      BinConstructor[] constructors = ((BinClass) var.getOwner().getBinCIType())
          .getDeclaredConstructors();

      int line;
      int column;

      for(int i=0; i < constructors.length; i++) {
        if (!constructors[i].hasExplicitConstructorInvocationWithThisKeword()) {

          BinItemFormatter formatter = constructors[i].getFormatter();

          line = formatter.findNewMemberPosition().getLine();
          column = formatter.findNewMemberPosition().getColumn();

          if (cu.isWithinGuardedBlocks(line, column)) {
            result.addEntry("Can't do, beacause can not initialize field" +
                " in constructor, which is in Guarded Block",
                RefactoringStatus.ERROR);
          }
        }
      }
    }

    return result.merge(d.getStatus());
  }

  public boolean supports(BinLocalVariable var) {
    return canBeAssignedInConstructor(var)
        && canHaveConstructors(var.getOwner().getBinCIType());
  }

  private boolean canBeAssignedInConstructor(BinLocalVariable var) {
    return!new AllowedModifiers().mustBeStatic(var);
  }

  private boolean canHaveConstructors(BinCIType type) {
    return!type.isAnonymous();
  }

  public boolean initializesInMethod() {
    return false;
  }

  public String getDisplayName() {
    return "Constructor(s)";
  }

  public char getMnemonic() {
    return 'Q';
  }

  public boolean removesVariableNameFromOriginalDeclaration() {
    return true;
  }
}
