/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings;


import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.statements.BinReturnStatement;
import net.sf.refactorit.classmodel.statements.BinStatementList;
import net.sf.refactorit.query.BinItemVisitor;

import java.util.ArrayList;
import java.util.List;


public final class RefactoringUtil {
  private RefactoringUtil() {
  }

  /**
   * @param method BinMethod to find return statements for
   * @return List of all BinReturnStatements for given method
   */
  public static List findReturnStatementsForMethod(BinMethod method) {
    class ReturnStatementsFinder extends BinItemVisitor {

      public ReturnStatementsFinder() {
      }

      public List returnStatementList = new ArrayList();

      public void visit(BinReturnStatement x) {
        returnStatementList.add(x);
        super.visit(x);
      }

      public List getReturnStatementList() {
        return this.returnStatementList;
      }
    };

    ReturnStatementsFinder visitor = null;
    visitor = new ReturnStatementsFinder();

    BinStatementList body = method.getBody();
    if (body != null) {
      // so body exist
      body.accept(visitor);
    }

    return visitor.getReturnStatementList();

  }

}
