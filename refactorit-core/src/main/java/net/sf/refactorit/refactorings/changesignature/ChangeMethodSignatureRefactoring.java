/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.changesignature;


import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.commonIDE.IDEController;
import net.sf.refactorit.refactorings.AbstractRefactoring;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.transformations.TransformationList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 *
 *
 * @author Tonis Vaga
 */
public class ChangeMethodSignatureRefactoring extends AbstractRefactoring {
  public static String key = "refactoring.changemethodsignature";

  private MethodSignatureChange change;
  private BinMethod method;
  private List overridesOverriddenList;

  public static final boolean debug = false;

  public ChangeMethodSignatureRefactoring(BinMethod binMethod) {
    super("Change Method Signature",
        IDEController.getInstance().createProjectContext());

    this.method = binMethod;

    overridesOverriddenList = method.findAllOverridesOverriddenInHierarchy();
    overridesOverriddenList.removeAll(findSynthetic(overridesOverriddenList));
  }

  private static Collection findSynthetic(List methods) {
    List result = new ArrayList();
    for (Iterator i = methods.iterator(); i.hasNext();) {
      BinMethod method = (BinMethod) i.next();
      if(method.isSynthetic()) {
        result.add(method);
      }
    }

    return result;
  }

  public TransformationList performChange() {
    return change.edit();
  }

  public RefactoringStatus checkPreconditions() {
    if (!method.getOwner().getBinCIType().isFromCompilationUnit()) {
      return new RefactoringStatus("Method " + method.getName() +
          " is not in sourcepath", RefactoringStatus.ERROR);
    }

    for (int index = 0; index < overridesOverriddenList.size(); ++index) {
      BinMethod item = (BinMethod) overridesOverriddenList.get(index);

      if (!item.getOwner().getBinCIType().isFromCompilationUnit()) {
        return new RefactoringStatus("Not all method overrides or" +
            " overriddens are in sourcepath", RefactoringStatus.ERROR);
      }
    }

    if (method.isNative()) {
      return new RefactoringStatus("Can't change signature of native method",
          RefactoringStatus.ERROR);
    }


    return new RefactoringStatus();
  }

  public RefactoringStatus checkUserInput() {
    Assert.must(method == change.getMethod());

    return change.checkCanChange();
  }

  public MethodSignatureChange createSingatureChange() {
  	return new MethodSignatureChange(method, overridesOverriddenList, this);

  }

  public void setChange(MethodSignatureChange change) {
    this.change = change;
  }

  public String getDescription() {
    StringBuffer buf = new StringBuffer("Change method signature: ");
    buf.append(change.getMethod().getName() + "(..)");
    return new String(buf); //super.getDescription();
  }

  public String getKey() {
    return key;
  }

  public MethodSignatureChange getChange() {
    return this.change;
  }

}
