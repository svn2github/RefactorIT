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
import net.sf.refactorit.classmodel.BinConstructor;
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.refactorings.AmbiguousImportImportException;
import net.sf.refactorit.refactorings.ImportManager;
import net.sf.refactorit.source.edit.CompoundASTImpl;
import net.sf.refactorit.transformations.RenameTransformation;
import net.sf.refactorit.transformations.TransformationList;

import java.util.Iterator;
import java.util.List;


/**
 * <p>Title: </p>
 * <p>Description: Class for method return value usage</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Aqris Software AS</p>
 * @author Tonis Vaga
 * @version 1.0
 */

class ReturnValueUsage extends TypeUsage {

  BinMethod method = null;

  public ReturnValueUsage(BinMethod method) {
    super(method);
    this.method = method;
  }

  public boolean checkCanUseSuper(SuperClassInfo convertInf, List resolvedMembers, List failureReasons) {
    if (method instanceof BinConstructor) {
      addFailureReason(failureReasons,"Can't change constructor return value");
      return false;
    } else {
      if ( !UseSuperTypeUtil.checkAllOverridenInSourcePath(method) ) {
        addFailureReason(failureReasons,"not all overridden methods in sourcepath");
        return false;
      }
      return super.checkCanUseSuper(convertInf, resolvedMembers, failureReasons);
    }
  }

  public void addTypeEditors(BinCIType newType,
  		final TransformationList transList, ImportManager importManager) {
  	List methodsToEdit = method.findAllOverridesOverriddenInHierarchy();
    methodsToEdit.add(method);
    /*List methodsToEdit = UseSuperTypeUtil.getAllOverrides(method);
  	if(method.getBodyAST() != null) {
      methodsToEdit.add(method);
    }*/
    
    if (Assert.enabled) {
      Assert.must(!(method instanceof BinConstructor),
          "" + method + ": usages " + getUsages());
    }
  	BinMethod item;
  	for (Iterator iter = methodsToEdit.iterator(); iter.hasNext(); ) {
  		item = (BinMethod) iter.next();
  		boolean useFqn = false;
  		try {
  			importManager.addExtraImports(newType.getTypeRef(),
  					item.getOwner());
  		} catch (AmbiguousImportImportException e) {
  		  useFqn = true;
  		}
      if(item.isSynthetic()) {
        continue;
      }
      transList.add(new RenameTransformation(item.getCompilationUnit(),
            CompoundASTImpl.compoundTypeAST(item.getReturnTypeAST()),
            UseSuperTypeUtil.formatWithTypeArguments(newType, useFqn,  item.getReturnType())));
  	}

  }

  public void addUsage(BinItem expr) {
    if (UseSuperTypeRefactoring.debug) {
      System.out.println("dependent added for " + method + ":" + expr);
    }
    super.addUsage(expr);
  }
}
