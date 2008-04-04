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
import net.sf.refactorit.classmodel.expressions.BinCITypeExpression;
import net.sf.refactorit.classmodel.expressions.BinMemberInvocationExpression;
import net.sf.refactorit.common.util.AppRegistry;
import net.sf.refactorit.refactorings.AmbiguousImportImportException;
import net.sf.refactorit.refactorings.ImportManager;
import net.sf.refactorit.transformations.RenameTransformation;
import net.sf.refactorit.transformations.TransformationList;

import org.apache.log4j.Logger;

import java.util.List;


/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Aqris Software AS</p>
 * @author Tonis Vaga
 * @version 1.0
 */

public class BinCITypeExpressionUsage extends TypeUsage {
  private BinCITypeExpression x;
  

  public void addTypeEditors(BinCIType type,
  		final TransformationList transList,
			ImportManager importManager) {
  	boolean useFqn = false;
  	try {
  		importManager.addExtraImports(type.getTypeRef(),
  				x.getOwner());
  	} catch (AmbiguousImportImportException e) {
  		useFqn = true;
  	} 
  	
  	String newTypeDesc = useFqn ? type.getQualifiedName() : type.getName();
  	
  	transList.add(new RenameTransformation(x.getCompilationUnit(), x.getRootAst(),
  			newTypeDesc));
  }

  public boolean checkCanUseSuper(SuperClassInfo superInf, List resolvedMembers, List failureReasons) {
    
    if ( isFromClasspathOrJsp(getWhat()) ) {
      return false;
    }

    if (x.getParent() instanceof BinMemberInvocationExpression) {
      BinMemberInvocationExpression invoked = (BinMemberInvocationExpression) x.
          getParent();
      return checkBinItem(invoked, superInf, resolvedMembers);
    } else {
      Logger logger = AppRegistry.getLogger(this.getClass());
      if ( logger.isDebugEnabled() ) {
        logger.debug("BinCITypeExpression ignored " + x.getParent());
      }
    }
    return false;
  }

  public BinCITypeExpressionUsage(BinCITypeExpression x) {
    super(x);
    this.x = x;
  }
}
