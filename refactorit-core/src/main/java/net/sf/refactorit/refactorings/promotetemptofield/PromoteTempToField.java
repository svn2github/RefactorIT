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
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinModifier;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.statements.BinForStatement;
import net.sf.refactorit.classmodel.statements.BinLocalVariableDeclaration;
import net.sf.refactorit.query.usage.Finder;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.filters.BinVariableSearchFilter;
import net.sf.refactorit.refactorings.AbstractRefactoring;
import net.sf.refactorit.refactorings.ImportManager;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.refactorings.rename.RenameLocal;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.module.RefactorItContext;
import net.sf.refactorit.utils.FileUtil;

import java.util.List;


/**
 *
 * @author  RISTO A
 */
public class PromoteTempToField extends AbstractRefactoring {
  public static String key = "refactoring.promotetemptofield";

  public static final FieldInitialization INITIALIZE_IN_METHOD = new
      InitializeInMethod();
  public static final FieldInitialization INITIALIZE_IN_CONSTRUCTOR = new
      InitializeInConstructor();
  public static final FieldInitialization INITIALIZE_IN_FIELD = new
      InitializeInField();

  public static final FieldInitialization DEFAULT_INITIALIZATION =
      INITIALIZE_IN_FIELD;

  public static final FieldInitialization[] ALL_INIT_LOCATIONS = new
      FieldInitialization[] {
      INITIALIZE_IN_FIELD, INITIALIZE_IN_METHOD, INITIALIZE_IN_CONSTRUCTOR};

  private BinLocalVariable var;
  private int modifiers;
  private String newName;

  private FieldInitialization fieldInitialization;

  public PromoteTempToField(RefactorItContext context, BinLocalVariable var,
      String newName,
      int modifiers, FieldInitialization fieldInitialization) {
    super("Promote Temp To Field", context);

    setVariable(var);
    setNewName(newName);
    setModifiers(modifiers);
    setFieldInitialization(fieldInitialization);
  }

  public void setVariable(BinLocalVariable var) {
    this.var = var;
  }

  public void setNewName(String newName) {
    this.newName = newName;
  }

  public void setModifiers(int modifiers) {
    this.modifiers = modifiers;
  }

  public void setFieldInitialization(FieldInitialization fieldInitialization) {
    this.fieldInitialization = fieldInitialization;
  }

  public BinLocalVariable getVariable() {
    return var;
  }

  public String getNewName() {
    return newName;
  }

  public int getModifiers() {
    return modifiers;
  }

  public FieldInitialization getInitializeLocation() {
    return fieldInitialization;
  }

  public RefactoringStatus checkPreconditions() {
    computeStaticFlag();
    CompilationUnit compilationUnit = var.getCompilationUnit();
    if(FileUtil.isJspFile(compilationUnit.getSource().getAbsolutePath())) {
      return new RefactoringStatus("Cannot convert variable declared in JSP file",
          RefactoringStatus.ERROR);
    }
    
    if (var.getClass() != BinLocalVariable.class) {
      return new RefactoringStatus("Cannot convert method and catch parameters",
          RefactoringStatus.ERROR);
    }

    if (var.getTypeRef().getBinType() instanceof BinCIType) {
      BinCIType varType = var.getTypeRef().getBinCIType();
      if (!varType.isAccessible(var.getOwner().getBinCIType())) {
        return new RefactoringStatus(
            "Variables of local types are not supported",
            RefactoringStatus.ERROR);
      }
    }

    // if declaration placed in GuardBlock - NB
    if (var.getCompilationUnit().isWithinGuardedBlocks(var.getStartLine(),
        var.getStartColumn())) {
      return new RefactoringStatus(
          "Cannot convert local variable declared in Guarded Block",
          RefactoringStatus.ERROR);

    }

    if (var.getParent() instanceof BinLocalVariableDeclaration) {
      BinItemVisitable declaredIn = var.getParent().getParent();
      if ((declaredIn instanceof BinForStatement)
          && (((BinForStatement) declaredIn).isForEachStatement())) {
        return new RefactoringStatus(
            "Cannot convert local variable declared in for-each statement",
            RefactoringStatus.ERROR);
      }
    }

    return new RefactoringStatus();
  }

  private void computeStaticFlag() {
    if (new AllowedModifiers().mustBeStatic(var)) {
      modifiers = BinModifier.setFlags(modifiers, BinModifier.STATIC);
    } else if (new AllowedModifiers().mustBeNonStatic(var)) {
      modifiers = BinModifier.clearFlags(modifiers, BinModifier.STATIC);
    }
  }

  public RefactoringStatus checkUserInput() {

    // can not process this refactoring
    // if variable used somewhere in GuardedBlocks and user rename it

    if (!var.getName().equals(newName)) {
      List invocations = Finder.getInvocations(var,
          new BinVariableSearchFilter(true, true, false, false, false));

      for (int i = 0; i < invocations.size(); i++) {
        InvocationData invData = (InvocationData) invocations.get(i);

        if (var.getCompilationUnit().isWithinGuardedBlocks(invData.
            getLineNumber(), invData.getWhereAst().getColumn())) {
          return new RefactoringStatus(
              "Can't do, beacause can not rename this variable in Guarded Block",
              RefactoringStatus.ERROR);
        }
      }
    }

    return fieldInitialization.checkUserInput(var, newName);
  }

  public TransformationList performChange() {
    TransformationList transList = new TransformationList();
    ImportManager importManager = new ImportManager();

    //FIXME: need to remove, but be shure that checkPrecondition() &
    // checkUserInput() are runned
    // (It seems that methods are called twice,
    // one time above and by AbstractRefactorinyAction)
    // NB! under tests, are runned only above
    transList.getStatus().merge(checkPreconditions()).merge(checkUserInput());
    if (transList.getStatus().isErrorOrFatal()) {
      return transList;
    }

    fieldInitialization.convertTempToField(newName, var, modifiers, transList,
        importManager);
    importManager.createEditors(transList);

    if (!var.getName().equals(newName)) {
      RenameLocal renamer = new RenameLocal(getContext(), var);
      renamer.setNewName(newName);
      renamer.setSkipDeclarations(fieldInitialization.
          removesVariableNameFromOriginalDeclaration());
      renamer.setShowConfirmationDialog(false);
      transList.merge(renamer.performChange());
    }
    return transList;
  }

  /** @return   never null */
  public static FieldInitialization getInitializationLocationForDisplayName(
      String name
      ) {
    for (int i = 0; i < ALL_INIT_LOCATIONS.length; i++) {
      if (ALL_INIT_LOCATIONS[i].getDisplayName().equals(name)) {
        return ALL_INIT_LOCATIONS[i];
      }
    }

    return DEFAULT_INITIALIZATION;
  }

  public String getDescription() {

    return "Promote  " + var.getTypeRef().getName() + " " + var.getName()
        + "  to field"; // super.getDescription();
  }

  public String getKey() {
    return key;
  }

}
