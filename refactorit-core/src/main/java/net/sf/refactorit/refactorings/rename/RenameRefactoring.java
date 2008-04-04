/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.rename;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPackage;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.statements.BinLabeledStatement;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.common.util.WildcardPattern;
import net.sf.refactorit.options.GlobalOptions;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.refactorings.AbstractRefactoring;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.module.RefactorItContext;



public abstract class RenameRefactoring extends AbstractRefactoring {
  public static String key = "refactoring.rename";

  /** to rename */
  private BinItem item;

  private String newName;

  private boolean renameInJavadocs;

  public RenameRefactoring(String name, RefactorItContext context, BinItem item) {
    super(name, context);
    this.item = item;
    if (Assert.enabled) {
      Assert.must(item != null, "Item shouldn't be null");
    }

    this.renameInJavadocs = "true".equals(
        GlobalOptions.getOption("rename.member.in_javadocs", "true"));
  }

  public RefactoringStatus checkPreconditions() {
    RefactoringStatus status = new RefactoringStatus();
    int parsingErrorsBefore
        = getProject().getProjectLoader().getErrorCollector().amountOfUserFriendlyErrors()
        + getProject().getProjectLoader().getErrorCollector().amountOfUserFriendlyInfos();

    getSupervisor(); // to discover bugs appeared by outside changes and show ErrorTab

    int parsingErrorsAfter
        = getProject().getProjectLoader().getErrorCollector().amountOfUserFriendlyErrors()
        + getProject().getProjectLoader().getErrorCollector().amountOfUserFriendlyInfos();

    if (parsingErrorsBefore < parsingErrorsAfter) {
      status.addEntry(
          "Some problems were discovered during thorough parsing of your sources."
          + "\nIt could be worth checking Errors tab for more details before continuing with Rename.",
          RefactoringStatus.WARNING);
    }

    return status;
  }

  public RefactoringStatus checkUserInput() {
    RefactoringStatus status = new RefactoringStatus();
    final MultiValueMap checkMap
        = getSupervisor().getInvocationsMap();
    if (!AbstractRefactoring.isAllChangeable(checkMap)) {
      boolean willEditForms
          = WildcardPattern.arrayToString(
          getContext().getProject().getOptions().getNonJavaFilesPatterns()).indexOf("*.form")
          != -1;
      if (willEditForms) {
        status.addEntry(
            "This item is used in automatically generated section,\n"
            + "but the corresponding .form file will be edited as well.\n"
            +
            "You should recompile the classes and restart the GUI editor after refactoring.",
            RefactoringStatus.INFO);
      } else {
        // TODO make correctly into RefactoringListStatus to show the tree
        status.addEntry(
            "This item is used in automatically generated section\n"
            + "which will be overwritten by external editor.\n"
            + "Check the RefactorIT/Project Options for non-java file patterns to be edited automatically.\n"
            + "By now you must change .form file manually." + "\n"
            + "Do you want to continue with rename?",
            RefactoringStatus.QUESTION);
      }
    }

    return status;
  }

  public TransformationList performChange() {
    //((Integer)null).intValue(); // NPE for BugreportWizard tests
    TransformationList transList = new TransformationList();
    if (getNewName() == null) {
      transList.getStatus().addEntry("newName is null", RefactoringStatus.ERROR);
    }

    if (getItem() == null) {
      transList.getStatus().addEntry("Item is null", RefactoringStatus.ERROR);
    }

    if ((getItem() instanceof BinLabeledStatement
        && ((BinLabeledStatement)getItem()).getLabelIdentifierName().equals(getNewName())) 
        || (getItem() instanceof BinMember
        && ((BinMember) getItem()).getName().equals(getNewName()))
        || (getItem() instanceof BinPackage
        && ((BinPackage) getItem()).getQualifiedName().equals(getNewName()))) {
      transList.getStatus().addEntry("New name matches old one: " + getNewName(),
          RefactoringStatus.CANCEL);
    }

    return transList;
  }

  public void setNewName(String newName) {
    this.newName = newName;
  }

  public String getNewName() {
    return this.newName;
  }

  public void setRenameInJavadocs(boolean renameInJavadocs) {
    if (this.renameInJavadocs != renameInJavadocs) {
      invalidateCache();
    }
    this.renameInJavadocs = renameInJavadocs;
  }

  public boolean isRenameInJavadocs() {
    return this.renameInJavadocs;
  }

  public BinItem getItem() {
    return item;
  }

  protected abstract ManagingIndexer getSupervisor();

  /** When options have changed we need to refresh collected information */
  protected abstract void invalidateCache();

  public String getDescription() {
    String oldname = null;
    if(item instanceof BinMember) {
      oldname = ((BinMember) item).getName();
    } else if (item instanceof BinPackage) {
      oldname = "package " + ((BinPackage) getItem()).getQualifiedName();
    }
    if (oldname !=null) {
      return "Rename " + oldname + " --> " + getNewName();
    }

    return null;
  }

  public String getKey() {
    return key;
  }

  public static RenameRefactoring getRefactoring(
      final RefactorItContext context, final BinItem item
  ) {
    RenameRefactoring refactoring = null;

    if (item instanceof BinMethod) {
      refactoring = new RenameMethod(context, (BinMethod) item);
    } else if (item instanceof BinField) {
      refactoring = new RenameField(context, (BinField) item);
    } else if (item instanceof BinCIType) {
      refactoring = new RenameType(context, (BinCIType) item);
    } else if (item instanceof BinParameter) {
      refactoring = new RenameMultiLocal(context, (BinParameter) item);
    } else if (item instanceof BinLocalVariable) {
      refactoring = new RenameLocal(context, (BinLocalVariable) item);
    } else if (item instanceof BinPackage) {
      refactoring = new RenamePackage(context, (BinPackage) item);
    } else if (item instanceof BinLabeledStatement) {
      refactoring  = new RenameLabel(context, (BinLabeledStatement)item);
    }

    return refactoring;
  }

}
