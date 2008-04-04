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
import net.sf.refactorit.classmodel.BinItem;
import net.sf.refactorit.classmodel.expressions.BinCastExpression;
import net.sf.refactorit.refactorings.AmbiguousImportImportException;
import net.sf.refactorit.refactorings.ImportManager;
import net.sf.refactorit.transformations.TransformationList;



// Referenced classes of package net.sf.refactorit.refactorings.usesupertype:
//      TypeUsage

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: Aqris Software AS</p>
 * @author Tonis Vaga
 * @version 1.0
 */

class CastUsage extends TypeUsage {

  BinCastExpression castExpression = null;

  public CastUsage(BinCastExpression x, BinItem usage) {
    super(x);
    this.castExpression = x;
    addUsage(usage);
  }
  public void addTypeEditors(BinCIType newType,
      final TransformationList transList, ImportManager importManager) {
  	try {
    	importManager.addExtraImports(newType.getTypeRef(), castExpression.getOwner());
    	transList.add(UseSuperTypeUtil.createCastTypeEditor(castExpression,
    			newType, false));
  	} catch (AmbiguousImportImportException e) {   		
    	transList.add(UseSuperTypeUtil.createCastTypeEditor(castExpression,
    			newType, true));
    }
  }
}
