/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */

package net.sf.refactorit.audit.rules;



import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.AwkwardExpression;
import net.sf.refactorit.audit.CorrectiveAction;
import net.sf.refactorit.audit.RuleViolation;
import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinClass;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinSourceConstruct;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.CompilationUnit;
import net.sf.refactorit.classmodel.MethodInvocationRules;
import net.sf.refactorit.classmodel.expressions.BinConstructorInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.common.util.StringUtil;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.source.edit.StringEraser;
import net.sf.refactorit.source.edit.StringInserter;
import net.sf.refactorit.transformations.TransformationManager;
import net.sf.refactorit.ui.module.TreeRefactorItContext;
import net.sf.refactorit.utils.AuditProfileUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Arseni Grigorjev
 */
public class ParameterOrderRule extends AuditRule {
  public static final String NAME = "parameter_order";
  
  private int SENSITIVITY = 65;
  
  public void init() {
    super.init();
    this.SENSITIVITY = AuditProfileUtils.getIntOption(getConfiguration(),
        "options", "precision", SENSITIVITY);
  }
  
  public void visit(BinMethodInvocationExpression expr){
    BinExpression[] expressions = expr.getExpressionList().getExpressions();
    BinParameter[] params = expr.getMethod().getParameters();
    
		// if there are less than 2 parameters they cannot be mixed :)
    if (params.length < 2){
      return;
    }
    
    BinCIType context = expr.getParentType();
    BinMethod method = expr.getMethod();
        
    // try to mix parameters in all possible combinations and see if the same
    // method will be called
    BinTypeRef[] newTypes = new BinTypeRef[params.length];
    fillTypes(params, newTypes);
    
    for (int i = 0; i < params.length - 1; i++){
      if ((!(expressions[i] instanceof BinVariableUseExpression)
          && !(expressions[i] instanceof BinFieldInvocationExpression))
          /* 
           * if name of param is null, can`t check similarity
           * i`m not sure when this appears - maybe when the method is not
           * on the project`s classpath.
           */
          || params[i].getName() == null){
        continue;
      }
            
      for (int k = i+1; k < params.length; k++){
        if ((!(expressions[k] instanceof BinVariableUseExpression)
            && !(expressions[k] instanceof BinFieldInvocationExpression))
            /*
             * if one of the types is primitive type and the other not,
             * the arguments can not be mixed (comiler would flag an error) -
             * this is why we will check with XOR the primitive type check.
             */
            || (newTypes[k].isPrimitiveType() ^ newTypes[i].isPrimitiveType())
            // check for problem described in outter cycle comment
            || params[k].getName() == null){
          continue;
        }
        
        newTypes[i] = params[k].getTypeRef();
        newTypes[k] = params[i].getTypeRef();
        
        BinMethod newMethod 
            = MethodInvocationRules.getMethodDeclaration(context,
            expr.getInvokedOn(), method.getName(), newTypes);
        
        if (newMethod == method){
          checkMixed(expr, expressions, params, i, k);
        }
        
        newTypes[i] = params[i].getTypeRef();
        newTypes[k] = params[k].getTypeRef();
      }
    }
    
    super.visit(expr);
  }
  
  public void visit(BinConstructorInvocationExpression expr){
    BinExpression[] expressions = expr.getExpressionList().getExpressions();
    BinParameter[] params = expr.getConstructor().getParameters();
    
		// if there are less than 2 parameters they cannot be mixed :)
    if (params.length < 2){
      return;
    }
    
    BinConstructor constr = expr.getConstructor();
    BinClass declaredClass = (BinClass) constr.getOwner().getBinType();
    
    analyzeConstructor(expr, declaredClass, constr, expressions, params);
    
    super.visit(expr);
  }
  
  public void visit(BinNewExpression expr){
    BinConstructor constr = expr.getConstructor();
    if (constr != null){
      BinExpression[] expressions = expr.getExpressionList().getExpressions();
      BinParameter[] params = constr.getParameters();

      // if there are less than 2 parameters they cannot be mixed :)
      if (params.length < 2){
        return;
      }
      
      BinClass declaredClass = (BinClass) expr.getTypeRef().getBinType();
      analyzeConstructor(expr, declaredClass, constr, expressions, params);
    }
    super.visit(expr);
  }

  private void analyzeConstructor(final BinExpression expr, 
      BinClass declaredClass, final BinConstructor constr, 
      final BinExpression[] expressions, final BinParameter[] params) {
    
    BinCIType context = expr.getParentType();
    
    // try to mix parameters in all possible combinations and see if the same
    // method will be called
    BinTypeRef[] newTypes = new BinTypeRef[params.length];
    fillTypes(params, newTypes);

    for (int i = 0; i < params.length - 1; i++){
      if ((!(expressions[i] instanceof BinVariableUseExpression)
          && !(expressions[i] instanceof BinFieldInvocationExpression))
          /* 
           * if name of param is null, can`t check similarity
           * i`m not sure when this appears - maybe when the method is not
           * on the project`s classpath.
           */
          || params[i].getName() == null){
        continue;
      }

      for (int k = i+1; k < params.length; k++){
        if ((!(expressions[k] instanceof BinVariableUseExpression)
            && !(expressions[k] instanceof BinFieldInvocationExpression))
            /*
             * if one of the types is primitive type and the other not,
             * the arguments can not be mixed (comiler would flag an error) -
             * this is why we will check with XOR the primitive type check.
             */
            || (newTypes[k].isPrimitiveType() ^ newTypes[i].isPrimitiveType())
            // check for problem described in outter cycle comment
            || params[k].getName() == null){
          continue;
        }

        newTypes[i] = params[k].getTypeRef();
        newTypes[k] = params[i].getTypeRef();

        BinConstructor newConstructor = declaredClass.getAccessibleConstructor(
            context, newTypes);

        if (newConstructor == constr){
          checkMixed(expr, expressions, params, i, k);
        }

        newTypes[i] = params[i].getTypeRef();
        newTypes[k] = params[k].getTypeRef();
      }
    }
  }
  
  public void checkMixed(BinExpression expr, BinExpression[] expressions, 
      BinParameter[] params, int i, int k){
    
    String param1 = params[i].getName();
    String param2 = params[k].getName();
    String arg1;
    String arg2;
    if (expressions[i] instanceof BinFieldInvocationExpression){
      arg1 = ((BinFieldInvocationExpression) expressions[i]).getField().getName();
    } else {
      arg1 = ((BinVariableUseExpression) expressions[i]).getVariable().getName();
    }
    
    if (expressions[k] instanceof BinFieldInvocationExpression){
      arg2 = ((BinFieldInvocationExpression) expressions[k]).getField().getName();
    } else {
      arg2 = ((BinVariableUseExpression) expressions[k]).getVariable().getName();
    }
    
    final float p1a1 = StringUtil.similarity(param1, arg1);
    final float p1a2 = StringUtil.similarity(param1, arg2);
    final float p2a1 = StringUtil.similarity(param2, arg1);
    final float p2a2 = StringUtil.similarity(param2, arg2);
    boolean possibleMix1 = false;
    boolean possibleMix2 = false;
    float cost1 = 0;
    float cost2 = 0;
        
    if (p1a1 < SENSITIVITY && p2a1 >= SENSITIVITY && p2a2 < p2a1){
  //System.out.println("Similar: points " + p2a1 + "(" + param2+","+arg1+ ")");
      possibleMix1 = true;
      cost1 = p2a1 + (p2a1 - p2a2);
    } 
    
    if (p2a2 < SENSITIVITY && p1a2 >= SENSITIVITY && p1a1 < p1a2){
  //System.out.println("Similar: points " + p1a2 + " (" + param1+","+arg2+ ")");
      possibleMix2 = true;
      cost2 = p1a2 + (p1a2 - p1a1);
    }
    
    if (possibleMix1 && cost1 >= cost2){
      addViolation(new ParameterOrder(expr, param2, arg1, i, k));
    } else if (possibleMix2 && cost2 > cost1){
      addViolation(new ParameterOrder(expr, param1, arg2, i, k));
    }    
  }
  
  public static void fillTypes(BinParameter[] source, BinTypeRef[] destinat){
    for (int i = 0; i < source.length; i++){
      destinat[i] = source[i].getTypeRef();
    }
  }
}

class ParameterOrder extends AwkwardExpression{
  // for corrective action
  private int pos1;
  private int pos2;

  ParameterOrder(BinExpression expression, String param, String arg, 
      int pos1, int pos2) {
        
    super(expression, "Check parameters order: arg. '" + arg + 
        "' is similar to param. '" + param + "' in method signature", "refact.audit.parameter_order");
    this.pos1 = pos1;
    this.pos2 = pos2;
  }

  public BinMember getSpecificOwnerMember() {
    return getSourceConstruct().getEnclosingStatement().getParentMember();
  }

  public List getCorrectiveActions() {
    return Collections.singletonList(ChangeArgumentsPlacesAction.instance);
  }

  public int getPos2() {
    return this.pos2;
  }

  public int getPos1() {
    return this.pos1;
  }
}

/**
 * Repairs parameter order in call Not multitarget for two reasons: <br>
 * 1) it is not often a real bug and each case should be reviewed sepparatly<br>
 * 2) if there are 3 mixed arguments in a call, or if one param can be placed
 * on two other places - two violations are thrown. And the result of 
 * multitarget repair for such case is unpredictable:)
 */
class ChangeArgumentsPlacesAction extends CorrectiveAction {
  public static final ChangeArgumentsPlacesAction instance 
      = new ChangeArgumentsPlacesAction();

  public String getKey() {
    return "refactorit.audit.action.parameter_order.change_arg_places";
  }

  public String getName() {
    return "Change arguments places";
  }

  public Set run(TreeRefactorItContext context, List violations) {
    RuleViolation violation = (RuleViolation) violations.get(0);
    if (!(violation instanceof ParameterOrder)) {
      return Collections.EMPTY_SET; // foreign violation - do nothing
    }
    
    int pos1 = ((ParameterOrder) violation).getPos1();
    int pos2 = ((ParameterOrder) violation).getPos2();
    
    TransformationManager manager = new TransformationManager(null);
    CompilationUnit compilationUnit = violation.getCompilationUnit();
    
    // get expression list from method/constr invokation expression
    BinExpression[] expressions;
    
    BinSourceConstruct expr = ((ParameterOrder) violation).getSourceConstruct();
    if (expr instanceof BinMethodInvocationExpression){
      expressions = ((BinMethodInvocationExpression) expr).getExpressionList()
          .getExpressions();
    } else if (expr instanceof BinConstructorInvocationExpression){
      expressions = ((BinConstructorInvocationExpression) expr)
          .getExpressionList().getExpressions();
    } else {
      try {
        expressions
            = ((BinNewExpression) expr).getExpressionList().getExpressions();
      } catch (Exception e) {
        return Collections.EMPTY_SET;
      }
    }
    
    // erase two expressions
    StringEraser eraser1 = new StringEraser(compilationUnit, 
        expressions[pos1].getStartLine(), expressions[pos1].getStartColumn()-1,
        expressions[pos1].getEndLine(), expressions[pos1].getEndColumn() - 1);
    manager.add(eraser1);
    
    StringEraser eraser2 = new StringEraser(compilationUnit, 
        expressions[pos2].getStartLine(), expressions[pos2].getStartColumn()-1,
        expressions[pos2].getEndLine(), expressions[pos2].getEndColumn() - 1);
    manager.add(eraser2);
    
    // insert them on places of eachother
    StringInserter inserter1 = new StringInserter(
        compilationUnit, 
        expressions[pos1].getStartLine(),
        expressions[pos1].getStartColumn() - 1,
        expressions[pos2].getText());
    manager.add(inserter1);
    
    StringInserter inserter2 = new StringInserter(
        compilationUnit, 
        expressions[pos2].getStartLine(),
        expressions[pos2].getStartColumn() - 1,
        expressions[pos1].getText());
    manager.add(inserter2);
    
    manager.setShowPreview(true);
    final RefactoringStatus status = manager.performTransformations();
    if (status.isCancel()){ // prevent reconciling if no sources were changed
      return Collections.EMPTY_SET;
    }
    return Collections.singleton(compilationUnit);
  }
}
