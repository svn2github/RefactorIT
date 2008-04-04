/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.usesupertype;


import net.sf.refactorit.classmodel.BinCatchParameter;
import net.sf.refactorit.classmodel.statements.BinTryStatement;
import net.sf.refactorit.classmodel.statements.BinTryStatement.CatchClause;

import java.util.List;

/**
 * CatchParameterUsage
 * 
 * @author <a href="mailto:tonis.vaga@aqris.com>Tonis Vaga</a>
 * @version $Revision: 1.2 $ $Date: 2005/01/02 15:23:26 $
 */
public class CatchParameterUsage extends ParameterUsage {

  /**
   * @param var
   */
  CatchParameterUsage(BinCatchParameter var) {
    super(var);
  }
  
  public boolean checkCanUseSuper(SuperClassInfo superInf,
      List resolvedMembers, List failureReasons) {
    BinCatchParameter par=(BinCatchParameter) getWhat();
    BinTryStatement statement=(BinTryStatement) par.getCatchClause().getParent();
    
    final CatchClause[] catches = statement.getCatches();
    for (int i = 0; i < catches.length; i++) {
      final BinCatchParameter catchParam = catches[i].getParameter();
      
 
      if ( catchParam.equals(par)) {
        // check if next element is more specific
        if ( catches.length > i+1 ) {
          final BinCatchParameter nextCatchParam = catches[i+1].getParameter();
          if ( nextCatchParam.getTypeRef().isDerivedFrom(superInf.getSupertypeRef()) ) {
            return false;
          }
        }
      } else if ( resolvedMembers.contains(catchParam)  ){
        addFailureReason(failureReasons,"one of parameters already converted to supertype");
        return false;
      }
    }
    
    if ( !superInf.getSupertypeRef().isDerivedFrom(
        superInf.getSupertypeRef().getProject().getTypeRefForName(
            "java.lang.Throwable"))) {
      addFailureReason(failureReasons,"exception supertype must extends Throwable");
      return false;
    }
    return super.checkCanUseSuper(superInf, resolvedMembers, failureReasons);
  }


}
