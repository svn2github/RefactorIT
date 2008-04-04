/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.changesignature.analyzer;


import net.sf.refactorit.classmodel.BinExpressionList;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinItemVisitable;
import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.SourceConstruct;
import net.sf.refactorit.classmodel.expressions.BinConstructorInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinNewExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.classmodel.expressions.MethodOrConstructorInvocationExpression;
import net.sf.refactorit.common.util.MultiValueMap;
import net.sf.refactorit.query.BinItemVisitor;
import net.sf.refactorit.query.usage.Finder;
import net.sf.refactorit.query.usage.InvocationData;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Aqris AS</p>
 * @author Kirill Buhhalko
 * @version
 */

public class MethodsParDeleteCallsNode extends CallNode {
  private BinParameter binParameter;

  public MethodsParDeleteCallsNode(Object bin, MethodsInvocationsMap finder, int type, MultiValueMap map, BinParameter binParameter) {
    super(bin);
    if (map != null) {
      if (getBin() instanceof BinMethod) {
        map.put(getBin(), this);
      }
    }
    this.finder = finder;
    this.typeD = type;
    this.selected = false;
    this.map = map;

    this.binParameter = binParameter;
  }

  protected void findChilds(MethodsInvocationsMap finder, final BinMethod method) {
    List called = finder.findAllMethodsAreCalledByThis(method);
    called = sort(called);
    for (int i = 0; called != null && i < called.size(); i++) {
      BinMethod calledMeth = (BinMethod) called.get(i);
      if (!alreadyContains(calledMeth)) {
        int[] isInvocatedThere = isParamInvocatedInThisMethod(binParameter,
            calledMeth);
        if (isInvocatedThere != null) {
          MethodsParDeleteCallsNode thisNode = new MethodsParDeleteCallsNode(calledMeth,
              finder, CHILD, map, calledMeth.getParameters()[isInvocatedThere[1]]);
          addChild(thisNode);
        }
      } else {
        addChild(new MethodsParDeleteCallsNode("recursive  call ->> "
            + calledMeth.getQualifiedName(), null, REC, map, null));
      }
    }
  }

  protected void findParents(MethodsInvocationsMap finder, final BinMethod method) {
    List called = finder.findAllMethodsWhichCallThis(method);
    called = sort(called);
    for (int i = 0; called != null && i < called.size(); i++) {
      BinMethod caller = (BinMethod) called.get(i);
      if (!alreadyContains(caller)) {
        int[] isParentsInv = isParentsParameterInvocation(binParameter, caller);
        if(isParentsInv != null) {

          MethodsParDeleteCallsNode thisNode = new MethodsParDeleteCallsNode(caller, finder,
              PARENT, map, caller.getParameters()[isParentsInv[1]]);
          addChild(thisNode);
        }

      } else {
        addChild(new MethodsParDeleteCallsNode("recursive  call ->> "
            + ((BinMethod) caller).getQualifiedName(), null, REC, map, null));
      }
    }
  }

  private static int[] isParamInvocatedInThisMethod(BinParameter binParameter,
      BinMethod binMethod) {
    List invocations = Finder.getInvocations(binParameter);

    for (int i = 0; invocations !=null && i < invocations.size(); i++) {
      SourceConstruct sc = ((InvocationData) invocations.get(i)).getInConstruct();
      if (sc instanceof BinVariableUseExpression) {
        BinItemVisitable parent = sc.getParent().getParent();
        if (parent instanceof MethodOrConstructorInvocationExpression) {
          if (((MethodOrConstructorInvocationExpression) parent).getMethod()
              == binMethod) {
            int[] result = {1, ((BinExpressionList) sc.getParent()).
                getExpressionIndex((BinExpression) sc)};
            return result;
          }

        }
      }
    }
    return null;
  }

  private int[] isParentsParameterInvocation(BinParameter binParameter,
      BinMethod parentMethod) {
    BinMethod method = binParameter.getMethod();

    BinMethodInvocationVisitor visitor = new BinMethodInvocationVisitor(method);
    parentMethod.accept(visitor);
    MethodOrConstructorInvocationExpression[] exprs = visitor.
        getMethodInvocations();

    for (int i = 0; i < exprs.length; i++) {
      BinExpression expr =
          exprs[i].getExpressionList().getExpressions()[binParameter.getIndex()];
      if (expr instanceof BinVariableUseExpression) {
        BinLocalVariable variable = ((BinVariableUseExpression) expr).
            getVariable();
        if (variable instanceof BinParameter) {
          if (((BinParameter) variable).getMethod() == parentMethod) {
            int[] res = {1, ((BinParameter) variable).getIndex()};
            return res;
          }
        }
      }
    }
    return null;
  }

  class BinMethodInvocationVisitor extends BinItemVisitor {
    private BinItem item;
    private ArrayList methodInvocations = new ArrayList();

    BinMethodInvocationVisitor(BinItem item) {
      this.item = item;
    }

    public void visit(BinMethodInvocationExpression x) {
      checkInvocation(x);
      super.visit(x);
    }

    public void visit(BinConstructorInvocationExpression x) {
      checkInvocation(x);
      super.visit(x);
    }

    public void visit(BinNewExpression x) {
      checkInvocation(x);
      super.visit(x);
    }

    private void checkInvocation(final MethodOrConstructorInvocationExpression x) {
      if(x.getMethod() == item) {
        methodInvocations.add(x);
      }
    }

    public MethodOrConstructorInvocationExpression[] getMethodInvocations() {
      return (MethodOrConstructorInvocationExpression[])this.methodInvocations.
          toArray(new MethodOrConstructorInvocationExpression[
          this.methodInvocations.size()]);
    }
  }

  public BinParameter getBinParameter() {
    return this.binParameter;
  }
}
