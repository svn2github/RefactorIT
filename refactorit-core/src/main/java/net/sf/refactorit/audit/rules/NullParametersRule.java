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
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.expressions.BinAssignmentExpression;
import net.sf.refactorit.classmodel.expressions.BinCastExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinLiteralExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.classmodel.statements.BinLocalVariableDeclaration;
import net.sf.refactorit.common.util.MultiValueMap;

import java.util.HashMap;
import java.util.Iterator;

/**
* @author Aleksei Sosnovski
*/

public class NullParametersRule extends AuditRule {
  public static final String NAME = "null_parameters";

  private MultiValueMap map = new MultiValueMap();
  private HashMap removeMap = new HashMap();

  public void visit(BinMethodInvocationExpression method) {
    BinExpression[] expressions = method.getExpressionList().getExpressions();

    boolean used = false;

    for (int i = 0; i < expressions.length; i++) {
      BinExpression expr = expressions[i];

      while (expr instanceof BinCastExpression) {
        expr = ((BinCastExpression) expr).getExpression();
      }

      if (expr instanceof BinLiteralExpression) {
        String str = ((BinLiteralExpression) expr).getLiteral();

        if (str.equals("null") && used == false) {
          addViolation(new NullParametersViolation(method));
          used = true;
        }// end of if (str.equals("null"))
      } else// end of  if (expr[i] instanceof BinLiteralExpression)

      if (expr instanceof BinVariableUseExpression) {
        BinVariable var = ((BinVariableUseExpression) expr).getVariable();

        for(Iterator iter = map.keySet().iterator(); iter.hasNext(); ) {
          BinVariable key = (BinVariable) iter.next();
          if (map.contains(key, var)) {
            addViolation(new NullParametersViolation(method, var));
          }
        }
      }
    }// end of  for (int i = 0; i < expr.length; i++)

     super.visit(method);
  } // end of visit(BinMethodInvocationExpression method) method

  public void visit(BinLocalVariableDeclaration dec) {
    BinVariable[] variables = dec.getVariables();

    for (int i = 0; i < variables.length; i++) {
      BinExpression expr = variables[i].getExpression();

      while (expr instanceof BinCastExpression) {
        expr = ((BinCastExpression) expr).getExpression();
      }

      if (expr instanceof BinLiteralExpression) {
        String str = ((BinLiteralExpression) expr).getLiteral();

        if (str.equals("null")) {
          map.put(variables[i], variables[i]);
        }// end of if (str.equals("null"))
      } else// end of  if (expr[i] instanceof BinLiteralExpression)

      if (expr instanceof BinVariableUseExpression) {
        BinVariable var = ((BinVariableUseExpression) expr).getVariable();

        for(Iterator iter = map.keySet().iterator(); iter.hasNext(); ) {
          BinVariable key = (BinVariable) iter.next();

          if (map.contains(key, var)) {
            map.put(key, variables[i]);
          }// end of if (map.contains(key, var))
        }// end of  for(Iterator iter = map.keySet().iterator()...
      }// end of if (expr instanceof BinVariableUseExpression)
    }// end of for (int i = 0; i < variables.length; i++)

     super.visit(dec);
  }// end of visit(BinLocalVariableDeclaration) method

  public void visit(BinAssignmentExpression expr) {
    BinExpression leftExpr = expr.getLeftExpression();
    BinExpression rightExpr = expr.getRightExpression();

    if (leftExpr instanceof BinVariableUseExpression) {
      BinVariable var = ((BinVariableUseExpression) leftExpr).getVariable();

      BinVariable rightVar = null;

      if (rightExpr instanceof BinVariableUseExpression) {
        rightVar = ((BinVariableUseExpression) rightExpr).getVariable();
      }

      for (Iterator iter = map.keySet().iterator(); iter.hasNext(); ) {
        BinVariable key = (BinVariable) iter.next();

        if (map.contains(key, var)) {
          removeMap.put(key, var);
        } // end of if (map.contains(key, var))

        if (rightVar != null && map.contains(key, rightVar)) {
          map.put(key, var);
        } // end of if (map.contains(key, rightVar))

      } // end of  for(Iterator iter = map.keySet().iterator()...

      for (Iterator iter = removeMap.keySet().iterator(); iter.hasNext(); ) {
        Object key = iter.next();
        Object value = removeMap.get(key);
        map.remove(key, value);
      }
      removeMap.clear();

      if (rightExpr instanceof BinLiteralExpression) {
        String str = ((BinLiteralExpression) rightExpr).getLiteral();
        if (str.equals("null")) {
          map.put(var, var);
        }// end of if (str.equals("null"))
      }// end of if (rightExpr instanceof BinLiteralExpression)
    }// end of if (leftExpr instanceof BinVariableUseExpression)

     super.visit(expr);
  }// end of visit(BinAssignmentExpression expr) method
}// end of NullArgumentRule class


class NullParametersViolation extends AwkwardExpression {

  public NullParametersViolation(BinExpression invocation) {
    super (invocation,
        "Null parameter(s) in " + invocation.getText(),
        "null_parameters");
  }

  public NullParametersViolation(BinExpression invocation, BinVariable var) {
    super (invocation,
        "Variable " + var.getName() + " may have null value in "
        + invocation.getText(), "null_parameters");
  }
}
