/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.usage;

import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinLogicalExpression;
import net.sf.refactorit.classmodel.statements.BinForStatement;
import net.sf.refactorit.classmodel.statements.BinLocalVariableDeclaration;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.classmodel.statements.BinWhileStatement;
import net.sf.refactorit.query.usage.filters.BinVariableSearchFilter;
import net.sf.refactorit.utils.EncapsulateUtils;
import net.sf.refactorit.utils.GetterSetterUtils;


/**
 * Field indexer for encapsulate field refactoring.
 * Does not index field declaration and field usages in getter and setter methods.
 */
public final class EncapsulateFieldIndexer extends FieldIndexer {

  private final String getterName;
  private final String setterName;

  public EncapsulateFieldIndexer(final ManagingIndexer supervisor,
      final BinField target, final String getterName, final String setterName) {
    super(supervisor, target, true);

    this.getterName = getterName;
    this.setterName = setterName;
    setSearchForNames(true);
  }

  public final void visit(final BinFieldInvocationExpression expression) {
    if (isTargetField(expression.getField())) {

      if (!isInSetterOrGetter(expression)) {
        getSupervisor().addInvocation(
            new EncapsulationInvocationData((BinField) getTarget(),
            getSupervisor().getCurrentLocation(),
            expression.getNameAst(),
            expression,
            isEncapsulationPossible(expression)));
      }
      return;
    }
    super.visit(expression);
  }

  /**
   * Determines whether replacing of this field with accessor method
   * is possible.
   */
  private static boolean isEncapsulationPossible(final
      BinFieldInvocationExpression expression) {
    //<FIX> author Aleksei Sosnovski
    if (expression.getParent() instanceof BinAssignmentExpression
        && ((BinAssignmentExpression) expression.getParent())
        .getLeftExpression() == expression) {
      BinItemVisitable parent = expression.getParent();

      if (parent.getParent() instanceof BinAssignmentExpression
          || parent.getParent() instanceof BinLogicalExpression) {
        while (!(parent instanceof BinForStatement)
            && !(parent instanceof BinLocalVariableDeclaration)
            && !(parent instanceof BinWhileStatement)
            && !(parent instanceof BinStatementList)) {
          parent = parent.getParent();
        }

        if (parent instanceof BinStatementList
            || parent instanceof BinLocalVariableDeclaration) {
          return true;
        }
      }
    }
    //</FIX>

    if ((BinVariableSearchFilter.isReadAccess(expression)
        && !BinVariableSearchFilter.isWriteAccess(expression))
        || (!BinVariableSearchFilter.isReadAccess(expression)
        && BinVariableSearchFilter.isWriteAccess(expression))) {
      return true;
    }
    //detect if field is used in i++ type expression and the value is not read
    return (EncapsulateUtils.isUsedInNotReadIncDec(expression)
        && EncapsulateUtils.isDirectUsage(expression));
  }

  /**
   * Determines whether the expression is inside the accessor of this field.
   */
  private boolean isInSetterOrGetter(final BinFieldInvocationExpression
      expression) {
    final BinTypeRef invokedOn = expression.getInvokedOn();
    // FIXME: it can be wrong here - invokedOn is type infront of DOT or implicit this
    if (invokedOn != null && invokedOn.equals(getTypeRef())) {
      final BinMember memberOf = expression.getParentMember();
      if (memberOf instanceof BinMethod) {
        final BinMethod method = (BinMethod) memberOf;
        if (GetterSetterUtils.isGetterMethod(method, (BinField) getTarget(),
            new String[] {getterName}, false) ||
            GetterSetterUtils.isSetterMethod(method, (BinField) getTarget(),
            setterName, false)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean isTargetField(final BinField field) {
    return field == getTarget();
  }

}
