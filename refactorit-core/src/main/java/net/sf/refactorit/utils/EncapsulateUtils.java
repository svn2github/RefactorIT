/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.utils;

import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinIncDecExpression;
import net.sf.refactorit.classmodel.statements.BinIfThenElseStatement;
import net.sf.refactorit.parser.ASTImpl;
import net.sf.refactorit.query.BinItemVisitor;

/**
 *
 * @author  tanel
 */
public final class EncapsulateUtils {

  /** Don't instantiate */
  private EncapsulateUtils() {
  }

public static BinMethod createVirtualGetter(String name, BinField field,
      int modifiers) {
    final BinMethod newMethod =
        new BinMethod(name,
        BinParameter.NO_PARAMS,
        field.getTypeRef(),
        modifiers,
        BinMethod.Throws.NO_THROWS) {

      /**
       * Override default traverse;
       * Take no actions here because this is a *VIRTUAL* member (and has no
       * content)
       */
      public void defaultTraverse(BinItemVisitor visitor) {
      }
    };

    // Create fake owner reference
    newMethod.setOwner(field.getOwner());
    return newMethod;
  }

  public static BinMethod createVirtualSetter(String name, BinField field,
      int modifiers) {
    final BinMethod newMethod =
        new BinMethod(name,
        new BinParameter[] {new BinParameter(field.getName(), field.getTypeRef(),
        0)}
        ,
        BinPrimitiveType.VOID.getTypeRef(),
        modifiers,
        BinMethod.Throws.NO_THROWS) {

      /**
       * Override default traverse;
       * Take no actions here because this is a *VIRTUAL* member (and has no
       * content)
       */
      public void defaultTraverse(BinItemVisitor visitor) {
      }
    };

    // Create fake owner reference
    newMethod.setOwner(field.getOwner());
    return newMethod;
  }

  public static boolean isUsedInNotReadIncDec(BinFieldInvocationExpression
      expression) {
    if (expression.getParent() instanceof BinIncDecExpression) {
      BinIncDecExpression incDecExpression = (BinIncDecExpression) expression.
          getParent();
      return (!
          (incDecExpression.getParent() instanceof BinExpression ||
          incDecExpression.getParent() instanceof BinIfThenElseStatement));
    }
    return false;
  }

  public static boolean isDirectUsage(BinFieldInvocationExpression
      invocationExpression) {
    BinExpression expression = invocationExpression.getExpression();
    if (expression != null) {
      ASTImpl ast = expression.getRootAst();
      if (ast.getFirstChild() == null) {
        return true;
      } else {
        return false;
      }
    } else {
      return true;
    }
  }
}
