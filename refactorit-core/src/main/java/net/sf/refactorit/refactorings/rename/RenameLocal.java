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
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.LocationAware;
import net.sf.refactorit.common.util.CollectionUtil;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.query.usage.LocalVariableNameIndexer;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.refactorings.LocalVariableDuplicatesFinder;
import net.sf.refactorit.refactorings.NameUtil;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.transformations.RenameTransformation;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.module.RefactorItContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Renames local variable or parameter.
 */
public class RenameLocal extends RenameMember {
  public static String key = "refactoring.rename.local";

  private ManagingIndexer supervisor;

  private boolean skipDeclarations;

  private boolean showConfirmationDialog = true;

  public RenameLocal(RefactorItContext context, BinLocalVariable variable) {
    super("RenameLocal", context, variable);
  }

  public RefactoringStatus checkUserInput() {
    RefactoringStatus status = super.checkUserInput();

    BinLocalVariable variable = (BinLocalVariable) getItem();

    // Require valid name
    if (!NameUtil.isValidIdentifier(getNewName())) {
      status.merge(
          new RefactoringStatus(
          "Not a valid Java 2 parameter or variable identifier",
          RefactoringStatus.ERROR));
    }

    // Check if specified name is already used by another local variable in an
    // accessible scope
    final LocalVariableDuplicatesFinder finder
        = new LocalVariableDuplicatesFinder(variable, getNewName(),
        (LocationAware) variable.getParent());
    variable.getParentMember().accept(finder);
    List duplicates = finder.getDuplicates();
    if (duplicates.size() > 0) {
      status.addEntry("Existing variables or parameters with the same name",
          duplicates,
          RefactoringStatus.ERROR);
    }

    final List conflicts
        = findAccessibleFieldsWithSameName(variable.getOwner());
    if (conflicts.size() > 0) {
      status.addEntry("Existing accessible fields with the same name",
          conflicts,
          RefactoringStatus.WARNING);
    }

    final BinField shadedStaticImport = findStaticImportWithSameName(variable.getCompilationUnit());
    if (shadedStaticImport != null) {
      status.addEntry("Existing field with the same name accessible via static import!",
      		RefactoringStatus.WARNING,
					shadedStaticImport);
    }

    return status;
  }

  /**
   *
   * @param compilationUnit
   * @return a field accessible via static import with the same name as the new name,
   * <code>null</code> if no such static import
   */
  private BinField findStaticImportWithSameName(CompilationUnit compilationUnit) {
  	return compilationUnit.getStaticImportField(getNewName(), getItem().getParentType());
  }

  private List findAccessibleFieldsWithSameName(final BinTypeRef owner) {
    List conflicts = new ArrayList();

    BinCIType type = owner.getBinCIType();
    List fields = type.getAccessibleFields(type);
    for (int i = 0, max = fields.size(); i < max; i++) {
      final BinField field = ((BinField) fields.get(i));
      if (getNewName().equals(field.getName())) {
        CollectionUtil.addNew(conflicts, field);
      }
    }

    return conflicts;
  }

  /** For nesting inside other refactorings -- does not invoke editor.performEdit() either */
  public TransformationList performChange() {
    TransformationList transList = super.performChange();

    if (!transList.getStatus().isOk()) {
      return transList;
    }

    final BinLocalVariable variable = (BinLocalVariable) getItem();

    final List list = getSupervisor().getInvocations();
    ConfirmationTreeTableModel model = new ConfirmationTreeTableModel(variable,
        list);


//    changed to generic preview

//    if (showConfirmationDialog) {
//      model = (ConfirmationTreeTableModel) DialogManager.getInstance()
//          .showConfirmations(getContext(), model, "refact.rename.local");
//      if (model == null) {
//        transList.getStatus().merge(
//            new RefactoringStatus("", RefactoringStatus.CANCEL));
//        return transList;
//      }
//    }

    List selectedUsages = model.getCheckedUsages();
    final MultiValueMap usages = ManagingIndexer.getInvocationsMap(
        selectedUsages);

    // Alter sources
    for (final Iterator i = usages.entrySet().iterator(); i.hasNext(); ) {
      final Map.Entry entry = (Map.Entry) i.next();

      transList.add(
          new RenameTransformation((CompilationUnit) entry.getKey(),
          (List) entry.getValue(),
          getNewName()));
    }

    return transList;
  }

  protected ManagingIndexer getSupervisor() {
    if (supervisor == null) {
      supervisor = new ManagingIndexer(true);

      // FIXME forward isRenameInJavadocs into indexer
      LocalVariableNameIndexer indexer =
          new LocalVariableNameIndexer(supervisor, (BinLocalVariable) getItem());
      indexer.setSkipDeclarations(skipDeclarations);

      BinLocalVariable var = ((BinLocalVariable) getItem());
      supervisor.visit(var.getParentMember().getCompilationUnit());
    }

    return supervisor;
  }

  public void invalidateCache() {
    this.supervisor = null;
  }

  public void setSkipDeclarations(boolean b) {
    skipDeclarations = b;
  }

  public void setShowConfirmationDialog(boolean b) {
    showConfirmationDialog = b;
  }

  public String getDescription() {
    return super.getDescription();
  }

  public String getKey() {
    return key;
  }
}
