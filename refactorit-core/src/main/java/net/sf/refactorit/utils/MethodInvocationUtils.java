/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.utils;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.MethodInvocationRules;
import net.sf.refactorit.classmodel.expressions.BinConstructorInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;


/**
 *
 * @author Arseni Grigorjev
 */
public class MethodInvocationUtils {
  /**
   * It is often needed to know, if we replace some expression with another
   * (with a different return type), would that cause a Method Overload
   * Resolution conflict? This utility performs check for this case.<br>
   * Checks method and constructor resolutions using MethodInvocationRules.
   *
   * @param expr Expression, that You want to replace.
   * @param newReturnType new return type of expression
   * 
   * @return <b>true</b>, if another method will be resolved after replace, or 
   *    <b>false</b>, if it is the same method.
   */
  public static final boolean confusesMethodResolution(
      final BinExpression expr, final BinTypeRef newReturnType){
    
    if (expr.getParent() instanceof BinExpressionList){
      
      final BinCIType context = expr.getParentType();
      final BinItemVisitable parent = expr.getParent()
          .getParent();
      
      if (parent instanceof BinMethodInvocationExpression){
        return analyzeMethodInvocation(expr, newReturnType, context, 
            (BinMethodInvocationExpression) parent);
      } else if (parent instanceof BinNewExpression){
        return analyzeNewExpression(expr, newReturnType, context, 
            (BinNewExpression) parent);
      } else if (parent instanceof BinConstructorInvocationExpression){
        return analyzeConstructorInvocation(expr, newReturnType, context, 
            (BinConstructorInvocationExpression) parent);
      }
    }
    return false;
  }
  
  private static final boolean analyzeMethodInvocation(
      final BinExpression expr, final BinTypeRef newReturnType, 
      final BinCIType context, final BinMethodInvocationExpression methodInvoc){
        
    BinTypeRef invokedOn = methodInvoc.getInvokedOn();
    BinMethod method = methodInvoc.getMethod();
    BinParameter[] params = method.getParameters();
    BinExpression[] expressions = methodInvoc.getExpressionList().getExpressions();

    BinTypeRef[] newTypes
        = createNewParameterSet(params, expressions, expr, newReturnType);

    // find method declaration for new parameters
    BinMethod newMethod = MethodInvocationRules.getMethodDeclaration(
        context, invokedOn, method.getName(), newTypes);

    // if methods are equal then no conflict
    return newMethod != method;
  }
  
  private static final boolean analyzeConstructorInvocation(
      final BinExpression expr, 
      final BinTypeRef newReturnType, 
      final BinCIType context, 
      final BinConstructorInvocationExpression constInvocExp) {
        
    final BinConstructor constr = constInvocExp.getConstructor();
    final BinClass declaredClass = (BinClass) constr.getOwner().getBinType();
    final BinParameter[] params = constr.getParameters();
    final BinExpression[] expressions
          = constInvocExp.getExpressionList().getExpressions();

    final BinTypeRef[] newTypes = createNewParameterSet(params, expressions, 
        expr, newReturnType);

    // find method constructor for new parameters
    final BinConstructor newConstructor = declaredClass
        .getAccessibleConstructor(context, newTypes);
    
    return constr != newConstructor;
  }

  private static final boolean analyzeNewExpression(final BinExpression expr, 
      final BinTypeRef newReturnType, final BinCIType context, 
      final BinNewExpression newExpression) {
        
    final BinClass declaredClass  = (BinClass) newExpression.getTypeRef()
        .getBinType();
    final BinConstructor constr = newExpression.getConstructor();
    if (constr != null){
      final BinParameter[] params = constr.getParameters();
      final BinExpression[] expressions
          = newExpression.getExpressionList().getExpressions();

      final BinTypeRef[] newTypes = createNewParameterSet(params, expressions, 
          expr, newReturnType);
      
      // find method constructor for new parameters
      final BinConstructor newConstr = declaredClass.getAccessibleConstructor(
            context, newTypes);
      return constr != newConstr;
    } else {
      return true;
    }
  }

  private static final BinTypeRef[] createNewParameterSet(
      final BinParameter[] params, 
      final BinExpression[] expressions, 
      final BinExpression expr, final BinTypeRef newReturnType) {
        
    final BinTypeRef[] newTypes = new BinTypeRef[params.length];
    try{
      for (int i = 0; ; i++){
        if (expressions[i] == expr){
          newTypes[i] = newReturnType;
        } else {
          newTypes[i] = params[i].getTypeRef();
        }
      }
    } catch (ArrayIndexOutOfBoundsException e){ }
    
    return newTypes;
  }
}
