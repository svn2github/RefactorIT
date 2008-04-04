/*
 * Copyright 2001-2008 Aqris Software AS. All rights reserved.
 * 
 * This program is dual-licensed under both the Common Development
 * and Distribution License ("CDDL") and the GNU General Public
 * License ("GPL"). You may elect to use one or the other of these
 * licenses.
 */
package net.sf.refactorit.refactorings.rename;


import net.sf.refactorit.classmodel.BinLocalVariable;
import net.sf.refactorit.classmodel.BinMethod;
import net.sf.refactorit.classmodel.BinParameter;
import net.sf.refactorit.common.util.Assert;
import net.sf.refactorit.query.usage.ManagingIndexer;
import net.sf.refactorit.refactorings.RefactoringStatus;
import net.sf.refactorit.transformations.TransformationList;
import net.sf.refactorit.ui.module.RefactorItContext;

import java.util.ArrayList;
import java.util.List;


public class RenameMultiLocal extends RenameMember{
  private RenameLocal[] toProcess;
  private boolean includeOverridedMethods = false;
  private RefactorItContext context;
  private BinLocalVariable variable;
  private RenameLocal renlocal;

  public RenameMultiLocal(RefactorItContext context, BinLocalVariable variable) {
    super("RenameParameter", context, variable);

    this.context = context;
    this.variable = variable;

    renlocal = new RenameLocal(context, variable);
  }

  private RenameLocal[] getVariableToProcess(BinParameter parameter) {
    List list = new ArrayList();
    List overridesOverriddenList;

    BinMethod binMethod = parameter.getMethod();

    if (includeOverridedMethods) {
      BinParameter binPar;
      overridesOverriddenList = binMethod.findAllOverridesOverriddenInHierarchy();
      for (int x = 0; x < overridesOverriddenList.size(); x++) {
        BinMethod method = (BinMethod) overridesOverriddenList.get(x);
        binPar = method.getParameters()[parameter.getIndex()];

        Assert.must(binPar.getTypeRef().equals(parameter.getTypeRef()),
            "Types in overrides/overriden methods not the same");

        list.add(new RenameLocal(context, binPar));
      }
    }

    return (RenameLocal[]) list.toArray(new RenameLocal[list.size()]);
  }

  private void forwardUserInput(RenameLocal rl) {
    rl.setNewName(this.getNewName());
    rl.setRenameInJavadocs(this.isRenameInJavadocs());
  }

  public RefactoringStatus checkUserInput() {
    RefactoringStatus status = new RefactoringStatus();

    forwardUserInput(renlocal);
    status.merge(this.renlocal.checkUserInput());

      if (includeOverridedMethods) {
        initToProcess();
        for (int x = 0; x < toProcess.length; x++) {
          forwardUserInput(toProcess[x]);
          status.merge(toProcess[x].checkUserInput());
        }
      }

    return status;
  }

  public TransformationList performChange() {
    TransformationList transformation = new TransformationList();



    TransformationList tl;
    tl = renlocal.performChange();
    if (tl.getStatus().isOk()) {
      transformation.merge(tl);
    }

    if(includeOverridedMethods) {
      initToProcess();
      for (int x = 0; x < toProcess.length; x++) {
        tl = toProcess[x].performChange();
        if (tl.getStatus().isOk()) {
          transformation.merge(tl);
        }

      }
    }



    return transformation;
  }

  protected ManagingIndexer getSupervisor() {
    renlocal.getSupervisor();

    if(includeOverridedMethods) {
      initToProcess();
      for (int x = 0; x < toProcess.length; x++) {
        toProcess[x].getSupervisor();
      }
    }

    return null;
  }

  protected void invalidateCache() {

    renlocal.invalidateCache();

    if(includeOverridedMethods) {
      initToProcess();
      for (int x = 0; x < toProcess.length; x++) {
        toProcess[x].invalidateCache();
      }
    }
  }

  public void setIncludeOverridedMethods(final boolean includeOverridedMethods) {
    this.includeOverridedMethods = includeOverridedMethods;
  }

  private void initToProcess() {
    if (toProcess == null) {
      toProcess = getVariableToProcess((BinParameter) variable);
    }
 }

 public String getDescription() {
   return super.getDescription();
 }

 public String getKey() {
   return key;
 }

}
