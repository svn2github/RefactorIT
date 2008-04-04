/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.movemember;


import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinField;
import net.sf.refactorit.classmodel.BinInitializer;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.BinVariable;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.statements.BinForStatement;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.classmodel.statements.BinTryStatement;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.common.util.FastStack;
import net.sf.refactorit.query.AbstractIndexer;

import java.util.ArrayList;
import java.util.List;


public class InstanceFinder extends AbstractIndexer {
  private FastStack scopedVars = new FastStack();
  private List foundVars = null;
  private BinMemberInvocationExpression expressionCalled;
  private BinMember location;

  public BinMember findInstance(BinMember location,
      BinMemberInvocationExpression expressionCalled,
      BinCIType instanceOfType) {
    this.expressionCalled = expressionCalled;
    this.location = location;

    BinMember instance =
        ReferenceUpdater.checkExpressionForInstance(expressionCalled.
        getExpression(),
        instanceOfType);

    if (instance == null
        && expressionCalled instanceof BinMethodInvocationExpression) {
      BinExpression[] params = ((BinMethodInvocationExpression)
          expressionCalled)
          .getExpressionList().getExpressions();
      for (int i = 0; i < params.length; i++) {
        instance = ReferenceUpdater.checkExpressionForInstance(params[i],
            instanceOfType);
        if (instance != null) {
          break;
        }
      }
    }

    if (instance == null) {
      BinCIType toVisit = location.getOwner().getBinCIType()
          /*.getTopLevelEnclosingType()*/
          ;
      toVisit.accept(this);

      if (Assert.enabled) {
        Assert.must(this.foundVars != null,
            "Didn't find any vars at all in: " + toVisit);
      }
      if (this.foundVars != null) {
        // first lets use exact type if any
        for (int i = this.foundVars.size() - 1; i >= 0; --i) {
          BinVariable var = (BinVariable)this.foundVars.get(i);
          BinTypeRef varTypeRef = var.getTypeRef();
          if (varTypeRef != null && varTypeRef.equals(instanceOfType.getTypeRef())) {
            instance = var;
            break;
          }
        }

        // then derived types
        if (instance == null) {
          for (int i = this.foundVars.size() - 1; i >= 0; --i) {
            BinVariable var = (BinVariable)this.foundVars.get(i);
            if (var.getTypeRef()
                .isDerivedFrom(instanceOfType.getTypeRef())) {
              instance = var;
              break;
            }
          }
        }
      }
    }
//System.err.println("findInstance: expressionCalled: " + expressionCalled
//  + " - " + instance);

    return instance;
  }

  private void startScope() {
    List newVars = new ArrayList();
    if (!scopedVars.empty()) {
      List vars = (List) scopedVars.peek();
      newVars.addAll(vars);
    }
    scopedVars.push(newVars);
  }

  private void endScope() {
    scopedVars.pop();
  }

  private void declareVar(BinVariable var) {
    if (var.getTypeRef().isReferenceType()) {
      if (!location.isStatic() ||
          (location.isStatic() && isValidForStaticLocation(var))) {
        ((List) scopedVars.peek()).add(var);
      }
    }
  }

  private boolean isValidForStaticLocation(BinVariable var) {
    return (var.isStatic() ||
        ((var instanceof BinLocalVariable) &&
        var.getParentMember() == location));
  }

  public void visit(BinCIType type) {
    startScope();
    super.visit(type);
    endScope();
  }

  public void visit(BinMethod method) {
    startScope();
    super.visit(method);
    endScope();
  }

  public void visit(BinConstructor ctr) {
    if (ctr.isSynthetic()) {return;
    }
    startScope();
    super.visit(ctr);
    endScope();
  }

  public void visit(BinInitializer initr) {
    startScope();
    super.visit(initr);
    endScope();
  }

  public void visit(BinForStatement fors) {
    startScope();
    super.visit(fors);
    endScope();
  }

  public void visit(BinStatementList stats) {
    startScope();
    super.visit(stats);
    endScope();
  }

  public void visit(BinTryStatement.CatchClause catchh) {
    startScope();
    super.visit(catchh);
    endScope();
  }

  public void visit(BinField x) {
    declareVar(x);
    super.visit(x);
  }

  public void visit(BinLocalVariable x) {
    declareVar(x);
    super.visit(x);
  }

  public void visit(BinMethodInvocationExpression expression) {
    if (this.expressionCalled == expression) {
      this.foundVars = new ArrayList((List) scopedVars.peek());
    }
    super.visit(expression);
  }

  public void visit(BinFieldInvocationExpression expression) {
    if (this.expressionCalled == expression) {
      this.foundVars = new ArrayList((List) scopedVars.peek());
    }
    super.visit(expression);
  }
}
