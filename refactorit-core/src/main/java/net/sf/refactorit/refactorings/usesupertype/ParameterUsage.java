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
import net.sf.refactorit.classmodel.BinMember;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.classmodel.BinTypeRef;
import net.sf.refactorit.refactorings.AmbiguousImportImportException;
import net.sf.refactorit.refactorings.ImportManager;
import net.sf.refactorit.transformations.TransformationList;

import java.util.Iterator;
import java.util.List;


/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description: Method parameter usage, holds also all overriden parameter
 * usages!
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company: Aqris Software AS
 * </p>
 *
 * @author Tonis Vaga
 * @version 1.0
 */

public class ParameterUsage extends VariableUsage {

  protected ParameterUsage(BinParameter var) {
    super(var);
  }

  BinParameter[] getOverridenParameters() {
    BinParameter par = (BinParameter) getVariable();

    BinMethod method = getMethod();

    if (method == null) {
      return BinParameter.NO_PARAMS;
    }
    List overridenMethods = UseSuperTypeUtil.getAllOverrides(method);

    BinParameter result[] = new BinParameter[overridenMethods.size()];
    int parIndex = par.getIndex();
    int index = 0;
    for (Iterator iter = overridenMethods.iterator(); iter.hasNext(); ) {
      BinMethod item = (BinMethod) iter.next();
      result[index++] = item.getParameters()[parIndex];
    }

    return result;
  }

  public BinMethod getMethod() {
    return ((BinParameter) getVariable()).getMethod();
  }

  public void addTypeEditors(BinCIType superType,
      final TransformationList transList,
      ImportManager importManager) {


  	boolean useFqn = false;

    BinMethod method = ((BinParameter)getVariable()).getMethod();
    boolean isSyntheticConstr = method instanceof BinConstructor && method.isSynthetic();

    if ( !isSyntheticConstr )  {
      try {
        importManager.addExtraImports(superType.getTypeRef(), UseSuperTypeUtil.getOwnerType(getVariable()));
      } catch (AmbiguousImportImportException e) {
        useFqn = true;
      }

      addVariableTypeEditors(getVariable(), superType, transList, useFqn);
    }


    BinParameter pars[] = getOverridenParameters();

    for (int i = 0; i < pars.length; i++) {

      isSyntheticConstr = pars[i].getMethod() instanceof BinConstructor
          && pars[i].getMethod().isSynthetic();
      if ( isSyntheticConstr )  {
        continue;
      }

      useFqn = false;

      try {
        importManager.addExtraImports(superType.getTypeRef(), UseSuperTypeUtil.getOwnerType(pars[i]));
      } catch (AmbiguousImportImportException e) {
        useFqn = true;
      }
      addVariableTypeEditors(pars[i], superType, transList, useFqn);

    }
  }

  public boolean checkCanUseSuper(SuperClassInfo superInf, List resolvedMembers, List failureReasons) {

    if (getMethod() != null
        && !UseSuperTypeUtil.checkAllOverridenInSourcePath(getMethod())) {

      addFailureReason(failureReasons,"Not all method hierarchy members in sourcepath");
      return false;
    }

    BinMethod method = getMethod();

    if (!super.checkCanUseSuper(superInf, resolvedMembers, failureReasons)) {
      return false;
    }

    // check if method with new signature exist
    // slow, check last
    // TODO: improve, rejects also cases where possible changes to other parameters can
    // still make different method

    if ( method != null &&
        checkIfMethodWithChangedParameterExists(superInf)!=null ) {
      addFailureReason(failureReasons,"method with same signature exists");

      return false;
    }
    return true;

  }

  /**
   * Checks if new method has clashes in hierarchy when method parameter is
   * changed to supertype
   *
   * @param superRef
   */
  private BinMember checkIfMethodWithChangedParameterExists(
      final SuperClassInfo superInfo) {

    final BinMethod meth = getMethod();

    List hierarchy = meth.findAllOverridesOverriddenInHierarchy();
    hierarchy.add(meth);
    BinParameter par = (BinParameter) getVariable();
    //SuperClassInfo classInfo = new SuperClassInfo(method.getOwner());

    BinTypeRef parameterTypes[];
    if (superInfo.hasChangedMethodSignature(meth)) {
      parameterTypes = superInfo.getVirtualMethodParameters(meth);
    } else {
      parameterTypes = BinParameter.parameterTypes(meth.getParameters());
    }
    BinMember result = null;
    
    for(int j = 0; j < hierarchy.size(); j++) {
    	BinMethod method = (BinMethod)hierarchy.get(j);
	    BinCIType ownerType = method.getOwner().getBinCIType();
	    
	
	    parameterTypes[par.getIndex()] = superInfo.getSupertypeRef();
	
	    if (method instanceof BinConstructor) {
	      result = superInfo.hasAccessibleConstructorWithSignature(ownerType,
	          parameterTypes);
	    } else {
	      result = superInfo.hasAccessibleMethodWithSignature(ownerType, superInfo.getSupertypeRef().getBinCIType(), method
	          .getName(), parameterTypes);
	      if(result != null) {
	      	break;
	      }
	    }
    }
    if(result == null) {
        superInfo.changeVirtualMethod(meth, parameterTypes);
    }
    
    return result;
  }
}
