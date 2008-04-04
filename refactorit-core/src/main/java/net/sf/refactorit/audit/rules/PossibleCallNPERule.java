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
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinCastExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.classmodel.statements.BinExpressionStatement;
import net.sf.refactorit.classmodel.statements.BinLocalVariableDeclaration;
import net.sf.refactorit.classmodel.statements.BinReturnStatement;
import net.sf.refactorit.classmodel.statements.BinStatement;
import net.sf.refactorit.common.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author Oleg Tsernetsov
 */
public class PossibleCallNPERule extends AuditRule {
  public static final String NAME = "possible_npe";

  private MultiValueMap returnCalls = new MultiValueMap();

  private MultiValueMap varAssignments = new MultiValueMap();

  private MultiValueMap varAssignmentScopes = new MultiValueMap();

  private MultiValueMap methodInvExpressions = new MultiValueMap();

  private List badMethods = new ArrayList();

  private List badVariables = new ArrayList();

  // check for unassigned variables - local vars and method params
  // method params are considered to be unassigned initially
  // (one can send null as a parameter)

  public final void visit(BinLocalVariableDeclaration decl) {
    BinVariable[] declvars = decl.getVariables();

    for (int i = 0; i < declvars.length; i++) {
      if (!declvars[i].getTypeRef().isPrimitiveType()
          && declvars[i].getExpression() != null) {
        BinExpression expr = declvars[i].getExpression();
        while (expr instanceof BinCastExpression) {
          expr = ((BinCastExpression) expr).getExpression();
        }
        varAssignments.put(declvars[i], expr);
      }
    }
    super.visit(decl);
  }

  public final void visit(BinAssignmentExpression expression) {
    final BinExpression leftExpression = expression.getLeftExpression();
    BinExpression rightExpression = expression.getRightExpression();

    if (leftExpression instanceof BinVariableUseExpression
        && !leftExpression.getReturnType().isPrimitiveType()) {

      final BinVariable var = ((BinVariableUseExpression) leftExpression)
          .getVariable();

      while (rightExpression instanceof BinCastExpression) {
        rightExpression = ((BinCastExpression) rightExpression).getExpression();
      }
      varAssignments.put(var, rightExpression);
    }
    super.visit(expression);
  }

  public void visit(BinMethodInvocationExpression expression) {
    if (expression.getParent() instanceof BinMethodInvocationExpression) {
      BinMethod previous = expression.getMethod();
      BinMethod current = ((BinMethodInvocationExpression) expression
          .getParent()).getMethod();
      methodInvExpressions.put(previous, expression);
    }
    super.visit(expression);
  }

  public void visit(BinReturnStatement st) {
    returnCalls.put(st.getMethod(), st);
    super.visit(st);
  }

  public BinMethod getInvocatorMethod(BinMethodInvocationExpression expr) {
    if (expr.getParentMember() instanceof BinMethod) {
      return (BinMethod) expr.getParentMember();
    }
    return null;
  }

  public void postProcess() {
    boolean isChanged = false;
    BinVariable var;
    BinExpression expr;

    MultiValueMap nullAssignments = new MultiValueMap();

    // prepare variable assignment scopes
    for (Iterator it = varAssignments.keySet().iterator(); it.hasNext();) {
      var = (BinVariable) it.next();
      for (Iterator iter = varAssignments.get(var).iterator(); iter.hasNext();) {
        expr = (BinExpression) iter.next();
        if (expr.getReturnType() == null) {
          nullAssignments.put(var, expr);
        } else {
          varAssignmentScopes.put(var, new AssignmentScope(expr));
        }
      }
    }

    // reduce scopes according to null pointer assignments
    for (Iterator it = nullAssignments.keySet().iterator(); it.hasNext();) {
      var = (BinVariable) it.next();
      for (Iterator iter = nullAssignments.get(var).iterator(); iter.hasNext();) {
        expr = (BinExpression) iter.next();
        reduceScope(var, expr);
      }
    }

    nullAssignments.clear();

    do {
      // handle variables
      do {
        isChanged = false;
        for (Iterator it = varAssignments.keySet().iterator(); it.hasNext();) {
          var = (BinVariable) it.next();
          for (Iterator iter = varAssignments.get(var).iterator(); iter
              .hasNext();) {
            expr = (BinExpression) iter.next();
            if (isBadExpression(expr)) {
              reduceScope(var, expr);
              badVariables.add(var);
              varAssignments.clearKey(var);
              isChanged = true;
              break;
            }
          }
          if (isChanged) {
            break;
          }
        }
      } while (isChanged);

      // handle methods
      for (Iterator keyIt = returnCalls.keySet().iterator(); keyIt.hasNext();) {
        BinMethod meth = (BinMethod) keyIt.next();
        for (Iterator it = returnCalls.get(meth).iterator(); it.hasNext();) {
          BinReturnStatement ret = (BinReturnStatement) it.next();
          if (isBadReturn(ret)) {
            registerSuspicious(meth);
            returnCalls.clearKey(meth);
            isChanged = true;
            break;
          }
        }
        if (isChanged) {
          break;
        }
      }
    } while (isChanged);

    badVariables.clear();
    varAssignments.clear();
    varAssignmentScopes.clear();
    returnCalls.clear();

    for (Iterator it = methodInvExpressions.keySet().iterator(); it.hasNext();) {
      BinMethod meth = (BinMethod) it.next();
      if (badMethods.contains(meth)) {
        for (Iterator iter = methodInvExpressions.get(meth).iterator(); iter
            .hasNext();) {
          BinMethodInvocationExpression methInv = (BinMethodInvocationExpression) iter
              .next();
          addViolation(new PossibleNPEViolation(methInv, methInv.getMethod()
              .getName()));
        }
      }
    }
    methodInvExpressions.clear();
    badMethods.clear();

  }

  private void reduceScope(BinVariable var, BinExpression expr) {
    AssignmentScope badScope = new AssignmentScope(expr);
    boolean changed = false;
    do {
      changed = false;
      if (varAssignmentScopes.keySet().contains(var)) {
        for (Iterator it = varAssignmentScopes.get(var).iterator(); it
            .hasNext();) {
          AssignmentScope curScope = (AssignmentScope) it.next();
          if (badScope.contains(curScope)) {
            varAssignmentScopes.remove(var, curScope);
            changed = true;
            break;
          } else if (curScope.contains(badScope)) {
            AssignmentScope tail = new AssignmentScope(badScope.getEndColumn(),
                badScope.getEndLine(), curScope.getEndColumn(), curScope
                    .getEndLine());
            curScope.setEndColumn(badScope.getStartColumn());
            curScope.setEndLine(badScope.getStartLine());
            varAssignmentScopes.put(var, tail);
            changed = true;
            break;
          } else if (badScope.cutsTop(curScope)) {
            curScope.setStartColumn(badScope.getEndColumn());
            curScope.setStartLine(badScope.getEndLine());
            changed = true;
            break;
          } else if (badScope.cutsTail(curScope)) {
            curScope.setEndColumn(badScope.getStartColumn());
            curScope.setEndLine(badScope.getStartLine());
            changed = true;
            break;
          }
        }
      }
    } while (changed);
  }

  private boolean isBadExpression(BinExpression expr) {
    while (expr instanceof BinCastExpression) {
      expr = ((BinCastExpression) expr).getExpression();
    }
    if (expr instanceof BinVariableUseExpression) {
      BinVariable var = ((BinVariableUseExpression) expr).getVariable();
      if (badVariables.contains(var)) {
        return true;
      }

      if (varAssignmentScopes.keySet().contains(var)) {
        AssignmentScope curScope = new AssignmentScope(expr);
        for (Iterator it = varAssignmentScopes.get(var).iterator(); it
            .hasNext();) {
          AssignmentScope sc = (AssignmentScope) it.next();
          ;
          if (sc.contains(curScope)) {
            return false;
          }
        }
        badVariables.add(var);
        varAssignments.clearKey(var);
        reduceScope(var, expr);
      }
      return true;
    } else if (expr instanceof BinMethodInvocationExpression) {
      if (badMethods.contains(((BinMethodInvocationExpression) expr)
          .getMethod())) {
        return true;
      }
    } else if (expr.getReturnType() == null) {
      return true;
    }
    return false;
  }

  private void registerSuspicious(BinMethod meth) {
    if (meth == null) {
      return;
    }
    List hierarchy = meth.findAllOverridesOverriddenInHierarchy();
    badMethods.addAll(hierarchy);
    badMethods.add(meth);
  }

  // Checks for implicit return of null, possibly unassigned variable or
  // bad method from suspMethods

  private boolean isBadReturn(BinReturnStatement ret) {
    if (ret == null || ret.getReturnExpression() == null) {
      return false;
    }
    BinExpression expr = ret.getReturnExpression();
    return isBadExpression(expr);
  }

  private class AssignmentScope {
    private int sCol, sLine, eCol, eLine;

    public AssignmentScope(int sCol, int sLine, int eCol, int eLine) {
      this.sCol = sCol;
      this.sLine = sLine;
      this.eCol = eCol;
      this.eLine = eLine;
    }

    public AssignmentScope(BinExpression expression) {
      sCol = expression.getStartColumn();
      sLine = expression.getStartLine();
      if (expression.getParent() != null
          && !assignScope(expression.getParent().getParent())) {
        eCol = expression.getEndColumn();
        eLine = expression.getEndLine();
      }
    }

    private boolean assignScope(BinItemVisitable item) {
      while (item.getParent() != null) {
        item = item.getParent();
        if ((item instanceof BinMember) || (item instanceof BinStatement)
            && !(item instanceof BinExpressionStatement)) {
          break;
        }
      }
      if (item instanceof BinMember) {
        eCol = ((BinMember) item).getEndColumn();
        eLine = ((BinMember) item).getEndLine();
      } else if (item instanceof BinStatement) {
        eCol = ((BinStatement) item).getEndColumn();
        eLine = ((BinStatement) item).getEndLine();
      } else {
        return false;
      }
      return true;
    }

    public String toString() {
      return " [" + sCol + ":" + sLine + "] [" + eCol + ":" + eLine + "]";
    }

    public boolean contains(AssignmentScope scope) {
      return ((sLine < scope.getStartLine() || (sLine == scope.getStartLine() && sCol <= scope
          .getStartColumn())) && (eLine > scope.getEndLine() || (eLine == scope
          .getEndLine() && eCol >= scope.getEndColumn())));
    }

    public boolean cutsTop(AssignmentScope scope) {
      return !contains(scope)
          && ((eLine > scope.getStartLine() || (eLine == scope.getStartLine() && eCol > scope
              .getStartColumn())) && (eLine < scope.getEndLine() || (eLine == scope
              .getEndLine() && eCol < scope.getEndColumn())));
    }

    public boolean cutsTail(AssignmentScope scope) {
      return scope.cutsTop(this);
    }

    public int getEndColumn() {
      return eCol;
    }

    public void setEndColumn(int col) {
      eCol = col;
    }

    public int getEndLine() {
      return eLine;
    }

    public void setEndLine(int line) {
      eLine = line;
    }

    public int getStartColumn() {
      return sCol;
    }

    public void setStartColumn(int col) {
      sCol = col;
    }

    public int getStartLine() {
      return sLine;
    }

    public void setStartLine(int line) {
      sLine = line;
    }
  }

}

class PossibleNPEViolation extends AwkwardExpression {

  PossibleNPEViolation(BinMethodInvocationExpression expression,
      String methodName) {
    super(expression, methodName + "() possibly returns null", "refact.audit.possible_npe");
  }

  public BinMember getSpecificOwnerMember() {
    return getSourceConstruct().getEnclosingStatement().getParentMember();
  }
}
