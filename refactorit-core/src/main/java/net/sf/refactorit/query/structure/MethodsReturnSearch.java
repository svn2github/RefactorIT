/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.query.structure;

import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.ui.module.RefactorItContext;


/**
 * Contains logic of searching by methods returning given type
 * @author Sergey Fedulov
 */
public class MethodsReturnSearch extends AbstractSearch {

  MethodsReturnSearch(RefactorItContext context, Object object, FindRequest findRequest){
    super(context, object, findRequest);
  }
  
  public SearchVisitor createVisitor() {
    return new ReturnSearchVisitor();
  }

  String getSearchable() {
    return "methods returning type";
  }

  private class ReturnSearchVisitor extends SearchVisitor {
    public void visit(BinMethod method) {
      BinTypeRef testable = method.getReturnType();

      if (isMatching(testable)) {
        results.add(method);
      }

      // we still keep visiting to get local types also;
      super.visit(method);
    }
  }
}
