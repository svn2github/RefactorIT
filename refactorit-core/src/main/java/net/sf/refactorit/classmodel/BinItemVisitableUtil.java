/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel;


import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.query.BinItemVisitor;

import java.util.ArrayList;
import java.util.List;


public final class BinItemVisitableUtil {
  public static boolean contains(BinItemVisitable parent,
      BinItemVisitable child) {
    if (child == parent) {
      return true;
    }

    if (child == null) {
      return false;
    }

    return contains(parent, child.getParent());
  }

  public static List getFieldsInvokedIn(BinItemVisitable item) {
    final List result = new ArrayList();

    item.accept(new BinItemVisitor() {
      public void visit(BinFieldInvocationExpression x) {
        result.add(x.getField());

        super.visit(x);
      }
    });

    return result;
  }

  public static List getLocalVariablesInvokedIn(BinItemVisitable item) {
    final List result = new ArrayList();

    item.accept(new BinItemVisitor() {
      public void visit(BinVariableUseExpression x) {
        result.add(x.getVariable());

        super.visit(x);
      }
    });

    return result;
  }

  public static List getVariablesInvokedIn(BinItemVisitable item) {
    List result = getFieldsInvokedIn(item);
    result.addAll(getLocalVariablesInvokedIn(item));
    return result;
  }

  public static boolean isMember(BinItemVisitable o) {
    return (o instanceof BinMember) && !(o instanceof BinLocalVariable)
        && !(o instanceof BinCIType);
  }
}
