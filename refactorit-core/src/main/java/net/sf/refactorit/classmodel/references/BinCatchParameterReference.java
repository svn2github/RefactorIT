/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.classmodel.references;

import net.sf.refactorit.classmodel.BinCatchParameter;
import net.sf.refactorit.classmodel.Project;
import net.sf.refactorit.classmodel.statements.BinTryStatement;

/**
 *
 * @author Arseni Grigorjev
 */
public class BinCatchParameterReference extends CacheableReference {
  
  protected BinItemReference tryStatementReference;
  protected int place;
  
  public BinCatchParameterReference(final BinCatchParameter param) {
    super(param, param.getProject());
    tryStatementReference = ((BinTryStatement) param.getCatchClause().getParent())
        .createReference();
    place = getPlaceInCatchList(param);
  }
  
  public Object findItem(Project project) {
    final BinTryStatement ownerTryStatement 
        = (BinTryStatement) tryStatementReference.restore(project);
    return ownerTryStatement.getCatches()[place].getParameter();
  }
  
  private int getPlaceInCatchList(BinCatchParameter param) {
    BinTryStatement tryStatement
        = (BinTryStatement) param.getCatchClause().getParent();
    BinTryStatement.CatchClause[] catchClauses = tryStatement.getCatches();
    for (int i = 0; i < catchClauses.length; i++) {
      if (catchClauses[i] == param.getCatchClause()) {
        return i;
      }
    }

    throw new IllegalArgumentException(
        "Catch clause was not found from its owner's list");
  }
}
