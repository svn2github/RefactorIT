/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.inlinevariable;


import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinInitializer;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.SourceConstruct;
import net.sf.refactorit.classmodel.BinMethod.Throws;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.classmodel.statements.BinCITypesDefStatement;
import net.sf.refactorit.classmodel.statements.BinTryStatement;
import net.sf.refactorit.classmodel.statements.BinTryStatement.CatchClause;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.query.usage.Finder;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.filters.BinVariableSearchFilter;
import net.sf.refactorit.refactorings.AbstractRefactoring;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.module.RefactorItContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


/** @author  RISTO A
 * @author Jevgeni Holodkov
 * */

public class InlineVariable extends AbstractRefactoring {
  private ExceptionSearchVisitor exceptionSearch;
  private BinVariable var;
  private List invocations;
  public static String key = "refactoring.inlinevariable";

  public InlineVariable(RefactorItContext context, BinVariable var) {
    super("Inline Temp", context);

    this.var = var;

    this.invocations = Finder.getInvocations(
        getProject(), var,
        new BinVariableSearchFilter(true, true, true, false, false));

    this.exceptionSearch = new ExceptionSearchVisitor();
  }

  public RefactoringStatus checkPreconditions() {
    RefactoringStatus status = new RefactoringStatus();
    HashSet uncatchedExceptions = new HashSet();

    BinExpression expression = this.var.getExpression();
    if (expression != null) {
      expression.accept(this.exceptionSearch);

      if (this.exceptionSearch.hasExceptions() == true) {
        for (int i = 0, max = this.invocations.size(); i < max; i++) {
          this.exceptionSearch.resetExceptionSet();
          final InvocationData invoc = (InvocationData)this.invocations.get(i);
          SourceConstruct source = invoc.getInConstruct();
          BinItemVisitable parent = source.getParent();
          if (!this.exceptionSearch.findException(parent)) {
            uncatchedExceptions.addAll(this.exceptionSearch.getExceptionSet());
          }
        }
        if (!uncatchedExceptions.isEmpty()) {
          status.addEntry(
              "Following exceptions are not handled in some invocation place(s):",
              new ArrayList(uncatchedExceptions), RefactoringStatus.WARNING);
        }
      }
    }
    status.merge(new Preconditions(var, invocations).status());

    return status;
  }

  public RefactoringStatus checkUserInput() {
    return new RefactoringStatus();
  }

  public TransformationList performChange() {
    TransformationList transList = new TransformationList();
    Inliner inliner = new Inliner(transList);
    inliner.inline(var, invocations);

    return transList;
  }

  public String getDescription() {

    return "Inline variable: " + var.getTypeRef().getQualifiedName() + " "
        + var.getName();
  }


  public String getKey() {
    return key;
  }
}


// Search for any type of Non RunTimeException located in class.
// if found - stores them to the SET.
class ExceptionSearchVisitor extends BinItemVisitor {
  private HashSet exceptionSet = new HashSet();
  private HashSet concreteExceptionSet = new HashSet(); // concrete ExceptionSet each invocation working with

  public HashSet getExceptionSet() {
    return this.exceptionSet;
  }

  public boolean isEmptyExceptionSet() {
    return this.concreteExceptionSet.isEmpty();
  }

  public void resetExceptionSet() {
    this.concreteExceptionSet = (HashSet)this.exceptionSet.clone();
  }

  public void clearExceptionSet() {
    this.concreteExceptionSet.clear();
  }

  public boolean isArrayInExceptionSet(Throws[] declaredThrows) {
    Iterator it = concreteExceptionSet.iterator();
    while (it.hasNext()) {
      boolean isExceptionExists = false;
      BinTypeRef fallingException = (BinTypeRef) it.next();
      for (int i = 0; i < declaredThrows.length; i++) {
        BinTypeRef declaredException = declaredThrows[i].getException();
        if (fallingException.isDerivedFrom(declaredException)) {
          isExceptionExists = true;
          break;
        }
      }
      if (!isExceptionExists) {
        return false;
      }
    }
    return true;
  }

  public void excludeArrayFromExceptionSet(Throws[] declaredThrows) {
    for (int i = 0; i < declaredThrows.length; i++) {
      BinTypeRef declaredException = declaredThrows[i].getException();
      this.excludeExeptionFromExceptionSet(declaredException);
    }
  }

  public void excludeArrayFromExceptionSet(CatchClause[] declaredCatches) {
    for (int i = 0; i < declaredCatches.length; i++) {
      BinTypeRef declaredException = declaredCatches[i].
          getParameter().getTypeRef();
      this.excludeExeptionFromExceptionSet(declaredException);
    }
  }

  public boolean areConstructorsInExceptionSet(BinConstructor[]
      constructorArray) {
    Throws[] declaredThrows = null;
    for (int i = 0; i < constructorArray.length; i++) {
      declaredThrows = constructorArray[i].getThrows();
      if (this.isArrayInExceptionSet(declaredThrows)) {
        continue;
      } else {
        return false;
      }
    }
    return true;
  }

  public void visit(BinNewExpression expression) {
    BinConstructor invoked = expression.getConstructor();
    if (invoked != null) {
      Throws[] throwArray = invoked.getThrows();
      this.fillExceptionSet(throwArray);
    }

    super.visit(expression);
  }

  public void visit(BinMethodInvocationExpression expression) {
    BinMethod object = (BinMethod) expression.getMember();
    Throws[] throwArray = object.getThrows();
    this.fillExceptionSet(throwArray);
    super.visit(expression);
  }

  public boolean hasExceptions() {
    return!this.exceptionSet.isEmpty();
  }

  public void visit(BinCITypesDefStatement x) {
    // skipping Inners classes
  }

  public boolean findException(BinItemVisitable parent) {
    if (parent instanceof BinMethod) {
      BinMethod object = (BinMethod) parent;
      Throws[] declaredThrows = object.getThrows();
      this.excludeArrayFromExceptionSet(declaredThrows);
      return true;
    }

    if (parent instanceof BinTryStatement) {
      BinTryStatement object = (BinTryStatement) parent;
      CatchClause[] declaredCatches = object.getCatches();
      this.excludeArrayFromExceptionSet(declaredCatches);
    }

    if (parent instanceof BinField) {
      BinField fieldObject = (BinField) parent;
      if (fieldObject.getParent().getParent() instanceof BinClass) {
        BinClass classObject = (BinClass) fieldObject.getParent().getParent();
        if (classObject.isAnonymous()) {
          this.clearExceptionSet();
          return true;
        }

        BinConstructor[] constructorArray =
            classObject.getDeclaredConstructors();

        if (this.areConstructorsInExceptionSet(constructorArray)) {
          if (constructorArray != null) {
            this.excludeArrayFromExceptionSet(
                constructorArray[0].getThrows());
          }
        }
      }
    }

    if (parent instanceof BinInitializer) {
      BinInitializer object = (BinInitializer) parent;
      if (object.isStatic()) {
        return false;
      }

      BinClass classObject = (BinClass) object.getParent();

      BinConstructor[] constructorArray = classObject.getDeclaredConstructors();

      if (this.areConstructorsInExceptionSet(constructorArray)) {
        if (constructorArray != null) {
          this.excludeArrayFromExceptionSet(constructorArray[0].getThrows());
        }
      }
    }

    if (parent != null) {
      this.findException(parent.getParent());
    }

    return this.isEmptyExceptionSet();
  }

  private boolean isRuntime(BinTypeRef exception) {
    final BinTypeRef runtimeRef = exception.getProject()
        .getTypeRefForName("java.lang.RuntimeException");
    return exception.equals(runtimeRef) || exception.isDerivedFrom(runtimeRef);
  }

  private void fillExceptionSet(Throws[] exceptions) {
    for (int i = 0; i < exceptions.length; i++) {
      BinTypeRef exception = exceptions[i].getException();
      if (!this.isRuntime(exception)) {
        exceptionSet.add(exception);
      }
    }
  }

  private void excludeExeptionFromExceptionSet(BinTypeRef declaredException) {
    Iterator it = concreteExceptionSet.iterator();
    while (it.hasNext()) {
      BinTypeRef fallingException = (BinTypeRef) it.next();
      if (fallingException.isDerivedFrom(declaredException)) {
        it.remove();
      }
    }
  }


}
