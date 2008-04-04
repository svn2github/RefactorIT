/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.usesupertype;

import net.sf.refactorit.classmodel.BinCIType;
import net.sf.refactorit.classmodel.BinSpecificTypeRef;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.classmodel.expressions.BinExpression;
import net.sf.refactorit.classmodel.expressions.BinFieldInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinMethodInvocationExpression;
import net.sf.refactorit.classmodel.expressions.BinVariableUseExpression;
import net.sf.refactorit.classmodel.statements.BinReturnStatement;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.query.usage.TypeIndexer;
import net.sf.refactorit.query.usage.filters.BinClassSearchFilter;



/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Aqris Software AS</p>
 * @author Tonis Vaga
 * @version 1.0
 */

public class UseSuperTypeIndexer extends TypeIndexer {

  BinCIType target = null;

  public UseSuperTypeIndexer(ManagingIndexer managingIndexer, BinCIType target) {
    super(managingIndexer, target, new BinClassSearchFilter(false, false));
    this.target = target;
  }

  public final void visit(BinReturnStatement x) {
    checkInvocation(x.getReturnExpression());
  }

  public final void visit(BinVariableUseExpression x) {
      checkInvocation(x);
    super.visit(x);
  }

  public void visit(BinFieldInvocationExpression x) {
    checkInvocation(x);
    super.visit(x);
  }

  public void visit(BinMethodInvocationExpression x) {
    checkInvocation(x);
    super.visit(x);
  }

  private void checkInvocation(BinExpression x) {
    if (x != null && x.getReturnType() != null
        && target.getTypeRef().equals(x.getReturnType().getNonArrayType())) {
      typeRefVisitor.init(null, x, null);
      BinTypeRef typeRef = BinSpecificTypeRef.create(
          x.getCompilationUnit(), x.getRootAst(), target.getTypeRef(), false);
      typeRef.accept(typeRefVisitor);
    }
  }
}
