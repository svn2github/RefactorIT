/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.inlinevariable;

import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.expressions.BinArrayInitExpression;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.statements.BinForStatement;
import net.sf.refactorit.query.usage.InvocationData;
import net.sf.refactorit.query.usage.filters.BinVariableSearchFilter;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.format.BinFormatter;

import java.util.List;



/**
 * @author  RISTO A
 */
class Preconditions {
  private BinVariable var;
  private List invocations;

  public Preconditions(BinVariable var, List invocations) {
    this.var = var;
    this.invocations = invocations;
  }

  public RefactoringStatus status() {
    RefactoringStatus status = new RefactoringStatus();
    status.merge(checkFieldSupport());
    status.merge(checkAssignedToOnlyOnce());
    status.merge(checkNotConstantArrayInitialization());
    status.merge(checkNotForInIterator());
    return status;
  }

  /**
   * @return
   */
  private RefactoringStatus checkNotForInIterator() {
    RefactoringStatus result = new RefactoringStatus();
    if ((var.getParent() != null) && (var.getParent().getParent() instanceof BinForStatement)) {
      if (((BinForStatement)var.getParent().getParent()).isForEachStatement()) {
        result.addEntry(
            "Cannot inline for-each loop iterator: " +
            BinFormatter.formatLocationAwareStartLine(var),
            RefactoringStatus.ERROR, var);
        
      }
    }
    return result;
    
  }

  private RefactoringStatus checkFieldSupport() {
    if (!(var instanceof BinField)) {
      return new RefactoringStatus();
    }
    RefactoringStatus result = new RefactoringStatus();

    for (int i = 0; i < invocations.size(); i++) {
      InvocationData data = (InvocationData) invocations.get(i);
      BinFieldInvocationExpression invocationExpr = (
          BinFieldInvocationExpression) data.getInConstruct();
      if (invocationExpr.hasDot()) {
        result.addEntry(
            "Prefixed usages and usages outside the declaring class not supported: " +
            BinFormatter.formatLocationAwareStartLine(data.getInConstruct()),
            RefactoringStatus.ERROR, data.getInConstruct());
      }
    }

    return result;
  }

  private RefactoringStatus checkAssignedToOnlyOnce() {
    final RefactoringStatus status = new RefactoringStatus();

    List writes = new BinVariableSearchFilter(false, true, true, false, false).
        filter(this.invocations, var.getOwner().getProject());

    if (var.getExpression() == null) {
      if (writes.size() == 0) {
        status.addEntry("Variable declaration contains no value: ",
            RefactoringStatus.ERROR);
        return status;
      } else {
        InvocationData data = (InvocationData) writes.get(0);
        BinSourceConstruct s = (BinSourceConstruct) data.getInConstruct();

        BinItemVisitable whereLocated = s;
        while (!(whereLocated instanceof BinMethod || whereLocated == null)) {
          whereLocated = whereLocated.getParent();
        }
        if (!(whereLocated instanceof BinConstructor)
            && (var instanceof BinField)) {
          // todo: add check that whereLocated is the constructor of the var class
          status.addEntry(
              "Lazy field initialization is not supported outside the constructors: "
              + BinFormatter.formatLocationAwareStartLine(s),
              RefactoringStatus.ERROR, s);
        }

        if (!(s.getParent() instanceof BinAssignmentExpression)) {
          status.addEntry("Complex expression inlining is not supported: "
              + BinFormatter.formatLocationAwareStartLine(s),
              RefactoringStatus.ERROR, s);
        } else {
          if (((BinAssignmentExpression) s.getParent()).getRightExpression().
              isChangingAnything()) {
            status.addEntry(
                "Unsafe inlining -- functionality of the program may be changed: "
                + BinFormatter.formatLocationAwareStartLine(s),
                RefactoringStatus.WARNING, s);
          }
        }

        writes.remove(0);

      }
    } else { 
      List reads = new BinVariableSearchFilter(true, false, true, false, false).
      	filter(this.invocations, var.getOwner().getProject());
      
      if (var.getExpression().isChangingAnything() && reads.size() > 1) {
        status.addEntry(
            "Possible change in functionality -- after inlining, the expression in the variable initializer is invoked more than once: "
            + BinFormatter.formatLocationAwareStartLine(var),
            RefactoringStatus.WARNING, var);
      }
      
    }

    for (int i = 0; i < writes.size(); i++) {
      BinSourceConstruct s = (BinSourceConstruct) ((InvocationData) writes.get(
          i)).getInConstruct();
      status.addEntry("Variable assigned to more than once: "
          + BinFormatter.formatLocationAwareStartLine(s),
          RefactoringStatus.ERROR, s);
    }

    return status;
  }
  
  

  private RefactoringStatus checkNotConstantArrayInitialization() {
    if (var.getExpression() instanceof BinArrayInitExpression
        /*&& var.getTypeRef().isArray()*/) {
      return new RefactoringStatus(
          "Array declarations without the \"new\" keyword are not supported: ",
          RefactoringStatus.ERROR);
    } else {
      return new RefactoringStatus();
    }
  }

}
