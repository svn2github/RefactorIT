/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.classmodel.expressions;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinPrimitiveType;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CommonTypeFinder;
import net.sf.refactorit.classmodel.TypeConversionRules;
import net.sf.refactorit.parser.ASTImpl;


/**
 * x == y ? n : m
 */
public final class BinConditionalExpression extends BinExpression {
  public BinConditionalExpression(BinExpression condition,
      BinExpression trueExpression, BinExpression falseExpression,
      ASTImpl rootAst) {
    super(rootAst);
    this.condition = condition;
    this.trueExpression = trueExpression;
    this.falseExpression = falseExpression;
  }

  private final BinTypeRef findReturnType() {
    // JLS 15.25 Conditional Operator ?:
    // The type of a conditional expression is determined as follows:
    //
    // * If the second and third operands have the same type
    //  (which may be the null type), then that is the type of the conditional
    //  expression.
    // * Otherwise, if the second and third operands have numeric type, then
    //   there are several cases:
    //   * If one of the operands is of type byte and the other is of type
    //     short, then the type of the conditional expression is short.
    //   * If one of the operands is of type T where T is byte, short, or char,
    //     and the other operand is a constant expression of type int whose
    //     value is representable in type T, then the type of the conditional
    //     expression is T.
    //   * Otherwise, binary numeric promotion (§5.6.2) is applied to the
    //     operand types, and the type of the conditional expression is the
    //     promoted type of the second and third operands. Note that binary
    //     numeric promotion performs value set conversion (§5.1.8).
    // * If one of the second and third operands is of the null type and the
    //   type of the other is a reference type, then the type of the conditional
    //   expression is that reference type.
    // * If the second and third operands are of different reference types, then
    //   it must be possible to convert one of the types to the other type (call
    //   this latter type T) by assignment conversion (§5.2); the type of the
    //   conditional expression is T. It is a compile-time error if neither type
    //   is assignment compatible with the other type.

    // FIXME: Implement the contract above
    final BinTypeRef leftType = trueExpression.getReturnType();
    final BinTypeRef rightType = falseExpression.getReturnType();

    if (leftType == null) {
    	return rightType;
    }
    if (rightType == null) {
    	return leftType;
    }
    if (leftType.equals(rightType)) {
    	return leftType;
    }
    if ((rightType.equals(BinPrimitiveType.BYTE.getTypeRef()) &&
    		leftType.equals(BinPrimitiveType.SHORT.getTypeRef()))
    	||
			 (rightType.equals(BinPrimitiveType.SHORT.getTypeRef()) &&
	    		leftType.equals(BinPrimitiveType.BYTE.getTypeRef()))) {
    	return BinPrimitiveType.SHORT.getTypeRef();
    }
    BinTypeRef downGraded = tryDowngradeConstantIntExpr(trueExpression, falseExpression);
    if (downGraded != null) {
    	return downGraded;
    }
    downGraded = tryDowngradeConstantIntExpr(falseExpression, trueExpression);
    if (downGraded != null) {
    	return downGraded;
    }

    BinTypeRef result = checkUnboxedWideningPrimitiveConversion(leftType,
        rightType);
    if ( result != null) {
      return result;
    }

    result = checkWideningPrimitivevConversion(leftType, rightType);
    if (result != null) {
      return result;
    }
    if (TypeConversionRules.isWideningReferenceConversion(leftType, rightType)) {
    	return rightType;
    }
    if (TypeConversionRules.isWideningReferenceConversion(rightType, leftType)) {
    	return leftType;
    }

    if(leftType.isDerivedFrom(rightType)) {
      return rightType;
    }

    if(rightType.isDerivedFrom(leftType)) {
      return leftType;
    }

    // find common parent for left,right expressions
    CommonTypeFinder finder = new CommonTypeFinder(leftType, rightType);
    BinTypeRef commonType = finder.getCommonType();
    if(commonType != null) {
      return commonType;
    }

    //FIXME: what to return here?
    //throw null;
    return leftType; // XXX: this is just a fast hack!
  }

  private BinTypeRef checkUnboxedWideningPrimitiveConversion(BinTypeRef
      leftType, BinTypeRef rightType) {
    BinTypeRef lPrim = null, rPrim = null;
    if(leftType.isPrimitiveType()) {
       lPrim = leftType;
    } else {
      lPrim = TypeConversionRules.getUnboxingPrimitiveByType(leftType);
    }

    if (rightType.isPrimitiveType()) {
      rPrim = rightType;
    } else {
      rPrim = TypeConversionRules.getUnboxingPrimitiveByType(rightType);
    }
    if (lPrim != null && rPrim != null) {
      BinTypeRef result = checkWideningPrimitivevConversion(lPrim, rPrim);
      if (lPrim.equals(result)) {
        return leftType;
      }
      if (rPrim.equals(result)) {
        return rightType;
      }
    }

    return null;
  }

  private BinTypeRef checkWideningPrimitivevConversion(BinTypeRef leftType,
      BinTypeRef rightType) {
    if (TypeConversionRules.isWideningPrimitiveConversion(leftType, rightType)) {
      return rightType;
    }
    if (TypeConversionRules.isWideningPrimitiveConversion(rightType, leftType)) {
      return leftType;
    }
    return null;
  }

  private BinTypeRef tryDowngradeConstantIntExpr(BinExpression expr1, BinExpression expr2) {
  	if (isConstantExpressionOfTypeInt(expr1)) {
  		int value = getIntFromConstantExression(expr1);
  		BinTypeRef type2 = expr2.getReturnType();
  		if (type2.equals(BinPrimitiveType.BYTE.getTypeRef())) {
  			if ((value >= Byte.MIN_VALUE) && (value <= Byte.MAX_VALUE)) {
  				return BinPrimitiveType.BYTE.getTypeRef();
  			}
  		} else if (type2.equals(BinPrimitiveType.SHORT.getTypeRef())) {
  			if ((value >= Short.MIN_VALUE) && (value <= Short.MAX_VALUE)) {
  				return BinPrimitiveType.SHORT.getTypeRef();
  			}
  		} else if (type2.equals(BinPrimitiveType.CHAR.getTypeRef())) {
  			if ((value >= Character.MIN_VALUE) && (value <= Character.MAX_VALUE)) {
  				return BinPrimitiveType.CHAR.getTypeRef();
  			}
  		}

  	}
  	return null;
  }

  /**
   *
   * @param expr
   * @return <code>true</code> if expr is constant expression of type int.
   * Currently only recognizes int literals :(
   */
  private boolean isConstantExpressionOfTypeInt(BinExpression expr) {
  	return ((expr instanceof BinLiteralExpression) &&
  		(expr.getReturnType().equals(BinPrimitiveType.INT.getTypeRef())));
  }

  /**
   * Extracts an int value from literal.
   * There are many other types of contsnat expressions that can return int but
   * we are not uet able to analyze them.
   *
   * @param expr
   * @return
   * @throws IllegalArgumentException if expr is not a constant expreesion of type int
   * IMPORTANT: this method and <code>isConstantExpressionOfTypeInt</code> must be in sync!
   * @see isConstantExpressionOfTypeInt
   */
  private int getIntFromConstantExression(BinExpression expr) throws IllegalArgumentException {
    if (expr instanceof BinLiteralExpression) {
    	String literal = ((BinLiteralExpression)expr).getLiteral();
    	try {
	    	return Integer.parseInt(literal);
    	} catch (NumberFormatException e) {
      	throw new IllegalArgumentException("Not an INT literal "+ literal);
    	}
    } else {
    	throw new IllegalArgumentException("Don't know how to get value from constant expression of type "+ expr.getClass());
    }
  }


  public final BinExpression getCondition() {
    return condition;
  }

  public final BinExpression getTrueExpression() {
    return trueExpression;
  }

  public final BinExpression getFalseExpression() {
    return falseExpression;
  }

  public final void accept(net.sf.refactorit.query.BinItemVisitor visitor) {
    visitor.visit(this);
  }

  public final void defaultTraverse(net.sf.refactorit.query.BinItemVisitor visitor) {
    condition.accept(visitor);
    trueExpression.accept(visitor);
    falseExpression.accept(visitor);
  }

  public final void clean() {
    condition.clean();
    condition = null;
    trueExpression.clean();
    trueExpression = null;
    falseExpression.clean();
    falseExpression = null;
    super.clean();
  }

  public final boolean isSame(BinItem other) {
    if (!(other instanceof BinConditionalExpression)) {
      return false;
    }
    final BinConditionalExpression expr = (BinConditionalExpression) other;
    return this.condition.isSame(expr.condition)
        && this.trueExpression.isSame(expr.trueExpression)
        && this.falseExpression.isSame(expr.falseExpression);
  }

  public BinTypeRef ensureReturnType(BinCIType context, String methodName,
      BinTypeRef[] methodExpressionTypes) {

    if (!haveSuchMetdhod(returnType, methodName, methodExpressionTypes,
        context) && (!returnType.isPrimitiveType())) {
      CommonTypeFinder finder = new CommonTypeFinder(this.
          getTrueExpression().getReturnType(),
          this.getFalseExpression().getReturnType());
      BinTypeRef[] commonType = finder.getAllCommonTypes();

      for (int i = 0; i < commonType.length; i++) {
        if (haveSuchMetdhod(commonType[i], methodName, methodExpressionTypes,
            context)) {
          returnType = commonType[i];
          return returnType;
        }
      }
    }
    return returnType;
  }

  private boolean haveSuchMetdhod(BinTypeRef target, String methodName,
      BinTypeRef[] methodExpressionTypes, BinCIType context) {
    BinMethod[] methods = target.getBinCIType().
        getAccessibleMethods(context);
    for (int j = 0; j < methods.length; j++) {
      if (methods[j].getName().equalsIgnoreCase(methodName)) {
        if (isEqualsMethodParams(methods[j].getTypeParameters(),
            methodExpressionTypes)) {
          return true;
        }
      }
    }
    return false;
  }

  private static final boolean isEqualsMethodParams(BinTypeRef[] t1,
      BinTypeRef[] t2) {
    if (t1.length != t2.length) {
      return false;
    }
    for (int i = 0; i < t1.length; i++) {
      if (!t1[i].equals(t2[i])) {
        return false;
      }
    }
    return true;
  }

  public BinTypeRef ensureReturnType(BinCIType context, String fieldName) {
    if (!haveSuchFieldInner(returnType, fieldName,
        context) && (!returnType.isPrimitiveType())) {
      CommonTypeFinder finder = new CommonTypeFinder(this.
          getTrueExpression().getReturnType(),
          this.getFalseExpression().getReturnType());
      BinTypeRef[] commonType = finder.getAllCommonTypes();

      for (int i = 0; i < commonType.length; i++) {
        if (haveSuchFieldInner(commonType[i], fieldName, context)) {
          returnType = commonType[i];
          return returnType;
        }
      }

    }
    return returnType;
  }

  private boolean haveSuchFieldInner(BinTypeRef target, String name,
      BinCIType context) {
    BinField field = target.getBinCIType()
        .getAccessibleField(name, context);

    if (field != null) {
      return true;
    }

    BinTypeRef inner = target.getBinCIType()
        .getDeclaredType(name);
    if (inner != null) {
      return true;
    }
    return false;
  }

  public final BinTypeRef getReturnType() {
    if (returnType == null) {
      returnType = findReturnType();
    }
    return returnType;
  }

  private BinExpression condition;
  private BinExpression trueExpression;
  private BinExpression falseExpression;
  private BinTypeRef returnType = null;

}
