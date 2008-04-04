/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.audit.rules.complexity;

import net.sf.refactorit.audit.AuditRule;
import net.sf.refactorit.audit.AwkwardExpression;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.statements.BinExpressionStatement;
import net.sf.refactorit.classmodel.statements.BinReturnStatement;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.utils.AuditProfileUtils;

import org.w3c.dom.Element;

/**
 * This audit rule actually combines two similar audits:<br>
 * 1) Find functions that consist of a single statement, which calls antoher 
 * function.<br>
 * 2) Some method simply calls <code>super.sameMethod();</code> Need to consider 
 * access modifiers<br>
 *
 * @author Arseni Grigorjev
 */
public class MethodCallsMethodRule extends AuditRule {
  public static final String NAME = "method_calls_method";
  private boolean skip_proxy_invocations = true;
  private boolean skip_super = true;
  private boolean skip_delegation = true;
  private int find_option = 1;
  
  public void init(){
    final Element configuration = getConfiguration();
    skip_proxy_invocations = AuditProfileUtils.getBooleanOption(
        configuration, "skip", "proxy", skip_proxy_invocations);
    skip_delegation = AuditProfileUtils.getBooleanOption(configuration,
        "skip", "delegation", skip_delegation);
    skip_super = AuditProfileUtils.getBooleanOption(configuration, "skip",
        "super", skip_super);
    find_option = AuditProfileUtils.getIntOption(configuration, "options",
        "find", find_option);
    super.init();
  }
  
  public void visit (BinMethod method){
            
    SingleMethodCallFinder finder = new SingleMethodCallFinder();
    finder.reset();
    method.accept(finder);
    
    if (finder.getExpr() != null){
      BinMethodInvocationExpression expression = finder.getExpr();
      BinExpression subExpr = expression.getExpression();
      
      // if show only super.sameMethod() calls option is active
      if (find_option == 1){
        if (subExpr != null
            && subExpr instanceof BinLiteralExpression
            && ((BinLiteralExpression) subExpr).getLiteral()
            .equals(BinLiteralExpression.SUPER)){
          
          addViolation(new MethodCallsSuper(expression));
        }
      } else {
      // if show all option is active 
        
        // start analyzing skipping conditions.
        // check if it is multi-level (proxy) call:
        if (subExpr != null 
            && subExpr instanceof BinMethodInvocationExpression){

          if (!skip_proxy_invocations){
            addViolation(new MethodCallsMethod(expression));
          } 
        } else {
          // check if it is super.sameMethod() call:
          if (subExpr != null
              && subExpr instanceof BinLiteralExpression
              && ((BinLiteralExpression) subExpr).getLiteral()
              .equals(BinLiteralExpression.SUPER)){

            if (!skip_super){
              addViolation(new MethodCallsSuper(expression));
            }
          } else {
            
            // check if it call on some field
            if (subExpr != null
                && !(subExpr instanceof BinLiteralExpression
                && ((BinLiteralExpression) subExpr).getLiteral()
                .equals(BinLiteralExpression.THIS))){

              if (!skip_delegation){
                addViolation(new MethodCallsMethod(expression));
              }
            } else {
              // this case is never skipped in options (need add more cases?)
              addViolation(new MethodCallsMethod(expression));
            }
          }
        }
      }
    }  
    super.visit(method);
  }
  
  static class SingleMethodCallFinder extends BinItemVisitor {
    private BinMethodInvocationExpression expr = null;
    
    public SingleMethodCallFinder() {
    }
    
    public void reset(){
      this.expr = null;
    }
          
    public void visit(BinMethodInvocationExpression x) {
      this.expr = x;
    }
    
    public void visit(BinReturnStatement x){
      BinExpression expression = x.getReturnExpression();
      if (expression != null &&
          expression instanceof BinMethodInvocationExpression){
        super.visit(x);      
      }
    }
    
    public void visit(BinExpressionStatement x){
      BinExpression expression = x.getExpression();
      if (expression instanceof BinMethodInvocationExpression){
        super.visit(x);      
      }
    }
    
    public void visit(BinStatementList x){
      BinStatement[] stmnts = x.getStatements();
      
      if (stmnts.length == 1 &&
          (stmnts[0] instanceof BinReturnStatement 
          || stmnts[0] instanceof BinExpressionStatement)){
        super.visit(x);
      }
    }
    
    public BinMethodInvocationExpression getExpr() {
      return this.expr;
    }
  }
}

class MethodCallsMethod extends AwkwardExpression {
  public MethodCallsMethod(BinExpression expression) {
    super(expression, "Single statement method only calls another method", "refact.audit.method_calls_method");
  }

  public BinMember getSpecificOwnerMember() {
    return getSourceConstruct().getEnclosingStatement().getParentMember();
  }
}

class MethodCallsSuper extends AwkwardExpression {
  public MethodCallsSuper(BinExpression expression) {
    super(expression, "Single statement method calls super.sameMethod()", "refact.audit.method_calls_method");
  }

  public BinMember getSpecificOwnerMember() {
    return getSourceConstruct().getEnclosingStatement().getParentMember();
  }
}
