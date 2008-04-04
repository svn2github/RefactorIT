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
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.statements.BinLabeledStatement;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.query.usage.LabelNameIndexer;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.refactorings.NameUtil;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.transformations.RenameTransformation;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.module.RefactorItContext;

import java.util.Iterator;
import java.util.List;


/**
 * @author Oleg Tsernetsov
 * 
 */
public class RenameLabel extends RenameRefactoring {
  public static String key = "refactoring.rename.label";
  private ManagingIndexer supervisor;

  public RenameLabel(RefactorItContext context, BinLabeledStatement statement) {
    super("Rename Label", context, statement);
  }
  
  public RefactoringStatus checkUserInput() {
    RefactoringStatus status = new RefactoringStatus();
    status.merge(super.checkUserInput());
    
    BinLabeledStatement statement = (BinLabeledStatement)getItem();
    String newName = getNewName();
    
    if (!NameUtil.isValidIdentifier(newName)) {
      status.merge(new RefactoringStatus(
          "Not a valid Java 2 method identifier",
          RefactoringStatus.ERROR));
    }
    
    if (!isSuitableLabelName(statement, newName)) {
      status.merge(new RefactoringStatus(
          "Label with name '" + newName + "' already exists",
          RefactoringStatus.ERROR));
    }
    return status;
  }
  
  private boolean isSuitableLabelName(BinLabeledStatement statement, String newName) {
    BinMember owner = statement.getParentMember();
    DuplicateFinder indexer = new DuplicateFinder(statement, newName);
    owner.accept(indexer);
    return indexer.isNameOk();
  }
  
  public static void meth1() {
    loop1:
    for(int i=0; i < 10; i++) {
      loop2:
      for(int j = 0; j<10; j++) {
        if(j == i*2) {
          break loop1;
        } else {
          continue loop2;
        }
      }
    }
  
  
  }
  
  public RefactoringStatus checkPreconditions() {
    RefactoringStatus result = super.checkPreconditions();
    return result;
  }
  
  public TransformationList performChange() {
    TransformationList transList = super.performChange();
    if(!transList.getStatus().isOk()) {
      return transList;
    }
    
    BinLabeledStatement target = (BinLabeledStatement) getItem();
    List invocations = getSupervisor().getInvocations();
    
    MultiValueMap cuUsages = ManagingIndexer.getInvocationsMap(invocations);
    for(Iterator it = cuUsages.keySet().iterator(); it.hasNext();) {
      CompilationUnit cu = (CompilationUnit)it.next();
      transList.add(
          new RenameTransformation(cu, cuUsages.get(cu), getNewName()));
    }
    return transList;
  }
  
  protected ManagingIndexer getSupervisor() {
    if(supervisor == null) {
      supervisor = new ManagingIndexer();
      new LabelNameIndexer(supervisor, (BinLabeledStatement)getItem());
      BinCIType owner = ((BinLabeledStatement)getItem()).getOwner().getBinCIType();
      supervisor.visit(owner);
    }
    return this.supervisor;
  }

  protected void invalidateCache() {
    this.supervisor = null;
  }
  
  private class DuplicateFinder extends BinItemVisitor {
    private BinLabeledStatement target;
    private boolean isNameOk = true;
    private String newName;
    
    public DuplicateFinder(BinLabeledStatement target, String newName) {
      this.target = target;
      this.newName = newName;
    }
    
    public void visit(BinLabeledStatement st) {
      if(st != target && newName.equals(st.getLabelIdentifierName())) {
        isNameOk = false;
      } else {
        super.visit(st);
      }
    }
    public boolean isNameOk() {
      return isNameOk;
    }
  }
}
